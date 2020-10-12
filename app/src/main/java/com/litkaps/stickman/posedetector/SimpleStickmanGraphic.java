package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

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
public class SimpleStickmanGraphic extends Graphic {

    private static final float DOT_RADIUS = 1.0f;
    private final Pose pose;
    private final Paint blackPaint;
    private final Paint whitePaint;

    SimpleStickmanGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood) {
        super(overlay);

        this.pose = pose;
        blackPaint = new Paint();
        blackPaint.setStrokeWidth(5);
        blackPaint.setColor(Color.BLACK);
        blackPaint.setTextSize((float) (50));

        whitePaint = new Paint();
        whitePaint.setStrokeWidth(5);
        whitePaint.setColor(Color.WHITE);
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

        drawLine(canvas, neckPoint, headCenterPoint, blackPaint);

        float headRadius = getDistance(pointBetweenEyes, pointBetweenMouthCorners);
        drawCircle(canvas, headCenterPoint, (headRadius * 3) + 5, blackPaint, false);
        drawCircle(canvas, headCenterPoint, (headRadius * 3), whitePaint, true);

        float leftMouthX = leftMouth.getPosition().x;
        float leftMouthY = leftMouth.getPosition().y;
        float rightMouthX = rightMouth.getPosition().x;
        float rightMouthY = rightMouth.getPosition().y;

        drawLine(canvas, leftMouth.getPosition(), rightMouth.getPosition(), blackPaint);
        drawCurvedLine(canvas, leftMouthX, leftMouthY, rightMouthX, rightMouthY, 15, blackPaint);

        drawEyes(canvas, leftEyeInner, leftEyeOuter, leftEye, 10, -13, blackPaint);
        drawEyes(canvas, rightEyeInner, rightEyeOuter, rightEye, 10, -13, blackPaint);

        for (PoseLandmark landmark : landmarks) {
            drawPoint(canvas, landmark.getPosition(), whitePaint);
            canvas.drawText(
                    String.format(Locale.US, "%.2f", landmark.getInFrameLikelihood()),
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y),
                    blackPaint);
        }

    }

    void drawCircle(Canvas canvas, @Nullable PointF point, float radius, Paint paint, boolean fill) {
        if (point == null) {
            return;
        }
        if (fill) {
            paint.setStyle(Paint.Style.FILL);
        }
        canvas.drawCircle(translateX(point.x), translateY(point.y), radius, paint);
    }

    void drawPoint(Canvas canvas, @Nullable PointF point, Paint paint) {
        drawCircle(canvas, point, DOT_RADIUS, paint, true);
    }

    void drawLine(Canvas canvas, @Nullable PointF start, @Nullable PointF end, Paint paint) {
        if (start == null || end == null) {
            return;
        }
        canvas.drawLine(
                translateX(start.x), translateY(start.y), translateX(end.x), translateY(end.y), paint);
    }

    void drawCurvedLine(Canvas canvas, float x1, float y1, float x2, float y2, int curveRadius, Paint paint) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        final Path path = new Path();
        float midX = x1 + ((x2 - x1) / 2);
        float midY = y1 + ((y2 - y1) / 2);
        float xDiff = midX - x1;
        float yDiff = midY - y1;
        double angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        double angleRadians = Math.toRadians(angle);
        float pointX = (float) (midX + curveRadius * Math.cos(angleRadians));
        float pointY = (float) (midY + curveRadius * Math.sin(angleRadians));

        path.moveTo(translateX(x1), translateY(y1));
        path.cubicTo(translateX(x1), translateY(y1), translateX(pointX), translateY(pointY), translateX(x2), translateY(y2));

        canvas.drawPath(path, paint);
    }

    void drawEyes(
            Canvas canvas,
            PoseLandmark eyeInner,
            PoseLandmark eyeOuter,
            PoseLandmark eye,
            int lowerEyelidRadius,
            int upperEyelidRadius,
            Paint paint) {
        float leftEyeInnerX = eyeInner.getPosition().x;
        float leftEyeInnerY = eyeInner.getPosition().y;
        float leftEyeOuterX = eyeOuter.getPosition().x;
        float leftEyeOuterY = eyeOuter.getPosition().y;

        drawPoint(canvas, eye.getPosition(), paint);
        drawCurvedLine(canvas,
                leftEyeInnerX, leftEyeInnerY,
                leftEyeOuterX, leftEyeOuterY,
                upperEyelidRadius,
                blackPaint);
        drawCurvedLine(canvas,
                leftEyeInnerX, leftEyeInnerY,
                leftEyeOuterX, leftEyeOuterY,
                lowerEyelidRadius,
                blackPaint);
    }

    PointF getPointBetween(PointF a, PointF b) {
        return new PointF((a.x + b.x) / 2, (a.y + b.y) / 2);
    }

    float getDistance(PointF a, PointF b) {
        return (float) Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }
}
