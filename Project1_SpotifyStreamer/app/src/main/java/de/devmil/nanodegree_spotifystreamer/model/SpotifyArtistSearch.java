package de.devmil.nanodegree_spotifystreamer.model;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import de.devmil.nanodegree_spotifystreamer.data.ArtistSearchResult;
import de.devmil.nanodegree_spotifystreamer.utils.ImageScoring;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;

public class SpotifyArtistSearch {

    private SpotifyApi spotifyApi;
    private List<SpotifyArtistSearchListener> listeners = new ArrayList<>();
    private ArtistSearchTask currentSearch;
    private ImageScoring<Image> scoring;

    public SpotifyArtistSearch(SpotifyApi api, int preferredArtistImageSizePx)
    {
        spotifyApi = api;
        scoring = new ImageScoring<>(preferredArtistImageSizePx,
                new ImageScoring.ImageSizeRetriever<Image>() {
                    @Override
                    public int getHeight(Image image) {
                        return image.height;
                    }

                    @Override
                    public int getWidth(Image image) {
                        return image.width;
                    }
                });
    }

    public void queryForNameAsync(String name, int delayMS)
    {
        SpotifyService service = spotifyApi.getService();

        if(currentSearch != null && !currentSearch.isCancelled()) {
            currentSearch.cancel(true);
        }

        currentSearch = new ArtistSearchTask(service, delayMS);

        //this call allows the search processes to run in parallel.
        //the cancel mechanism controlled here makes sure that only the newest
        //search fires "onNewResult"
        currentSearch.executeOnExecutor(ArtistSearchTask.THREAD_POOL_EXECUTOR, name);
    }

    public void addListener(SpotifyArtistSearchListener listener)
    {
        if(listener == null)
            return;
        if(listeners.contains(listener))
            return;
        listeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeListener(SpotifyArtistSearchListener listener)
    {
        listeners.remove(listener);
    }

    private ArtistSearchResult buildResult(String searchTerm, ArtistsPager artistsPager) {
        ArrayList<de.devmil.nanodegree_spotifystreamer.data.Artist> artists = new ArrayList<>();

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
                    imageUrl = getPreferredImage(a.images).url;
                }

                artists.add(new de.devmil.nanodegree_spotifystreamer.data.Artist(a.id, a.name, imageUrl));
            }
        }
        return new ArtistSearchResult(searchTerm, artists);
    }

    private Image getPreferredImage(List<Image> images) {

        return scoring.getNearest(images);
    }

    private boolean isSomethingRunning() {
        return
                currentSearch != null
                && !currentSearch.isCancelled()
                && !currentSearch.isFinished()
                && !currentSearch.isCrashed();
    }

    private void onSearchRunningUpdated()
    {
        for(SpotifyArtistSearchListener listener : listeners)
        {
            if(listener != null)
                listener.onSearchRunningUpdated(isSomethingRunning());
        }
    }

    private void onNewResult(ArtistSearchResult result)
    {
        for(SpotifyArtistSearchListener listener : listeners)
        {
            if(listener != null)
                listener.onNewResult(result);
        }
    }

    class ArtistSearchTask extends AsyncTask<String, Integer, ArtistSearchResult>
    {
        private SpotifyService service;
        private int delayMS;
        private boolean isFinished;
        private boolean isCrashed;

        public ArtistSearchTask(SpotifyService service, int delayMS)
        {
            this.service = service;
            this.delayMS = delayMS;
            isFinished = false;
            isCrashed = false;
        }

        public boolean isFinished()
        {
            return isFinished;
        }

        public boolean isCrashed()
        {
            return isCrashed;
        }

        @Override
        protected ArtistSearchResult doInBackground (String...params){
            onSearchRunningUpdated();
            try {
                Thread.sleep(delayMS);
            } catch (InterruptedException ignored) {
            }
            if (isCancelled()) {
                return null;
            }
            String searchTerm;
            if (params.length == 1) {
                //always use the last parameter passed here
                searchTerm = params[0];
                try {
                    ArtistsPager artistsPager = service.searchArtists(searchTerm);

                    if (!isCancelled()) {
                        return buildResult(searchTerm, artistsPager);
                    }
                }
                catch(RetrofitError e)
                {
                    isCrashed = true;
                    onSearchRunningUpdated();
                    onNewResult(new ArtistSearchResult(searchTerm, new ArrayList<de.devmil.nanodegree_spotifystreamer.data.Artist>()));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute (ArtistSearchResult artistSearchResult){
            if (artistSearchResult != null) {
                onNewResult(artistSearchResult);
                isFinished = true;
                onSearchRunningUpdated();
            }
        }
    }
}
