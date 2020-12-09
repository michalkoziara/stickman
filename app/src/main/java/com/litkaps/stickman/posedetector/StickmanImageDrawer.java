package com.litkaps.stickman.posedetector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.pose.Pose;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.ImageGraphic;
import com.litkaps.stickman.VideoEncoder;

/**
 * The class {@code StickmanImageDrawer} draws a stickman image basing
 * on the pose detector processor.
 */
public class StickmanImageDrawer {
    private final Paint stickmanPaint = new Paint(Color.BLACK);

    private int figureID;
    private int accessoryID;
    private int accessoryType = -1;

    private Bitmap backgroundImage;
    private Bitmap scaledBackgroundImage;
    private boolean isUpdated;

    private VideoEncoder encoder;

    public StickmanImageDrawer(PoseDetectorProcessor poseDetectorProcessor) {
        stickmanPaint.setStrokeWidth(16);

        poseDetectorProcessor.computePoseOnSuccess(this::draw);
    }

    public void draw(@NonNull Pose pose, @NonNull GraphicOverlay graphicOverlay, Bitmap cameraImage) {
        if (backgroundImage != null) {
            if (isUpdated) {
                scaledBackgroundImage = Bitmap.createScaledBitmap(
                        backgroundImage,
                        cameraImage.getWidth(),
                        cameraImage.getHeight(),
                        true);

                isUpdated = false;
            }

            graphicOverlay.add(new ImageGraphic(graphicOverlay, scaledBackgroundImage));
        } else {
            scaledBackgroundImage = null;
            graphicOverlay.add(new ImageGraphic(graphicOverlay, cameraImage));
        }

        if (figureID == 0) {
            graphicOverlay.add(new ClassicStickmanGraphic(graphicOverlay, pose, accessoryID, accessoryType, stickmanPaint));
        } else if (figureID == 1) {
            graphicOverlay.add(new ComicStickmanGraphic(graphicOverlay, pose, accessoryID, accessoryType, stickmanPaint));
        } else if (figureID == 2) {
            graphicOverlay.add(new FlexibleComicStickmanGraphic(graphicOverlay, pose, accessoryID, accessoryType, stickmanPaint));
        }

        if (encoder != null) {
            encoder.queueFrame(
                    new StickmanImage(
                            graphicOverlay.getGraphicBitmap(),
                            figureID,
                            accessoryID,
                            accessoryType,
                            stickmanPaint.getColor(),
                            stickmanPaint.getStrokeWidth()
                    )
            );
        }
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

    public void setVideoEncoder(VideoEncoder encoder) {
        this.encoder = encoder;
    }

    public void clearVideoEncoder() {
        this.encoder.stopEncoding();
        this.encoder = null;
    }
}
