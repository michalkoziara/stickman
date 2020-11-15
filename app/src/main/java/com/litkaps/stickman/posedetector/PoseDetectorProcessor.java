package com.litkaps.stickman.posedetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.litkaps.stickman.CameraImageGraphic;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.VisionProcessorBase;

import androidx.annotation.NonNull;

/**
 * A processor to run pose detector.
 */
public class PoseDetectorProcessor extends VisionProcessorBase<Pose> {
    private Paint stickmanPaint = new Paint(Color.BLACK);
    private int figureID;
    private int accessoryID;
    private int accessoryType = -1;

    private static final String TAG = "PoseDetectorProcessor";

    private final PoseDetector detector;
    private final boolean showInFrameLikelihood;

    private Bitmap backgroundImage;
    private Bitmap scaledBackgroundImage;
    private boolean isUpdated;

    public PoseDetectorProcessor(
            Context context, PoseDetectorOptionsBase options, boolean showInFrameLikelihood) {
        super(context);
        this.showInFrameLikelihood = showInFrameLikelihood;
        detector = PoseDetection.getClient(options);
        stickmanPaint.setStrokeWidth(16);
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
        if (backgroundImage != null) {
            if (isUpdated) {
                scaledBackgroundImage = Bitmap.createScaledBitmap(
                        backgroundImage,
                        cameraImage.getWidth(),
                        cameraImage.getHeight(),
                        true);

                isUpdated = false;
            }

            graphicOverlay.add(new CameraImageGraphic(graphicOverlay, scaledBackgroundImage));
        } else {
            scaledBackgroundImage = null;
            graphicOverlay.add(new CameraImageGraphic(graphicOverlay, cameraImage));
        }

        if(figureID == 0)
            graphicOverlay.add(new ClassicStickmanGraphic(graphicOverlay, pose, showInFrameLikelihood, accessoryID, accessoryType, stickmanPaint));
        else if(figureID == 1)
            graphicOverlay.add(new ComicStickmanGraphic(graphicOverlay, pose, showInFrameLikelihood, accessoryID, accessoryType, stickmanPaint));
        else if(figureID == 2)
            graphicOverlay.add(new FlexibleComicStickmanGraphic(graphicOverlay, pose, showInFrameLikelihood, accessoryID, accessoryType, stickmanPaint));
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Pose detection failed!", e);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        backgroundImage = bitmap;
        isUpdated = true;
    }

    public void setFigureID(int figureID) {
        this.figureID = figureID;
    }

    public void setFigureColor(int color) {
        stickmanPaint.setColor(color);
    }

    public void setFigureLineWidth(int width) {
        stickmanPaint.setStrokeWidth(width);
    }

    public void setFigureAccessory(int accessoryID, int accessoryType) {
        this.accessoryID = accessoryID;
        this.accessoryType = accessoryType;
    }
}
