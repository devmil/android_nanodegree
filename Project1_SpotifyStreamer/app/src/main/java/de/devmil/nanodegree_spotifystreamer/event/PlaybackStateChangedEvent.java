package de.devmil.nanodegree_spotifystreamer.event;

public class PlaybackStateChangedEvent {
    @PlaybackState.Value
    private int oldState;
    @PlaybackState.Value
    private int newState;

    public PlaybackStateChangedEvent(@PlaybackState.Value int oldState, @PlaybackState.Value int newState) {
        this.oldState = oldState;
        this.newState = newState;
    }

    @PlaybackState.Value
    public int getOldState() {
        return oldState;
    }

    @PlaybackState.Value
    public int getNewState() {
        return newState;
    }
}
