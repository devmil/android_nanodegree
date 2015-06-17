package de.devmil.nanodegree_spotifystreamer.media;

import android.media.MediaPlayer;
import android.support.annotation.IntDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This wrapper tries to hide some of the weirdness of the MediaPlayer. It can be played while the preparation of the given url isn't done yet
 * and keeps that state for the point of time where the preparation is ready.
 * It also provides a layer of safety to the calls to getCurrentPosition and getDuration as it checks
 * weather the preparation is finished or not before handling off this call to the real MediaPlayer
 */
public class MediaPlayerWrapper implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private String currentUrl;
    private boolean isPrepared;
    private boolean hasError;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef( {STATE_STOPPED, STATE_PLAYING, STATE_PAUSED} )
    @interface States {}

    static final int STATE_STOPPED = 0;
    static final int STATE_PLAYING = 1;
    static final int STATE_PAUSED = 2;

    @States
    private int state = STATE_STOPPED;

    private MediaPlayer mediaPlayer;

    private MediaPlayer.OnCompletionListener onCompletionListener;
    private MediaPlayer.OnPreparedListener onPreparedListener;

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener completionListener) {
        this.onCompletionListener = completionListener;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setUrl(String url) {
        currentUrl = url;
        isPrepared = false;
        hasError = false;
        removeMediaPlayer();
        ensureMediaplayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPrepared() {
        return isPrepared && !hasError;
    }

    public boolean hasError() {
        return hasError;
    }

    public void play() {
        state = STATE_PLAYING;
        if(isPrepared) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        state = STATE_PAUSED;
        if(isPrepared) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        state = STATE_STOPPED;
        if(isPrepared) {
            mediaPlayer.stop();
        }
    }

    public void release() {
        removeMediaPlayer();
    }

    public int getCurrentPosition() {
        if(mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if(mediaPlayer != null && isPrepared) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int ms) {
        if(mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(ms);
        }
    }

    private void removeMediaPlayer() {
        if(mediaPlayer == null) {
            return;
        }
        try
        {
            mediaPlayer.stop();
        } catch(IllegalStateException e) {
            e.printStackTrace();
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void ensureMediaplayer() {
        if (mediaPlayer != null) {
            return;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        state = STATE_STOPPED;
        if(onCompletionListener != null)
            onCompletionListener.onCompletion(mp);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        if(state == STATE_PLAYING)
            mediaPlayer.start();
        if(onPreparedListener != null)
            onPreparedListener.onPrepared(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        hasError = true;
        return false;
    }
}
