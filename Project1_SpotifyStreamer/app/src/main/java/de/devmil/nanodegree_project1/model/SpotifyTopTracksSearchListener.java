package de.devmil.nanodegree_project1.model;

import de.devmil.nanodegree_project1.data.SpotifyTopTracksSearchResult;

public interface SpotifyTopTracksSearchListener {
    /**
     * This method gets called when the search starts or is finished
     * @param isRunning determines if *any* search is currently running
     */
    void onSearchRunningUpdated(boolean isRunning);

    /**
     * Gets fired whenever there is a new result available
     * @param result contains the result of the finished search
     */
    void onNewResult(SpotifyTopTracksSearchResult result);
}
