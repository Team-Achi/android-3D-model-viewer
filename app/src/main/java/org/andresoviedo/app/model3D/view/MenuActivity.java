package org.andresoviedo.app.model3D.view;

import android.Manifest;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader;
import org.andresoviedo.util.android.AndroidUtils;
import org.andresoviedo.util.android.AssetUtils;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.android.FileUtils;
import org.andresoviedo.util.view.TextActivity;
import org.andresoviedo.dddmodel2.R;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuActivity extends ListActivity {

    /**
     * Load file user data
     */
    private Map<String, Object> loadModelParameters = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setListAdapter(new ArrayAdapter<>(this, R.layout.activity_menu_item,
                getResources().getStringArray(R.array.menu_items)));

        launchModelRendererActivity();
    }

    private void launchModelRendererActivity() {
        Log.i("Menu", "Launching renderer for '"  + "'");
        Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
        intent.putExtra("uri", "");
        intent.putExtra("immersiveMode", "true");

        // content provider case
        if (!loadModelParameters.isEmpty()) {
            intent.putExtra("type", loadModelParameters.get("type").toString());
            loadModelParameters.clear();
        }

        startActivity(intent);
    }
}
