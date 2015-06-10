package de.devmil.nanodegree_spotifystreamer.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface PlaybackState {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ERROR, INIT, READY, PAUSED, PLAYING})
    public @interface Value {};

    int ERROR = -1;
    int INIT = 0;
    int READY = 1;
    int PAUSED = 2;
    int PLAYING = 3;
}
