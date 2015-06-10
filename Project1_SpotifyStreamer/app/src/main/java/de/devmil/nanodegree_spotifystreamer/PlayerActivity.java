package de.devmil.nanodegree_spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.devmil.nanodegree_spotifystreamer.data.PlayerData;
import de.devmil.nanodegree_spotifystreamer.data.TracksSearchResult;
import de.devmil.nanodegree_spotifystreamer.data.Track;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackNavigationOptionsChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackPositionChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackState;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackStateChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackTrackChangedEvent;
import de.devmil.nanodegree_spotifystreamer.service.MediaPlayService;
import de.greenrobot.event.EventBus;

public class PlayerActivity extends AppCompatActivity {

    private static final String PARAM_SEARCH_RESULT = "PARAM_SEARCH_RESULT";
    private static final String PARAM_SELECTED_TRACK_INDEX = "PARAM_SELECTED_TRACK_INDEX";
    private static final String PARAM_ARTIST_NAME = "PARAM_ARTIST_NAME";

    public static Intent createLaunchIntent(Context context, TracksSearchResult searchResult, int selectedTrackIndex, String artistName)
    {
        Intent result = new Intent(context, PlayerActivity.class);
        result.putExtra(PARAM_SEARCH_RESULT, searchResult);
        result.putExtra(PARAM_SELECTED_TRACK_INDEX, selectedTrackIndex);
        result.putExtra(PARAM_ARTIST_NAME, artistName);
        return result;
    }

    private TracksSearchResult searchResult;
    private int selectedTrackIndex;
    private String artistName;

    private TextView labelArtist;
    private TextView labelTitle;
    private ImageView imageAlbum;
    private TextView labelAlbum;
    private SeekBar sliderPosition;
    private TextView labelPlayed;
    private TextView labelDuration;
    private ImageButton buttonPrev;
    private ImageButton buttonPlayPause;
    private ImageButton buttonNext;

    private boolean userSlideMode = false;

    private MediaPlayService.MediaPlayBinder serviceBinder;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();

        selectedTrackIndex = intent.getIntExtra(PARAM_SELECTED_TRACK_INDEX, 0);
        searchResult = intent.getParcelableExtra(PARAM_SEARCH_RESULT);
        artistName = intent.getStringExtra(PARAM_ARTIST_NAME);

        labelArtist = (TextView)findViewById(R.id.activity_player_label_artist);
        labelTitle = (TextView)findViewById(R.id.activity_player_label_title);
        imageAlbum =(ImageView)findViewById(R.id.activity_player_img_album);
        labelAlbum = (TextView)findViewById(R.id.activity_player_label_album);
        sliderPosition = (SeekBar)findViewById(R.id.activity_player_slider_progress);
        labelPlayed = (TextView)findViewById(R.id.activity_player_label_played);
        labelDuration = (TextView)findViewById(R.id.activity_player_label_duration);
        buttonPrev = (ImageButton)findViewById(R.id.activity_player_btn_prev);
        buttonPlayPause = (ImageButton)findViewById(R.id.activity_player_btn_play_pause);
        buttonNext = (ImageButton)findViewById(R.id.activity_player_btn_next);

        labelArtist.setText(artistName);
        sliderPosition.setMax(0);

        sliderPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    labelPlayed.setText(formatDurationString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userSlideMode = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userSlideMode = false;
                if(serviceBinder != null) {
                    serviceBinder.seekTo(seekBar.getProgress());
                }
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigatePrev();
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateNext();
            }
        });
        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        setTrackData(searchResult.getTracks().get(selectedTrackIndex), selectedTrackIndex);

        if(savedInstanceState == null) {
            //TODO: do some fancy input animation?
        }

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        bindToMusicPlayService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //TODO save instance state
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //TODO restore instance state
    }

    private void navigateNext() {
        MediaPlayService.executeCommand(this, MediaPlayService.COMMAND_NEXT);
    }

    private void navigatePrev() {
        MediaPlayService.executeCommand(this, MediaPlayService.COMMAND_PREV);
    }

    private void playPause()
    {
        MediaPlayService.executeCommand(this, MediaPlayService.COMMAND_TOGGLE_PLAY_PAUSE);
    }

    private void setTrackData(Track track, int selectedTrackIndex) {
        labelTitle.setText(track.getTrackName());

        if(track.getAlbumThumbnailLargeUrl() != null) {
            Picasso.with(this).load(track.getAlbumThumbnailLargeUrl()).into(imageAlbum);
        } else {
            imageAlbum.setImageDrawable(null);
        }

        labelAlbum.setText(track.getAlbumName());

        this.selectedTrackIndex = selectedTrackIndex;
    }

    private void setImageButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setClickable(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private String formatDurationString(int durationMS) {
        int remaining = durationMS;
        int minutes = remaining / 60000;
        remaining = remaining % 60000;
        int seconds = remaining / 1000;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void bindToMusicPlayService() {
        if(serviceConnection != null) {
            return;
        }
        Intent serviceStartIntent = new Intent(this, MediaPlayService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceBinder = (MediaPlayService.MediaPlayBinder)service;
                serviceBinder.setPlayerData(new PlayerData(artistName, searchResult.getTracks(), selectedTrackIndex));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceBinder = null;
            }
        };
        bindService(serviceStartIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromMusicPlayService() {
        if(serviceConnection == null) {
            return;
        }
        unbindService(serviceConnection);
        serviceBinder = null;
        serviceConnection = null;
    }

    @Override
    protected void onPause() {
        unbindFromMusicPlayService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        bindToMusicPlayService();
        super.onResume();
    }

    public void onEventMainThread(PlaybackNavigationOptionsChangedEvent e) {
        setImageButtonEnabled(buttonNext, e.isCanNavigateNext());
        setImageButtonEnabled(buttonPrev, e.isCanNavigatePrev());
    }

    public void onEventMainThread(PlaybackPositionChangedEvent e) {
        labelPlayed.setText(formatDurationString(e.getPositionMS()));
        labelDuration.setText(formatDurationString(e.getDurationMS()));
    }

    public void onEventMainThread(PlaybackStateChangedEvent e) {
        int newState = e.getNewState();
        boolean canPlay = newState > PlaybackState.INIT;
        setImageButtonEnabled(buttonPlayPause, canPlay);
    }

    public void onEventMainThread(PlaybackTrackChangedEvent e) {
        setTrackData(e.getNewTrack(), e.getNewIndex());
    }
}
