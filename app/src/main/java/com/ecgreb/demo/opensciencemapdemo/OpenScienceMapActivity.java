package com.ecgreb.demo.opensciencemapdemo;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.oscim.android.MapActivity;
import org.oscim.backend.AssetAdapter;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.theme.ThemeFile;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import java.io.InputStream;

public class OpenScienceMapActivity extends MapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_science_map);

        VectorTileLayer baseLayer = map().setBaseMap(new OSciMap4TileSource());
        map().layers().add(new BuildingLayer(map(), baseLayer));
        map().layers().add(new LabelLayer(map(), baseLayer));
        map().setTheme(Theme.DEFAULT);
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
