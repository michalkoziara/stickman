package com.litkaps.stickman.posedetector;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class StickmanData implements Serializable {
    public final int stickmanTypeId;
    public final int accessoryId;
    public final int accessoryType;
    public final int stickmanColor;
    public final double stickmanLineSize;

    public StickmanData(
            int stickmanTypeId,
            int accessoryId,
            int accessoryType,
            int stickmanColor,
            double stickmanLineSize) {
        this.stickmanTypeId = stickmanTypeId;
        this.accessoryId = accessoryId;
        this.accessoryType = accessoryType;
        this.stickmanColor = stickmanColor;
        this.stickmanLineSize = stickmanLineSize;
    }

    @NonNull
    @Override
    public String toString() {
        return "StickmanMetadata{" +
                "stickmanTypeId=" + stickmanTypeId +
                ", accessoryId=" + accessoryId +
                ", accessoryType=" + accessoryType +
                ", stickmanColor=" + stickmanColor +
                ", stickmanLineSize=" + stickmanLineSize +
                '}';
    }
}