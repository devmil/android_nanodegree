package de.devmil.nanodegree_project1.model;

public class SpotifyArtistResult {
    private String id;
    private String name;
    private String imageUrl;

    public SpotifyArtistResult(String id, String name, String imageUrl)
    {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getId()
    {
        return id;
    }

    public boolean hasImage()
    {
        return imageUrl != null;
    }

    public String getName()
    {
        return name;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }
}
