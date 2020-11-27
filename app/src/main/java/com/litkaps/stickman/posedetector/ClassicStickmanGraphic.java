package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;

import java.util.List;

public class ClassicStickmanGraphic extends StickmanGraphic {

    ClassicStickmanGraphic(GraphicOverlay overlay, Pose pose, int accessoryID, int accessoryType, Paint stickmanPaint) {
        super(overlay, pose, accessoryID, accessoryType, stickmanPaint);
    }

    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) {
            return;
        }

        PointF leftShoulder = getPosition(PoseLandmark.LEFT_SHOULDER);
        PointF rightShoulder = getPosition(PoseLandmark.RIGHT_SHOULDER);
        PointF leftElbow = getPosition(PoseLandmark.LEFT_ELBOW);
        PointF rightElbow = getPosition(PoseLandmark.RIGHT_ELBOW);
        PointF leftWrist = getPosition(PoseLandmark.LEFT_WRIST);
        PointF rightWrist = getPosition(PoseLandmark.RIGHT_WRIST);
        PointF leftHip = getPosition(PoseLandmark.LEFT_HIP);
        PointF rightHip = getPosition(PoseLandmark.RIGHT_HIP);
        PointF leftKnee = getPosition(PoseLandmark.LEFT_KNEE);
        PointF rightKnee = getPosition(PoseLandmark.RIGHT_KNEE);
        PointF leftAnkle = getPosition(PoseLandmark.LEFT_ANKLE);
        PointF rightAnkle = getPosition(PoseLandmark.RIGHT_ANKLE);
        PointF leftEye = getPosition(PoseLandmark.LEFT_EYE);
        PointF rightEye = getPosition(PoseLandmark.RIGHT_EYE);
        PointF leftMouth = getPosition(PoseLandmark.LEFT_MOUTH);
        PointF rightMouth = getPosition(PoseLandmark.RIGHT_MOUTH);

        drawLine(canvas, leftAnkle, leftKnee, stickmanPaint);
        drawLine(canvas, rightAnkle, rightKnee, stickmanPaint);

        PointF waistPoint = getPointBetween(rightHip, leftHip);
        drawLine(canvas, leftKnee, waistPoint, stickmanPaint);
        drawLine(canvas, rightKnee, waistPoint, stickmanPaint);

        PointF neckPoint = getPointBetween(rightShoulder, leftShoulder);
        drawLine(canvas, waistPoint, neckPoint, stickmanPaint);

        drawLine(canvas, neckPoint, leftElbow, stickmanPaint);
        drawLine(canvas, neckPoint, rightElbow, stickmanPaint);

        drawLine(canvas, leftElbow, leftWrist, stickmanPaint);
        drawLine(canvas, rightElbow, rightWrist, stickmanPaint);

        PointF pointBetweenEyes = getPointBetween(rightEye, leftEye);
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth, rightMouth);
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        // Adds the neck.
        drawLine(canvas, neckPoint, headCenterPoint, stickmanPaint);

        drawHead(canvas);
        drawSmile(canvas);
        drawRectangularEyes(canvas);
        drawAccessory(canvas);
    }
}
