package de.devmil.nanodegree_spotifystreamer.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.devmil.nanodegree_spotifystreamer.R;
import de.devmil.nanodegree_spotifystreamer.data.Track;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackDataChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackNavigationOptionsChangedEvent;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackPlayingStateChanged;
import de.devmil.nanodegree_spotifystreamer.event.PlaybackTrackChangedEvent;
import de.devmil.nanodegree_spotifystreamer.service.MediaPlayService;
import de.devmil.nanodegree_spotifystreamer.utils.GlideConfig;
import de.greenrobot.event.EventBus;

public class PlayerFragment extends DialogFragment {

    private static final String KEY_DIALOG_MODE = "DIALOG_MODE";

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

    public static PlayerFragment create(boolean isDialogMode) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(KEY_DIALOG_MODE, isDialogMode);

        PlayerFragment result = new PlayerFragment();
        result.setArguments(arguments);
        return result;
    }

    public PlayerFragment() {
        // Required empty public constructor
    }

    private Context getContext() {
        Activity activity = getActivity();
        if(activity != null) {
            return activity;
        }
        Dialog dialog = getDialog();
        if(dialog != null) {
            return dialog.getContext();
        }
        return null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        Context context = inflater.getContext();

        if(getArguments() != null && getArguments().getBoolean(KEY_DIALOG_MODE)) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        labelArtist = (TextView)view.findViewById(R.id.fragment_player_label_artist);
        labelTitle = (TextView)view.findViewById(R.id.fragment_player_label_title);
        imageAlbum =(ImageView)view.findViewById(R.id.fragment_player_img_album);
        labelAlbum = (TextView)view.findViewById(R.id.fragment_player_label_album);
        sliderPosition = (SeekBar)view.findViewById(R.id.fragment_player_slider_progress);
        labelPlayed = (TextView)view.findViewById(R.id.fragment_player_label_played);
        labelDuration = (TextView)view.findViewById(R.id.fragment_player_label_duration);
        buttonPrev = (ImageButton)view.findViewById(R.id.fragment_player_btn_prev);
        buttonPlayPause = (ImageButton)view.findViewById(R.id.fragment_player_btn_play_pause);
        buttonNext = (ImageButton)view.findViewById(R.id.fragment_player_btn_next);

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

        if(savedInstanceState == null) {
            //TODO: do some fancy input animation?
        }

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        context.startService(MediaPlayService.createStartIntent(context));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void navigateNext() {
        Context context = getContext();
        if(context == null) {
            return;
        }
        MediaPlayService.executeCommand(context, MediaPlayService.COMMAND_NEXT);
    }

    private void navigatePrev() {
        Context context = getContext();
        if(context == null) {
            return;
        }
        MediaPlayService.executeCommand(context, MediaPlayService.COMMAND_PREV);
    }

    private void playPause()
    {
        Context context = getContext();
        if(context == null) {
            return;
        }
        MediaPlayService.executeCommand(context, MediaPlayService.COMMAND_TOGGLE_PLAY_PAUSE);
    }

    private void updateTrackData() {
        if(serviceBinder == null) {
            return;
        }
        Track selectedTrack = serviceBinder.getActiveTrack();
        if(selectedTrack == null) {
            return;
        }

        String artistName = serviceBinder.getArtistName();

        labelArtist.setText(artistName);
        labelTitle.setText(selectedTrack.getTrackName());

        if(selectedTrack.getAlbumThumbnailLargeUrl() != null) {
            try {
                GlideConfig.configure(Glide.with(this).load(selectedTrack.getAlbumThumbnailLargeUrl())).into(imageAlbum);
            } catch(Exception e) {
                e.printStackTrace();
                imageAlbum.setImageDrawable(null);
            }
        } else {
            imageAlbum.setImageDrawable(null);
        }

        labelAlbum.setText(selectedTrack.getAlbumName());
    }

    private void setImageButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setClickable(enabled);
        button.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void updatePlayPauseButton(boolean currentlyPlaying) {
        buttonPlayPause.setImageResource(currentlyPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }

    private String formatDurationString(int durationMS) {
        int remaining = durationMS;
        int minutes = remaining / 60000;
        remaining = remaining % 60000;
        int seconds = remaining / 1000;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void bindToMusicPlayService() {
        Context context = getContext();
        if(context == null) {
            return;
        }
        if(serviceConnection != null) {
            return;
        }
        Intent serviceStartIntent = MediaPlayService.createStartIntent(context);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceBinder = (MediaPlayService.MediaPlayBinder)service;
                serviceBinder.onBound();
                serviceBinder.fireInitialEvents();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceBinder = null;
            }
        };
        context.bindService(serviceStartIntent, serviceConnection, 0);
    }

    private void unbindFromMusicPlayService() {
        Context context = getContext();
        if(context == null) {
            return;
        }
        if(serviceConnection == null) {
            return;
        }
        serviceBinder.beforeUnbind();
        context.unbindService(serviceConnection);
        serviceBinder = null;
        serviceConnection = null;
    }

    @Override
    public void onPause() {
        unbindFromMusicPlayService();
        super.onPause();
    }

    @Override
    public void onResume() {
        bindToMusicPlayService();
        super.onResume();
    }

    public void onEventMainThread(PlaybackNavigationOptionsChangedEvent e) {
        setImageButtonEnabled(buttonNext, e.isCanNavigateNext());
        setImageButtonEnabled(buttonPrev, e.isCanNavigatePrev());
    }

    public void onEventMainThread(PlaybackDataChangedEvent e) {
        if(!e.isReady()) {
            labelPlayed.setText("");
            labelDuration.setText("");

        } else {
            labelDuration.setText(formatDurationString(e.getDurationMS()));
            sliderPosition.setMax(e.getDurationMS());
            if(!userSlideMode) {
                labelPlayed.setText(formatDurationString(e.getPositionMS()));
                sliderPosition.setProgress(e.getPositionMS());
            }
        }
        sliderPosition.setEnabled(e.isReady());
        setImageButtonEnabled(buttonPlayPause, e.isReady());
    }

    public void onEventMainThread(PlaybackTrackChangedEvent e) {
        updateTrackData();
    }

    public void onEventMainThread(PlaybackPlayingStateChanged e) {
        updatePlayPauseButton(e.isPlaying());
    }
}
