package de.devmil.nanodegree_spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.devmil.nanodegree_spotifystreamer.data.Artist;
import de.devmil.nanodegree_spotifystreamer.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity implements SearchFragment.SearchFragmentListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchFragment searchFragment = (SearchFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_search);
        searchFragment.setHighlightSelection(false);
    }

    @Override
    public void onArtistSelected(Artist artist) {
        if(artist != null) {
            Intent launchIntent = ArtistTop10TracksActivity
                    .createLaunchIntent(
                            this,
                            artist.getId(),
                            artist.getName());
            startActivity(launchIntent);
        }

    }
}
