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
            int stickmanColor,
            double stickmanLineThickness,
            float[] poseLandmarkPositionX,
            float[] poseLandmarkPositionY) {
        this.bitmap = bitmap;
        this.metadata = new StickmanData(
                stickmanTypeId,
                accessoryId,
                accessoryType,
                stickmanColor,
                stickmanLineThickness,
                poseLandmarkPositionX,
                poseLandmarkPositionY
        );
    }
}
