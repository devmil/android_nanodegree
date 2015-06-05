package de.devmil.nanodegree_project1.processing;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import de.devmil.nanodegree_project1.model.SpotifyArtistResult;
import de.devmil.nanodegree_project1.model.SpotifyArtistSearchResult;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by michaellamers on 04.06.15.
 */
public class SpotifyArtistSearch {

    private SpotifyApi spotifyApi;
    private List<SpotifyArtistSearchListener> listeners = new ArrayList<>();
    private AsyncTask<String, Integer, SpotifyArtistSearchResult> currentSearch;

    public SpotifyArtistSearch(SpotifyApi api)
    {
        spotifyApi = api;
    }

    public void queryForNameAsync(String name)
    {
        final SpotifyService service = spotifyApi.getService();

        if(currentSearch != null && !currentSearch.isCancelled()) {
            currentSearch.cancel(true);
        }
        currentSearch = new AsyncTask<String, Integer, SpotifyArtistSearchResult>() {
            @Override
            protected SpotifyArtistSearchResult doInBackground(String... params) {
                if(params.length > 0) {
                    ArtistsPager artistsPager = service.searchArtists(params[params.length - 1]);
                    if(!isCancelled()) {
                        return buildResult(artistsPager);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(SpotifyArtistSearchResult spotifyArtistSearchResult) {
                onNewResult(spotifyArtistSearchResult);
            }
        };
        currentSearch.execute(name);
    }

    public void addListener(SpotifyArtistSearchListener listener)
    {
        if(listener == null)
            return;
        if(listeners.contains(listener))
            return;
        listeners.add(listener);
    }

    public void removeListener(SpotifyArtistSearchListener listener)
    {
        listeners.remove(listener);
    }

    private SpotifyArtistSearchResult buildResult(ArtistsPager artistsPager) {
        ArrayList<SpotifyArtistResult> artists = new ArrayList<>();

        List<Artist> spotifyArtists = null;

        if(artistsPager != null && artistsPager.artists != null && artistsPager.artists.items != null)
            spotifyArtists = artistsPager.artists.items;

        if(spotifyArtists != null) {
            for (Artist a : spotifyArtists) {
                if (a == null)
                    continue;
                if (!"artist".equals(a.type))
                    continue;
                String imageUrl = null;
                if (a.images != null && a.images.size() > 0) {
                    Image img = a.images.get(0);
                    if (img != null) {
                        imageUrl = img.url;
                    }
                }

                artists.add(new SpotifyArtistResult(a.id, a.name, imageUrl));
            }
        }
        return new SpotifyArtistSearchResult(artists);
    }

    private void onNewResult(SpotifyArtistSearchResult result)
    {
        for(SpotifyArtistSearchListener listener : listeners)
        {
            if(listener != null)
                listener.onNewResult(result);
        }
    }
}
