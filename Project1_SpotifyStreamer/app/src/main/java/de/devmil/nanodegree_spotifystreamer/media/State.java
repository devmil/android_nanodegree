package de.devmil.nanodegree_spotifystreamer.media;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface for all TracksPlayer states.
 */
public interface State {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ INIT, READY, PLAYING, PAUSED, FINISHED })
    @interface ID {}

    int INIT = 0;
    int READY = 1;
    int PLAYING = 2;
    int PAUSED = 3;
    int FINISHED = 4;

    @ID int getId();

    void onEnter();

    void onLeave(@ID int newState);

    void onPlay();

    void onPause();

    void onStop();

    void onSongFinished();

    void onMetadataChanged();

    void seekTo(int ms);

    void onActiveTrackIndexChanged();
}
