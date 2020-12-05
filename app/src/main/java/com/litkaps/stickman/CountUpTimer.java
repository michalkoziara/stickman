package com.litkaps.stickman;

import android.os.CountDownTimer;

public abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private final long duration;

    protected CountUpTimer(long durationMs) {
        super(durationMs, INTERVAL_MS);
        this.duration = durationMs;
    }

    public abstract void onTick(int msSecond);

    @Override
    public void onTick(long msUntilFinished) {
        int msSecond = (int) (duration - msUntilFinished);
        onTick(msSecond);
    }

    @Override
    public void onFinish() {
        onTick(duration / 1000);
    }
}