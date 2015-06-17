package de.devmil.nanodegree_spotifystreamer.media;

public interface TracksPlayerListener {
    void onCurrentTrackChanged(int oldIndex, int newIndex);
    void onNavigationOptionsChanged();
    void onPositionChanged();
    void onPrepared();
    void onStateChanged(@State.ID int oldState, @State.ID int newState);
}
