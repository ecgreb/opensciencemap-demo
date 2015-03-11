package com.ecgreb.demo.opensciencemapdemo;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapzen.android.lost.LocationClient;
import com.mapzen.android.lost.LocationListener;
import com.mapzen.android.lost.LocationRequest;

import org.oscim.android.MapActivity;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.AssetAdapter;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.theme.ThemeFile;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import java.io.InputStream;
import java.util.ArrayList;

public class OpenScienceMapActivity extends MapActivity {

    private LocationClient locationClient;
    private ItemizedLayer<MarkerItem> locationMarkerLayer;
    private ArrayList<MarkerItem> markerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_science_map);

        VectorTileLayer baseLayer = map().setBaseMap(new OSciMap4TileSource());
        map().layers().add(new BuildingLayer(map(), baseLayer));
        map().layers().add(new LabelLayer(map(), baseLayer));
        map().setTheme(Theme.DEFAULT);

        locationClient = new LocationClient(this,
                new LocationClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Location lastLocation = locationClient.getLastLocation();
                        if (lastLocation != null) {
                            setMapPosition(lastLocation);
                        }

                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(1000);
                        locationClient.requestLocationUpdates(locationRequest,
                                new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        setMapPosition(location);
                                        if (locationMarkerLayer != null) {
                                            locationMarkerLayer.removeAllItems();
                                            locationMarkerLayer.addItem(getUserLocationMarker(location));
                                        }

                                        Log.d("OpenScienceMapDemo", location.toString());
                                    }
                                });
                    }

                    @Override
                    public void onDisconnected() {
                    }
                });

        locationClient.connect();
//        trackMeBro();
    }

    private void trackMeBro() {
        ArrayList<MarkerItem> markers = new ArrayList<MarkerItem>(1);
        locationMarkerLayer = new ItemizedLayer<MarkerItem>(map(),
                markers, AndroidGraphics.makeMarker(getResources()
                .getDrawable(R.drawable.ic_locate_me), MarkerItem.HotspotPlace.CENTER), null);
        map().layers().add(locationMarkerLayer);
        locationClient.setMockMode(true);
        locationClient.setMockTrace("ymca.gpx");
    }

    private MarkerItem getUserLocationMarker(Location location) {
        MarkerItem markerItem = new MarkerItem("Me", "current location",
                new GeoPoint(location.getLatitude(), location.getLongitude()));

        MarkerSymbol symbol = AndroidGraphics.makeMarker(
                getResources().getDrawable(R.drawable.ic_locate_me),
                MarkerItem.HotspotPlace.CENTER);

        markerItem.setMarker(symbol);
        return markerItem;
    }

    private void setMapPosition(Location location) {
        map().setMapPosition(location.getLatitude(), location.getLongitude(),
                Math.pow(2, map().getMapPosition().getZoomLevel()));
        map().updateMap(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_science_map, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public enum Theme implements ThemeFile {
        DEFAULT("styles/default.xml"),
        TRONRENDER("styles/tronrender.xml"),
        NEWTRON("styles/newtron.xml"),
        OSMARENDER("styles/osmarender.xml");

        private final String mPath;

        private Theme(String path) {
            mPath = path;
        }

        @Override
        public InputStream getRenderThemeAsStream() {
            return AssetAdapter.readFileAsStream(mPath);
        }
    }
}
