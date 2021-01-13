package com.litkaps.stickman.posedetector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.MainActivity;

/**
 * Base class for stickman drawing.
 */
abstract class StickmanGraphic extends GraphicOverlay.Graphic {
    protected int accessoryID;
    protected int accessoryType;

    protected PosePositions posePositions;
    protected Paint stickmanPaint;
    protected Paint whitePaint;
    protected Paint facePaint;

    private static final String TAG = "StickmanGraphic";

    StickmanGraphic(GraphicOverlay overlay, PosePositions posePositions, int accessoryID, int accessoryType, Paint stickmanPaint) {
        super(overlay);

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);

        facePaint = new Paint();
        facePaint.setStrokeWidth(scale(5));
        facePaint.setColor(Color.BLACK);

        this.posePositions = posePositions;
        this.accessoryID = accessoryID;
        this.accessoryType = accessoryType;
        this.stickmanPaint = stickmanPaint;
    }

    @Override
    public void draw(Canvas canvas) {
    }

    void drawAccessory(Canvas canvas) {
        if (accessoryType == -1) {
            return;
        }

        Bitmap accessory = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                accessoryID
        );

        PointF pointBetweenEyes = getPointBetween(
                getPosition(PoseLandmark.RIGHT_EYE_OUTER),
                getPosition(PoseLandmark.LEFT_EYE)
        );

        PointF pointBetweenMouthCorners = getPointBetween(
                getPosition(PoseLandmark.LEFT_MOUTH),
                getPosition(PoseLandmark.RIGHT_MOUTH)
        );

        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);
        float headRadius = getDistance(pointBetweenEyes, pointBetweenMouthCorners);

        Matrix matrix = null;
        try {
            switch (accessoryType) {
                case MainActivity.HAT:
                    matrix = calculateTransformMatrix(
                            getPositionX(PoseLandmark.RIGHT_EYE_INNER),
                            getPositionY(PoseLandmark.RIGHT_EYE_INNER),
                            getPositionX(PoseLandmark.LEFT_EYE_INNER),
                            getPositionY(PoseLandmark.LEFT_EYE_INNER),
                            headCenterPoint.x,
                            headCenterPoint.y - headRadius,
                            accessory.getWidth(),
                            accessory.getHeight(),
                            headRadius / accessory.getWidth() * 7f,
                            0
                    );

                    break;

                case MainActivity.HANDHELD: // handheld
                    float scaleX = (
                            getDistance(
                                    getPosition(PoseLandmark.RIGHT_HIP, false),
                                    getPosition(PoseLandmark.RIGHT_SHOULDER, false)
                            ) / accessory.getWidth()
                    ) * scale(2);

                    matrix = calculateTransformMatrix(
                            getPositionX(PoseLandmark.RIGHT_WRIST),
                            getPositionY(PoseLandmark.RIGHT_WRIST),
                            getPositionX(PoseLandmark.RIGHT_ELBOW),
                            getPositionY(PoseLandmark.RIGHT_ELBOW),
                            getPositionX(PoseLandmark.RIGHT_WRIST),
                            getPositionY(PoseLandmark.RIGHT_WRIST),
                            accessory.getWidth(),
                            accessory.getHeight(),
                            scaleX,
                            -45f
                    );

                    break;

                case MainActivity.GLASSES: // glasses
                    matrix = calculateTransformMatrix(
                            getPositionX(PoseLandmark.RIGHT_EYE_INNER),
                            getPositionY(PoseLandmark.RIGHT_EYE_INNER),
                            getPositionX(PoseLandmark.LEFT_EYE_INNER),
                            getPositionY(PoseLandmark.LEFT_EYE_INNER),
                            headCenterPoint.x,
                            headCenterPoint.y - headRadius / 1.92f,
                            accessory.getWidth(),
                            accessory.getHeight(),
                            headRadius / accessory.getWidth() * 3.4f,
                            0
                    );

                    break;
                default:
                    break;
            }
        } catch (PoseLandmarkException poseLandmarkException) {
            Log.e(TAG, "Error drawing accessory.");

            // If encountered error then stop drawing accessory.
            return;
        }

        if (matrix != null) {
            canvas.drawBitmap(accessory, matrix, null);
        }
    }

    void drawRectangularEyes(Canvas canvas) {
        Paint paint = new Paint(stickmanPaint.getColor());
        paint.setStyle(Paint.Style.FILL);

        PointF leftEyePoint = getPosition(PoseLandmark.LEFT_EYE);
        PointF rightEyePoint = getPosition(PoseLandmark.RIGHT_EYE);

        // Stop drawing if eye not found.
        if (leftEyePoint == null || rightEyePoint == null) {
            return;
        }

        // Calculate a distance between pupillaries.
        float pupilDistance = leftEyePoint.x - rightEyePoint.x;

        Matrix matrix = new Matrix();
        float deltaX = leftEyePoint.x - rightEyePoint.x;
        float deltaY = leftEyePoint.y - rightEyePoint.y;
        float thetaRadians = (float) Math.atan2(deltaY, deltaX) / 5f;
        // subtract 180 degrees (3.142 Rad) if necessary
        thetaRadians = isImageFlipped() ? thetaRadians - 3.142f : thetaRadians;
        matrix.setRotate((float) Math.toDegrees(thetaRadians));

        // Draw left eye
        canvas.drawRect(
                rightEyePoint.x - pupilDistance / 3f,
                rightEyePoint.y - pupilDistance / 2.2f,
                rightEyePoint.x + pupilDistance / 4.2f,
                rightEyePoint.y + pupilDistance / 2f,
                paint
        );

        // Draw right eye
        canvas.drawRect(
                leftEyePoint.x - pupilDistance / 3f,
                leftEyePoint.y - pupilDistance / 2.2f,
                leftEyePoint.x + pupilDistance / 4.2f,
                leftEyePoint.y + pupilDistance / 2f,
                paint
        );
    }

    public void drawSmile(Canvas canvas) {
        Paint paint = new Paint(stickmanPaint.getColor());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(scale(5));

        try {
            // upper lip
            drawLine(
                    canvas,
                    getPosition(PoseLandmark.LEFT_MOUTH),
                    getPosition(PoseLandmark.RIGHT_MOUTH),
                    paint
            );

            // lower lip
            drawCurvedLine(
                    canvas,
                    getPositionX(PoseLandmark.LEFT_MOUTH),
                    getPositionY(PoseLandmark.LEFT_MOUTH),
                    getPositionX(PoseLandmark.RIGHT_MOUTH),
                    getPositionY(PoseLandmark.RIGHT_MOUTH),
                    8,
                    paint
            );
        } catch (PoseLandmarkException poseLandmarkException) {
            Log.e(TAG, "Error drawing smile.");
        }
    }

    public void drawHead(Canvas canvas) {
        PointF pointBetweenEyes = getPointBetween(
                getPosition(PoseLandmark.RIGHT_EYE_OUTER),
                getPosition(PoseLandmark.LEFT_EYE)
        );

        PointF pointBetweenMouthCorners = getPointBetween(
                getPosition(PoseLandmark.LEFT_MOUTH),
                getPosition(PoseLandmark.RIGHT_MOUTH)
        );

        PointF headCenterPoint = getPointBetween(pointBetweenEyes, pointBetweenMouthCorners);

        float headRadius = getDistance(pointBetweenEyes, pointBetweenMouthCorners);
        float outerCircleRadius = (headRadius * 1.46f) + stickmanPaint.getStrokeWidth();
        float innerCircleRadius = (headRadius * 1.46f);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(headCenterPoint.x, headCenterPoint.y, outerCircleRadius, stickmanPaint);
        canvas.drawCircle(headCenterPoint.x, headCenterPoint.y, innerCircleRadius, paint);
    }

    public PointF getPosition(int poseLandmarkIndex, boolean translateCoordinate) {
        PointF positionPoint = posePositions.getLandmarkPosition(poseLandmarkIndex);

        if (translateCoordinate) {
            return translatePoint(positionPoint);
        }

        return positionPoint;
    }

    public PointF getPosition(int poseLandmarkIndex) {
        return getPosition(poseLandmarkIndex, true);
    }

    public float getPositionX(int poseLandmarkIndex) throws PoseLandmarkException {
        PointF position = getPosition(poseLandmarkIndex);

        if (position == null) {
            throw new PoseLandmarkException("Not found point for given pose landmark.");
        }

        return position.x;
    }

    public float getPositionY(int poseLandmarkIndex) throws PoseLandmarkException {
        PointF position = getPosition(poseLandmarkIndex);

        if (position == null) {
            throw new PoseLandmarkException("Not found point for given pose landmark.");
        }

        return position.y;
    }
}