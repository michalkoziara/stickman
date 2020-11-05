package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.GraphicOverlay.Graphic;

import java.util.List;
import java.util.Locale;

/**
 * Draw a stickman
 */
public class ClassicStickmanGraphic extends Graphic {

    private static final float DOT_RADIUS = 1.0f;
    private final Pose pose;
    private final Paint blackPaint;
    private final Paint whitePaint;
    private final Paint facePaint;
    boolean showInFrameLikelihood;

    ClassicStickmanGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood) {
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

        drawLine(canvas, leftAnkle.getPosition(), leftKnee.getPosition(), blackPaint);
        drawLine(canvas, rightAnkle.getPosition(), rightKnee.getPosition(), blackPaint);

        PointF waistPoint = getPointBetween(rightHip.getPosition(), leftHip.getPosition());
        drawLine(canvas, leftKnee.getPosition(), waistPoint, blackPaint);
        drawLine(canvas, rightKnee.getPosition(), waistPoint, blackPaint);

        PointF neckPoint = getPointBetween(rightShoulder.getPosition(), leftShoulder.getPosition());
        drawLine(canvas, waistPoint, neckPoint, blackPaint);

        drawLine(canvas, neckPoint, leftElbow.getPosition(), blackPaint);
        drawLine(canvas, neckPoint, rightElbow.getPosition(), blackPaint);

        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), blackPaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), blackPaint);

        PointF pointBetweenEyes = getPointBetween(rightEye.getPosition(), leftEye.getPosition());
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth.getPosition(), rightMouth.getPosition());
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        // neck
        drawLine(canvas, neckPoint, headCenterPoint, blackPaint);

        float headRadius = 1.1f * getDistance(pointBetweenEyes, pointBetweenMouthCorners);

        drawCircle(canvas, headCenterPoint, (headRadius * 3) + 10, blackPaint, false);
        drawCircle(canvas, headCenterPoint, (headRadius * 3), whitePaint, true);

        float leftMouthX = leftMouth.getPosition().x;
        float leftMouthY = leftMouth.getPosition().y;
        float rightMouthX = rightMouth.getPosition().x;
        float rightMouthY = rightMouth.getPosition().y;

        drawLine(canvas, leftMouth.getPosition(), rightMouth.getPosition(), facePaint);
        drawCurvedLine(canvas, leftMouthX, leftMouthY, rightMouthX, rightMouthY, 15, facePaint);

        //drawEyes(canvas, leftEyeInner, leftEyeOuter, leftEye, 10, -13, facePaint);
        //drawEyes(canvas, rightEyeInner, rightEyeOuter, rightEye, 10, -13, facePaint);

        float eyeWidth = rightEye.getPosition().x - leftEye.getPosition().x;

//        Rect leftEyeRect = new Rect(
//                translateX(leftEye.getPosition().x) - eyeWidth/2,
//                translateX(leftEye.getPosition().y),
//                translateX(leftEye.getPosition().x) + eyeWidth/2,
//                translateX(leftEye.getPosition().y + 5)
//        );

        canvas.drawRect(
                translateX(leftEye.getPosition().x) - eyeWidth/2,
                translateX(leftEye.getPosition().y - eyeWidth/3),
                translateX(leftEye.getPosition().x) + eyeWidth/2,
                translateX(leftEye.getPosition().y + eyeWidth/3), blackPaint
        );

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
