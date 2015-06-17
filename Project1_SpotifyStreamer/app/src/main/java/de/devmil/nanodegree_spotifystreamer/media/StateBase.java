package de.devmil.nanodegree_spotifystreamer.media;

/**
 * This class simply implements all methods (but the Id) for the State interface.
 * The idea is that each state only overrides the methods that it is interested in
 */
abstract class StateBase implements State {

    @Override
    public void onEnter() {}

    @Override
    public void onLeave(@ID int newState) {}

    @Override
    public void onPlay() {}

    @Override
    public void onPause() {}

    @Override
    public void onStop() {}

    @Override
    public void onSongFinished() {}

    @Override
    public void onMetadataChanged() {}

    @Override
    public void seekTo(int ms) {}

    @Override
    public void onActiveTrackIndexChanged() {}
}
