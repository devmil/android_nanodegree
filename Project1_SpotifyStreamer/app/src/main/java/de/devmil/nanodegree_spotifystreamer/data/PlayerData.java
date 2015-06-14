package de.devmil.nanodegree_spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PlayerData implements Parcelable {
    private List<Track> tracks;
    private String artistId;
    private String artistName;
    private int activeTrackIndex;

    public PlayerData(String artistId, String artistName, List<Track> tracks, int initialActiveTrackIndex) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.tracks = new ArrayList<>(tracks);
        this.activeTrackIndex = initialActiveTrackIndex;
    }

    public PlayerData(Parcel source) {
        this.artistId = source.readString();
        this.artistName = source.readString();
        this.tracks = source.createTypedArrayList(Track.CREATOR);
        this.activeTrackIndex = source.readInt();
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getActiveTrackIndex() {
        return activeTrackIndex;
    }

    public void setActiveTrackIndex(int activeTrackIndex) {
        this.activeTrackIndex = activeTrackIndex;
    }

    public boolean canNavigatePrev() {
        return activeTrackIndex > 0;
    }

    public boolean canNavigateNext() {
        return activeTrackIndex < tracks.size() - 1;
    }

    public Track getActiveTrack() {
        if(tracks != null && tracks.size() > activeTrackIndex && activeTrackIndex >= 0)
            return tracks.get(activeTrackIndex);
        return null;
    }

    public static final Creator<PlayerData> CREATOR = new Parcelable.Creator<PlayerData>() {

        @Override
        public PlayerData createFromParcel(Parcel source) {
            return new PlayerData(source);
        }

        @Override
        public PlayerData[] newArray(int size) {
            return new PlayerData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistId);
        dest.writeString(artistName);
        dest.writeTypedList(tracks);
        dest.writeInt(activeTrackIndex);
    }
}
