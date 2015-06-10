package de.devmil.nanodegree_spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by michaellamers on 10.06.15.
 */
public class Artist implements Parcelable {
    private String id;
    private String name;
    private String imageUrl;

    public Artist(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Artist(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        imageUrl = parcel.readString();
    }

    public String getId() {
        return id;
    }

    public boolean hasImage() {
        return imageUrl != null;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public static final Creator<Artist> CREATOR =
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
