package de.devmil.nanodegree_spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by michaellamers on 10.06.15.
 */
public class Track implements Parcelable {
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

    public static final Creator<Track> CREATOR = new Creator<Track>() {
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
