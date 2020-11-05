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

/**
 * Draw a stickman
 */
public class FlexibleComicStickmanGraphic extends Graphic {
    private final Pose pose;
    private final Paint blackPaint;
    private final Paint whitePaint;
    private final Paint facePaint;
    boolean showInFrameLikelihood;

    FlexibleComicStickmanGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood) {
        super(overlay);

        this.pose = pose;
        blackPaint = new Paint();
        blackPaint.setStrokeWidth(13);
        blackPaint.setColor(Color.BLACK);
        blackPaint.setTextSize((float) (50));

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        blackPaint.setStrokeWidth(16);

        facePaint = new Paint();
        facePaint.setStrokeWidth(5);
        facePaint.setColor(Color.BLACK);
        this.showInFrameLikelihood = showInFrameLikelihood;
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
        drawLine(canvas, neckPoint, headCenterPoint, blackPaint);

        float headRadius = 1.1f * getDistance(pointBetweenEyes, pointBetweenMouthCorners);

        // head
        drawCircle(canvas, headCenterPoint, (headRadius * 3) + 10, blackPaint, false);
        drawCircle(canvas, headCenterPoint, (headRadius * 3), whitePaint, true);

        float leftMouthX = leftMouth.getPosition().x;
        float leftMouthY = leftMouth.getPosition().y;
        float rightMouthX = rightMouth.getPosition().x;
        float rightMouthY = rightMouth.getPosition().y;

        // smile
        drawLine(canvas, leftMouth.getPosition(), rightMouth.getPosition(), facePaint);
        drawCurvedLine(canvas, leftMouthX, leftMouthY, rightMouthX, rightMouthY, 15, facePaint);

        //drawEyes(canvas, leftEyeInner, leftEyeOuter, leftEye, 10, -13, facePaint);
        //drawEyes(canvas, rightEyeInner, rightEyeOuter, rightEye, 10, -13, facePaint);
        Path path = new Path();
        path.setFillType(Path.FillType.WINDING);


        // Left body
        drawLine(canvas, leftShoulder.getPosition(), leftElbow.getPosition(), blackPaint);
        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), blackPaint);
        drawLine(canvas, leftShoulder.getPosition(), leftHip.getPosition(), blackPaint);
        //drawLine(canvas, leftHip.getPosition(), leftKnee.getPosition(), blackPaint);
        //drawLine(canvas, leftKnee.getPosition(), leftAnkle.getPosition(), blackPaint);

        path.moveTo(translateX(leftAnkle.getPosition().x), translateY(leftAnkle.getPosition().y));
        path.quadTo( translateX(leftKnee.getPosition().x), translateY(leftKnee.getPosition().y), translateX(leftHip.getPosition().x), translateY(leftHip.getPosition().y));

        // Right body
        drawLine(canvas, rightShoulder.getPosition(), rightElbow.getPosition(), blackPaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), blackPaint);
        drawLine(canvas, rightShoulder.getPosition(), rightHip.getPosition(), blackPaint);
        drawLine(canvas, rightHip.getPosition(), rightKnee.getPosition(), blackPaint);
        drawLine(canvas, rightKnee.getPosition(), rightAnkle.getPosition(), blackPaint);

        canvas.drawPath(path, blackPaint);

        // shoulder line
        drawLine(canvas, leftShoulder.getPosition(), rightShoulder.getPosition(), blackPaint);
        // waist line
        drawLine(canvas, leftHip.getPosition(), rightHip.getPosition(), blackPaint);

        float eyeWidth = rightEye.getPosition().x - leftEye.getPosition().x;

        // left eye
        canvas.drawRect(
                translateX(leftEye.getPosition().x) - eyeWidth/2,
                translateX(leftEye.getPosition().y - eyeWidth/3),
                translateX(leftEye.getPosition().x) + eyeWidth/2,
                translateX(leftEye.getPosition().y + eyeWidth/3), blackPaint
        );

        // right eye
        canvas.drawRect(
                translateX(rightEye.getPosition().x) - eyeWidth/2,
                translateX(rightEye.getPosition().y - eyeWidth/3),
                translateX(rightEye.getPosition().x) + eyeWidth/2,
                translateX(rightEye.getPosition().y + eyeWidth/3), blackPaint
        );


//        for (PoseLandmark landmark : landmarks) {
//            drawPoint(canvas, landmark.getPosition(), whitePaint);
//            canvas.drawText(
//                    String.format(Locale.US, "%.2f", landmark.getInFrameLikelihood()),
//                    translateX(landmark.getPosition().x),
//                    translateY(landmark.getPosition().y),
//                    blackPaint);
//        }

    }

}
