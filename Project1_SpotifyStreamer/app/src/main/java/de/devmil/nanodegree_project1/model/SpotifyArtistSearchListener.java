package de.devmil.nanodegree_project1.model;

import java.util.List;

public interface SpotifyArtistSearchListener {

    void onNewResult(List<SpotifyArtistResult> result);
}
