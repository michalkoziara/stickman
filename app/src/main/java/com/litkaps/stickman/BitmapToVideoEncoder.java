package com.litkaps.stickman;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.litkaps.stickman.posedetector.StickmanImage;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.media.MediaFormat.KEY_MIME;

public class BitmapToVideoEncoder {
    public int frameRate = 15;

    private static final String TAG = BitmapToVideoEncoder.class.getSimpleName();

    private final IBitmapToVideoEncoderCallback mCallback;
    private File mOutputFile;
    private Queue<StickmanImage> mEncodeQueue = new ConcurrentLinkedQueue<>();
    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;

    private final Object mFrameSync = new Object();
    private CountDownLatch mNewFrameLatch;

    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding.
    private static final int BIT_RATE = 16000000;

    private static final int I_FRAME_INTERVAL = 1;

    private int mGenerateIndex = 0;
    private int mTrackIndex;
    private boolean mNoMoreFrames = false;
    private boolean mAbort = false;
    private int metadataTrackIndex;

    public interface IBitmapToVideoEncoderCallback {
        void onEncodingComplete(File outputFile);
    }

    public BitmapToVideoEncoder(IBitmapToVideoEncoderCallback callback, int frameRate) {
        this.frameRate = frameRate > 0 ? frameRate : this.frameRate;
        mCallback = callback;
    }

    public boolean isEncodingStarted() {
        return (mediaCodec != null) && (mediaMuxer != null) && !mNoMoreFrames && !mAbort;
    }

    public int getActiveBitmaps() {
        return mEncodeQueue.size();
    }

    public void startEncoding(int width, int height, FileDescriptor fileDescriptor) {
        if (frameRate == 0) {
            frameRate = 15;
        }

        MediaCodecInfo codecInfo = selectCodec();
        if (codecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        Log.d(TAG, "found codec: " + codecInfo.getName());

        try {
            mediaCodec = MediaCodec.createByCodecName(codecInfo.getName());
        } catch (IOException e) {
            Log.e(TAG, "Unable to create MediaCodec " + e.getMessage());
            return;
        }

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        try {
            mediaMuxer = new MediaMuxer(fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            MediaFormat metadataFormat = new MediaFormat();
            metadataFormat.setString(KEY_MIME, "application/pose");
            metadataTrackIndex = mediaMuxer.addTrack(metadataFormat);
        } catch (IOException e) {
            Log.e(TAG, "MediaMuxer creation failed. " + e.getMessage());
            return;
        }

        Log.d(TAG, "Initialization complete. Starting encoder...");

        Completable.fromAction(this::encode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void stopEncoding() {
        if (mediaCodec == null || mediaMuxer == null) {
            Log.d(TAG, "Failed to stop encoding since it never started");
            return;
        }
        Log.d(TAG, "Stopping encoding");

        mNoMoreFrames = true;

        synchronized (mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
                mNewFrameLatch.countDown();
            }
        }
    }

    public void abortEncoding() {
        if (mediaCodec == null || mediaMuxer == null) {
            Log.d(TAG, "Failed to abort encoding since it never started");
            return;
        }
        Log.d(TAG, "Aborting encoding");

        mNoMoreFrames = true;
        mAbort = true;
        mEncodeQueue = new ConcurrentLinkedQueue<>(); // Drops all frames.

        synchronized (mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
                mNewFrameLatch.countDown();
            }
        }
    }

    public void queueFrame(StickmanImage image) {
        if (mediaCodec == null || mediaMuxer == null) {
            Log.d(TAG, "Failed to queue frame. Encoding not started");
            return;
        }

        Log.d(TAG, "Queueing frame");
        mEncodeQueue.add(image);

        synchronized (mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch.getCount() > 0)) {
                mNewFrameLatch.countDown();
            }
        }
    }

