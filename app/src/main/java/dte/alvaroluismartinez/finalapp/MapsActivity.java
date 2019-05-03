package dte.alvaroluismartinez.finalapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {
    public GoogleMap mMap;
    private boolean aparece = false;
    private List<Polyline> polylines = new ArrayList<Polyline>();
    //private final LatLng etsist = new LatLng(40.389618, -3.628935);
    private ArrayList<String> allCamsCoordinates;
    private ArrayList<String> allCamsNames;
    private ArrayList<String> allCamsUrls;
    private ArrayList<Marker> markers;
    private String oneCamCoordinate;
    private String oneCamName;
    private boolean isCluster;
    private LatLngBounds.Builder builder;

    private LatLng myLocation;

    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        Intent myIntent = getIntent();
        this.allCamsCoordinates = myIntent.getStringArrayListExtra("allCordinates");
        this.allCamsUrls = myIntent.getStringArrayListExtra("allUrls");
        this.allCamsNames = myIntent.getStringArrayListExtra("allNames");
        this.oneCamCoordinate = myIntent.getStringExtra("coordinate");
        this.oneCamName = myIntent.getStringExtra("name");
        this.isCluster = myIntent.getBooleanExtra("isCluster",false);
        markers = new ArrayList<Marker>();

        myLocation = new LatLng(myIntent.getDoubleExtra("myLat",90.0), myIntent.getDoubleExtra("myLong",360.0));
        if(myLocation.longitude==360.0 || myLocation.latitude==90.0){
            myLocation = null;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        String[] coordinatesArray;
        double lat;
        double lon;
        LatLng location;

        mMap.setOnMapLoadedCallback(this);

        /*Marker marker = mMap.addMarker(new MarkerOptions().position(etsist).title("Marker in etsist"));
        mMap.setOnMarkerClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(etsist));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f), 10, null);*/

        builder = new LatLngBounds.Builder();
        if(allCamsCoordinates != null && !isCluster){
            addMarkers();
            doZoomBounds();
        }
        else if (isCluster){
            DefaultClusterRenderer render = null;
            mClusterManager = new ClusterManager<>(this, googleMap);
            googleMap.setOnCameraIdleListener(mClusterManager);
            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);

            addItems();
            mClusterManager.cluster();

            doZoomBounds();
        }
        else{
            coordinatesArray = oneCamCoordinate.split(",");
            lon = Double.parseDouble(coordinatesArray[0]);
            lat = Double.parseDouble(coordinatesArray[1]);
            location = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(location));

            /*mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f), 10, null);*/

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f));

            //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150););
        }
        if(myLocation != null){
            mMap.addMarker(new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        mMap.setOnMarkerClickListener(this);

        if(!checkLocationPermission())
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onMapLoaded(){
        //hacerZoomLimites();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(etsist));
        //mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ), 5000, null );
        boolean bol = aparece;
        if (aparece) {
            aparece = false;
            bol = true;
        } else {
            aparece = true;

            bol = false;
        }
        //CameraPosition cameraPosition = new CameraPosition.Builder().target(etsist).zoom(17).build();

        //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);

        return bol;

    }

    public void onClick_radio(View v) {
        RadioButton rb1 = (RadioButton) findViewById(R.id.r1map);
        RadioButton rb2 = (RadioButton) findViewById(R.id.r2Sat);
        RadioButton rb3 = (RadioButton) findViewById(R.id.r3hyb);
        boolean check = ((RadioButton) v).isChecked();
        switch (v.getId()) {
            case R.id.r1map:
                if (check)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.r2Sat:
                if (check)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.r3hyb:
                if (check)
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }

    private Collection<MyItem> addItems(){
        String[] coordinatesArray;
        double lat;
        double lon;
        int i = 0;
        MyItem item = null;
        Collection<MyItem> listItems = new ArrayList<MyItem>();
        for(String coordinates : allCamsCoordinates){
            coordinatesArray = coordinates.split(",");
            lon = Double.parseDouble(coordinatesArray[0]);
            lat = Double.parseDouble(coordinatesArray[1]);
            item = new MyItem(lat, lon, allCamsNames.get(i), allCamsUrls.get(i));
            listItems.add(item);
            builder.include(new LatLng(lat,lon));
            mClusterManager.addItem(item);
            i++;
        }
        return listItems;
    }

    private void addMarkers(){
        String[] coordinatesArray;
        double lat;
        double lon;
        LatLng location;
        int i = 0;
        Marker marker;

        for(String coordinates : allCamsCoordinates){
            coordinatesArray = coordinates.split(",");
            lon = Double.parseDouble(coordinatesArray[0]);
            lat = Double.parseDouble(coordinatesArray[1]);
            location = new LatLng(lat, lon);
            if(allCamsNames.get(i).equals(oneCamName)){
                marker = mMap.addMarker(new MarkerOptions().position(location).title(allCamsNames.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
            else{
                marker = mMap.addMarker(new MarkerOptions().position(location).title(allCamsNames.get(i)));
            }
            builder.include(new LatLng(lat,lon));
            markers.add(marker);
            i++;
        }
    }

    private void doZoomBounds() {
        CameraUpdate cu = null;
        LatLngBounds bounds = null;
        if (allCamsCoordinates != null) {
            bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.15); // 15% de la pantalla
            cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cu);
        }
    }

    public void onClick_Location(View v){
        /*if(!checkLocationPermission())
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
*/
        final GPSLocation myLoc = new GPSLocation();
        GPSLocation.LocationResult locationResult = new GPSLocation.LocationResult() {
            @Override
            public void gotLocation(Location location, Context context) {
                if(myLocation == null){
                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(myLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                }
                /*mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f), 10, null);*/
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17.0f));
            }
        };
        myLoc.init(this, locationResult);

    }


    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
