package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.GraphicOverlay.Graphic;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * Draw a stickman
 */
public class ComicStickmanGraphic extends StickmanGraphic{

    ComicStickmanGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood, int accessoryID, int accessoryType, Paint stickmanPaint) {
        super(overlay, pose, showInFrameLikelihood, accessoryID, accessoryType, stickmanPaint);
    }

    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) {
            return;
        }

        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

//        PoseLandmark nose = pose.getPoseLandmark(PoseLandmark.NOSE);
//        PoseLandmark leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER);
        PoseLandmark leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
//        PoseLandmark leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER);
//        PoseLandmark rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER);
        PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
//        PoseLandmark rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER);
//        PoseLandmark leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR);
//        PoseLandmark rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR);
        PoseLandmark leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH);
        PoseLandmark rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH);

        PointF neckPoint = getPointBetween(rightShoulder.getPosition(), leftShoulder.getPosition());

        PointF pointBetweenEyes = getPointBetween(rightEye.getPosition(), leftEye.getPosition());
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth.getPosition(), rightMouth.getPosition());
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        // neck
        drawLine(canvas, neckPoint, headCenterPoint, stickmanPaint);

        // Left body
        drawLine(canvas, leftShoulder.getPosition(), leftElbow.getPosition(), stickmanPaint);
        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), stickmanPaint);
        drawLine(canvas, leftShoulder.getPosition(), leftHip.getPosition(), stickmanPaint);
        drawLine(canvas, leftHip.getPosition(), leftKnee.getPosition(), stickmanPaint);
        drawLine(canvas, leftKnee.getPosition(), leftAnkle.getPosition(), stickmanPaint);

        // Right body
        drawLine(canvas, rightShoulder.getPosition(), rightElbow.getPosition(), stickmanPaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), stickmanPaint);
        drawLine(canvas, rightShoulder.getPosition(), rightHip.getPosition(), stickmanPaint);
        drawLine(canvas, rightHip.getPosition(), rightKnee.getPosition(), stickmanPaint);
        drawLine(canvas, rightKnee.getPosition(), rightAnkle.getPosition(), stickmanPaint);

        // shoulder line
        drawLine(canvas, leftShoulder.getPosition(), rightShoulder.getPosition(), stickmanPaint);
        // waist line
        drawLine(canvas, leftHip.getPosition(), rightHip.getPosition(), stickmanPaint);

        drawHead(canvas);
        drawSmile(canvas);
        drawRectangularEyes(canvas);
        drawAccessory(canvas);
    }

}
