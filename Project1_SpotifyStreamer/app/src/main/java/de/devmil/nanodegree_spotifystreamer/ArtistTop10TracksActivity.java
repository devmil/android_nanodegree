package de.devmil.nanodegree_spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Locale;

import de.devmil.nanodegree_spotifystreamer.data.PlayerData;
import de.devmil.nanodegree_spotifystreamer.data.TracksSearchResult;
import de.devmil.nanodegree_spotifystreamer.data.Track;
import de.devmil.nanodegree_spotifystreamer.fragments.ArtistTop10TracksFragment;
import de.devmil.nanodegree_spotifystreamer.fragments.ArtistTop10TracksFragment.ArtistTop10FragmentListener;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyTopTracksSearch;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyTopTracksSearchListener;
import de.devmil.nanodegree_spotifystreamer.service.MediaPlayService;
import de.devmil.nanodegree_spotifystreamer.utils.ViewUtils;
import kaaes.spotify.webapi.android.SpotifyApi;

public class ArtistTop10TracksActivity extends AppCompatActivity implements ArtistTop10FragmentListener {

    private static final String PARAM_ARTIST_ID = "PARAM_ARTIST_ID";
    private static final String PARAM_ARTIST_NAME = "PARAM_ARTIST_NAME";

    public static Intent createLaunchIntent(Context context, String artistId, String artistName) {
        Intent result = new Intent(context, ArtistTop10TracksActivity.class);
        result.putExtra(PARAM_ARTIST_ID, artistId);
        result.putExtra(PARAM_ARTIST_NAME, artistName);

        return result;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_top10_tracks);

        final String artistId = getIntent().getStringExtra(PARAM_ARTIST_ID);
        final String artistName = getIntent().getStringExtra(PARAM_ARTIST_NAME);

        getSupportActionBar().setSubtitle(artistName);

        ArtistTop10TracksFragment fragment = (ArtistTop10TracksFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_artist_top10_tracks);
        fragment.initData(artistId, artistName);
    }


    @Override
    public void onLaunchPlayer() {
        startActivity(PlayerActivity.createLaunchIntent(this));
    }
}
