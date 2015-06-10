package de.devmil.nanodegree_spotifystreamer.event;

import de.devmil.nanodegree_spotifystreamer.data.Track;

public class PlaybackTrackChangedEvent {
    private int oldIndex;
    private int newIndex;
    private Track newTrack;

    public PlaybackTrackChangedEvent(int oldIndex, int newIndex, Track newTrack) {
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
        this.newTrack = newTrack;
    }

    public int getOldIndex() {
        return oldIndex;
    }

    public int getNewIndex() {
        return newIndex;
    }

    public Track getNewTrack() {
        return newTrack;
    }
}
