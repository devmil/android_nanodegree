package de.devmil.nanodegree_spotifystreamer.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
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

import de.devmil.nanodegree_spotifystreamer.R;
import de.devmil.nanodegree_spotifystreamer.data.PlayerData;
import de.devmil.nanodegree_spotifystreamer.data.Track;
import de.devmil.nanodegree_spotifystreamer.data.TracksSearchResult;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyTopTracksSearch;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyTopTracksSearchListener;
import de.devmil.nanodegree_spotifystreamer.service.MediaPlayService;
import de.devmil.nanodegree_spotifystreamer.utils.GlideConfig;
import de.devmil.nanodegree_spotifystreamer.utils.ViewUtils;
import kaaes.spotify.webapi.android.SpotifyApi;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link de.devmil.nanodegree_spotifystreamer.fragments.ArtistTop10TracksFragment.ArtistTop10FragmentListener} interface
 * to handle interaction events.
 */
public class ArtistTop10TracksFragment extends Fragment {

    private ArtistTop10FragmentListener listener;

    private static final String KEY_TRACKS_RESULT = "TRACKS_RESULT";
    private static final String KEY_ARG_ARTIST_ID = "ARG_ARTIST_ID";
    private static final String KEY_ARG_ARTIST_NAME = "ARG_ARTIST_NAME";

    private ListView trackListView;
    private LinearLayout llNoResult;
    private LinearLayout llIndicator;

    private SpotifyTopTracksSearch topTracksSearch;

    private SearchResultAdapter resultAdapter;

    private boolean isResultAvailable;
    private boolean isProgressIndicatorActive;

    private boolean resultRestored = false;

    private String artistName;
    private String artistId;

    public static ArtistTop10TracksFragment create(String artistId, String artistName) {
        Bundle arguments = new Bundle();
        arguments.putString(KEY_ARG_ARTIST_ID, artistId);
        arguments.putString(KEY_ARG_ARTIST_NAME, artistName);

        ArtistTop10TracksFragment result = new ArtistTop10TracksFragment();
        result.setArguments(arguments);
        return result;
    }


