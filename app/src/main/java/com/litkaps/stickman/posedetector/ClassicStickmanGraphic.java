package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;

import java.util.List;

/**
 * Draw a stickman
 */
public class ClassicStickmanGraphic extends StickmanGraphic {

    ClassicStickmanGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood, int accessoryID, int accessoryType, Paint stickmanPaint) {
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

        PoseLandmark nose = pose.getPoseLandmark(PoseLandmark.NOSE);
        PoseLandmark leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER);
        PoseLandmark leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
        PoseLandmark leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER);
        PoseLandmark rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER);
        PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
        PoseLandmark rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER);
        PoseLandmark leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR);
        PoseLandmark rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR);
        PoseLandmark leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH);
        PoseLandmark rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH);

        drawLine(canvas, leftAnkle.getPosition(), leftKnee.getPosition(), stickmanPaint);
        drawLine(canvas, rightAnkle.getPosition(), rightKnee.getPosition(), stickmanPaint);

        PointF waistPoint = getPointBetween(rightHip.getPosition(), leftHip.getPosition());
        drawLine(canvas, leftKnee.getPosition(), waistPoint, stickmanPaint);
        drawLine(canvas, rightKnee.getPosition(), waistPoint, stickmanPaint);

        PointF neckPoint = getPointBetween(rightShoulder.getPosition(), leftShoulder.getPosition());
        drawLine(canvas, waistPoint, neckPoint, stickmanPaint);

        drawLine(canvas, neckPoint, leftElbow.getPosition(), stickmanPaint);
        drawLine(canvas, neckPoint, rightElbow.getPosition(), stickmanPaint);

        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), stickmanPaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), stickmanPaint);

        PointF pointBetweenEyes = getPointBetween(rightEye.getPosition(), leftEye.getPosition());
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth.getPosition(), rightMouth.getPosition());
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        // neck
        drawLine(canvas, neckPoint, headCenterPoint, stickmanPaint);

        float headRadius = 1.1f * getDistance(pointBetweenEyes, pointBetweenMouthCorners);

        //drawCircle(canvas, headCenterPoint, (headRadius * 3) + 10, stickmanPaint, false);
        //drawCircle(canvas, headCenterPoint, (headRadius * 3), whitePaint, true);

        drawHead(canvas);
        drawSmile(canvas);
        drawRectangularEyes(canvas);
        drawAccessory(canvas);
    }
}
