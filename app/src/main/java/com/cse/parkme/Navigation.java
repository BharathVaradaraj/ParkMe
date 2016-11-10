package com.cse.parkme;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static android.os.SystemClock.sleep;

public class Navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    ClusterManager<MyItem> mClusterManager;
    SearchView searchView;
    String query;
    SearchManager searchManager;
    private GoogleMap mMap;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.normal) {
            String str = null;
            mMap.setMapStyle(new MapStyleOptions(str));
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        } else if (id == R.id.terrain) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        } else if (id == R.id.sat) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        } else if (id == R.id.night) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            boolean success = mMap.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.style_json)));

            if (!success) {
                Log.e("MapsActivityString", "Style parsing failed.");
            }


        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0,90,0,0);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            mMap.setMyLocationEnabled(true);
        }
        setUpMarkers();
        // Move the camera to Bangalore Region
        LatLng test = new LatLng(12.97194, 77.59369);
        //mMap.addMarker(new MarkerOptions().position(test).title("Marker in Bangalore").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(test, 11));
        searchLocation();

    }

    private void searchLocation() {

        //Search location by address
        Intent searchIntent =getIntent();
        if(Intent.ACTION_SEARCH.equals(searchIntent.getAction())){
            query = searchIntent.getStringExtra(SearchManager.QUERY);
            Log.i("Search=", query);
            //Searching by reverse geocoding
            List<Address> addressList = null;
            if(query != null || !query.equals(""))
            {
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(query , 5);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude() , address.getLongitude());
                Log.i("latlng:", String.valueOf(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            }

        }

    }

    private void setUpMarkers() {

        mClusterManager = new ClusterManager<MyItem>(this, mMap);
        mClusterManager.setRenderer(new MarkerRenderer());
        //mClusterManager.setOnClusterClickListener(this);
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        // Add cluster items (markers) to the cluster manager.
        addItems();
        mClusterManager.cluster();
    }

    private void addItems() {
        //Firebase retrieve marker data
        final DatabaseReference mUserReference = mDatabase.child("Sensors");
        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Data", dataSnapshot.getValue().toString());
                Double lat = null;
                Double lng = null;
                String status = null;
                mMap.clear();
                mClusterManager.clearItems();
                HashMap<String, Object> mhash = (HashMap<String, Object>) dataSnapshot.getValue();
                for (Object key : mhash.values()) {
                    HashMap<String, Double> loc_data = (HashMap<String, Double>) key;
                    HashMap<String, String> stat = (HashMap<String, String>) key;
                    lat = loc_data.get("lat");
                    lng = loc_data.get("lng");
                    status = stat.get("status");
                    Log.v("loc_data", lat + " " + lng);
                    //LatLng mark = new LatLng(lat, lng);
                    //MarkerOptions marker = new MarkerOptions();
                    if (status.equals("0")) {
                        MyItem mark = new MyItem(lat, lng, R.drawable.green_marker);
                        mClusterManager.addItem(mark);
                    } else {
                        MyItem mark = new MyItem(lat, lng, R.drawable.red_marker);
                        mClusterManager.addItem(mark);
                    }
                }
                mClusterManager.cluster();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private class MarkerRenderer extends DefaultClusterRenderer<MyItem> {
        //private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());

        public MarkerRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            //Bitmap icon = mIconGenerator.makeIcon();
            int test = item.MarkerIcons;
            markerOptions.icon(BitmapDescriptorFactory.fromResource(test));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }
}