    public ArtistTop10TracksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_artist_top10_tracks, container, false);

        final Context context = inflater.getContext();

        trackListView = (ListView)view.findViewById(R.id.fragment_artist_top10_tracks_list);
        llNoResult = (LinearLayout)view.findViewById(R.id.fragment_artist_top10_tracks_ll_no_result);
        llIndicator = (LinearLayout)view.findViewById(R.id.fragment_artist_top10_tracks_ll_indicator);

        resultAdapter = new SearchResultAdapter(context);

        trackListView.setAdapter(resultAdapter);

        int imageSizePxSmallInView = (int) ViewUtils.getPxFromDip(context, getResources().getDimension(R.dimen.fragment_artist_top10_tracks_entry_image_size));
        int imageSizePxLargeInView = (int) ViewUtils.getPxFromDip(context, getResources().getDimension(R.dimen.activity_playing_image_size));
        topTracksSearch = new SpotifyTopTracksSearch(new SpotifyApi(), imageSizePxSmallInView, imageSizePxLargeInView);

        topTracksSearch.addListener(new SpotifyTopTracksSearchListener() {
            @Override
            public void onSearchRunningUpdated(boolean isRunning) {
                setProgressIndicatorShown(isRunning);
            }

            @Override
            public void onNewResult(TracksSearchResult result) {
                resultAdapter.setCurrentResult(result);
                updateIsResultAvailableState();
            }
        });

        resultRestored = restoreResultFromBundle(savedInstanceState);

        if(getArguments() != null) {
            String artistId = getArguments().getString(KEY_ARG_ARTIST_ID, null);
            String artistName = getArguments().getString(KEY_ARG_ARTIST_NAME, null);

            if(artistId != null
                    && artistName != null) {
                initData(artistId, artistName);
            }
        }

        return view;
    }

    public void initData(String artistId, String artistName) {

        this.artistId = artistId;
        this.artistName = artistName;

        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Activity activity = getActivity();
                if(activity == null) {
                    return;
                }
                TracksSearchResult searchResult = resultAdapter.getCurrentResult();
                if(searchResult != null) {

                    activity.startService(MediaPlayService.createStartIntent(activity));

                    final PlayerData playerData =
                            new PlayerData(
                                    ArtistTop10TracksFragment.this.artistId,
                                    ArtistTop10TracksFragment.this.artistName,
                                    searchResult.getTracks(),
                                    position);
                    activity.bindService(MediaPlayService.createStartIntent(activity), new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            Activity a = getActivity();
                            if(a == null) {
                                return;
                            }
                            MediaPlayService.MediaPlayBinder binder = (MediaPlayService.MediaPlayBinder) service;
                            binder.setIsTabletMode(listener.isTabletMode());
                            binder.setPlayerData(playerData);

                            fireOnLaunchPlayer();

                            a.unbindService(this);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {

                        }
                    }, Context.BIND_ABOVE_CLIENT);
                }
            }
        });

        if(!resultRestored) {
            String countryCode = Locale.getDefault().getCountry();
            topTracksSearch.queryForTopTracksAsync(artistId, countryCode);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(resultAdapter != null) {
            TracksSearchResult result = resultAdapter.getCurrentResult();
            if(result != null) {
                outState.putParcelable(KEY_TRACKS_RESULT, result);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ArtistTop10FragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ArtistTop10FragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private boolean restoreResultFromBundle(Bundle bundle) {
        if(bundle != null && bundle.containsKey(KEY_TRACKS_RESULT)) {
            TracksSearchResult result = bundle.getParcelable(KEY_TRACKS_RESULT);
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
            TracksSearchResult currentResult = resultAdapter.getCurrentResult();
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
        Activity activity = getActivity();
        if(activity == null) {
            return;
        }
        //just to be on the safe side.
        //the current implementation of "SpotifyArtistSearch" fires its event in the UI thread but
        //this isn't guaranteed for the future
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float inactiveAlpha = getResources().getFraction(R.fraction.activity_artist_top10_tracks_inactive_alpha, 1, 1);
                int animationDurationMS = getResources().getInteger(R.integer.activity_artist_top10_tracks_alpha_change_durations_ms);

                float lvResultAlpha;
                float llNoResultAlpha;
                float llIndicatorAlpha;

                if (isResultAvailable) {
                    lvResultAlpha = 1;
                    llNoResultAlpha = 0;
                } else {
                    lvResultAlpha = 0;
                    llNoResultAlpha = 1;
                }

                if (isProgressIndicatorActive) {
                    lvResultAlpha *= inactiveAlpha;
                    llNoResultAlpha *= inactiveAlpha;

                    llIndicatorAlpha = 1;
                } else {
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

        private TracksSearchResult currentResult;
        private Context context;
        private LayoutInflater inflater;

        SearchResultAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public TracksSearchResult getCurrentResult() {
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
                convertView = inflater.inflate(R.layout.fragment_artist_top10_tracks_entry, parent, false);
                ViewHolder vh = new ViewHolder();
                vh.ivTrack = (ImageView)convertView.findViewById(R.id.fragment_artist_top10_tracks_entry_image);
                vh.txtTrackName = (TextView)convertView.findViewById(R.id.fragment_artist_top10_tracks_entry_track_name);
                vh.txtAlbumName = (TextView)convertView.findViewById(R.id.fragment_artist_top10_tracks_entry_track_album_name);
                convertView.setTag(vh);
            }
            ViewHolder vh = (ViewHolder)convertView.getTag();

            Track entry = (Track)getItem(position);

            //nothing to do here, the data in the view is already valid
            if(entry.getId().equals(vh.trackId))
                return convertView;

            setEntryToViews(entry, vh);

            return convertView;
        }

        private void setEntryToViews(Track entry, ViewHolder viewHolder) {
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
                GlideConfig.configure(Glide.with(context).load(imageUrlToLoad)).into(viewHolder.ivTrack);
            }
            else
            {
                viewHolder.ivTrack.setImageDrawable(null);
            }
        }

        public void setCurrentResult(TracksSearchResult currentResult) {
            Activity activity = getActivity();
            if(activity == null) {
                return;
            }
            this.currentResult = currentResult;
            //just to be on the safe side.
            //the current implementation of "SpotifyArtistSearch" fires its event in the UI thread but
            //this isn't guaranteed for the future
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    private void fireOnLaunchPlayer() {
        if(listener != null) {
            listener.onLaunchPlayer();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ArtistTop10FragmentListener {
        void onLaunchPlayer();
        boolean isTabletMode();
    }

}
