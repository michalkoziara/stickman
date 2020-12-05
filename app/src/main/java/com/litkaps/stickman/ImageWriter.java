package com.litkaps.stickman;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ImageWriter {
    final ContentResolver resolver;

    ImageWriter(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public Single<Boolean> saveBitmapToImage(Bitmap bitmap, String name) {
        return Single
                .create((SingleOnSubscribe<Boolean>) emitter -> {
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
                    imageDetails.put(MediaStore.Images.Media.DESCRIPTION, "Stickman application media.");

                    long now = System.currentTimeMillis() / 1000;
                    imageDetails.put(MediaStore.Images.Media.DATE_ADDED, now);
                    imageDetails.put(MediaStore.Images.Media.DATE_MODIFIED, now);

                    Uri imageContentUri = resolver.insert(imageCollection, imageDetails);
                    if (imageContentUri == null || imageContentUri.getPath() == null) {
                        emitter.onSuccess(false);
                    } else {
                        try (OutputStream stream = resolver.openOutputStream(imageContentUri)) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                            emitter.onSuccess(false);
                        }
                    }

                    emitter.onSuccess(true);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
