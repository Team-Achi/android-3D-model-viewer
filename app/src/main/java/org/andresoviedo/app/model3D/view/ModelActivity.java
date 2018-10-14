package org.andresoviedo.app.model3D.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.andresoviedo.app.model3D.demo.SceneLoader;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.dddmodel2.R;

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

        // Show the Up button in the action bar.
//        setupActionBar();

        // TODO: Alert user when there is no multitouch support (2 fingers). He won't be able to rotate or zoom
        ContentUtils.printTouchCapabilities(getPackageManager());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.model_toggle_lights:
//                scene.toggleLighting();
//                break;
//            case R.id.model_load_texture:
//                Intent target = ContentUtils.createGetContentIntent("image/*");
//                Intent intent = Intent.createChooser(target, "Select a file");
//                try {
//                    startActivityForResult(intent, REQUEST_CODE_LOAD_TEXTURE);
//                } catch (ActivityNotFoundException e) {
//                    // The reason for the existence of aFileChooser
//                }
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)

    private void setupActionBar() {

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

        // getActionBar().setDisplayHomeAsUpEnabled(true);

        // }

    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.model, menu);

        return true;

    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    private void setupOnSystemVisibilityChangeListener() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {

            return;

        }

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {

            // Note that system bars will only be "visible" if none of the

            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.

        });

    }



    @Override

    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {


        }

    }

    // This snippet hides the system bars.

    @TargetApi(Build.VERSION_CODES.KITKAT)

    private void hideSystemUIKitKat() {

        // Set the IMMERSIVE flag.

        // Set the content to appear under the system bars so that the content

        // doesn't resize when the system bars hide and show.

        final View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    private void hideSystemUIJellyBean() {

        final View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);

    }



    // This snippet shows the system bars. It does this by removing all the flags

    // except for the ones that make the content appear under the system bars.

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    private void showSystemUI() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {

            return;

        }

        final View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

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
