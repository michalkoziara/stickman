package com.litkaps.stickman.editor;

class Frame {
    int backgroundColor;
    long frameIndex;
    long frameTime;

    public Frame(long frameIndex, long frameTime) {
        this.frameIndex = frameIndex;
        this.frameTime = frameTime;
    }
}
