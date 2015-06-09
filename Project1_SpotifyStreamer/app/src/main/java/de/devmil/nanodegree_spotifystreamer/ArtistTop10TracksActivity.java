package de.devmil.nanodegree_spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.devmil.nanodegree_spotifystreamer.data.SpotifyTopTracksSearchResult;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyTopTracksSearch;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyTopTracksSearchListener;
import de.devmil.nanodegree_spotifystreamer.utils.ViewUtils;
import kaaes.spotify.webapi.android.SpotifyApi;

public class ArtistTop10TracksActivity extends AppCompatActivity {

    private static final String PARAM_ARTIST_ID = "de.devmil.nanodegree_spotifystreamer.PARAM_ARTIST_ID";
    private static final String PARAM_ARTIST_NAME = "de.devmil.nanodegree_spotifystreamer.PARAM_ARTIST_NAME";

    public static Intent createLaunchIntent(Context context, String artistId, String artistName) {
        Intent result = new Intent(context, ArtistTop10TracksActivity.class);
        result.putExtra(PARAM_ARTIST_ID, artistId);
        result.putExtra(PARAM_ARTIST_NAME, artistName);

        return result;
    }

    private static final String KEY_TRACKS_RESULT = "de.devmil.nanodegree_spotifystreamer.ArtistTop10TracksActivity.TRACKS_RESULT";

    private ListView trackListView;
    private LinearLayout llNoResult;
    private LinearLayout llIndicator;

    @SuppressWarnings("FieldCanBeLocal")
    private SpotifyTopTracksSearch topTracksSearch;

    private SearchResultAdapter resultAdapter;

