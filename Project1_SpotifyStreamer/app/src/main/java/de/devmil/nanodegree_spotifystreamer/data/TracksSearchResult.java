package de.devmil.nanodegree_spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TracksSearchResult implements Parcelable {
    private String artistId;
    private String countryCode;
    private List<Track> tracks;

    public TracksSearchResult(String artistId, String countryCode, List<Track> tracks) {
        this.artistId = artistId;
        this.countryCode = countryCode;
        this.tracks = new ArrayList<>(tracks);
    }

    public TracksSearchResult(Parcel parcel)
    {
        this.artistId = parcel.readString();
        this.countryCode = parcel.readString();
        this.tracks = parcel.createTypedArrayList(Track.CREATOR);
    }

    public String getArtistId() {
        return artistId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public static final Parcelable.Creator<TracksSearchResult> CREATOR = new Creator<TracksSearchResult>() {
        @Override
        public TracksSearchResult createFromParcel(Parcel source) {
            return new TracksSearchResult(source);
        }

        @Override
        public TracksSearchResult[] newArray(int size) {
            return new TracksSearchResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistId);
        dest.writeString(countryCode);
        dest.writeTypedList(tracks);
    }

}
