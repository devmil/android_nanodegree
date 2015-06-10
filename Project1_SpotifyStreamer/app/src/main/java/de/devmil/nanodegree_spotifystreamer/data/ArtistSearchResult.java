package de.devmil.nanodegree_spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ArtistSearchResult implements Parcelable {
    private String searchTerm;
    private List<Artist> artists;

    public ArtistSearchResult(String searchTerm, List<Artist> artists)
    {
        this.searchTerm = searchTerm;
        this.artists = new ArrayList<>(artists);
    }

    public ArtistSearchResult(Parcel parcel)
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

    public static final Parcelable.Creator<ArtistSearchResult> CREATOR =
            new Creator<ArtistSearchResult>() {
                @Override
                public ArtistSearchResult createFromParcel(Parcel source) {
                    return new ArtistSearchResult(source);
                }

                @Override
                public ArtistSearchResult[] newArray(int size) {
                    return new ArtistSearchResult[size];
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

}
