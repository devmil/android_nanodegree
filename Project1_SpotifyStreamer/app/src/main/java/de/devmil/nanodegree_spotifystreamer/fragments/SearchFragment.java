package de.devmil.nanodegree_spotifystreamer.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.devmil.nanodegree_spotifystreamer.R;
import de.devmil.nanodegree_spotifystreamer.data.Artist;
import de.devmil.nanodegree_spotifystreamer.data.ArtistSearchResult;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyArtistSearch;
import de.devmil.nanodegree_spotifystreamer.model.SpotifyArtistSearchListener;
import de.devmil.nanodegree_spotifystreamer.utils.GlideConfig;
import de.devmil.nanodegree_spotifystreamer.utils.ViewUtils;
import kaaes.spotify.webapi.android.SpotifyApi;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.SearchFragmentListener} interface
 * to handle interaction events.
 */
public class SearchFragment extends Fragment {
    private boolean highlightSelection;

    private SearchFragmentListener listener;


    private ListView lvResult;
    private LinearLayout llNoResult;
    private LinearLayout llIndicator;

    private EditText editSearch;
    private SearchResultAdapter resultAdapter;
    private SpotifyArtistSearch artistSearch;
    private boolean restoringInstanceState = false;

    private boolean isProgressIndicatorActive = false;
    private boolean isResultAvailable = false;

    private static final int SEARCH_DELAY_MS = 1000;

    private static final String KEY_SEARCH_RESULT = "SEARCH_RESULT";
    private static final String KEY_SCROLL_POSITION = "SCROLL_POSITION";
    private static final String KEY_SELECTED_ITEM = "SELECTED_ITEM";

    private Integer initialScrollPosition = null;
    private int selectedItemIndex = ListView.INVALID_POSITION;

