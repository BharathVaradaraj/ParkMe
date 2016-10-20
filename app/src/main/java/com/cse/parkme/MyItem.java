package com.cse.parkme;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class MyItem implements ClusterItem{
    private final LatLng mPosition;
    public final int MarkerIcons;

    public MyItem(double lat, double lng, int icons) {
        mPosition = new LatLng(lat, lng);
        MarkerIcons = icons;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
