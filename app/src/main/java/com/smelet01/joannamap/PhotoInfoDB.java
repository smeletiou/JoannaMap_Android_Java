package com.smelet01.joannamap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

@Entity
public class PhotoInfoDB implements Serializable, ClusterItem {
    private static final long serialVersionUID = 1L;

    public PhotoInfoDB(@NonNull String uri, double latitude, double longitude, String orientation) {
        this.uri = uri;
        this.latitude = latitude;
        this.longitude = longitude;
        this.orientation = orientation;
    }

    @PrimaryKey
    @NonNull
    public String uri;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "orientation")
    public String orientation;

    @Override
    public String toString() {
        return "PhotoInfoDB{" +
                "uri='" + uri + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", orientation='" + orientation + '\'' +
                '}';
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
