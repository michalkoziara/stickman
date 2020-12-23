package com.litkaps.stickman.posedetector;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class StickmanData implements Serializable {
    public int stickmanTypeId;
    public int accessoryId;
    public int accessoryType;
    public int backgroundColor;
    public int stickmanColor;
    public Uri imageUri;
    public float stickmanLineThickness;
    public float[] poseLandmarkPositionX;
    public float[] poseLandmarkPositionY;

    public StickmanData(
            int stickmanTypeId,
            int accessoryId,
            int accessoryType,
            int backgroundColor,
            int stickmanColor,
            float stickmanLineThickness,
            float[] poseLandmarkPositionX,
            float[] poseLandmarkPositionY) {
        this.stickmanTypeId = stickmanTypeId;
        this.accessoryId = accessoryId;
        this.accessoryType = accessoryType;
        this.backgroundColor = backgroundColor;
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
                ", stickmanLineThickness=" + stickmanLineThickness +
                '}';
    }
}