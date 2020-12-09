package com.litkaps.stickman.posedetector;

import android.graphics.PointF;

import com.google.mlkit.vision.mediapipe.pose.PoseHolder;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PosePositions {
    float[] poseLandmarkPositionX;
    float[] poseLandmarkPositionY;


    public PosePositions(Pose pose) {
        List<PoseLandmark> poseLandmarks = pose.getAllPoseLandmarks();
        poseLandmarkPositionX = new float[poseLandmarks.size()];
        poseLandmarkPositionY = new float[poseLandmarks.size()];

        for (int i = 0; i < poseLandmarks.size(); i++) {
            PointF poseLandmarkPosition = poseLandmarks.get(i).getPosition();
            poseLandmarkPositionX[i] = poseLandmarkPosition.x;
            poseLandmarkPositionY[i] = poseLandmarkPosition.y;
        }
    }

    public PosePositions(float[] poseLandmarkPositionX, float[] poseLandmarkPositionY) {
        this.poseLandmarkPositionX = poseLandmarkPositionX;
        this.poseLandmarkPositionY = poseLandmarkPositionY;
    }

    public PointF getLandmarkPosition(int i) {
        return new PointF(poseLandmarkPositionX[i], poseLandmarkPositionY[i]);
    }

    public boolean isEmpty() {
        return (poseLandmarkPositionX == null && poseLandmarkPositionY == null)
                || (poseLandmarkPositionX.length == 0 && poseLandmarkPositionY.length == 0);
    }
}
