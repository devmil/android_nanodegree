package de.devmil.nanodegree_project1.model;

import java.util.ArrayList;
import java.util.List;

public class SpotifyArtistSearchResult {
    private List<SpotifyArtistResult> artists;

    public SpotifyArtistSearchResult(List<SpotifyArtistResult> artists)
    {
        this.artists = new ArrayList<>(artists);
    }

    public List<SpotifyArtistResult> getArtists()
    {
        return artists;
    }
}
