package com.litkaps.stickman.posedetector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;

import java.util.List;
import java.util.Locale;

/**
 * generic class for stickman drawing
 */
class StickmanGraphic extends GraphicOverlay.Graphic {
    int accessoryID;
    int accessoryType;

    Pose pose;
    Paint stickmanPaint;
    Paint whitePaint;
    Paint facePaint;

    StickmanGraphic(GraphicOverlay overlay, Pose pose, int accessoryID, int accessoryType, Paint stickmanPaint) {
        super(overlay);

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);

        facePaint = new Paint();
        facePaint.setStrokeWidth(5);
        facePaint.setColor(Color.BLACK);

        this.pose = pose;
        this.accessoryID = accessoryID;
        this.accessoryType = accessoryType;
        this.stickmanPaint = stickmanPaint;
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

        drawHead(canvas);
        drawSmile(canvas);
        drawRectangularEyes(canvas);
        drawAccessory(canvas);
    }

    void drawAccessory(Canvas canvas) {
        if (accessoryType == -1)
            return;
        Bitmap accessory = BitmapFactory.decodeResource(getApplicationContext().getResources(), accessoryID);

        PointF pointBetweenEyes = getPointBetween(translatePoint(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER).getPosition()), translatePoint(pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition()));
        PointF pointBetweenMouthCorners = getPointBetween(translatePoint(pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition()), translatePoint(pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition()));
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        float headRadius =  getDistance(pointBetweenEyes, pointBetweenMouthCorners);

        Matrix matrix = null;
        switch (accessoryType) {
            case 0: // hat
                matrix = calculateTransformMatrix(
                        translateX(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER).getPosition().x),
                        translateY(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER).getPosition().y),
                        translateX(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER).getPosition().x),
                        translateY(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER).getPosition().y),
                        headCenterPoint.x,
                        headCenterPoint.y - headRadius,
                        accessory.getWidth(),
                        accessory.getHeight(),
                        headRadius / accessory.getWidth() * 7f,
                        0
                );

                break;

            case 1: // handheld
                float scaleX =
                        (getDistance(
                                pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition(),
                                pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition()
                        ) / accessory.getWidth()
                        ) * scale(2);

                matrix = calculateTransformMatrix(
                        translateX(pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().x),
                        translateY(pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().y),
                        translateX(pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().x),
                        translateY(pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().y),
                        translateX(pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().x),
                        translateY(pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().y),
                        accessory.getWidth(),
                        accessory.getHeight(),
                        scaleX,
                        -45f
                );

                break;

            case 3: // glasses
                matrix = calculateTransformMatrix(
                        translateX(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER).getPosition().x),
                        translateY(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER).getPosition().y),
                        translateX(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER).getPosition().x),
                        translateY(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER).getPosition().y),
                        headCenterPoint.x,
                        headCenterPoint.y - headRadius/1.92f,
                        accessory.getWidth(),
                        accessory.getHeight(),
                        headRadius / accessory.getWidth() * 3.4f,
                        0
                );

                break;
        }

        canvas.drawBitmap(accessory, matrix, null);
    }

    void drawRectangularEyes(Canvas canvas) {
        Paint paint = new Paint(stickmanPaint.getColor());
        paint.setStyle(Paint.Style.FILL);

        PoseLandmark leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
        PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);

        // pupillary distance
        float pd = translateX(leftEye.getPosition().x) - translateX(rightEye.getPosition().x);

        Matrix matrix = new Matrix();
        float deltaX = leftEye.getPosition().x - rightEye.getPosition().x;
        float deltaY = leftEye.getPosition().y - rightEye.getPosition().y;
        float thetaRadians = (float) Math.atan2(deltaY, deltaX) / 5f;
        thetaRadians = isImageFlipped() ? -thetaRadians : thetaRadians;
        matrix.setRotate((float) Math.toDegrees(thetaRadians));

        // draw left eye
        canvas.drawRect(
                translateX(rightEye.getPosition().x) - pd / 3f,
                translateY(rightEye.getPosition().y) - pd / 2.2f,
                translateX(rightEye.getPosition().x) + pd / 4.2f,
                translateY(rightEye.getPosition().y) + pd / 2f,
                paint
        );

        // draw right eye
        canvas.drawRect(
                translateX(leftEye.getPosition().x) - pd / 3f,
                translateY(leftEye.getPosition().y) - pd / 2.2f,
                translateX(leftEye.getPosition().x) + pd / 4.2f,
                translateY(leftEye.getPosition().y) + pd / 2f,
                paint
        );

    }

    public void drawSmile(Canvas canvas) {
        Paint paint = new Paint(stickmanPaint.getColor());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        // upper lip
        drawLine(
                canvas,
                pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition(),
                pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition(),
                paint
        );

        // lower lip
        drawCurvedLine(
                canvas,
                pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().x,
                pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().y,
                pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().x,
                pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().y,
                8,
                paint
        );
    }

    public void drawHead(Canvas canvas) {
        PointF pointBetweenEyes = getPointBetween(translatePoint(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER).getPosition()), translatePoint(pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition()));
        PointF pointBetweenMouthCorners = getPointBetween(translatePoint(pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition()), translatePoint(pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition()));
        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        float headRadius = getDistance(pointBetweenEyes, pointBetweenMouthCorners);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(headCenterPoint.x, headCenterPoint.y, (headRadius * 1.46f) + stickmanPaint.getStrokeWidth(), stickmanPaint);
        canvas.drawCircle(headCenterPoint.x, headCenterPoint.y, (headRadius * 1.46f), paint);
    }

}