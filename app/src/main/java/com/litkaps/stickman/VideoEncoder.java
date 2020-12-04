package com.litkaps.stickman;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;

public class VideoEncoder {
    public interface VideoEncoderCallback extends BitmapToVideoEncoder.IBitmapToVideoEncoderCallback {
        void onEncodingComplete(File outputFile);
    }

    private final BitmapToVideoEncoder bitmapToVideoEncoder;
    private final ContentResolver resolver;
    private final ContentValues videoDetails;
    private final Uri videoContentUri;

    VideoEncoder(String name, int width, int height, ContentResolver resolver, VideoEncoderCallback onSuccess) {
        bitmapToVideoEncoder = new BitmapToVideoEncoder(onSuccess);
        this.resolver = resolver;

        Uri videoCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoCollection = MediaStore.Video.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        videoDetails = new ContentValues();
        videoDetails.put(MediaStore.Video.Media.TITLE, name + ".mp4");
        videoDetails.put(MediaStore.Video.Media.DISPLAY_NAME, name + ".mp4");
        videoDetails.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        videoDetails.put(MediaStore.Video.Media.DESCRIPTION, "Stickman application media.");

        long now = System.currentTimeMillis() / 1000;
        videoDetails.put(MediaStore.Video.Media.DATE_ADDED, now);
        videoDetails.put(MediaStore.Video.Media.DATE_MODIFIED, now);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoDetails.put(MediaStore.Video.Media.IS_PENDING, 1);
        }

        videoContentUri = resolver.insert(videoCollection, videoDetails);

        try {
            if (videoContentUri != null) {
                ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(videoContentUri, "rw");

                if (parcelFileDescriptor != null) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    bitmapToVideoEncoder.startEncoding(width, height, fileDescriptor);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void queueFrame(Bitmap frame) {
        if (frame != null && bitmapToVideoEncoder != null) {
            bitmapToVideoEncoder.queueFrame(frame);
        }
    }

    public void stopEncoding() {
        if (bitmapToVideoEncoder != null) {
            bitmapToVideoEncoder.stopEncoding();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                videoDetails.put(MediaStore.Video.Media.IS_PENDING, 0);
            }

            resolver.update(videoContentUri, videoDetails, null, null);
        }
    }
}
