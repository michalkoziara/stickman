package com.litkaps.stickman.posedetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.VisionProcessorBase;

/**
 * A processor to run pose detector.
 */
public class PoseDetectorProcessor extends VisionProcessorBase<Pose> {
    protected interface PoseDetectCallback {
        void onDetectionComplete(@NonNull Pose pose, @NonNull GraphicOverlay graphicOverlay, Bitmap cameraImage);
    }

    private static final String TAG = "PoseDetectorProcessor";

    private final PoseDetector detector;
    private PoseDetectCallback poseDetectCallback;

    public PoseDetectorProcessor(
            Context context, PoseDetectorOptionsBase options) {
        super(context);
        detector = PoseDetection.getClient(options);
    }

    @Override
    public void stop() {
        super.stop();
        detector.close();
    }

    @Override
    protected Task<Pose> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    protected void onSuccess(@NonNull Pose pose, @NonNull GraphicOverlay graphicOverlay, Bitmap cameraImage) {
        poseDetectCallback.onDetectionComplete(pose, graphicOverlay, cameraImage);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Pose detection failed!", e);
    }

    protected void computePoseOnSuccess(PoseDetectCallback poseDetectCallback) {
        this.poseDetectCallback = poseDetectCallback;
    }
}