    public void setHighlightSelection(boolean highlightSelection) {
        this.highlightSelection = highlightSelection;
        if(lvResult != null && highlightSelection) {
            lvResult.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_search, container, false);

        final Context context = inflater.getContext();

        lvResult = (ListView)result.findViewById(R.id.fragment_search_lv_search_result);
        llNoResult = (LinearLayout)result.findViewById(R.id.fragment_search_ll_no_result);
        llIndicator = (LinearLayout)result.findViewById(R.id.fragment_search_ll_indicator);
        editSearch = (EditText)result.findViewById(R.id.fragment_search_edit_search);

        resultAdapter = new SearchResultAdapter(context);

        int imageSizePxInView = (int) ViewUtils.getPxFromDip(context, getResources().getDimension(R.dimen.fragment_search_result_entry_image_size));
        artistSearch = new SpotifyArtistSearch(new SpotifyApi(), imageSizePxInView);
        artistSearch.addListener(new SpotifyArtistSearchListener() {
            @Override
            public void onSearchRunningUpdated(boolean isRunning) {
                setProgressIndicatorShown(isRunning);
            }

            @Override
            public void onNewResult(ArtistSearchResult result) {
                resultAdapter.setCurrentResult(result);
                updateIsResultAvailableState();
            }
        });

        lvResult.setAdapter(resultAdapter);
        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist currentArtist = (Artist)resultAdapter.getItem(position);
                selectedItemIndex = position;
                fireOnArtistSelected(currentArtist);
            }
        });

        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String searchTerm = v.getText().toString();
                //don't trigger a new search automatically while the instance is restored
                if(restoringInstanceState) {
                    return true;
                }
                //Hitting the Enter key triggers an immediate search
                if(actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    artistSearch.queryForNameAsync(searchTerm, 0);
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
                //don't trigger a new search automatically while the instance is restored
                if(restoringInstanceState) {
                    return;
                }
                String searchTerm = s.toString();
                //Text change triggers a delayed search
                artistSearch.queryForNameAsync(searchTerm, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        restoringInstanceState = true;

        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(KEY_SEARCH_RESULT)) {
                ArtistSearchResult sr = savedInstanceState.getParcelable(KEY_SEARCH_RESULT);
                if(sr != null && resultAdapter != null) {
                    resultAdapter.setCurrentResult(sr);
                    updateIsResultAvailableState();
                }
            }

            if(savedInstanceState.containsKey(KEY_SCROLL_POSITION)) {
                initialScrollPosition = savedInstanceState.getInt(KEY_SCROLL_POSITION);
                lvResult.smoothScrollToPosition(initialScrollPosition);
            }

            if(savedInstanceState.containsKey(KEY_SELECTED_ITEM)) {
                highlightSelection = true;
                selectedItemIndex = savedInstanceState.getInt(KEY_SELECTED_ITEM);
                lvResult.smoothScrollToPosition(selectedItemIndex);
            }

            if(resultAdapter != null) {
                String searchTerm = editSearch.getText().toString();
                ArtistSearchResult currentResult = resultAdapter.getCurrentResult();
                if(currentResult != null
                        && !searchTerm.equals(currentResult.getSearchTerm())) {
                    artistSearch.queryForNameAsync(searchTerm, 0);
                }
            }
        }

        restoringInstanceState = false;

        if(highlightSelection) {
            lvResult.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        return result;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SearchFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SearchFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //LifeCycle

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(resultAdapter != null) {
            ArtistSearchResult result = resultAdapter.getCurrentResult();
            if(result != null) {
                outState.putParcelable(KEY_SEARCH_RESULT, result);
            }
        }
        outState.putInt(KEY_SCROLL_POSITION, lvResult.getFirstVisiblePosition());
        if(highlightSelection) {
            outState.putInt(KEY_SELECTED_ITEM, selectedItemIndex);
        }
    }

    private void updateIsResultAvailableState()
    {
        boolean available = false;
        if(resultAdapter != null) {
            ArtistSearchResult currentResult = resultAdapter.getCurrentResult();
            available = currentResult != null && currentResult.getArtists().size() > 0;
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
        Activity activity = getActivity();
        if(activity == null)
            return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float inactiveAlpha = getResources().getFraction(R.fraction.activity_main_inactive_alpha, 1, 1);
                int animationDurationMS = getResources().getInteger(R.integer.activity_main_alpha_change_durations_ms);

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

                lvResult.animate().alpha(lvResultAlpha).setDuration(animationDurationMS).start();
                llNoResult.animate().alpha(llNoResultAlpha).setDuration(animationDurationMS).start();
                llIndicator.animate().alpha(llIndicatorAlpha).setDuration(animationDurationMS).start();
            }
        });
    }

    private void fireOnArtistSelected(Artist artist) {
        if(listener != null) {
            listener.onArtistSelected(artist);
        }
    }

    class SearchResultAdapter extends BaseAdapter {
        class ViewHolder {
            String artistId;
            ImageView ivArtist;
            TextView txtName;
        }

        private ArtistSearchResult currentResult;
        private Context context;
        private LayoutInflater inflater;

        SearchResultAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public ArtistSearchResult getCurrentResult() {
            return currentResult;
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
                convertView = inflater.inflate(R.layout.fragment_search_result_entry, parent, false);
                ViewHolder vh = new ViewHolder();
                vh.ivArtist = (ImageView)convertView.findViewById(R.id.fragment_search_result_entry_image);
                vh.txtName = (TextView)convertView.findViewById(R.id.fragment_search_result_entry_label);
                convertView.setTag(vh);
            }
            ViewHolder vh = (ViewHolder)convertView.getTag();

            Artist entry = (Artist)getItem(position);

            if(entry == null) {
                return convertView;
            }

            //nothing to do here, the data in the view is already valid
            if(entry.getId().equals(vh.artistId))
                return convertView;

            setEntryToViews(entry, vh);

            return convertView;
        }

        private void setEntryToViews(Artist entry, ViewHolder viewHolder) {
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
                GlideConfig.configure(Glide.with(context).load(imageUrlToLoad)).into(viewHolder.ivArtist);
            }
            else
            {
                viewHolder.ivArtist.setImageDrawable(null);
            }
        }

        public void setCurrentResult(ArtistSearchResult currentResult) {
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
    public interface SearchFragmentListener {
        // TODO: Update argument type and name
        public void onArtistSelected(Artist artist);
    }

}
