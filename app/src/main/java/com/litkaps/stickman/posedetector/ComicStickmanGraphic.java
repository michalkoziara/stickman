package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;

import java.util.List;

/**
 * Draw a stickman.
 */
public class ComicStickmanGraphic extends StickmanGraphic {

    ComicStickmanGraphic(GraphicOverlay overlay, PosePositions posePositions, int accessoryID, int accessoryType, Paint stickmanPaint) {
        super(overlay, posePositions, accessoryID, accessoryType, stickmanPaint);
    }

    @Override
    public void draw(Canvas canvas) {
        if (posePositions.isEmpty()) {
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

        PointF neckPoint = getPointBetween(rightShoulder, leftShoulder);

        PointF pointBetweenEyes = getPointBetween(rightEye, leftEye);
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth, rightMouth);
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        // Neck
        drawLine(canvas, neckPoint, headCenterPoint, stickmanPaint);

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, stickmanPaint);
        drawLine(canvas, leftElbow, leftWrist, stickmanPaint);
        drawLine(canvas, leftShoulder, leftHip, stickmanPaint);
        drawLine(canvas, leftHip, leftKnee, stickmanPaint);
        drawLine(canvas, leftKnee, leftAnkle, stickmanPaint);

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, stickmanPaint);
        drawLine(canvas, rightElbow, rightWrist, stickmanPaint);
        drawLine(canvas, rightShoulder, rightHip, stickmanPaint);
        drawLine(canvas, rightHip, rightKnee, stickmanPaint);
        drawLine(canvas, rightKnee, rightAnkle, stickmanPaint);

        // Shoulder line
        drawLine(canvas, leftShoulder, rightShoulder, stickmanPaint);
        // Waist line
        drawLine(canvas, leftHip, rightHip, stickmanPaint);

        drawHead(canvas);
        drawSmile(canvas);
        drawRectangularEyes(canvas);
        drawAccessory(canvas);
    }

}
