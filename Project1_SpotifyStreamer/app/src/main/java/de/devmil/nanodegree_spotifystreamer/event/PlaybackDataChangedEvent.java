package de.devmil.nanodegree_spotifystreamer.event;

public class PlaybackDataChangedEvent {
    private int positionMS;
    private int durationMS;
    private boolean isReady;
    private boolean hasError;

    public PlaybackDataChangedEvent(int positionMS, int durationMS, boolean isReady, boolean hasError) {
        this.positionMS = positionMS;
        this.durationMS = durationMS;
        this.isReady = isReady;
        this.hasError = hasError;
    }

    public int getPositionMS() {
        return positionMS;
    }

    public int getDurationMS() {
        return durationMS;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean hasError() {
        return hasError;
    }
}
