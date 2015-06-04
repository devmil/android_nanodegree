package de.devmil.nanodegree_project1.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public SpotifyArtistSearch(SpotifyApi api)
    {
        spotifyApi = api;
    }

    public void queryForNameAsync(String name)
    {
        SpotifyService service = spotifyApi.getService();
        service.searchArtists(name, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                onNewResult(buildResult(artistsPager));
            }

            @Override
            public void failure(RetrofitError error) {
                onNewResult(new ArrayList<SpotifyArtistResult>());
            }
        });
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

    private List<SpotifyArtistResult> buildResult(ArtistsPager artistsPager) {
        ArrayList<SpotifyArtistResult> result = new ArrayList<>();
        if(artistsPager == null)
            return result;
        if(artistsPager.artists == null)
            return result;
        if(artistsPager.artists.items == null)
            return result;
        for(Artist a : artistsPager.artists.items)
        {
            if(a == null)
                continue;
            if(!"artist".equals(a.type))
                continue;
            String imageUrl = null;
            if(a.images != null && a.images.size() > 0) {
                Image img = a.images.get(0);
                if(img != null)
                {
                    imageUrl = img.url;
                }
            }

            result.add(new SpotifyArtistResult(a.id, a.name, imageUrl));
        }
        return result;
    }

    private void onNewResult(List<SpotifyArtistResult> result)
    {
        for(SpotifyArtistSearchListener listener : listeners)
        {
            if(listener != null)
                listener.onNewResult(result);
        }
    }
}
