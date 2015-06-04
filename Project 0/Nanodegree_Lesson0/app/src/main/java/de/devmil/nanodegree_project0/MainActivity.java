package de.devmil.nanodegree_project0;

import android.app.Activity;
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


        configureButton(btnStreamer, R.string.toast_launch_spotify_streamer);
        configureButton(btnScores, R.string.toast_launch_scores);
        configureButton(btnLibrary, R.string.toast_launch_library);
        configureButton(btnBuildItBigger, R.string.toast_launch_build_it_bigger);
        configureButton(btnXYZReader, R.string.toast_launch_xyz_reader);
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
}
