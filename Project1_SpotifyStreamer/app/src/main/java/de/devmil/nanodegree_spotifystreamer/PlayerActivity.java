package de.devmil.nanodegree_spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.devmil.nanodegree_spotifystreamer.data.SpotifyTopTracksSearchResult;

public class PlayerActivity extends AppCompatActivity {

    private static final String PARAM_SEARCH_RESULT = "de.devmil.nanodegree_spotifystreamer.PlayerActivity.PARAM_SEARCH_RESULT";
    private static final String PARAM_SELECTED_TRACK_INDEX = "de.devmil.nanodegree_spotifystreamer.PlayerActivity.PARAM_SELECTED_TRACK_INDEX";
    private static final String PARAM_ARTIST_NAME = "de.devmil.nanodegree_spotifystreamer.PlayerActivity.PARAM_ARTIST_NAME";

    public static Intent createLaunchIntent(Context context, SpotifyTopTracksSearchResult searchResult, int selectedTrackIndex, String artistName)
    {
        Intent result = new Intent(context, PlayerActivity.class);
        result.putExtra(PARAM_SEARCH_RESULT, searchResult);
        result.putExtra(PARAM_SELECTED_TRACK_INDEX, selectedTrackIndex);
        result.putExtra(PARAM_ARTIST_NAME, artistName);
        return result;
    }

    private SpotifyTopTracksSearchResult searchResult;
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

    private MediaPlayer mediaPlayer;
    private boolean userSlideMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();

        int initialSelectedTrackIndex = intent.getIntExtra(PARAM_SELECTED_TRACK_INDEX, 0);

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
                mediaPlayer.seekTo(seekBar.getProgress());
                updateMediaControls();
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

        setTrackData(initialSelectedTrackIndex);

        if(savedInstanceState == null) {
            //TODO: do some fancy input animation?
        }
    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
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
        int newIndex = selectedTrackIndex+1;
        if(newIndex < searchResult.getTracks().size()) {
            setTrackData(newIndex);
        }
    }

    private void navigatePrev() {
        int newIndex = selectedTrackIndex - 1;
        if(newIndex >= 0) {
            setTrackData(newIndex);
        }
    }

    private void playPause()
    {
        //TODO control play / pause state and toggle between them
    }

    private void setTrackData(int selectedTrackIndex) {
        if(this.selectedTrackIndex == selectedTrackIndex) {
            return;
        }
        if(searchResult.getTracks().size() <= selectedTrackIndex) {
            return;
        }
        SpotifyTopTracksSearchResult.Track track = searchResult.getTracks().get(selectedTrackIndex);

        labelTitle.setText(track.getTrackName());

        if(track.getAlbumThumbnailLargeUrl() != null) {
            Picasso.with(this).load(track.getAlbumThumbnailLargeUrl()).into(imageAlbum);
        } else {
            imageAlbum.setImageDrawable(null);
        }

        labelAlbum.setText(track.getAlbumName());

        this.selectedTrackIndex = selectedTrackIndex;

        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            setImageButtonEnabled(buttonPlayPause, false);
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                initMediaControls();
                updateMediaControls();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //TODO: handle errors, cleanup MediaPlayer? try again? ...
                return false;
            }
        });
        try {
            mediaPlayer.setDataSource(track.getPreviewUrl());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        initNavigationControls();
    }

    private void initNavigationControls()
    {
        setImageButtonEnabled(buttonNext, selectedTrackIndex < searchResult.getTracks().size() - 1);
        setImageButtonEnabled(buttonPrev, selectedTrackIndex > 0);
    }

    private void initMediaControls()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null) {
                    MediaPlayer localMediaPlayerCopy = mediaPlayer;
                    int duration = localMediaPlayerCopy.getDuration();

                    sliderPosition.setMax(duration);

                    labelDuration.setText(formatDurationString(duration));
                    labelPlayed.setText(formatDurationString(0));

                    setImageButtonEnabled(buttonPlayPause, true);
                } else {
                    setImageButtonEnabled(buttonPlayPause, false);
                }
            }
        });
    }

    private void setImageButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setClickable(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void updateMediaControls()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null) {
                    MediaPlayer localMediaPlayerCopy = mediaPlayer;
                    int position = localMediaPlayerCopy.getCurrentPosition();

                    labelPlayed.setText(formatDurationString(position));
                }
            }
        });
    }

    private String formatDurationString(int durationMS) {
        int remaining = durationMS;
        int minutes = remaining / 60000;
        remaining = remaining % 60000;
        int seconds = remaining / 1000;

        return String.format("%02d:%02d", minutes, seconds);
    }
}
