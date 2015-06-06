package de.devmil.nanodegree_project1.model;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.devmil.nanodegree_project1.data.SpotifyTopTracksSearchResult;
import de.devmil.nanodegree_project1.utils.ImageScoring;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class SpotifyTopTracksSearch {
    private SpotifyApi spotifyApi;
    private List<SpotifyTopTracksSearchListener> listeners = new ArrayList<>();
    private TopTracksSearchTask currentSearch;
    private ImageScoring<Image> scoringSmall;
    private ImageScoring<Image> scoringLarge;

    public SpotifyTopTracksSearch(SpotifyApi api, int preferredTrackImageSizeSmallPx, int preferredTrackImageSizeLargePx)
    {
        spotifyApi = api;
        ImageScoring.ImageSizeRetriever<Image> sizeRetriever = new ImageScoring.ImageSizeRetriever<Image>() {
            @Override
            public int getHeight(Image image) {
                return image.height;
            }

            @Override
            public int getWidth(Image image) {
                return image.width;
            }
        };
        scoringSmall = new ImageScoring<>(preferredTrackImageSizeSmallPx, sizeRetriever);
        scoringLarge = new ImageScoring<>(preferredTrackImageSizeLargePx, sizeRetriever);
    }

    public void queryForTopTracksAsync(String artistId, String countryCode)
    {
        SpotifyService service = spotifyApi.getService();

        if(currentSearch != null && !currentSearch.isCancelled()) {
            currentSearch.cancel(true);
        }

        currentSearch = new TopTracksSearchTask(service);

        //this call allows the search processes to run in parallel.
        //the cancel mechanism controlled here makes sure that only the newest
        //search fires "onNewResult"
        currentSearch.executeOnExecutor(TopTracksSearchTask.THREAD_POOL_EXECUTOR, artistId, countryCode);
    }

    public void addListener(SpotifyTopTracksSearchListener listener)
    {
        if(listener == null)
            return;
        if(listeners.contains(listener))
            return;
        listeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeListener(SpotifyTopTracksSearchListener listener)
    {
        listeners.remove(listener);
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
        for(SpotifyTopTracksSearchListener listener : listeners)
        {
            if(listener != null)
                listener.onSearchRunningUpdated(isSomethingRunning());
        }
    }

    private void onNewResult(SpotifyTopTracksSearchResult result)
    {
        for(SpotifyTopTracksSearchListener listener : listeners)
        {
            if(listener != null)
                listener.onNewResult(result);
        }
    }

    class TopTracksSearchTask extends AsyncTask<String, Integer, SpotifyTopTracksSearchResult> {
        private SpotifyService service;
        private boolean isFinished;
        private boolean isCrashed;

        public TopTracksSearchTask(SpotifyService service)
        {
            this.service = service;
            isFinished = false;
            isCrashed = false;
        }

        public boolean isFinished() {
            return isFinished;
        }

        public boolean isCrashed() {
            return isCrashed;
        }

        @Override
        protected SpotifyTopTracksSearchResult doInBackground(String... params) {
            onSearchRunningUpdated();
            if (isCancelled()) {
                return null;
            }

            if(params.length != 2) {
                isCrashed = true;
                return null;
            }

            String artistId = params[0];
            final String countryCode = params[1];

            HashMap<String, Object> map = new HashMap<>();
            map.put(SpotifyService.COUNTRY, countryCode);

            Tracks tracks = null;
            try {
                tracks = service.getArtistTopTrack(artistId, map);
            }
            catch (RetrofitError error) {
                isCrashed = true;
                onSearchRunningUpdated();
                onNewResult(new SpotifyTopTracksSearchResult(artistId, countryCode, new ArrayList<SpotifyTopTracksSearchResult.Track>()));
            }

            List<SpotifyTopTracksSearchResult.Track> resultTracks = new ArrayList<>();

            if(tracks != null) {
                for(Track t : tracks.tracks) {
                    String trackId = t.id;
                    String trackName = t.name;
                    String previewUrl = t.preview_url;
                    String albumName = null;
                    String albumThumbnailSmallUrl = null;
                    String albumThumbnailLargeUrl = null;
                    if(t.album != null) {
                        albumName = t.album.name;
                        if(t.album.images != null) {

                            Image smallImage = scoringSmall.getNearest(t.album.images);
                            if(smallImage != null) {
                                albumThumbnailSmallUrl = smallImage.url;
                            }

                            Image largeImage = scoringLarge.getNearest(t.album.images);
                            if(largeImage != null) {
                                albumThumbnailLargeUrl = largeImage.url;
                            }
                        }
                    }

                    resultTracks.add(
                        new SpotifyTopTracksSearchResult.Track(
                            trackId,
                            trackName,
                            previewUrl,
                            albumName,
                            albumThumbnailSmallUrl,
                            albumThumbnailLargeUrl
                        )
                    );
                }
            }

            return new SpotifyTopTracksSearchResult(artistId, countryCode, resultTracks);
        }

        @Override
        protected void onPostExecute(SpotifyTopTracksSearchResult spotifyTopTracksSearchResult) {
            if (spotifyTopTracksSearchResult != null) {
                onNewResult(spotifyTopTracksSearchResult);
                isFinished = true;
                onSearchRunningUpdated();
            }
        }
    }
}
