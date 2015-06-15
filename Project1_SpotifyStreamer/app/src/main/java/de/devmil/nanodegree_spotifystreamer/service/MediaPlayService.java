package de.devmil.nanodegree_spotifystreamer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Timer;
import java.util.TimerTask;

import de.devmil.nanodegree_spotifystreamer.data.PlayerData;
import de.devmil.nanodegree_spotifystreamer.data.Track;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackDataChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackNavigationOptionsChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackTrackChangedEvent;
import de.greenrobot.event.EventBus;

/**
 * the media player service controls the playback of the track preview streams.
 * It also is the owner of the currently active playlist so the activity binds to it and
 * listens for events to get the latest and greatest data and status updates
 */
public class MediaPlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

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

    /**
     * this is a convenient method for all clients wanting to send commands to this service.
     * It creates an Intent, fills it appropriately and sends it to the service.
     * @param context
     * @param command
     */
    public static void executeCommand(Context context, @Command int command) {

        context.startService(createCommandIntent(context, command));
    }

    public static Intent createCommandIntent(Context context, @Command int command) {
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
        Intent intent = createStartIntent(context);
        intent.setAction(action);
        return intent;
    }

    public static Intent createStartIntent(Context context) {
        Intent intent = new Intent(context, MediaPlayService.class);
        return intent;
    }

    private static final long DURATION_UPDATE_DELAY_MS = 300;

    private boolean isStartPlayingIntended = false;
    private boolean isServiceBound = false;
    private boolean isStopped = false;
    private State currentState = null;
    private MediaPlayerWrapper mediaPlayer;
    private PlayerData playerData;

    /**
     * The binder for the activity <-> service communication
     */
    public class MediaPlayBinder extends Binder
    {
        public void setPlayerData(PlayerData data) {
            updatePlayerData(data);
        }

        public PlayerData getPlayerData() {
            return playerData;
        }

        public boolean seekTo(int ms) {
            if(currentState != null) {
                currentState.seekTo(ms);
                return true;
            }
            return false;
        }

        public void onBound() {
            PlayerNotification.cancel(MediaPlayService.this);
            hideNotification();
            isServiceBound = true;
            isStopped = false;
            if(playerData != null) {
                fireCurrentMediaPlayerPositions();
                fireCurrentNavigationOptionsChanged();
                fireTrackChangedEvent(playerData.getActiveTrackIndex(), playerData.getActiveTrackIndex(), playerData.getActiveTrack());
            }
        }

        public void beforeUnbind() {
            isServiceBound = false;
            isStopped = false;
            if(currentState != null) {
                boolean isPlay = currentState.getId() == State.PLAYING;
                boolean isPaused = currentState.getId() == State.PAUSED;

                if(isPlay || isPaused) {
                    showNotification(isPlay);
                }
            }
        }
    }

    /**
     * updates the PlayerData with the given new PlayerData.
     * This method also fires the appropriate events
     * @param playerData
     */
    private void updatePlayerData(PlayerData playerData) {
        int oldIndex = -1;
        int activeTrackIndex = -1;
        Track activeTrack = null;
        if(this.playerData != null) {
            oldIndex = this.playerData.getActiveTrackIndex();
        }
        if(playerData != null) {
            activeTrackIndex = playerData.getActiveTrackIndex();
            activeTrack = playerData.getActiveTrack();
        }
        this.playerData = playerData;
        if(currentState != null) {
            currentState.onMetadataChanged();
        }
        fireCurrentNavigationOptionsChanged();
        fireTrackChangedEvent(oldIndex, activeTrackIndex, activeTrack);
        fireCurrentMediaPlayerPositions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String action = intent.getAction();
        if(ACTION_NEXT.equals(action)) {
            doNext();
        } else if(ACTION_PREV.equals(action)) {
            doPrev();
        } else if(ACTION_PAUSE.equals(action)) {
            if(currentState != null) {
                currentState.onPause();
            }
        } else if(ACTION_PLAY.equals(action)) {
            if(currentState != null) {
                currentState.onPlay();
            }
        } else if(ACTION_STOP.equals(action)) {
            isStopped = true;
            if(currentState != null) {
                currentState.onStop();
            }
        } else if(ACTION_TOGGLE_PLAY_PAUSE.equals(action)) {
            if(currentState != null) {
                if(currentState.getId() == State.PLAYING) {
                    currentState.onPause();
                } else {
                    currentState.onPlay();
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MediaPlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        traverseTo(State.INIT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeMediaPlayer();
        if(currentState != null) {
            currentState.onLeave(State.INIT);
            currentState = null;
        }
        PlayerNotification.cancel(MediaPlayService.this);
        stopForeground(true);
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

    private void doNext() {
        if(currentState != null
                && currentState.getId() != State.INIT) {
            if(playerData.canNavigateNext()) {
                int oldIndex = playerData.getActiveTrackIndex();
                int newIndex = oldIndex + 1;
                playerData.setActiveTrackIndex(newIndex);

                currentState.onActiveTrackIndexChanged();

                fireTrackChangedEvent(oldIndex, newIndex, playerData.getActiveTrack());
                fireCurrentNavigationOptionsChanged();
            }
        }
    }

    private void doPrev() {
        if(currentState != null
                && currentState.getId() != State.INIT) {
            if(playerData.canNavigatePrev()) {
                int oldIndex = playerData.getActiveTrackIndex();
                int newIndex = oldIndex - 1;
                playerData.setActiveTrackIndex(newIndex);

                currentState.onActiveTrackIndexChanged();

                fireTrackChangedEvent(oldIndex, newIndex, playerData.getActiveTrack());
                fireCurrentNavigationOptionsChanged();
            }
        }
    }

    private void removeMediaPlayer() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void ensureMediaPlayer() {
        if(mediaPlayer != null) {
            return;
        }
        mediaPlayer = new MediaPlayerWrapper();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    private void fireCurrentNavigationOptionsChanged() {
        if(playerData == null) {
            return;
        }
        EventBus.getDefault().post(new PlaybackNavigationOptionsChangedEvent(playerData.getActiveTrackIndex(), playerData.canNavigatePrev(), playerData.canNavigateNext()));
    }

    private void fireTrackChangedEvent(int oldIndex, int newIndex, Track newTrack) {
        EventBus.getDefault().post(new PlaybackTrackChangedEvent(oldIndex, newIndex, newTrack));
    }

    private void fireCurrentMediaPlayerPositions() {
        if(mediaPlayer == null) {
            return;
        }
        EventBus.getDefault().post(
                new PlaybackDataChangedEvent(
                        mediaPlayer.getCurrentPosition(),
                        mediaPlayer.getDuration(),
                        mediaPlayer.isPrepared(),
                        mediaPlayer.hasError()));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(currentState != null) {
            currentState.onSongFinished();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        fireCurrentMediaPlayerPositions();
    }

    private void doSeekTo(int ms) {
        mediaPlayer.seekTo(ms);
        fireCurrentMediaPlayerPositions();
    }

    private void traverseTo(@State.ID int stateId) {
        if(currentState != null
                && currentState.getId() == stateId)
            return;
        State state = createState(stateId);
        if(currentState != null) {
            currentState.onLeave(stateId);
        }
        currentState = state;
        if(currentState != null) {
            currentState.onEnter();
        }
    }

    private State createState(@State.ID int stateId) {
        switch(stateId) {
            case State.INIT:
                return new StateInit();
            case State.READY:
                return new StateReady();
            case State.PLAYING:
                return new StatePlaying();
            case State.PAUSED:
                return new StatePaused();
            case State.FINISHED:
                return new StateFinished();
        }
        return null;
    }

    private AsyncTask<Object, Object, Object> imageLoadingTask;

    private void showNotification(final boolean currentlyPlaying) {

        if(isServiceBound)
            return;

        final String albumImageUri = playerData.getActiveTrack().getAlbumThumbnailLargeUrl();

        if(imageLoadingTask != null) {
            imageLoadingTask.cancel(true);
        }

        imageLoadingTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                new Handler(MediaPlayService.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MediaPlayService.this).load(albumImageUri).asBitmap().dontAnimate().into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if(!isCancelled()) {
                                    showNotification(resource, currentlyPlaying, false);
                                }
                            }
                        });
                    }
                });
                return null;
            }
        };

