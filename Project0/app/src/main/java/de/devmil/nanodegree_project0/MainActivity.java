package de.devmil.nanodegree_project0;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button btnStreamer;
    private Button btnScores;
    private Button btnLibrary;
    private Button btnBuildItBigger;
    private Button btnXYZReader;
    private Button btnCapstone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStreamer = (Button)findViewById(R.id.activity_main_btn_spotify_streamer);
        btnScores = (Button)findViewById(R.id.activity_main_btn_scores_app);
        btnLibrary = (Button)findViewById(R.id.activity_main_btn_library_app);
        btnBuildItBigger = (Button)findViewById(R.id.activity_main_btn_build_it_bigger);
        btnXYZReader = (Button)findViewById(R.id.activity_main_btn_xyz_reader);
        btnCapstone = (Button)findViewById(R.id.activity_main_btn_my_own_app);


        configureButton(btnStreamer, new ComponentName("de.devmil.nanodegree_spotifystreamer", "de.devmil.nanodegree_spotifystreamer.MainActivity"));
        configureButton(btnScores, new ComponentName("barqsoft.footballscores", "barqsoft.footballscores.MainActivity"));
        configureButton(btnLibrary, new ComponentName("it.jaschke.alexandria", "it.jaschke.alexandria.MainActivity"));
        configureButton(btnBuildItBigger, new ComponentName("com.udacity.gradle.builditbigger", "com.udacity.gradle.builditbigger.MainActivity"));
        configureButton(btnXYZReader, new ComponentName("com.example.xyzreader", "com.example.xyzreader.ui.ArticleListActivity"));
        configureButton(btnCapstone, R.string.toast_launch_capstone);
    }

    private void configureButton(Button button, final int toastMessageOnClickId)
    {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, toastMessageOnClickId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureButton(Button button, final ComponentName activityToLaunch)
    {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = new Intent();
                launchIntent.setComponent(activityToLaunch);

                startActivity(launchIntent);
            }
        });
    }
}
