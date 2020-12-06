package com.litkaps.stickman;

class OptionModel {
    String name;
    int imageResourceID;
    int accessoryType;
    int accessoryID;
    int tint = -1;

    // Figure accessory
    OptionModel(int iconResourceID, int accessoryID, int accessoryType) {
        this.imageResourceID = iconResourceID;
        this.accessoryType = accessoryType;
        this.accessoryID = accessoryID;
    }

    OptionModel(int iconResourceID, int tint) {
        this.imageResourceID = iconResourceID;
        this.tint = tint;
    }

    OptionModel(int tint) {
        imageResourceID = R.drawable.ic_baseline_paint_24;
        this.tint = tint;
    }
}