    private void encode() {
        Log.d(TAG, "Encoder started");

        while (!mNoMoreFrames || !mEncodeQueue.isEmpty()) {

            StickmanImage image = mEncodeQueue.poll();
            if (image == null) {
                synchronized (mFrameSync) {
                    mNewFrameLatch = new CountDownLatch(1);
                }

                try {
                    mNewFrameLatch.await();
                } catch (InterruptedException e) {
                }

                image = mEncodeQueue.poll();
            }

            if (image == null) continue;

            byte[] byteConvertFrame = getNV21(image.bitmap.getWidth(), image.bitmap.getHeight(), image.bitmap);

            long timeoutUsec = 500000;
            int inputBufIndex = mediaCodec.dequeueInputBuffer(timeoutUsec);
            long ptsUsec = computePresentationTime(mGenerateIndex);
            if (inputBufIndex >= 0) {
                final ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufIndex);
                inputBuffer.clear();
                inputBuffer.put(byteConvertFrame);
                mediaCodec.queueInputBuffer(inputBufIndex, 0, byteConvertFrame.length, ptsUsec, 0);
                mGenerateIndex++;
            }

            byte[] imageMetadata = new byte[]{};
            try {
                imageMetadata = SerializationUtils.convertToBytes(image.metadata);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
            int encoderStatus = mediaCodec.dequeueOutputBuffer(mBufferInfo, timeoutUsec);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // No output available yet.
                Log.e(TAG, "No output from encoder available");
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Not expected for an encoder.
                MediaFormat newFormat = mediaCodec.getOutputFormat();
                mTrackIndex = mediaMuxer.addTrack(newFormat);
                mediaMuxer.start();
            } else if (encoderStatus < 0) {
                Log.e(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } else if (mBufferInfo.size != 0) {
                ByteBuffer encodedData = mediaCodec.getOutputBuffer(encoderStatus);
                if (encodedData == null) {
                    Log.e(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
                } else {
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    mediaMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);

                    ByteBuffer metaData = ByteBuffer.allocate(imageMetadata.length);
                    metaData.put(imageMetadata);

                    MediaCodec.BufferInfo metaInfo = new MediaCodec.BufferInfo();
                    // Associate this metadata with the video frame by setting
                    // the same timestamp as the video frame.
                    metaInfo.presentationTimeUs = ptsUsec;
                    metaInfo.offset = 0;
                    metaInfo.flags = 0;
                    metaInfo.size = imageMetadata.length;
                    mediaMuxer.writeSampleData(metadataTrackIndex, metaData, metaInfo);

                    mediaCodec.releaseOutputBuffer(encoderStatus, false);
                }
            }
        }

        release();

        if (mAbort) {
            mOutputFile.delete();
        } else {
            mCallback.onEncodingComplete(mOutputFile);
        }
    }

    private void release() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
            Log.d(TAG, "RELEASE CODEC");
        }
        if (mediaMuxer != null) {
            try {
                mediaMuxer.stop();
                mediaMuxer.release();
            } catch (IllegalStateException illegalStateException) {
                illegalStateException.printStackTrace();
            }

            mediaMuxer = null;
            Log.d(TAG, "RELEASE MUXER");
        }
    }

    private static MediaCodecInfo selectCodec() {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(BitmapToVideoEncoder.MIME_TYPE)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int r;
        int g;
        int b;
        int y;
        int u;
        int v;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                r = (argb[index] & 0xff0000) >> 16;
                g = (argb[index] & 0xff00) >> 8;
                b = (argb[index] & 0xff);

                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                yuv420sp[yIndex++] = (byte) ((y < 0) ? 0 : Math.min(y, 255));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((u < 0) ? 0 : Math.min(u, 255));
                    yuv420sp[uvIndex++] = (byte) ((v < 0) ? 0 : Math.min(v, 255));

                }

                index++;
            }
        }
    }

    private long computePresentationTime(long frameIndex) {
        if (frameRate == 0) {
            frameRate = 15;
        }

        return 132 + frameIndex * 1000000 / frameRate;
    }
}