package de.devmil.nanodegree_project1.processing;

import de.devmil.nanodegree_project1.model.SpotifyArtistSearchResult;

public interface SpotifyArtistSearchListener {

    void onNewResult(SpotifyArtistSearchResult result);
}
