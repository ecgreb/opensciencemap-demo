package com.ecgreb.demo.opensciencemapdemo;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapzen.android.Pelias;
import com.mapzen.android.gson.Feature;
import com.mapzen.android.gson.Result;
import com.mapzen.android.lost.api.FusedLocationProviderApi;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LostApiClient;

import org.oscim.android.MapActivity;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.AssetAdapter;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.theme.ThemeFile;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OpenScienceMapActivity extends MapActivity {

    private FusedLocationProviderApi locationClient;
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

        new LostApiClient.Builder(this).build().connect();
        locationClient = LocationServices.FusedLocationApi;

        Location lastLocation = locationClient.getLastLocation();
        if (lastLocation != null) {
            setMapPositionWithZoom(lastLocation);
        }

        //trackMeBro();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(200);
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

        addMarkerLayer();

        final TextView search = (TextView) findViewById(R.id.search_box);
        final Button submit = (Button) findViewById(R.id.submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = locationClient.getLastLocation();
                String lat = Double.toString(location.getLatitude());
                String lng = Double.toString(location.getLongitude());

                Pelias.getPelias()
                        .suggest(search.getText().toString(), lat, lng, new Callback<Result>() {
                            @Override
                            public void success(Result result, Response response) {
                                List<Feature> features = result.getFeatures();

                                if (features.isEmpty()) {
                                    Log.d("Pelias", "no results");
                                    return;
                                }

                                for (Feature feature : features) {
                                    Log.d("Pelias", feature.getProperties().getText());
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.e("Pelias", "oh crap");
                            }
                        });
            }
        });

        final Button findMe = (Button) findViewById(R.id.find_me);
        findMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationClient.getLastLocation() != null) {
                    centerOn(locationClient.getLastLocation());
                }
            }
        });
    }

    private void centerOn(Location location) {
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        map().animator().animateTo(point);
    }

    private void trackMeBro() {
        locationClient.setMockMode(true);
        locationClient.setMockTrace(new File(Environment.getExternalStorageDirectory(),
                "ymca.gpx"));
    }

    private void addMarkerLayer() {
        ArrayList<MarkerItem> markers = new ArrayList<MarkerItem>(1);
        locationMarkerLayer = new ItemizedLayer<MarkerItem>(map(),
                markers, AndroidGraphics.makeMarker(getResources()
                .getDrawable(R.drawable.ic_locate_me), MarkerItem.HotspotPlace.CENTER), null);
        map().layers().add(locationMarkerLayer);
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
        MapPosition mapPosition = map().getMapPosition();
        mapPosition.setPosition(location.getLatitude(), location.getLongitude());
        map().setMapPosition(mapPosition);
        map().updateMap(true);
    }

    private void setMapPositionWithZoom(Location location) {
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
