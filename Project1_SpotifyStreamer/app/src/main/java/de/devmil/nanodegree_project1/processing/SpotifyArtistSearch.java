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
import retrofit.RetrofitError;

public class SpotifyArtistSearch {

    private SpotifyApi spotifyApi;
    private List<SpotifyArtistSearchListener> listeners = new ArrayList<>();
    private ArtistSearchTask currentSearch;

    private int preferredArtistImageSizePx = 1000;

    public SpotifyArtistSearch(SpotifyApi api, int preferredArtistImageSizePx)
    {
        spotifyApi = api;
        this.preferredArtistImageSizePx = preferredArtistImageSizePx;
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

    private SpotifyArtistSearchResult buildResult(String searchTerm, ArtistsPager artistsPager) {
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
                    imageUrl = getPreferredImage(a.images).url;
                }

                artists.add(new SpotifyArtistResult(a.id, a.name, imageUrl));
            }
        }
        return new SpotifyArtistSearchResult(searchTerm, artists);
    }

    private Image getPreferredImage(List<Image> images) {
        Image currentResult = null;
        int currentResultDiff = Integer.MAX_VALUE;
        for(Image image : images) {

            //differences in width and height are scored equally
            //lower resolutions get a penalty multiplicator :)

            int diffY = image.height - preferredArtistImageSizePx;
            int diffX = image.width - preferredArtistImageSizePx;

            //bad bad low res image!
            if(diffY < 0)
                diffY *= 3;
            if(diffX < 0)
                diffX *= 3;

            int diff = Math.abs(diffY) + Math.abs(diffX);
            if(diff < currentResultDiff) {
                currentResultDiff = diff;
                currentResult = image;
            }
        }
        return currentResult;
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

    private void onNewResult(SpotifyArtistSearchResult result)
    {
        for(SpotifyArtistSearchListener listener : listeners)
        {
            if(listener != null)
                listener.onNewResult(result);
        }
    }

    class ArtistSearchTask extends AsyncTask<String, Integer, SpotifyArtistSearchResult>
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
        protected SpotifyArtistSearchResult doInBackground (String...params){
            onSearchRunningUpdated();
            try {
                Thread.sleep(delayMS);
            } catch (InterruptedException ignored) {
            }
            if (isCancelled()) {
                return null;
            }
            String searchTerm;
            if (params.length > 0) {
                //always use the last parameter passed here
                searchTerm = params[params.length - 1];
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
                    onNewResult(new SpotifyArtistSearchResult(searchTerm, new ArrayList<SpotifyArtistResult>()));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute (SpotifyArtistSearchResult spotifyArtistSearchResult){
        if (spotifyArtistSearchResult != null) {
            onNewResult(spotifyArtistSearchResult);
            isFinished = true;
            onSearchRunningUpdated();
        }
    }
    }
}
