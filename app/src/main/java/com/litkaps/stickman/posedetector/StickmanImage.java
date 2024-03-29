package com.litkaps.stickman.posedetector;

import android.graphics.Bitmap;

public class StickmanImage {
    public Bitmap bitmap;
    public StickmanData metadata;

    public StickmanImage(
            Bitmap bitmap,
            int stickmanTypeId,
            int accessoryId,
            int accessoryType,
            int backgroundColor,
            int stickmanColor,
            float stickmanLineThickness,
            float[] poseLandmarkPositionX,
            float[] poseLandmarkPositionY) {
        this.bitmap = bitmap;
        this.metadata = new StickmanData(
                stickmanTypeId,
                accessoryId,
                accessoryType,
                backgroundColor,
                stickmanColor,
                stickmanLineThickness,
                poseLandmarkPositionX,
                poseLandmarkPositionY
        );
    }
}
