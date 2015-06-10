package de.devmil.nanodegree_spotifystreamer.event;

public class PlaybackPositionChangedEvent {
    private int positionMS;
    private int durationMS;

    public PlaybackPositionChangedEvent(int positionMS, int durationMS) {
        this.positionMS = positionMS;
        this.durationMS = durationMS;
    }

    public int getPositionMS() {
        return positionMS;
    }

    public int getDurationMS() {
        return durationMS;
    }
}
