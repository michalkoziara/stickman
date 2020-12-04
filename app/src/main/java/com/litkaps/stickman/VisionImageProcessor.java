package com.litkaps.stickman;

import android.os.Build.VERSION_CODES;

import androidx.annotation.RequiresApi;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.common.MlKitException;

/**
 * An interface to process the images with different vision detectors and custom image models.
 */
public interface VisionImageProcessor {

    /**
     * Processes ImageProxy image data, e.g. used for CameraX live preview case.
     */
    @RequiresApi(VERSION_CODES.KITKAT)
    void processImageProxy(ImageProxy image, GraphicOverlay graphicOverlay) throws MlKitException;

    /**
     * Stops the underlying machine learning model and release resources.
     */
    void stop();
}
