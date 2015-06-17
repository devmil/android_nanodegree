package de.devmil.nanodegree_spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import de.devmil.nanodegree_spotifystreamer.data.Artist;
import de.devmil.nanodegree_spotifystreamer.fragments.ArtistTop10TracksFragment;
import de.devmil.nanodegree_spotifystreamer.fragments.PlayerFragment;
import de.devmil.nanodegree_spotifystreamer.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity implements SearchFragment.SearchFragmentListener, ArtistTop10TracksFragment.ArtistTop10FragmentListener {

    private boolean isTwoPaneLayout = false;
    private FrameLayout fragmentPlaceholder;

    private static final String TOP10TRACKS_FRAGMENT_TAG = "T10TFTAG";
    private static final String PLAYER_FRAGMENT_TAG = "PLAYERTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchFragment searchFragment = (SearchFragment)getSupportFragmentManager().findFragmentById(R.id.activity_main_fragment_search);
        fragmentPlaceholder = (FrameLayout)findViewById(R.id.activity_main_fragment_artist_top10_tracks_placeholder);
        if(fragmentPlaceholder != null) {
            isTwoPaneLayout = true;
        }
        searchFragment.setHighlightSelection(isTwoPaneLayout);
    }

    @Override
    public void onArtistSelected(Artist artist) {
        if(artist != null) {
            if(!isTwoPaneLayout) {
                Intent launchIntent = ArtistTop10TracksActivity
                        .createLaunchIntent(
                                this,
                                artist.getId(),
                                artist.getName());
                startActivity(launchIntent);
            } else {
                ArtistTop10TracksFragment fragment = ArtistTop10TracksFragment.create(artist.getId(), artist.getName());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_artist_top10_tracks_placeholder, fragment, TOP10TRACKS_FRAGMENT_TAG)
                        .commit();
                getSupportActionBar().setSubtitle(artist.getName());
            }
        }

    }

    @Override
    public void onLaunchPlayer() {
        if(isTwoPaneLayout) {
            PlayerFragment playerFragment = PlayerFragment.create(true);
            playerFragment.show(getSupportFragmentManager(), PLAYER_FRAGMENT_TAG);
        }
    }
}
