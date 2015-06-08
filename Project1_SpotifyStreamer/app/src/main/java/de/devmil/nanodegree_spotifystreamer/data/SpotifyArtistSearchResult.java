package de.devmil.nanodegree_spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SpotifyArtistSearchResult implements Parcelable {
    private String searchTerm;
    private List<Artist> artists;

    public SpotifyArtistSearchResult(String searchTerm, List<Artist> artists)
    {
        this.searchTerm = searchTerm;
        this.artists = new ArrayList<>(artists);
    }

    public SpotifyArtistSearchResult(Parcel parcel)
    {
        this.searchTerm = parcel.readString();
        this.artists = parcel.createTypedArrayList(Artist.CREATOR);
    }

    public String getSearchTerm()
    {
        return searchTerm;
    }

    public List<Artist> getArtists()
    {
        return artists;
    }

    public static final Parcelable.Creator<SpotifyArtistSearchResult> CREATOR =
            new Creator<SpotifyArtistSearchResult>() {
                @Override
                public SpotifyArtistSearchResult createFromParcel(Parcel source) {
                    return new SpotifyArtistSearchResult(source);
                }

                @Override
                public SpotifyArtistSearchResult[] newArray(int size) {
                    return new SpotifyArtistSearchResult[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(searchTerm);
        dest.writeTypedList(artists);
    }

    public static class Artist implements Parcelable {
        private String id;
        private String name;
        private String imageUrl;

        public Artist(String id, String name, String imageUrl)
        {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
        }

        public Artist(Parcel parcel) {
            id = parcel.readString();
            name = parcel.readString();
            imageUrl = parcel.readString();
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

        public static final Parcelable.Creator<Artist> CREATOR =
                new Creator<Artist>() {
                    @Override
                    public Artist createFromParcel(Parcel source) {
                        return new Artist(source);
                    }

                    @Override
                    public Artist[] newArray(int size) {
                        return new Artist[size];
                    }
                };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(imageUrl);
        }
    }
}
