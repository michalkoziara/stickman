package com.litkaps.stickman;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.litkaps.stickman.posedetector.StickmanImage;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.DATE_MODIFIED;
import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.IS_PENDING;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static android.provider.MediaStore.Video.VideoColumns.DESCRIPTION;


public class VideoEncoder {
    public interface VideoEncoderCallback extends BitmapToVideoEncoder.IBitmapToVideoEncoderCallback {
        void onEncodingComplete(File outputFile);
    }

    private final BitmapToVideoEncoder bitmapToVideoEncoder;
    private final ContentResolver resolver;
    private final ContentValues videoDetails;

    private Uri videoContentUri;

    public VideoEncoder(String name, int width, int height, ContentResolver resolver, VideoEncoderCallback onSuccess) {
        this(name, width, height, resolver, onSuccess, 5);
    }

    public VideoEncoder(String name, int width, int height, ContentResolver resolver, VideoEncoderCallback onSuccess, int frameRate) {
        bitmapToVideoEncoder = new BitmapToVideoEncoder(onSuccess, frameRate);
        this.resolver = resolver;

        Uri videoCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoCollection = MediaStore.Video.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        videoDetails = new ContentValues();
        videoDetails.put(TITLE, name + ".mp4");
        videoDetails.put(DISPLAY_NAME, name + ".mp4");
        videoDetails.put(MIME_TYPE, "video/mp4");
        videoDetails.put(DESCRIPTION, "Stickman application media.");

        long now = System.currentTimeMillis() / 1000;
        videoDetails.put(DATE_ADDED, now);
        videoDetails.put(DATE_MODIFIED, now);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoDetails.put(IS_PENDING, 1);
        }

        Schedulers.io().createWorker().schedule(() -> {
            videoContentUri = resolver.insert(videoCollection, videoDetails);

            if (videoContentUri != null) {
                try (ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(videoContentUri, "rw")) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    bitmapToVideoEncoder.startEncoding(width, height, fileDescriptor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setFrameRate(int frameRate) {
        bitmapToVideoEncoder.frameRate = frameRate;
    }

    public void queueFrame(StickmanImage frame) {
        if (frame != null && bitmapToVideoEncoder != null) {
            bitmapToVideoEncoder.queueFrame(frame);
        }
    }

    public void stopEncoding() {
        if (bitmapToVideoEncoder != null) {
            bitmapToVideoEncoder.stopEncoding();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                videoDetails.put(IS_PENDING, 0);
            }

            Schedulers.io().createWorker().schedule(
                    () -> resolver.update(videoContentUri, videoDetails, null, null)
            );
        }
    }
}
