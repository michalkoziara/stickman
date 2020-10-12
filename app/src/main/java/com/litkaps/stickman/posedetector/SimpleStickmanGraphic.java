package com.litkaps.stickman.posedetector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.litkaps.stickman.GraphicOverlay;
import com.litkaps.stickman.GraphicOverlay.Graphic;

import java.util.List;
import java.util.Locale;

/** Draw a stickman  */
public class SimpleStickmanGraphic extends Graphic {

    private static final float DOT_RADIUS = 8.0f;
    private final Pose pose;
    private final Paint blackPaint;

    SimpleStickmanGraphic(GraphicOverlay overlay, Pose pose, boolean showInFrameLikelihood) {
        super(overlay);

        this.pose = pose;
        blackPaint = new Paint(Color.WHITE);
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

        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        PointF waist =
                new PointF((rightHip.getPosition().x + leftHip.getPosition().x)/2,
                (rightHip.getPosition().y + leftHip.getPosition().y)/2);


        PointF neck =
                new PointF((rightShoulder.getPosition().x + leftShoulder.getPosition().x)/2,
                        (rightShoulder.getPosition().y + leftShoulder.getPosition().y)/2);


        drawPoint(canvas, waist, blackPaint);
        drawPoint(canvas, leftAnkle.getPosition(), blackPaint);
        drawPoint(canvas, rightAnkle.getPosition(), blackPaint);
        drawPoint(canvas, neck, blackPaint);

        drawLine(canvas, leftAnkle.getPosition(), waist, blackPaint);
        drawLine(canvas, rightAnkle.getPosition(), waist, blackPaint);
        drawLine(canvas, waist, neck, blackPaint);

        drawLine(canvas, neck, leftElbow.getPosition(), blackPaint);
        drawLine(canvas, neck, rightElbow.getPosition(), blackPaint);

        drawLine(canvas, leftElbow.getPosition(), leftWrist.getPosition(), blackPaint);
        drawLine(canvas, rightElbow.getPosition(), rightWrist.getPosition(), blackPaint);
    }

    void drawPoint(Canvas canvas, @Nullable PointF point, Paint paint) {
        if (point == null) {
            return;
        }
        canvas.drawCircle(translateX(point.x), translateY(point.y), DOT_RADIUS, paint);
    }

    void drawLine(Canvas canvas, @Nullable PointF start, @Nullable PointF end, Paint paint) {
        if (start == null || end == null) {
            return;
        }
        canvas.drawLine(
                translateX(start.x), translateY(start.y), translateX(end.x), translateY(end.y), paint);
    }
}
