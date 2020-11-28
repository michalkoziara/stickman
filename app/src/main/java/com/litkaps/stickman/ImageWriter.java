package com.litkaps.stickman;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageWriter {
    final ContentResolver resolver;

    ImageWriter(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public boolean saveBitmapToImage(Bitmap bitmap, String name) {
        Uri imageCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues imageDetails = new ContentValues();
        imageDetails.put(MediaStore.Images.Media.TITLE, name);
        imageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        imageDetails.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        long now = System.currentTimeMillis() / 1000;
        imageDetails.put(MediaStore.Images.Media.DATE_ADDED, now);
        imageDetails.put(MediaStore.Images.Media.DATE_MODIFIED, now);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageDetails.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }

        Uri imageContentUri = resolver.insert(imageCollection, imageDetails);
        if (imageContentUri == null || imageContentUri.getPath() == null) {
            return false;
        }

        try (OutputStream stream = resolver.openOutputStream(imageContentUri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }

        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        try (ParcelFileDescriptor parcelFileDescriptor =
                     resolver.openFileDescriptor(imageContentUri, "rw", null)) {
            if (parcelFileDescriptor != null) {
                ExifInterface exifInterface = new ExifInterface(parcelFileDescriptor.getFileDescriptor());
                exifInterface.setAttribute(ExifInterface.TAG_DATETIME, dateFormat.format(new Date()));
                exifInterface.saveAttributes();
            } else {
                return false;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }

        return true;
    }
}
