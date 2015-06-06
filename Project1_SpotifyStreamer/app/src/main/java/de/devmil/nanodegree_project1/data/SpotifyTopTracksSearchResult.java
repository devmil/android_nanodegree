package de.devmil.nanodegree_project1.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SpotifyTopTracksSearchResult implements Parcelable {
    private String artistId;
    private String countryCode;
    private List<Track> tracks;

    public SpotifyTopTracksSearchResult(String artistId, String countryCode, List<Track> tracks) {
        this.artistId = artistId;
        this.countryCode = countryCode;
        this.tracks = new ArrayList<>(tracks);
    }

    public SpotifyTopTracksSearchResult(Parcel parcel)
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

    public static final Parcelable.Creator<SpotifyTopTracksSearchResult> CREATOR = new Creator<SpotifyTopTracksSearchResult>() {
        @Override
        public SpotifyTopTracksSearchResult createFromParcel(Parcel source) {
            return new SpotifyTopTracksSearchResult(source);
        }

        @Override
        public SpotifyTopTracksSearchResult[] newArray(int size) {
            return new SpotifyTopTracksSearchResult[size];
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

    public static class Track implements Parcelable {
        private String id;
        private String trackName;
        private String previewUrl;
        private String albumName;
        private String albumThumbnailSmallUrl;
        private String albumThumbnailLargeUrl;

        public Track(String id, String trackName, String previewUrl, String albumName, String albumThumbnailSmallUrl, String albumThumbnailLargeUrl) {
            this.id = id;
            this.trackName = trackName;
            this.previewUrl = previewUrl;
            this.albumName = albumName;
            this.albumThumbnailSmallUrl = albumThumbnailSmallUrl;
            this.albumThumbnailLargeUrl = albumThumbnailLargeUrl;
        }

        public Track(Parcel parcel) {
            this.id = parcel.readString();
            this.trackName = parcel.readString();
            this.previewUrl = parcel.readString();
            this.albumName = parcel.readString();
            this.albumThumbnailSmallUrl = parcel.readString();
            this.albumThumbnailLargeUrl = parcel.readString();
        }

        public boolean hasThumbnailImageSmall() {
            return albumThumbnailSmallUrl != null;
        }

        public String getId() {
            return id;
        }

        public String getTrackName() {
            return trackName;
        }

        public String getPreviewUrl() {
            return previewUrl;
        }

        public String getAlbumName() {
            return albumName;
        }

        public String getAlbumThumbnailSmallUrl() {
            return albumThumbnailSmallUrl;
        }

        public String getAlbumThumbnailLargeUrl() {
            return albumThumbnailLargeUrl;
        }

        public static final Parcelable.Creator<Track> CREATOR = new Creator<Track>() {
            @Override
            public Track createFromParcel(Parcel source) {
                return new Track(source);
            }

            @Override
            public Track[] newArray(int size) {
                return new Track[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(trackName);
            dest.writeString(previewUrl);
            dest.writeString(albumName);
            dest.writeString(albumThumbnailSmallUrl);
            dest.writeString(albumThumbnailLargeUrl);
        }
    }
}
