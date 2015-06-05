package de.devmil.nanodegree_project1;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.devmil.nanodegree_project1.model.SpotifyArtistResult;
import de.devmil.nanodegree_project1.model.SpotifyArtistSearchResult;
import de.devmil.nanodegree_project1.processing.SpotifyArtistSearch;
import de.devmil.nanodegree_project1.processing.SpotifyArtistSearchListener;
import kaaes.spotify.webapi.android.SpotifyApi;

public class MainActivity extends AppCompatActivity {

    private ListView lvResult;
    private LinearLayout llNoResult;
    private EditText editSearch;
    private SearchResultAdapter resultAdapter;
    private SpotifyArtistSearch artistSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvResult = (ListView)findViewById(R.id.activity_main_lv_search_result);
        llNoResult = (LinearLayout)findViewById(R.id.activity_main_ll_no_result);
        editSearch = (EditText)findViewById(R.id.activity_main_edit_search);

        resultAdapter = new SearchResultAdapter(this);

        artistSearch = new SpotifyArtistSearch(new SpotifyApi());
        artistSearch.addListener(new SpotifyArtistSearchListener() {
            @Override
            public void onNewResult(SpotifyArtistSearchResult result) {
                resultAdapter.setCurrentResult(result);
                setResultAvailable(result.getArtists().size() > 0);
            }
        });

        lvResult.setAdapter(resultAdapter);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO: delay
                artistSearch.queryForNameAsync(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setResultAvailable(final boolean available)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(available)
                {
                    lvResult.setVisibility(View.VISIBLE);
                    llNoResult.setVisibility(View.GONE);
                }
                else
                {
                    lvResult.setVisibility(View.GONE);
                    llNoResult.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    class SearchResultAdapter extends BaseAdapter {
        class ViewHolder
        {
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
                convertView = inflater.inflate(R.layout.activity_main_result_entry, null);
                ViewHolder vh = new ViewHolder();
                vh.ivArtist = (ImageView)convertView.findViewById(R.id.activity_main_result_entry_image);
                vh.txtName = (TextView)convertView.findViewById(R.id.activity_main_result_entry_label);
                convertView.setTag(vh);
            }
            ViewHolder vh = (ViewHolder)convertView.getTag();

            SpotifyArtistResult entry = (SpotifyArtistResult)getItem(position);

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
            }
            else
            {
                viewHolder.txtName.setText("");
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }
}
