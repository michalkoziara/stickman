package com.litkaps.stickman;

class OptionModel {
    String name;
    int imageResourceID;
    String accessoryType;
    int accessoryID;
    int tint = -1;

    OptionModel(String name, int iconResourceID) {
        this.name = name;
        this.imageResourceID = iconResourceID;
    }

    // Figure accessory
    OptionModel(String name, int iconResourceID, int accessoryID, String accessoryType) {
        this.name = name;
        this.imageResourceID = iconResourceID;
        this.accessoryType = accessoryType;
        this.accessoryID = accessoryID;
    }

    OptionModel(String name, int iconResourceID, int tint) {
        this.name = name;
        this.imageResourceID = iconResourceID;
        this.tint = tint;
    }

    OptionModel(int tint) {
        imageResourceID = R.drawable.ic_baseline_paint_24;
        this.tint = tint;
    }
}