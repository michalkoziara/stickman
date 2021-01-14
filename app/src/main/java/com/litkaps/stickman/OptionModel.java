package com.litkaps.stickman;

public class OptionModel {
    public String name;
    public int imageResourceID;
    public int accessoryType;
    public int accessoryID;
    public int tint = -1;

    // Figure accessory.
    public OptionModel(int iconResourceID, int accessoryID, int accessoryType) {
        this.imageResourceID = iconResourceID;
        this.accessoryType = accessoryType;
        this.accessoryID = accessoryID;
    }

    public OptionModel(int iconResourceID, int tint, String name) {
        this.imageResourceID = iconResourceID;
        this.tint = tint;
        this.name = name;
    }

    public OptionModel(int iconResourceID, int tint) {
        this.imageResourceID = iconResourceID;
        this.tint = tint;
    }

    public OptionModel(int tint) {
        imageResourceID = R.drawable.ic_baseline_paint_24;
        this.tint = tint;
    }
}