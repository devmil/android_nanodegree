package de.devmil.nanodegree_spotifystreamer.service;

import android.media.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

import de.devmil.nanodegree_spotifystreamer.data.PlayerData;
import de.devmil.nanodegree_spotifystreamer.data.Track;

public class TracksPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private static final long DURATION_UPDATE_DELAY_MS = 300;

    private boolean isStartPlayingIntended = false;
    private State currentState = null;
    private MediaPlayerWrapper mediaPlayer;
    private PlayerData playerData;

    private TracksPlayerListener listener;

    public TracksPlayer() {
        traverseTo(State.INIT);
    }

    public void setListener(TracksPlayerListener listener) {
        this.listener = listener;
    }

    /**
     * updates the PlayerData with the given new PlayerData.
     * This method also fires the appropriate events
     * @param playerData new player data
     */
    public void updatePlayerData(PlayerData playerData) {
        int oldIndex = -1;
        int activeTrackIndex = -1;
        if(this.playerData != null) {
            oldIndex = this.playerData.getActiveTrackIndex();
        }
        if(playerData != null) {
            activeTrackIndex = playerData.getActiveTrackIndex();
        }
        this.playerData = playerData;
        if(currentState != null) {
            currentState.onMetadataChanged();
        }
        fireNavigationOptionsChanged();
        fireTrackChanged(oldIndex, activeTrackIndex);
        firePositionChanged();
    }

    public void next() {
        doNext();
    }

    public void prev() {
        doPrev();
    }

    public void pause() {
        if (currentState != null) {
            currentState.onPause();
        }
    }

    public void play() {
        if (currentState != null) {
            currentState.onPlay();
        }
    }

    public void stop() {
        if (currentState != null) {
            currentState.onStop();
        }
    }

    public void togglePlayPause() {
        if (currentState != null) {
            if (currentState.getId() == State.PLAYING) {
                pause();
            } else {
                play();
            }
        }
    }

    public void release() {
        removeMediaPlayer();
        if(currentState != null) {
            currentState.onLeave(State.INIT);
            currentState = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(currentState != null) {
            currentState.onSongFinished();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        fireOnPrepared();
    }

    public void doSeekTo(int ms) {
        mediaPlayer.seekTo(ms);
        firePositionChanged();
    }

    public int getActiveTrackIndex() {
        if(playerData != null) {
            return playerData.getActiveTrackIndex();
        }
        return 0;
    }

    public boolean canNavigatePrev() {
        return playerData != null && playerData.canNavigatePrev();
    }

    public boolean canNavigateNext() {
        return playerData != null && playerData.canNavigateNext();
    }

    public Track getActiveTrack() {
        if(playerData != null) {
            return playerData.getActiveTrack();
        }
        return null;
    }

    public int getCurrentPosition() {
        if(mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if(mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }


    public boolean isPrepared() {
        return mediaPlayer != null && mediaPlayer.isPrepared();
    }

    public boolean hasError() {
        return mediaPlayer != null && mediaPlayer.hasError();
    }

    public String getArtistName() {
        if(playerData != null) {
            return playerData.getArtistName();
        }
        return null;
    }

    public boolean isPlaying() {
        return currentState != null && currentState.getId() == State.PLAYING;
    }

    public boolean isPaused() {
        return currentState != null && currentState.getId() == State.PAUSED;
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

                fireTrackChanged(oldIndex, newIndex);
                fireNavigationOptionsChanged();
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

                fireTrackChanged(oldIndex, newIndex);
                fireNavigationOptionsChanged();
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

    private void fireOnPrepared() {
        if(listener != null) {
            listener.onPrepared();
        }
    }

    private void firePositionChanged() {
        if(listener != null) {
            listener.onPositionChanged();
        }
    }

    private void fireTrackChanged(int oldIndex, int newIndex) {
        if(listener != null) {
            listener.onCurrentTrackChanged(oldIndex, newIndex);
        }
    }

    private void fireNavigationOptionsChanged() {
        if(listener != null) {
            listener.onNavigationOptionsChanged();
        }
    }

    private void fireStateChanged(@State.ID int oldState, @State.ID int newState) {
        if(listener != null) {
            listener.onStateChanged(oldState, newState);
        }
    }

    private void traverseTo(@State.ID int stateId) {
        if(currentState != null
                && currentState.getId() == stateId)
            return;
        @State.ID
        int oldStateId = State.INIT;
        State state = createState(stateId);
        if(currentState != null) {
            currentState.onLeave(stateId);
            oldStateId = currentState.getId();
        }
        currentState = state;
        if(currentState != null) {
            currentState.onEnter();
        }
        fireStateChanged(oldStateId, stateId);
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
        }
    }

    class StatePlaying extends StateBase {

        private Timer updateDurationTimer;

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
                        firePositionChanged();
                    }
                }
            }, 0, DURATION_UPDATE_DELAY_MS);
        }

        @Override
        public void onLeave(@ID int newState) {
            super.onLeave(newState);
            updateDurationTimer.cancel();
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
        }

        @Override
        public void onLeave(@ID int newState) {
            super.onLeave(newState);
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
