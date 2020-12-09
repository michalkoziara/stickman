package com.litkaps.stickman.posedetector;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class StickmanData implements Serializable {
    public final int stickmanTypeId;
    public final int accessoryId;
    public final int accessoryType;
    public final int stickmanColor;
    public final double stickmanLineThickness;
    public final float[] poseLandmarkPositionX;
    public final float[] poseLandmarkPositionY;

    public StickmanData(
            int stickmanTypeId,
            int accessoryId,
            int accessoryType,
            int stickmanColor,
            double stickmanLineThickness,
            float[] poseLandmarkPositionX,
            float[] poseLandmarkPositionY) {
        this.stickmanTypeId = stickmanTypeId;
        this.accessoryId = accessoryId;
        this.accessoryType = accessoryType;
        this.stickmanColor = stickmanColor;
        this.stickmanLineThickness = stickmanLineThickness;
        this.poseLandmarkPositionX = poseLandmarkPositionX;
        this.poseLandmarkPositionY = poseLandmarkPositionY;
    }

    @NonNull
    @Override
    public String toString() {
        return "StickmanMetadata{" +
                "stickmanTypeId=" + stickmanTypeId +
                ", accessoryId=" + accessoryId +
                ", accessoryType=" + accessoryType +
                ", stickmanColor=" + stickmanColor +
                ", stickmanLineSize=" + stickmanLineThickness +
                '}';
    }
}