package de.devmil.nanodegree_spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    public static Intent createLaunchIntent(Context context)
    {
        Intent result = new Intent(context, PlayerActivity.class);
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
    }
}
