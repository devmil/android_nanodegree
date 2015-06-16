package de.devmil.nanodegree_spotifystreamer.event;

public class PlaybackPlayingStateChanged {
    private boolean isPlaying;

    public PlaybackPlayingStateChanged(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