//        showNotification(null, currentlyPlaying, true);

        imageLoadingTask.execute();
    }

    private void hideNotification() {
        if(imageLoadingTask != null) {
            imageLoadingTask.cancel(true);
        }
        PlayerNotification.cancel(this);
    }

    private void showNotification(Bitmap albumArtBitmap, boolean currentlyPlaying, boolean bindToNotification) {
        if(playerData == null
                || playerData.getActiveTrack() == null) {
            return;
        }

        String artistName = playerData.getArtistName();
        String trackName = playerData.getActiveTrack().getTrackName();

        Notification notification = PlayerNotification.notify(this, artistName, trackName, albumArtBitmap, currentlyPlaying);

        if(bindToNotification) {
            startForeground(PlayerNotification.NOTIFICATION_ID_STICKY, notification);
        }
    }

    class StateInit extends StateBase {

        @Override
        public int getId() {
            return INIT;
        }

        @Override
        public void onPlay() {
            super.onPlay();
            isStartPlayingIntended = true;
        }

        @Override
        public void onPause() {
            super.onPause();
            isStartPlayingIntended = false;
        }

        @Override
        public void onStop() {
            super.onStop();
            isStartPlayingIntended = false;
        }

        @Override
        public void onMetadataChanged() {
            super.onMetadataChanged();
            traverseTo(State.READY);
        }
    }

    class StateReady extends StateBase {

        @Override
        public void onEnter() {
            super.onEnter();
            ensureMediaPlayer();
            mediaPlayer.stop();
            mediaPlayer.setUrl(getCurrentUrl());
            if(isStartPlayingIntended) {
                isStartPlayingIntended = false;
                traverseTo(State.PLAYING);
            } else if(!isStopped) {
                showNotification(false);
            }
        }

        @Override
        public int getId() {
            return READY;
        }

        @Override
        public void onPlay() {
            super.onPlay();
            traverseTo(State.PLAYING);
        }

        @Override
        public void onActiveTrackIndexChanged() {
            super.onActiveTrackIndexChanged();
            showNotification(false);
        }
    }

    class StatePlaying extends StateBase {

        private Timer updateDurationTimer;
        private AsyncTask<Object, Object, Object> imageLoadingTask;

        public StatePlaying() {
            updateDurationTimer = new Timer("update duration timer");
        }

        @Override
        public void onEnter() {
            super.onEnter();
            mediaPlayer.play();
            updateDurationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(mediaPlayer != null) {
                        fireCurrentMediaPlayerPositions();
                    }
                }
            }, 0, DURATION_UPDATE_DELAY_MS);
            showNotification(true);
        }

        @Override
        public void onLeave(@ID int newState) {
            super.onLeave(newState);
            updateDurationTimer.cancel();
            if(imageLoadingTask != null) {
                imageLoadingTask.cancel(true);
            }
        }

        @Override
        public int getId() {
            return State.PLAYING;
        }

        @Override
        public void onPause() {
            super.onPause();
            traverseTo(State.PAUSED);
        }

        @Override
        public void onStop() {
            super.onStop();
            traverseTo(State.READY);
        }

        @Override
        public void onMetadataChanged() {
            super.onMetadataChanged();
            traverseTo(State.READY);
        }

        @Override
        public void onSongFinished() {
            super.onSongFinished();
            doNext();
        }

        @Override
        public void seekTo(int ms) {
            super.seekTo(ms);
            doSeekTo(ms);
        }

        @Override
        public void onActiveTrackIndexChanged() {
            super.onActiveTrackIndexChanged();
            isStartPlayingIntended = true;
            traverseTo(State.READY);
        }
    }

    class StatePaused extends StateBase {

        @Override
        public void onEnter() {
            super.onEnter();
            mediaPlayer.pause();
            showNotification(false);
        }

        @Override
        public void onLeave(@ID int newState) {
            super.onLeave(newState);
            if(imageLoadingTask != null) {
                imageLoadingTask.cancel(true);
            }
        }

        @Override
        public int getId() {
            return State.PAUSED;
        }

        @Override
        public void onPlay() {
            super.onPlay();
            traverseTo(State.PLAYING);
        }

        @Override
        public void onStop() {
            super.onStop();
            traverseTo(State.READY);
        }

        @Override
        public void onMetadataChanged() {
            super.onMetadataChanged();
            traverseTo(State.READY);
        }

        @Override
        public void seekTo(int ms) {
            super.seekTo(ms);
            doSeekTo(ms);
        }

        @Override
        public void onActiveTrackIndexChanged() {
            super.onActiveTrackIndexChanged();
            isStartPlayingIntended = false;
            traverseTo(State.READY);
        }
    }

    class StateFinished extends StateBase {

        @Override
        public void onEnter() {
            super.onEnter();
        }

        @Override
        public int getId() {
            return State.FINISHED;
        }

        @Override
        public void onPlay() {
            super.onPlay();
            traverseTo(State.PLAYING);
        }

        @Override
        public void onStop() {
            super.onStop();
            traverseTo(State.READY);
        }

        @Override
        public void onMetadataChanged() {
            super.onMetadataChanged();
            traverseTo(State.READY);
        }

        @Override
        public void onActiveTrackIndexChanged() {
            super.onActiveTrackIndexChanged();
            isStartPlayingIntended = false;
            traverseTo(State.READY);
        }
    }
}
