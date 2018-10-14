package org.andresoviedo.app.model3D.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.andresoviedo.app.model3D.demo.SceneLoader;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.dddmodel2.R;

import java.io.IOException;

/**
 * This activity represents the container for our 3D viewer.
 *
 * @author andresoviedo
 */
public class ModelActivity extends Activity {

    private static final int REQUEST_CODE_LOAD_TEXTURE = 1000;

    /**
     * Type of model if file name has no extension (provided though content provider)
     */
    private int paramType;
    /**
     * The file to load. Passed as input parameter
     */
    private Uri paramUri;
    /**
     * Enter into Android Immersive mode so the renderer is full screen or not
     */
    private boolean immersiveMode = true;
    /**
     * Background GL clear color. Default is light gray
     */
    private float[] backgroundColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    private ModelSurfaceView gLView;

    private SceneLoader scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get input parameters
        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.getString("uri") != null) {
                this.paramUri = Uri.parse(b.getString("uri"));
            }
            this.paramType = b.getString("type") != null ? Integer.parseInt(b.getString("type")) : -1;
            this.immersiveMode = "true".equalsIgnoreCase(b.getString("immersiveMode"));

        }
        Log.i("Renderer", "Params: uri '" + paramUri + "'");

        // Create our 3D sceneario

        scene = new SceneLoader(this);
        scene.init();

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = new ModelSurfaceView(this);
        setContentView(gLView);

//        // Show the Up button in the action bar.
//        setupActionBar();

        // TODO: Alert user when there is no multitouch support (2 fingers). He won't be able to rotate or zoom
        ContentUtils.printTouchCapabilities(getPackageManager());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.model_toggle_lights:
                scene.toggleLighting();
                break;
            case R.id.model_load_texture:
                Intent target = ContentUtils.createGetContentIntent("image/*");
                Intent intent = Intent.createChooser(target, "Select a file");
                try {
                    startActivityForResult(intent, REQUEST_CODE_LOAD_TEXTURE);
                } catch (ActivityNotFoundException e) {
                    // The reason for the existence of aFileChooser
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public Uri getParamUri() {
        return paramUri;
    }

    public float[] getBackgroundColor() {
        return backgroundColor;
    }

    public SceneLoader getScene() {
        return scene;
    }

    public ModelSurfaceView getGLView() {
        return gLView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_LOAD_TEXTURE:
                // The URI of the selected file
                final Uri uri = data.getData();
                if (uri != null) {
                    Log.i("ModelActivity", "Loading texture '" + uri + "'");
                    ContentUtils.setThreadActivity(this);
                }
        }
    }
}
