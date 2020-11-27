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
        PoseLandmark leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
        PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
        PoseLandmark leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH);
        PoseLandmark rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH);

        PointF neckPoint = getPointBetween(rightShoulder.getPosition(), leftShoulder.getPosition());

        PointF pointBetweenEyes = getPointBetween(rightEye.getPosition(), leftEye.getPosition());
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth.getPosition(), rightMouth.getPosition());
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        // neck
        drawLine(canvas, neckPoint, headCenterPoint, stickmanPaint);

        // Left body
        drawLine(canvas, leftShoulder.getPosition(), leftHip.getPosition(), stickmanPaint);
        // Right body
        drawLine(canvas, rightShoulder.getPosition(), rightHip.getPosition(), stickmanPaint);

        // shoulder line
        drawLine(canvas, leftShoulder.getPosition(), rightShoulder.getPosition(), stickmanPaint);
        // waist line
        drawLine(canvas, leftHip.getPosition(), rightHip.getPosition(), stickmanPaint);


        Path path = new Path();
        stickmanPaint.setStyle(Paint.Style.STROKE);

        // legs
        path.moveTo(translateX(leftAnkle.getPosition().x), translateY(leftAnkle.getPosition().y));
        path.quadTo(translateX(leftKnee.getPosition().x), translateY(leftKnee.getPosition().y), translateX(leftHip.getPosition().x), translateY(leftHip.getPosition().y));
        path.moveTo(translateX(rightAnkle.getPosition().x), translateY(rightAnkle.getPosition().y));
        path.quadTo(translateX(rightKnee.getPosition().x), translateY(rightKnee.getPosition().y), translateX(rightHip.getPosition().x), translateY(rightHip.getPosition().y));
        canvas.drawPath(path, stickmanPaint);

        // arms
        path.moveTo(translateX(leftWrist.getPosition().x), translateY(leftWrist.getPosition().y));
        path.quadTo(translateX(leftElbow.getPosition().x), translateY(leftElbow.getPosition().y), translateX(leftShoulder.getPosition().x), translateY(leftShoulder.getPosition().y));
        path.moveTo(translateX(rightWrist.getPosition().x), translateY(rightWrist.getPosition().y));
        path.quadTo(translateX(rightElbow.getPosition().x), translateY(rightElbow.getPosition().y), translateX(rightShoulder.getPosition().x), translateY(rightShoulder.getPosition().y));

        canvas.drawPath(path, stickmanPaint);

        stickmanPaint.setStyle(Paint.Style.FILL);

        drawHead(canvas);
        drawSmile(canvas);
        drawRectangularEyes(canvas);
        drawAccessory(canvas);
    }
}
