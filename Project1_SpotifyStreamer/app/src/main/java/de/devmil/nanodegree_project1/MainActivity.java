package de.devmil.nanodegree_project1;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.devmil.nanodegree_project1.model.SpotifyArtistResult;
import de.devmil.nanodegree_project1.model.SpotifyArtistSearchResult;
import de.devmil.nanodegree_project1.processing.SpotifyArtistSearch;
import de.devmil.nanodegree_project1.processing.SpotifyArtistSearchListener;
import de.devmil.nanodegree_project1.utils.ViewUtils;
import kaaes.spotify.webapi.android.SpotifyApi;

public class MainActivity extends AppCompatActivity {

    private ListView lvResult;
    private LinearLayout llNoResult;
    private LinearLayout llIndicator;
    @SuppressWarnings("FieldCanBeLocal")
    private EditText editSearch;
    private SearchResultAdapter resultAdapter;
    private SpotifyArtistSearch artistSearch;

    private boolean isProgressIndicatorActive = false;
    private boolean isResultAvailable = false;

    private static final int SEARCH_DELAY_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvResult = (ListView)findViewById(R.id.activity_main_lv_search_result);
        llNoResult = (LinearLayout)findViewById(R.id.activity_main_ll_no_result);
        llIndicator = (LinearLayout)findViewById(R.id.activity_main_ll_indicator);
        editSearch = (EditText)findViewById(R.id.activity_main_edit_search);

        resultAdapter = new SearchResultAdapter(this);

        int imageSizePxInView = (int)ViewUtils.getPxFromDip(this, getResources().getDimension(R.dimen.activity_main_result_image_size));
        artistSearch = new SpotifyArtistSearch(new SpotifyApi(), imageSizePxInView);
        artistSearch.addListener(new SpotifyArtistSearchListener() {
            @Override
            public void onSearchRunningUpdated(boolean isRunning) {
                setProgressIndicatorShown(isRunning);
            }

            @Override
            public void onNewResult(SpotifyArtistSearchResult result) {
                resultAdapter.setCurrentResult(result);
                setResultAvailable(result.getArtists().size() > 0);
            }
        });

        lvResult.setAdapter(resultAdapter);

        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Hitting the Enter key triggers an immediate search
                if(actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    artistSearch.queryForNameAsync(v.getText().toString(), 0);
                    return true;
                }
                return false;
            }
        });
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Text change triggers a delayed search
                artistSearch.queryForNameAsync(s.toString(), SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setResultAvailable(boolean available)
    {
        isResultAvailable = available;
        updateAlphaValues();
    }

    private void setProgressIndicatorShown(boolean isShown)
    {
        isProgressIndicatorActive = isShown;
        updateAlphaValues();
    }

    private void updateAlphaValues()
    {
        //just to be on the safe side.
        //the current implementation of "SpotifyArtistSearch" fires its event in the UI thread but
        //this isn't guaranteed for the future
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float inactiveAlpha = getResources().getFraction(R.fraction.activity_main_inactive_alpha, 1, 1);
                int animationDurationMS = getResources().getInteger(R.integer.activity_main_alpha_change_durations_ms);

                float lvResultAlpha;
                float llNoResultAlpha;
                float llIndicatorAlpha;

                if(isResultAvailable) {
                    lvResultAlpha = 1;
                    llNoResultAlpha = 0;
                }
                else {
                    lvResultAlpha = 0;
                    llNoResultAlpha = 1;
                }

                if(isProgressIndicatorActive) {
                    //only adapt the already visible view and leave the other one untouched
                    if(lvResultAlpha == 1)
                        lvResultAlpha = inactiveAlpha;
                    if(llNoResultAlpha == 1)
                        llNoResultAlpha = inactiveAlpha;

                    llIndicatorAlpha = 1;
                }
                else {
                    llIndicatorAlpha = 0;
                }

                lvResult.animate().alpha(lvResultAlpha).setDuration(animationDurationMS).start();
                llNoResult.animate().alpha(llNoResultAlpha).setDuration(animationDurationMS).start();
                llIndicator.animate().alpha(llIndicatorAlpha).setDuration(animationDurationMS).start();
            }
        });
    }

    class SearchResultAdapter extends BaseAdapter {
        class ViewHolder
        {
            String artistId;
            ImageView ivArtist;
            TextView txtName;
        }

        private SpotifyArtistSearchResult currentResult;
        private Context context;
        private LayoutInflater inflater;

        SearchResultAdapter(Context context)
        {
            this.context = context;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return (currentResult == null || currentResult.getArtists() == null) ? 0 : currentResult.getArtists().size();
        }

        @Override
        public Object getItem(int position) {
            if(currentResult == null)
                return null;
            if(currentResult.getArtists() == null)
                return null;
            if(currentResult.getArtists().size() <= position)
                return null;
            return currentResult.getArtists().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
            {
                convertView = inflater.inflate(R.layout.activity_main_result_entry, parent, false);
                ViewHolder vh = new ViewHolder();
                vh.ivArtist = (ImageView)convertView.findViewById(R.id.activity_main_result_entry_image);
                vh.txtName = (TextView)convertView.findViewById(R.id.activity_main_result_entry_label);
                convertView.setTag(vh);
            }
            ViewHolder vh = (ViewHolder)convertView.getTag();

            SpotifyArtistResult entry = (SpotifyArtistResult)getItem(position);

            //nothing to do here, the data in the view is already valid
            if(entry.getId().equals(vh.artistId))
                return convertView;

            setEntryToViews(entry, vh);

            return convertView;
        }

        private void setEntryToViews(SpotifyArtistResult entry, ViewHolder viewHolder) {
            String imageUrlToLoad = null;
            if(entry != null) {
                viewHolder.txtName.setText(entry.getName());
                if(entry.hasImage()) {
                    imageUrlToLoad = entry.getImageUrl();
                }
                viewHolder.artistId = entry.getId();
            }
            else
            {
                viewHolder.txtName.setText("");
                viewHolder.artistId = null;
            }

            if(imageUrlToLoad != null)
            {
                Picasso.with(context).load(imageUrlToLoad).into(viewHolder.ivArtist);
            }
            else
            {
                viewHolder.ivArtist.setImageDrawable(null);
            }
        }

        public void setCurrentResult(SpotifyArtistSearchResult currentResult) {
            this.currentResult = currentResult;
            //just to be on the safe side.
            //the current implementation of "SpotifyArtistSearch" fires its event in the UI thread but
            //this isn't guaranteed for the future
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }
}
