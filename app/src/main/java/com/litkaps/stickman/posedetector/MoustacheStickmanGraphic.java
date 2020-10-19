package com.litkaps.stickman.posedetector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.R;

import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

class MoustacheStickmanGraphic extends GraphicOverlay.Graphic {

    private static final float DOT_RADIUS = 1.0f;
    private final Pose pose;
    private final Paint whitePaint;

    MoustacheStickmanGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood) {
        super(overlay);

        this.pose = pose;
        whitePaint = new Paint();
        whitePaint.setStrokeWidth(5);
        whitePaint.setColor(Color.BLACK);
        whitePaint.setTextSize((float) (30));

    }

    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty())
            return;

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

        PoseLandmark leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY);
        PoseLandmark rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY);
        PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
        PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
        PoseLandmark leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB);
        PoseLandmark rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
        PoseLandmark leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL);
        PoseLandmark rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL);
        PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
        PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);

        PoseLandmark leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
        PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
        PoseLandmark leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH);
        PoseLandmark rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH);

        drawLine(canvas, leftShoulder.getPosition(), rightShoulder.getPosition(), whitePaint);
        drawLine(canvas, leftHip.getPosition(), rightHip.getPosition(), whitePaint);

        // Left body
        drawLine(canvas, leftShoulder.getPosition(), leftElbow.getPosition(), whitePaint);
        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), whitePaint);
        drawLine(canvas, leftShoulder.getPosition(), leftHip.getPosition(), whitePaint);
        drawLine(canvas, leftHip.getPosition(), leftKnee.getPosition(), whitePaint);
        drawLine(canvas, leftKnee.getPosition(), leftAnkle.getPosition(), whitePaint);
        drawLine(canvas, leftWrist.getPosition(), leftThumb.getPosition(), whitePaint);
        drawLine(canvas, leftWrist.getPosition(), leftPinky.getPosition(), whitePaint);
        drawLine(canvas, leftWrist.getPosition(), leftIndex.getPosition(), whitePaint);
        drawLine(canvas, leftAnkle.getPosition(), leftHeel.getPosition(), whitePaint);
        drawLine(canvas, leftHeel.getPosition(), leftFootIndex.getPosition(), whitePaint);

        // Right body
        drawLine(canvas, rightShoulder.getPosition(), rightElbow.getPosition(), whitePaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), whitePaint);
        drawLine(canvas, rightShoulder.getPosition(), rightHip.getPosition(), whitePaint);
        drawLine(canvas, rightHip.getPosition(), rightKnee.getPosition(), whitePaint);
        drawLine(canvas, rightKnee.getPosition(), rightAnkle.getPosition(), whitePaint);
        drawLine(canvas, rightWrist.getPosition(), rightThumb.getPosition(), whitePaint);
        drawLine(canvas, rightWrist.getPosition(), rightPinky.getPosition(), whitePaint);
        drawLine(canvas, rightWrist.getPosition(), rightIndex.getPosition(), whitePaint);
        drawLine(canvas, rightAnkle.getPosition(), rightHeel.getPosition(), whitePaint);
        drawLine(canvas, rightHeel.getPosition(), rightFootIndex.getPosition(), whitePaint);


        PointF pointBetweenEyes = getPointBetween(rightEye.getPosition(), leftEye.getPosition());
        PointF pointBetweenMouthCorners = getPointBetween(leftMouth.getPosition(), rightMouth.getPosition());
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        float headRadius = getDistance(pointBetweenEyes, pointBetweenMouthCorners);
        //drawCircle(canvas, headCenterPoint, (headRadius * 3) + 5, whitePaint, false);
        drawCircle(canvas, headCenterPoint, (headRadius * 3), whitePaint, false);

        float mouthWidth =  pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().x - pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().x;

        Bitmap moustache = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.moustache);

        float deltaX = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().x - pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().x;
        float deltaY =  pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().y - pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().y;
        float thetaRadians = (float)Math.atan2(deltaY, deltaX);

        Matrix matrix = new Matrix();
        matrix.preRotate(thetaRadians);

        float sx = 2 * mouthWidth / 480.f;
        float sy = 2 * mouthWidth / 480.f;;
        matrix.setScale(2 * mouthWidth / 480.f, 2 * mouthWidth / 480.f);
        //matrix.setScale(0.05f, 0.05f, 0, 0);

        matrix.postTranslate(translateX(pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().x) - moustache.getWidth() * sx/2, translateY(pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().y) - moustache.getHeight() * sy/2);

        canvas.drawBitmap(moustache, matrix, null);

//        for (PoseLandmark landmark : landmarks) {
//            drawPoint(canvas, landmark.getPosition(), whitePaint);
//            canvas.drawText(
//                    String.format("%.0f", landmark.getPosition().x) + ", " + String.format("%.0f", landmark.getPosition().y),
//                    translateX(landmark.getPosition().x),
//                    translateY(landmark.getPosition().y),
//                    whitePaint);
//        }

    }

    void drawCircle(Canvas canvas, @Nullable PointF point, float radius, Paint paint, boolean fill) {
        if (point == null) {
            return;
        }
        if (fill) {
            paint.setStyle(Paint.Style.FILL);
        }
        else
            paint.setStyle(Paint.Style.STROKE);

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
                whitePaint);
        drawCurvedLine(canvas,
                leftEyeInnerX, leftEyeInnerY,
                leftEyeOuterX, leftEyeOuterY,
                lowerEyelidRadius,
                whitePaint);
    }

    PointF getPointBetween(PointF a, PointF b) {
        return new PointF((a.x + b.x) / 2, (a.y + b.y) / 2);
    }

    float getDistance(PointF a, PointF b) {
        return (float) Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle, float scaleX, float scaleY) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        matrix.postScale(scaleX, scaleY);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
