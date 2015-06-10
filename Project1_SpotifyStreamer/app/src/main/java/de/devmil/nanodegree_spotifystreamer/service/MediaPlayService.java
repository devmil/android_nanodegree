package de.devmil.nanodegree_spotifystreamer.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.devmil.nanodegree_spotifystreamer.data.PlayerData;
import de.devmil.nanodegree_spotifystreamer.data.Track;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackNavigationOptionsChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackPositionChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackState;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackStateChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackTrackChangedEvent;
import de.greenrobot.event.EventBus;

public class MediaPlayService extends IntentService implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String ACTION_PLAY = "ACTION_PLAY";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private static final String ACTION_STOP = "ACTION_STOP";
    private static final String ACTION_PREV = "ACTION_PREV";
    private static final String ACTION_NEXT = "ACTION_NEXT";
    private static final String ACTION_TOGGLE_PLAY_PAUSE = "ACTION_TOGGLE_PLAY_PAUSE";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ COMMAND_NEXT, COMMAND_PAUSE, COMMAND_PREV, COMMAND_STOP, COMMAND_PLAY, COMMAND_TOGGLE_PLAY_PAUSE })
    @interface Command {}

    public static final int COMMAND_PLAY = 0;
    public static final int COMMAND_PAUSE = 1;
    public static final int COMMAND_STOP = 2;
    public static final int COMMAND_PREV = 3;
    public static final int COMMAND_NEXT = 4;
    public static final int COMMAND_TOGGLE_PLAY_PAUSE = 5;

    public static void executeCommand(Context context, @Command int command) {
        String action = "";
        switch(command) {
            case COMMAND_PLAY:
                action = ACTION_PLAY;
                break;
            case COMMAND_PAUSE:
                action = ACTION_PAUSE;
                break;
            case COMMAND_STOP:
                action = ACTION_STOP;
                break;
            case COMMAND_PREV:
                action = ACTION_PREV;
                break;
            case COMMAND_NEXT:
                action = ACTION_NEXT;
                break;
            case COMMAND_TOGGLE_PLAY_PAUSE:
                action = ACTION_TOGGLE_PLAY_PAUSE;
                break;
        }
        Intent intent = new Intent(context, MediaPlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    public MediaPlayService() {
        super("MediaPlayService");
    }

    public class MediaPlayBinder extends Binder
    {
        public void setPlayerData(PlayerData data) {
            updatePlayerData(data);
        }

        public PlayerData getPlayerData() {
            return playerData;
        }

        public boolean seekTo(int ms) {
            if(mediaPlayer != null
                    && playbackState >= PlaybackState.READY) {
                mediaPlayer.seekTo(ms);
                firePositionChangedEvent(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
                return true;
            }
            return false;
        }
    }

    private MediaPlayer mediaPlayer;
    private String currentUrl = null;
    private PlayerData playerData;
    private @PlaybackState.Value int playbackState = PlaybackState.INIT;

    private void updatePlayerData(PlayerData playerData) {
        doStop();
        int oldIndex = -1;
        if(playerData != null) {
            oldIndex = playerData.getActiveTrackIndex();
        }
        this.playerData = playerData;
        ensureMediaPlayerFor(getCurrentUrl());
        fireTrackChangedEvent(oldIndex, playerData.getActiveTrackIndex(), playerData.getActiveTrack());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MediaPlayBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(ACTION_NEXT.equals(action)) {
            doNext();
        } else if(ACTION_PREV.equals(action)) {
            doPrev();
        } else if(ACTION_PAUSE.equals(action)) {
            doPause();
        } else if(ACTION_PLAY.equals(action)) {
            doPlay();
        } else if(ACTION_STOP.equals(action)) {
            doStop();
        } else if(ACTION_TOGGLE_PLAY_PAUSE.equals(action)) {
            if(playbackState == PlaybackState.PAUSED
                    || playbackState == PlaybackState.READY) {
                doPlay();
            }
            else if(playbackState == PlaybackState.PLAYING) {
                doPause();
            }
        }
    }

    private String getCurrentUrl() {
        if(playerData == null) {
            return null;
        }
        Track track = playerData.getActiveTrack();
        if(track == null) {
            return null;
        }
        return track.getPreviewUrl();
    }

    private void doPlay() {
        switch(playbackState) {
            case PlaybackState.READY:
            case PlaybackState.PAUSED:
                mediaPlayer.start();
                gotoState(PlaybackState.PLAYING);
                break;
        }
    }

    private void doStop() {
        switch(playbackState) {
            case PlaybackState.PAUSED:
            case PlaybackState.PLAYING:
                mediaPlayer.stop();
                gotoState(PlaybackState.READY);
                break;
        }
    }

    private void doPause() {
        switch(playbackState) {
            case PlaybackState.PLAYING:
                mediaPlayer.pause();
                gotoState(PlaybackState.PAUSED);
                break;
        }
    }

    private void doNext() {
        switch(playbackState) {
            case PlaybackState.PLAYING:
            case PlaybackState.PAUSED:
            case PlaybackState.READY:
            {
                if(playerData.canNavigateNext()) {
                    int oldIndex = playerData.getActiveTrackIndex();
                    int newIndex = oldIndex + 1;
                    playerData.setActiveTrackIndex(newIndex);
                    ensureMediaPlayerFor(getCurrentUrl());
                    fireTrackChangedEvent(oldIndex, newIndex, playerData.getActiveTrack());
                    if(!playerData.canNavigateNext()) {
                        fireNavigationOptionsChanged(newIndex, playerData.canNavigatePrev(), playerData.canNavigateNext());
                    }
                }
            }
                break;
        }
    }

    private void doPrev() {
        switch(playbackState) {
            case PlaybackState.PLAYING:
            case PlaybackState.PAUSED:
            case PlaybackState.READY:
            {
                if(playerData.canNavigatePrev()) {
                    int oldIndex = playerData.getActiveTrackIndex();
                    int newIndex = oldIndex - 1;
                    playerData.setActiveTrackIndex(newIndex);
                    ensureMediaPlayerFor(getCurrentUrl());
                    fireTrackChangedEvent(oldIndex, newIndex, playerData.getActiveTrack());
                    if(!playerData.canNavigatePrev()) {
                        fireNavigationOptionsChanged(newIndex, playerData.canNavigatePrev(), playerData.canNavigateNext());
                    }
                }
            }
            break;
        }
    }

    private void ensureMediaPlayerFor(String url) {
        if(mediaPlayer == null
                || currentUrl == null
                || !currentUrl.equals(url)) {
            //this removes the current media player
            gotoState(PlaybackState.INIT);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                removeMediaPlayer();
                gotoState(PlaybackState.ERROR);
            }
        }
    }

    private void removeMediaPlayer() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            currentUrl = null;
        }
    }

    private void gotoState(@PlaybackState.Value int newState) {
        int oldState = playbackState;
        if(oldState == newState) {
            return;
        }
        playbackState = newState;
        fireStateChangedEvent(oldState, newState);
        if(playbackState <= PlaybackState.INIT) {
            removeMediaPlayer();
        }
    }

    private void fireNavigationOptionsChanged(int currentIndex, boolean canNavigatePrev, boolean canNavigateNext) {
        EventBus.getDefault().post(new PlaybackNavigationOptionsChangedEvent(currentIndex, canNavigatePrev, canNavigateNext));
    }

    private void fireStateChangedEvent(@PlaybackState.Value int oldState, @PlaybackState.Value int newState) {
        EventBus.getDefault().post(new PlaybackStateChangedEvent(oldState, newState));
    }

    private void fireTrackChangedEvent(int oldIndex, int newIndex, Track newTrack) {
        EventBus.getDefault().post(new PlaybackTrackChangedEvent(oldIndex, newIndex, newTrack));
    }

    private void firePositionChangedEvent(int positionMS, int durationMS) {
        EventBus.getDefault().post(new PlaybackPositionChangedEvent(positionMS, durationMS));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        doNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        gotoState(PlaybackState.ERROR);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        gotoState(PlaybackState.READY);
        fireNavigationOptionsChanged(playerData.getActiveTrackIndex(), playerData.canNavigatePrev(), playerData.canNavigateNext());
        firePositionChangedEvent(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
    }

}
