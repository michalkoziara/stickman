package com.litkaps.stickman;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageWriter {
    public boolean saveBitmapToImage(Bitmap bitmap, String directoryName, String uniqueName) {
        try {
            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String file_path = absolutePath + directoryName;
            File dir = new File(file_path);

            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }

            Log.d("APP", " absolute path: " + file_path);
            File file = new File(dir, uniqueName + ".jpg");
            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            return true;
        } catch (IOException ioException) {
            return false;
        }
    }
}
