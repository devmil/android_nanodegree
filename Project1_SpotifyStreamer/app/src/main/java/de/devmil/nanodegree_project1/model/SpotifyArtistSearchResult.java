package de.devmil.nanodegree_project1.model;

import java.util.ArrayList;
import java.util.List;

public class SpotifyArtistSearchResult {
    private String searchTerm;
    private List<SpotifyArtistResult> artists;

    public SpotifyArtistSearchResult(String searchTerm, List<SpotifyArtistResult> artists)
    {
        this.searchTerm = searchTerm;
        this.artists = new ArrayList<>(artists);
    }

    @SuppressWarnings("unused")
    public String getSearchTerm()
    {
        return searchTerm;
    }

    public List<SpotifyArtistResult> getArtists()
    {
        return artists;
    }
}
