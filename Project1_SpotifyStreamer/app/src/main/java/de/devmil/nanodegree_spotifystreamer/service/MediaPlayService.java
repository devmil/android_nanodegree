package de.devmil.nanodegree_spotifystreamer.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.devmil.nanodegree_spotifystreamer.PlayerActivity;
import de.devmil.nanodegree_spotifystreamer.data.PlayerData;
import de.devmil.nanodegree_spotifystreamer.data.Track;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackDataChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackNavigationOptionsChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackPlayingStateChanged;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackTrackChangedEvent;
import de.devmil.nanodegree_spotifystreamer.media.State;
import de.devmil.nanodegree_spotifystreamer.media.TracksPlayer;
import de.devmil.nanodegree_spotifystreamer.media.TracksPlayerListener;
import de.devmil.nanodegree_spotifystreamer.utils.GlideConfig;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * the media player service controls the playback of the track preview streams.
 * It also is the owner of the currently active playlist so the activity binds to it and
 * listens for events to get the latest and greatest data and status updates
 */
public class MediaPlayService extends Service implements TracksPlayerListener {

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
     * @param context Context
     * @param command Command to be executed
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
        return new Intent(context, MediaPlayService.class);
    }

    private static final int NOTIFICATION_IMAGE_DOWNLOAD_TIMEOUT_SECONDS = 2;
    private static final long SERVICE_TIMEOUT_MS = 5l /* min */ * 60l /* sec */ * 1000l /* msec */;

    private boolean isServiceBound = false;
    private boolean isStopped = false;
    private TracksPlayer tracksPlayer;

    private Timer serviceTimeout;


    /**
     * The binder for the activity <-> service communication
     */
    public class MediaPlayBinder extends Binder
    {
        public void setPlayerData(PlayerData data)  {
            if(tracksPlayer != null) {
                tracksPlayer.updatePlayerData(data);
            }
        }

        public String getArtistName() {
            return tracksPlayer == null ? null : tracksPlayer.getArtistName();
        }

        public Track getActiveTrack() {
            return tracksPlayer == null ? null : tracksPlayer.getActiveTrack();
        }

        public boolean seekTo(int ms) {
            if(tracksPlayer != null) {
                tracksPlayer.doSeekTo(ms);
                return true;
            }
            return false;
        }

        public void onBound() {
            PlayerNotification.cancel(MediaPlayService.this);
            hideNotification();
            isServiceBound = true;
            isStopped = false;
            updateTimeout();
        }

        public void fireInitialEvents() {
            if(tracksPlayer != null) {
                fireCurrentMediaPlayerPositions();
                fireCurrentNavigationOptionsChanged();
                fireTrackChangedEvent(tracksPlayer.getActiveTrackIndex(), tracksPlayer.getActiveTrackIndex());
                firePlayingStateChanged();
            }
        }

        public void beforeUnbind() {
            isServiceBound = false;
            isStopped = false;
            if(tracksPlayer != null) {
                updateNotification();
            }
            updateTimeout();
        }
    }

    @Override
    public void onCurrentTrackChanged(int oldIndex, int newIndex) {
        fireTrackChangedEvent(oldIndex, newIndex);
        updateNotification();
    }

    @Override
    public void onNavigationOptionsChanged() {
        if(tracksPlayer != null) {
            EventBus.getDefault().post(new PlaybackNavigationOptionsChangedEvent(tracksPlayer.getActiveTrackIndex(), tracksPlayer.canNavigatePrev(), tracksPlayer.canNavigateNext()));
        }
    }

    @Override
    public void onPositionChanged() {
        fireCurrentMediaPlayerPositions();
    }

    @Override
    public void onPrepared() {
        fireCurrentMediaPlayerPositions();
    }

    @Override
    public void onStateChanged(@State.ID int oldState, @State.ID int newState) {
        if(oldState == State.PLAYING
                || newState == State.PLAYING) {
            firePlayingStateChanged();
        }
        updateNotification();
        updateTimeout();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent != null
                && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_NEXT.equals(action)) {
                tracksPlayer.next();
            } else if (ACTION_PREV.equals(action)) {
                tracksPlayer.prev();
            } else if (ACTION_PAUSE.equals(action)) {
                tracksPlayer.pause();
            } else if (ACTION_PLAY.equals(action)) {
                tracksPlayer.play();
            } else if (ACTION_STOP.equals(action)) {
                isStopped = true;
                tracksPlayer.stop();
                hideNotification();
            } else if (ACTION_TOGGLE_PLAY_PAUSE.equals(action)) {
                tracksPlayer.togglePlayPause();
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
        tracksPlayer = new TracksPlayer();
        tracksPlayer.setListener(this);
        restartTimeout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tracksPlayer.release();
        tracksPlayer.setListener(null);
        PlayerNotification.cancel(MediaPlayService.this);
        stopForeground(true);
        stopTimeout();
    }

    private void updateTimeout() {
        //the timeout gets started when there is no music playing and when there is no UI bound to this service
        if(isServiceBound
                || (tracksPlayer != null && tracksPlayer.isPlaying())) {
            stopTimeout();
        } else {
            restartTimeout();
        }
    }

    private void restartTimeout() {
        stopTimeout();
        serviceTimeout = new Timer("MusicService timeout");
        serviceTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                stopSelf();
            }
        }, SERVICE_TIMEOUT_MS);
    }

    private void stopTimeout() {
        if(serviceTimeout != null) {
            serviceTimeout.cancel();
            serviceTimeout = null;
        }
    }

    private void fireCurrentNavigationOptionsChanged() {
        if(tracksPlayer == null) {
            return;
        }
        EventBus.getDefault().post(new PlaybackNavigationOptionsChangedEvent(tracksPlayer.getActiveTrackIndex(), tracksPlayer.canNavigatePrev(), tracksPlayer.canNavigateNext()));
    }

    private void fireTrackChangedEvent(int oldIndex, int newIndex) {
        if(tracksPlayer != null) {
            EventBus.getDefault().post(new PlaybackTrackChangedEvent(oldIndex, newIndex, tracksPlayer.getActiveTrack()));
        }
    }

    private void fireCurrentMediaPlayerPositions() {
        if(tracksPlayer == null) {
            return;
        }
        EventBus.getDefault().post(
                new PlaybackDataChangedEvent(
                        tracksPlayer.getCurrentPosition(),
                        tracksPlayer.getDuration(),
                        tracksPlayer.isPrepared(),
                        tracksPlayer.hasError()));
    }

    private void firePlayingStateChanged() {
        if(tracksPlayer == null) {
            return;
        }
        EventBus.getDefault().post(new PlaybackPlayingStateChanged(tracksPlayer.isPlaying()));
    }

    private void updateNotification() {
        if(tracksPlayer == null)
            return;
        if(shouldShowNotification()) {
            showNotification(tracksPlayer.isPlaying());
        } else {
            hideNotification();
        }
    }

    private boolean shouldShowNotification() {
        if(tracksPlayer == null)
            return false;
        if(isServiceBound)
            return false;
        if(isStopped)
            return false;
        return true;
    }

    private Subscription imageLoadingTask;

    private Observable<Bitmap> downloadImage(final String url) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                subscriber.onStart();
                GlideConfig.configure(
                    Glide
                        .with(MediaPlayService.this)
                        .load(url)
                        .asBitmap()
                        .dontAnimate()
                )
                .into(
                    new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            subscriber.onNext(resource);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        }
                    }
                );
            }
        });
    }

    private Observable<Bitmap> getErrorImage() {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });
    }

    private void showNotification(final boolean currentlyPlaying) {

        if(isServiceBound)
            return;

        if(tracksPlayer == null || tracksPlayer.getActiveTrack() == null)
            return;
        final String albumImageUri = tracksPlayer.getActiveTrack().getAlbumThumbnailSmallUrl();

        if(imageLoadingTask != null) {
            imageLoadingTask.unsubscribe();
        }

        imageLoadingTask = getErrorImage()
                .delay(NOTIFICATION_IMAGE_DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .mergeWith(downloadImage(albumImageUri))
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Bitmap bitmap) {
                        if (!shouldShowNotification())
                            return;
                        showNotification(bitmap, currentlyPlaying, false);
                        if(bitmap != null) {
                            this.unsubscribe();
                        }
                    }
                });
    }

    private void hideNotification() {
        if(imageLoadingTask != null) {
            imageLoadingTask.unsubscribe();
            imageLoadingTask = null;
        }
        PlayerNotification.cancel(this);
    }

    private void showNotification(Bitmap albumArtBitmap, boolean currentlyPlaying, boolean bindToNotification) {
        if(tracksPlayer == null
                || tracksPlayer.getActiveTrack() == null) {
            return;
        }

        //boolean differentiate between smartphone and tablet mode
        Intent launchPlayerIntent = PlayerActivity.createLaunchIntent(this);

        String artistName = tracksPlayer.getArtistName();
        String trackName = tracksPlayer.getActiveTrack().getTrackName();

        Notification notification = PlayerNotification.notify(this, artistName, trackName, albumArtBitmap, currentlyPlaying, launchPlayerIntent);

        if(bindToNotification) {
            startForeground(PlayerNotification.NOTIFICATION_ID_STICKY, notification);
        }
    }
}
