package com.flash.booster.monitor;

import android.view.Choreographer;
import android.view.Display;
import android.view.WindowManager;

public class FpsMonitor implements Choreographer.FrameCallback {

    public interface Listener {
        void onUpdate(String fps, String refreshRate);
    }

    private final WindowManager windowManager;
    private final Listener listener;

    private long frames = 0;
    private long lastTime = 0;
    private boolean running = false;

    public FpsMonitor(WindowManager wm, Listener callback) {
        windowManager = wm;
        listener = callback;
    }

    public void start() {
        if (running) return;

        running = true;
        frames = 0;
        lastTime = 0;

        Choreographer.getInstance().postFrameCallback(this);
    }

    public void stop() {
        running = false;
        Choreographer.getInstance().removeFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!running) return;

        frames++;

        if (lastTime == 0) {
            lastTime = frameTimeNanos;
        }

        long elapsed = frameTimeNanos - lastTime;

        if (elapsed >= 1000000000L) {
            double fps = frames * 1000000000.0 / elapsed;

            Display display = windowManager.getDefaultDisplay();
            float refresh = display.getRefreshRate();

            listener.onUpdate(
                String.format("%.1f FPS", fps),
                String.format("%.0f Hz", refresh)
            );

            frames = 0;
            lastTime = frameTimeNanos;
        }

        Choreographer.getInstance().postFrameCallback(this);
    }
}
