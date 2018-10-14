package org.andresoviedo.app.model3D.demo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.andresoviedo.android_3d_model_engine.services.Object3DBuilder;
import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.app.model3D.view.ModelActivity;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class loads a 3D scene as an example of what can be done with the app
 * 
 * @author andresoviedo
 *
 */
public class ExampleSceneLoader extends SceneLoader {

	public ExampleSceneLoader(ModelActivity modelActivity) {
		super(modelActivity);
	}

	// TODO: fix this warning
	@SuppressLint("StaticFieldLeak")
    public void init() {
		super.init();
		new AsyncTask<Void, Void, Void>() {

			ProgressDialog dialog = new ProgressDialog(parent);
			List<Exception> errors = new ArrayList<>();

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog.setCancelable(false);
				dialog.setMessage("Loading demo...");
				dialog.show();
			}

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Set up ContentUtils so referenced materials and/or textures could be find
                    ContentUtils.setThreadActivity(parent);
                    ContentUtils.provideAssets(parent);

                    // test loading object
                    try {
                        String fileName;
                        for (int i = 11; i < 47; i++) {
                            fileName = new String("teeth" + i + ".obj");
                            addNewObject(fileName);
                        }
						addNewObject("gum_and_tongue.obj");
                    } catch (Exception ex) {
                        errors.add(ex);
                    }

                    } catch (Exception ex) {
                    errors.add(ex);
                } finally{
                    ContentUtils.setThreadActivity(null);
                    ContentUtils.clearDocumentsProvided();
                }
                return null;
            }

            private void addNewObject(String name) {
			    try {
                    Object3DData box11 = Object3DBuilder.loadV5(parent, Uri.parse("assets://assets/models/" + name));
                    float[] a = box11.getDimensions().getCenter3f();
                    box11.setColor(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
                    addObject(box11);
                } catch (Exception e) {
			        return;
                }
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				if (!errors.isEmpty()) {
					StringBuilder msg = new StringBuilder("There was a problem loading the data");
					for (Exception error : errors) {
						Log.e("Example", error.getMessage(), error);
						msg.append("\n" + error.getMessage());
					}
					Toast.makeText(parent.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
	}
}