    private boolean isResultAvailable;
    private boolean isProgressIndicatorActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_top10_tracks);

        String artistId = getIntent().getStringExtra(PARAM_ARTIST_ID);
        final String artistName = getIntent().getStringExtra(PARAM_ARTIST_NAME);

        //noinspection ConstantConditions
        getSupportActionBar().setSubtitle(artistName);

        trackListView = (ListView)findViewById(R.id.activity_artist_top10_tracks_list);
        llNoResult = (LinearLayout)findViewById(R.id.activity_artist_top10_tracks_ll_no_result);
        llIndicator = (LinearLayout)findViewById(R.id.activity_artist_top10_tracks_ll_indicator);

        resultAdapter = new SearchResultAdapter(this);

        trackListView.setAdapter(resultAdapter);

        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SpotifyTopTracksSearchResult searchResult = resultAdapter.getCurrentResult();
                if(searchResult != null) {
                    startActivity(PlayerActivity.createLaunchIntent(ArtistTop10TracksActivity.this, searchResult, position, artistName));
                }
            }
        });

        int imageSizePxSmallInView = (int) ViewUtils.getPxFromDip(this, getResources().getDimension(R.dimen.activity_artist_top10_tracks_entry_image_size));
        int imageSizePxLargeInView = (int) ViewUtils.getPxFromDip(this, getResources().getDimension(R.dimen.activity_playing_image_size));
        topTracksSearch = new SpotifyTopTracksSearch(new SpotifyApi(), imageSizePxSmallInView, imageSizePxLargeInView);

        topTracksSearch.addListener(new SpotifyTopTracksSearchListener() {
            @Override
            public void onSearchRunningUpdated(boolean isRunning) {
                setProgressIndicatorShown(isRunning);
            }

            @Override
            public void onNewResult(SpotifyTopTracksSearchResult result) {
                resultAdapter.setCurrentResult(result);
                updateIsResultAvailableState();
            }
        });

        String countryCode = Locale.getDefault().getCountry();

        if(!restoreResultFromBundle(savedInstanceState)) {
            topTracksSearch.queryForTopTracksAsync(artistId, countryCode);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(resultAdapter != null) {
            SpotifyTopTracksSearchResult result = resultAdapter.getCurrentResult();
            if(result != null) {
                outState.putParcelable(KEY_TRACKS_RESULT, result);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        restoreResultFromBundle(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private boolean restoreResultFromBundle(Bundle bundle) {
        if(bundle != null && bundle.containsKey(KEY_TRACKS_RESULT)) {
            SpotifyTopTracksSearchResult result = bundle.getParcelable(KEY_TRACKS_RESULT);
            if(result != null && resultAdapter != null) {
                resultAdapter.setCurrentResult(result);
                updateIsResultAvailableState();
                return true;
            }
        }
        return false;
    }

    private void updateIsResultAvailableState()
    {
        boolean available = false;
        if(resultAdapter != null) {
            SpotifyTopTracksSearchResult currentResult = resultAdapter.getCurrentResult();
            available = currentResult != null && currentResult.getTracks().size() > 0;
        }
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
                float inactiveAlpha = getResources().getFraction(R.fraction.activity_artist_top10_tracks_inactive_alpha, 1, 1);
                int animationDurationMS = getResources().getInteger(R.integer.activity_artist_top10_tracks_alpha_change_durations_ms);

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
                    lvResultAlpha *= inactiveAlpha;
                    llNoResultAlpha *= inactiveAlpha;

                    llIndicatorAlpha = 1;
                }
                else {
                    llIndicatorAlpha = 0;
                }

                trackListView.animate().alpha(lvResultAlpha).setDuration(animationDurationMS).start();
                llNoResult.animate().alpha(llNoResultAlpha).setDuration(animationDurationMS).start();
                llIndicator.animate().alpha(llIndicatorAlpha).setDuration(animationDurationMS).start();
            }
        });
    }

    class SearchResultAdapter extends BaseAdapter {
        class ViewHolder {
            String trackId;
            ImageView ivTrack;
            TextView txtTrackName;
            TextView txtAlbumName;
        }

        private SpotifyTopTracksSearchResult currentResult;
        private Context context;
        private LayoutInflater inflater;

        SearchResultAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public SpotifyTopTracksSearchResult getCurrentResult() {
            return currentResult;
        }

        @Override
        public int getCount() {
            return (currentResult == null || currentResult.getTracks() == null) ? 0 : currentResult.getTracks().size();
        }

        @Override
        public Object getItem(int position) {
            if(currentResult == null)
                return null;
            if(currentResult.getTracks() == null)
                return null;
            if(currentResult.getTracks().size() <= position)
                return null;
            return currentResult.getTracks().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
            {
                convertView = inflater.inflate(R.layout.activity_artist_top10_tracks_entry, parent, false);
                ViewHolder vh = new ViewHolder();
                vh.ivTrack = (ImageView)convertView.findViewById(R.id.activity_artist_top10_tracks_entry_image);
                vh.txtTrackName = (TextView)convertView.findViewById(R.id.activity_artist_top10_tracks_entry_track_name);
                vh.txtAlbumName = (TextView)convertView.findViewById(R.id.activity_artist_top10_tracks_entry_track_album_name);
                convertView.setTag(vh);
            }
            ViewHolder vh = (ViewHolder)convertView.getTag();

            SpotifyTopTracksSearchResult.Track entry = (SpotifyTopTracksSearchResult.Track)getItem(position);

            //nothing to do here, the data in the view is already valid
            if(entry.getId().equals(vh.trackId))
                return convertView;

            setEntryToViews(entry, vh);

            return convertView;
        }

        private void setEntryToViews(SpotifyTopTracksSearchResult.Track entry, ViewHolder viewHolder) {
            String imageUrlToLoad = null;
            if(entry != null) {
                viewHolder.txtTrackName.setText(entry.getTrackName());
                viewHolder.txtAlbumName.setText(entry.getAlbumName());
                if(entry.hasThumbnailImageSmall()) {
                    imageUrlToLoad = entry.getAlbumThumbnailSmallUrl();
                }
                viewHolder.trackId = entry.getId();
            }
            else
            {
                viewHolder.txtTrackName.setText("");
                viewHolder.txtAlbumName.setText("");
                viewHolder.trackId = null;
            }

            if(imageUrlToLoad != null)
            {
                Picasso.with(context).load(imageUrlToLoad).into(viewHolder.ivTrack);
            }
            else
            {
                viewHolder.ivTrack.setImageDrawable(null);
            }
        }

        public void setCurrentResult(SpotifyTopTracksSearchResult currentResult) {
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
