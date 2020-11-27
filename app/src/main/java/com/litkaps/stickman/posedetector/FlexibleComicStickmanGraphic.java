package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;

import java.util.List;

/**
 * Draw a "cyanide and happiness" style stickman
 */
public class FlexibleComicStickmanGraphic extends StickmanGraphic {

    FlexibleComicStickmanGraphic(GraphicOverlay overlay, Pose pose, int accessoryID, int accessoryType, Paint stickmanPaint) {
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

        PointF neckPoint = getPointBetween(rightShoulder, leftShoulder);

        PointF pointBetweenEyes = getPointBetween(rightEye, leftEye);
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth, rightMouth);
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        // neck
        drawLine(canvas, neckPoint, headCenterPoint, stickmanPaint);

        // Left body
        drawLine(canvas, leftShoulder, leftHip, stickmanPaint);
        // Right body
        drawLine(canvas, rightShoulder, rightHip, stickmanPaint);

        // shoulder line
        drawLine(canvas, leftShoulder, rightShoulder, stickmanPaint);
        // waist line
        drawLine(canvas, leftHip, rightHip, stickmanPaint);

        Path path = new Path();
        stickmanPaint.setStyle(Paint.Style.STROKE);

        // legs
        path.moveTo(leftAnkle.x, leftAnkle.y);
        path.quadTo(leftKnee.x, leftKnee.y, leftHip.x, leftHip.y);
        path.moveTo(rightAnkle.x, rightAnkle.y);
        path.quadTo(rightKnee.x, rightKnee.y, rightHip.x, rightHip.y);
        canvas.drawPath(path, stickmanPaint);

        // arms
        path.moveTo(leftWrist.x, leftWrist.y);
        path.quadTo(leftElbow.x, leftElbow.y, leftShoulder.x, leftShoulder.y);
        path.moveTo(rightWrist.x, rightWrist.y);
        path.quadTo(rightElbow.x, rightElbow.y, rightShoulder.x, rightShoulder.y);

        canvas.drawPath(path, stickmanPaint);

        stickmanPaint.setStyle(Paint.Style.FILL);

        drawHead(canvas);
        drawSmile(canvas);
        drawRectangularEyes(canvas);
        drawAccessory(canvas);
    }
}
