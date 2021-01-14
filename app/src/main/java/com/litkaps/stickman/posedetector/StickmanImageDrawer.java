package com.litkaps.stickman.posedetector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.ImageGraphic;
import com.litkaps.stickman.MainActivity;
import com.litkaps.stickman.R;
import com.litkaps.stickman.VideoEncoder;

import java.util.List;

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
    private int backgroundColor = -1;
    private Bitmap scaledBackgroundImage;
    private Bitmap scaledBackgroundColor;
    private boolean isBackgroundImageUpdated;
    private boolean isBackgroundColorUpdated;

    private float stickmanStrokeWidth = 12;

    private VideoEncoder encoder;
    private boolean encodeStickmanData = false;

    public StickmanImageDrawer() {
        stickmanPaint.setStrokeWidth(stickmanStrokeWidth);
    }

    public void setPoseDetectCallback(PoseDetectorProcessor poseDetectorProcessor) {
        poseDetectorProcessor.setPoseDetectCallback(this::draw);
    }

    public void draw(@NonNull PosePositions posePositions, @NonNull GraphicOverlay graphicOverlay, Bitmap cameraImage) {
        if (backgroundImage != null) {
            if (isBackgroundImageUpdated) {
                scaledBackgroundImage = Bitmap.createScaledBitmap(
                        backgroundImage,
                        cameraImage.getWidth(),
                        cameraImage.getHeight(),
                        true);

                isBackgroundImageUpdated = false;
            }

            graphicOverlay.add(new ImageGraphic(graphicOverlay, scaledBackgroundImage));
        } else {
            scaledBackgroundImage = null;
            graphicOverlay.add(new ImageGraphic(graphicOverlay, cameraImage));
        }

        if (backgroundColor != -1) {
            if (isBackgroundColorUpdated) {
                Bitmap backgroundColorImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                backgroundColorImage.eraseColor(backgroundColor);
                scaledBackgroundColor = Bitmap.createScaledBitmap(
                        backgroundColorImage,
                        cameraImage.getWidth(),
                        cameraImage.getHeight(),
                        true);

                isBackgroundColorUpdated = false;
            }

            graphicOverlay.add(new ImageGraphic(graphicOverlay, scaledBackgroundColor));
        } else {
            scaledBackgroundColor = null;
        }


        stickmanPaint.setStrokeWidth(graphicOverlay.scaleFactor * stickmanStrokeWidth);

        if (figureID == 0) {
            graphicOverlay.add(new ClassicStickmanGraphic(graphicOverlay, posePositions, accessoryID, accessoryType, stickmanPaint));
        } else if (figureID == 1) {
            graphicOverlay.add(new ComicStickmanGraphic(graphicOverlay, posePositions, accessoryID, accessoryType, stickmanPaint));
        } else if (figureID == 2) {
            graphicOverlay.add(new FlexibleComicStickmanGraphic(graphicOverlay, posePositions, accessoryID, accessoryType, stickmanPaint));
        }

        if (encoder != null) {
            if (encodeStickmanData) {
                encoder.queueFrame(
                        new StickmanImage(
                                graphicOverlay.getGraphicBitmap(),
                                figureID,
                                accessoryID,
                                accessoryType,
                                backgroundColor,
                                stickmanPaint.getColor(),
                                stickmanStrokeWidth,
                                posePositions.poseLandmarkPositionX,
                                posePositions.poseLandmarkPositionY
                        )
                );
            } else {
                encoder.queueFrame(
                        new StickmanImage(
                                graphicOverlay.getBackgroundGraphicBitmap(),
                                figureID,
                                accessoryID,
                                accessoryType,
                                backgroundColor,
                                stickmanPaint.getColor(),
                                stickmanStrokeWidth,
                                posePositions.poseLandmarkPositionX,
                                posePositions.poseLandmarkPositionY
                        )
                );
            }
        }
    }

    public void setBackgroundImage(Bitmap bitmap) {
        backgroundImage = bitmap;
        isBackgroundImageUpdated = true;
    }

    public void setBackgroundColor(int colorValue) {
        this.backgroundColor = colorValue;
        isBackgroundColorUpdated = true;
    }

    public void setFigureID(int figureID) {
        this.figureID = figureID;
    }

    public void setFigureColor(int color) {
        stickmanPaint.setColor(color);
    }

    public void setFigureStrokeWidth(float width) {
        stickmanStrokeWidth = width;
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

    public void setEncodeStickmanData() {
        encodeStickmanData = true;
    }
}
