package de.devmil.nanodegree_spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.devmil.nanodegree_spotifystreamer.fragments.PlayerFragment;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG_PLAYER_FRAGMENT = "PFTAG";

    public static Intent createLaunchIntent(Context context)
    {
        Intent result = new Intent(context, PlayerActivity.class);
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if(savedInstanceState == null) {
            PlayerFragment fragment = new PlayerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_player_fragment_placeholder, fragment, TAG_PLAYER_FRAGMENT)
                    .commit();
        }
    }
}
