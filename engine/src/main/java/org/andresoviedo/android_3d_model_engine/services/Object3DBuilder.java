package org.andresoviedo.android_3d_model_engine.services;

import android.app.Activity;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import org.andresoviedo.android_3d_model_engine.model.AnimatedModel;
import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.collada.entities.Joint;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.FaceMaterials;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Faces;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Material;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Materials;
import org.andresoviedo.android_3d_model_engine.services.wavefront.WavefrontLoader.Tuple3;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.io.IOUtils;
import org.andresoviedo.util.math.Math3DUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public final class Object3DBuilder {

	private static final int COORDS_PER_VERTEX = 3;
	/**
	 * Default vertices colors
	 */
	private static float[] DEFAULT_COLOR = {1.0f, 1.0f, 0, 1.0f};

	final static float[] axisVertexLinesData = new float[]{
			//@formatter:off
			0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // right
			0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, // left
			0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, // up
			0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, // down
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, // z+
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, // z-

			0.95f, 0.05f, 0, 1, 0, 0, 0.95f, -0.05f, 0, 1, 0f, 0f, // Arrow X (>)
			-0.95f, 0.05f, 0, -1, 0, 0, -0.95f, -0.05f, 0, -1, 0f, 0f, // Arrow X (<)
			-0.05f, 0.95f, 0, 0, 1, 0, 0.05f, 0.95f, 0, 0, 1f, 0f, // Arrox Y (^)
			-0.05f, 0, 0.95f, 0, 0, 1, 0.05f, 0, 0.95f, 0, 0, 1, // Arrox z (v)

			1.05F, 0.05F, 0, 1.10F, -0.05F, 0, 1.05F, -0.05F, 0, 1.10F, 0.05F, 0, // Letter X
			-0.05F, 1.05F, 0, 0.05F, 1.10F, 0, -0.05F, 1.10F, 0, 0.0F, 1.075F, 0, // Letter Y
			-0.05F, 0.05F, 1.05F, 0.05F, 0.05F, 1.05F, 0.05F, 0.05F, 1.05F, -0.05F, -0.05F, 1.05F, -0.05F, -0.05F,
			1.05F, 0.05F, -0.05F, 1.05F // letter z
			//@formatter:on
	};

	final static float[] squarePositionData = new float[]{
			// @formatter:off
			-0.5f, 0.5f, 0.5f, // top left front
			-0.5f, -0.5f, 0.5f, // bottom left front
			0.5f, -0.5f, 0.5f, // bottom right front
			0.5f, 0.5f, 0.5f, // upper right front
			-0.5f, 0.5f, -0.5f, // top left back
			-0.5f, -0.5f, -0.5f, // bottom left back
			0.5f, -0.5f, -0.5f, // bottom right back
			0.5f, 0.5f, -0.5f // upper right back
			// @formatter:on
	};

	final static int[] squareDrawOrderData = new int[]{
			// @formatter:off
			// front
			0, 1, 2,
			0, 2, 3,
			// back
			7, 6, 5,
			4, 7, 5,
			// up
			4, 0, 3,
			7, 4, 3,
			// bottom
			1, 5, 6,
			2, 1, 6,
			// left
			4, 5, 1,
			0, 4, 1,
			// right
			3, 2, 6,
			7, 3, 6
			// @formatter:on
	};

	final static float[] cubePositionData = {
			//@formatter:off
			// Front face
			-1.0f, 1.0f, 1.0f,
			-1.0f, -1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			-1.0f, -1.0f, 1.0f,
			1.0f, -1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,

			// Right face
			1.0f, 1.0f, 1.0f,
			1.0f, -1.0f, 1.0f,
			1.0f, 1.0f, -1.0f,
			1.0f, -1.0f, 1.0f,
			1.0f, -1.0f, -1.0f,
			1.0f, 1.0f, -1.0f,

			// Back face
			1.0f, 1.0f, -1.0f,
			1.0f, -1.0f, -1.0f,
			-1.0f, 1.0f, -1.0f,
			1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, 1.0f, -1.0f,

			// Left face
			-1.0f, 1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, 1.0f, 1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, 1.0f,
			-1.0f, 1.0f, 1.0f,

			// Top face
			-1.0f, 1.0f, -1.0f,
			-1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, -1.0f,
			-1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, -1.0f,

			// Bottom face
			1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, 1.0f,
			-1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, 1.0f,
			-1.0f, -1.0f, 1.0f,
			-1.0f, -1.0f, -1.0f
	};

	final static float[] cubeColorData = {

			// Front face (red)
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,

			// Right face (green)
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,

			// Back face (blue)
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,

			// Left face (yellow)
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f,

			// Top face (cyan)
			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f, 1.0f,

			// Bottom face (magenta)
			1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 0.0f, 1.0f, 1.0f
	};

	final static float[] cubeNormalData =
			{
					// Front face
					0.0f, 0.0f, 1.0f,
					0.0f, 0.0f, 1.0f,
					0.0f, 0.0f, 1.0f,
					0.0f, 0.0f, 1.0f,
					0.0f, 0.0f, 1.0f,
					0.0f, 0.0f, 1.0f,

					// Right face
					1.0f, 0.0f, 0.0f,
					1.0f, 0.0f, 0.0f,
					1.0f, 0.0f, 0.0f,
					1.0f, 0.0f, 0.0f,
					1.0f, 0.0f, 0.0f,
					1.0f, 0.0f, 0.0f,

					// Back face
					0.0f, 0.0f, -1.0f,
					0.0f, 0.0f, -1.0f,
					0.0f, 0.0f, -1.0f,
					0.0f, 0.0f, -1.0f,
					0.0f, 0.0f, -1.0f,
					0.0f, 0.0f, -1.0f,

					// Left face
					-1.0f, 0.0f, 0.0f,
					-1.0f, 0.0f, 0.0f,
					-1.0f, 0.0f, 0.0f,
					-1.0f, 0.0f, 0.0f,
					-1.0f, 0.0f, 0.0f,
					-1.0f, 0.0f, 0.0f,

					// Top face
					0.0f, 1.0f, 0.0f,
					0.0f, 1.0f, 0.0f,
					0.0f, 1.0f, 0.0f,
					0.0f, 1.0f, 0.0f,
					0.0f, 1.0f, 0.0f,
					0.0f, 1.0f, 0.0f,

					// Bottom face
					0.0f, -1.0f, 0.0f,
					0.0f, -1.0f, 0.0f,
					0.0f, -1.0f, 0.0f,
					0.0f, -1.0f, 0.0f,
					0.0f, -1.0f, 0.0f,
					0.0f, -1.0f, 0.0f
			};


	final static float[] cubeTextureCoordinateData =
			{
					// Front face
					0.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f,

					// Right face
					0.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f,

					// Back face
					0.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f,

					// Left face
					0.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f,

					// Top face
					0.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f,

					// Bottom face
					0.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 0.0f,
					0.0f, 1.0f,
					1.0f, 1.0f,
					1.0f, 0.0f
			};
	//@formatter:on

	public static Object3DData buildPoint(float[] point) {
		return new Object3DData(createNativeByteBuffer(point.length * 4).asFloatBuffer().put(point))
				.setDrawMode(GLES20.GL_POINTS).setId("Point");
	}

	public static Object3DData buildLine(float[] line) {
		return new Object3DData(createNativeByteBuffer(line.length * 4).asFloatBuffer().put(line))
				.setDrawMode(GLES20.GL_LINES).setId("Line").setFaces(new Faces(0));
	}

	public static Object3DData buildAxis() {
		return new Object3DData(
				createNativeByteBuffer(axisVertexLinesData.length * 4).asFloatBuffer().put(axisVertexLinesData))
				.setDrawMode(GLES20.GL_LINES).setFaces(new Faces(0));
	}

	public static Object3DData buildCubeV1() {
		return new Object3DData(
				createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData))
				.setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV1").centerAndScale(1.0f).setFaces(new Faces(8));
	}

	public static Object3DData buildCubeV1_with_normals() {
		return new Object3DData(
				createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData))
				.setVertexColorsArrayBuffer(
						createNativeByteBuffer(cubeColorData.length * 4).asFloatBuffer().put(cubeColorData))
				.setVertexNormalsArrayBuffer(
						createNativeByteBuffer(cubeNormalData.length * 4).asFloatBuffer().put(cubeNormalData))
				.setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV1_light").centerAndScale(1.0f).setFaces(new Faces(8));
	}

	public static Object3DData buildSquareV2() {
		IntBuffer drawBuffer = createNativeByteBuffer(squareDrawOrderData.length * 4).asIntBuffer().put(squareDrawOrderData);
		FloatBuffer vertexBuffer = createNativeByteBuffer(squarePositionData.length * 4).asFloatBuffer().put(squarePositionData);
		return new Object3DData(vertexBuffer,drawBuffer.asReadOnlyBuffer()).setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV2")
				.centerAndScale(1.0f).setFaces(new Faces(8)).setDrawOrder(drawBuffer).setVertexArrayBuffer(vertexBuffer);
	}

	public static Object3DData buildCubeV3(byte[] textureData) {
		return new Object3DData(
				createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData),
				createNativeByteBuffer(cubeTextureCoordinateData.length * 4).asFloatBuffer()
						.put(cubeTextureCoordinateData).asReadOnlyBuffer(),
				textureData).setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV3").centerAndScale(1.0f).setFaces(new Faces(8));
	}

	public static Object3DData buildCubeV4(byte[] textureData) {
		return new Object3DData(
				createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData),
				createNativeByteBuffer(cubeColorData.length * 4).asFloatBuffer().put(cubeColorData).asReadOnlyBuffer(),
				createNativeByteBuffer(cubeTextureCoordinateData.length * 4).asFloatBuffer()
						.put(cubeTextureCoordinateData).asReadOnlyBuffer(),
				textureData).setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV4").centerAndScale(1.0f).setFaces(new Faces(8));
	}

	public static Object3DData loadV5(Activity activity, Uri modelUri) {
		try {
			//final String modelId = assetDir + "/" + assetFilename;

			InputStream is = new URL(modelUri.toString()).openStream();
			WavefrontLoader wfl = new WavefrontLoader(modelUri.toString());
			wfl.analyzeModel(is);
			is.close();

			wfl.allocateBuffers();

			is = new URL(modelUri.toString()).openStream();
			wfl.loadModel(is);
			is.close();

			Object3DData data3D = new Object3DData(wfl.getVerts(), wfl.getNormals(), wfl.getTexCoords(), wfl.getFaces(),
					wfl.getFaceMats(), wfl.getMaterials());
			data3D.setId(modelUri.toString());
			data3D.setUri(modelUri);
			// data3D.setAssetsDir(assetDir);
			data3D.setDimensions(wfl.getDimensions());
			//data3D.centerAndScale(5,new float[]{0,0,0});
			data3D.centerScale();

			data3D.setDrawMode(GLES20.GL_TRIANGLES);
			generateArrays(data3D);

			return data3D;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Object3DData generateArrays(Object3DData obj) throws IOException {

	    Log.i("Object3DBuilder","Generating arrays for "+obj.getId());

		Faces faces = obj.getFaces(); // model faces
		FaceMaterials faceMats = obj.getFaceMats();
		Materials materials = obj.getMaterials();

		if (faces == null)  {
			Log.i("Object3DBuilder", "No faces. Not generating arrays");
			return obj;
		}

		Log.i("Object3DBuilder", "Allocating vertex array buffer... Vertices ("+faces.getVerticesReferencesCount()+")");
		final FloatBuffer vertexArrayBuffer = createNativeByteBuffer(faces.getVerticesReferencesCount() * 3 * 4).asFloatBuffer();
		obj.setVertexArrayBuffer(vertexArrayBuffer);
		obj.setDrawUsingArrays(true);

		Log.i("Object3DBuilder", "Populating vertex array...");
		final FloatBuffer vertexBuffer = obj.getVerts();
		final IntBuffer indexBuffer = faces.getIndexBuffer();
		for (int i = 0; i < faces.getVerticesReferencesCount(); i++) {
			vertexArrayBuffer.put(i*3,vertexBuffer.get(indexBuffer.get(i) * 3));
			vertexArrayBuffer.put(i*3+1,vertexBuffer.get(indexBuffer.get(i) * 3 + 1));
			vertexArrayBuffer.put(i*3+2,vertexBuffer.get(indexBuffer.get(i) * 3 + 2));
		}

		Log.i("Object3DBuilder", "Allocating vertex normals buffer... Total normals ("+faces.facesNormIdxs.size()+")");
		// Normals buffer size = Number_of_faces X 3 (vertices_per_face) X 3 (coords_per_normal) X 4 (bytes_per_float)
		final FloatBuffer vertexNormalsArrayBuffer = createNativeByteBuffer(faces.getSize() * 3 * 3 * 4).asFloatBuffer();;
		obj.setVertexNormalsArrayBuffer(vertexNormalsArrayBuffer);

		// build file normals
		final FloatBuffer vertexNormalsBuffer = obj.getNormals();
		if (vertexNormalsBuffer != null && vertexNormalsBuffer.capacity() > 0) {
			Log.i("Object3DBuilder", "Populating normals buffer...");
			for (int n=0; n<faces.facesNormIdxs.size(); n++) {
				int[] normal = faces.facesNormIdxs.get(n);
				for (int i = 0; i < normal.length; i++) {
					vertexNormalsArrayBuffer.put(n*9+i*3,vertexNormalsBuffer.get(normal[i] * 3));
					vertexNormalsArrayBuffer.put(n*9+i*3+1,vertexNormalsBuffer.get(normal[i] * 3 + 1));
					vertexNormalsArrayBuffer.put(n*9+i*3+2,vertexNormalsBuffer.get(normal[i] * 3 + 2));
				}
			}
		} else {
			// calculate normals for all triangles
			Log.i("Object3DBuilder", "Model without normals. Calculating [" + faces.getIndexBuffer().capacity() / 3 + "] normals...");

			final float[] v0 = new float[3], v1 = new float[3], v2 = new float[3];
			for (int i = 0; i < faces.getIndexBuffer().capacity(); i += 3) {
				try {
					v0[0] = vertexBuffer.get(faces.getIndexBuffer().get(i) * 3);
					v0[1] = vertexBuffer.get(faces.getIndexBuffer().get(i) * 3 + 1);
					v0[2] = vertexBuffer.get(faces.getIndexBuffer().get(i) * 3 + 2);

					v1[0] = vertexBuffer.get(faces.getIndexBuffer().get(i + 1) * 3);
					v1[1] = vertexBuffer.get(faces.getIndexBuffer().get(i + 1) * 3 + 1);
					v1[2] = vertexBuffer.get(faces.getIndexBuffer().get(i + 1) * 3 + 2);

					v2[0] = vertexBuffer.get(faces.getIndexBuffer().get(i + 2) * 3);
					v2[1] = vertexBuffer.get(faces.getIndexBuffer().get(i + 2) * 3 + 1);
					v2[2] = vertexBuffer.get(faces.getIndexBuffer().get(i + 2) * 3 + 2);

					float[] normal = Math3DUtils.calculateFaceNormal2(v0, v1, v2);

					vertexNormalsArrayBuffer.put(i*3,normal[0]);
					vertexNormalsArrayBuffer.put(i*3+1,normal[1]);
					vertexNormalsArrayBuffer.put(i*3+2,normal[2]);
					vertexNormalsArrayBuffer.put(i*3+3,normal[0]);
					vertexNormalsArrayBuffer.put(i*3+4,normal[1]);
					vertexNormalsArrayBuffer.put(i*3+5,normal[2]);
					vertexNormalsArrayBuffer.put(i*3+6,normal[0]);
					vertexNormalsArrayBuffer.put(i*3+7,normal[1]);
					vertexNormalsArrayBuffer.put(i*3+8,normal[2]);
				} catch (BufferOverflowException ex) {
					throw new RuntimeException("Error calculating normal for face ["+i/3+"]");
				}
			}
		}


		FloatBuffer colorArrayBuffer = null;
		if (materials != null) {
			Log.i("Object3DBuilder", "Reading materials...");
			try(InputStream inputStream = ContentUtils.getInputStream(materials.mfnm)) {
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				materials.readMaterials(br);
				materials.showMaterials();
				br.close();
			} catch (Exception ex){
			    Log.e("Object3DBuilder","Couldn't load material file "+materials.mfnm+". "+ex.getMessage(), ex);
			    obj.addError(materials.mfnm+":"+ex.getMessage());
            }
		}

		if (materials != null && !faceMats.isEmpty()) {
			Log.i("Object3DBuilder", "Processing face materials...");
			colorArrayBuffer = createNativeByteBuffer(4 * faces.getVerticesReferencesCount() * 4)
					.asFloatBuffer();
			boolean anyOk = false;
			float[] currentColor = DEFAULT_COLOR;
			for (int i = 0; i < faces.getSize(); i++) {
				if (faceMats.findMaterial(i) != null) {
					Material mat = materials.getMaterial(faceMats.findMaterial(i));
					if (mat != null) {
						currentColor = mat.getKdColor() != null ? mat.getKdColor() : currentColor;
						anyOk = anyOk || mat.getKdColor() != null;
					}
				}
				colorArrayBuffer.put(currentColor);
				colorArrayBuffer.put(currentColor);
				colorArrayBuffer.put(currentColor);
			}
			if (!anyOk) {
				Log.i("Object3DBuilder", "Using single color.");
				colorArrayBuffer = null;
			}
		}
		obj.setVertexColorsArrayBuffer(colorArrayBuffer);


		String texture = null;
		byte[] textureData = null;
		if (materials != null && !materials.materials.isEmpty()) {

			// TODO: process all textures
			for (Material mat : materials.materials.values()) {
				if (mat.getTexture() != null) {
					texture = mat.getTexture();
					break;
				}
			}
			if (texture != null) {
			    Log.i("Object3DBuilder","Loading texture "+texture);
			    InputStream is = ContentUtils.getInputStream(texture);
			    if (is != null) {
                    textureData = IOUtils.read(is);
                    is.close();
                } else {
			        obj.addError("Texture '"+texture+"' not found in provided files");
                }
			} else {
				Log.i("Object3DBuilder", "Found material(s) but no texture");
			}
		} else{
			Log.i("Object3DBuilder", "No materials -> No texture");
		}


		//if (textureData != null) {
			ArrayList<Tuple3> texCoords = obj.getTexCoords();
			if (texCoords != null && texCoords.size() > 0) {

				Log.i("Object3DBuilder", "Allocating/populating texture buffer (flipTexCoord:"+obj.isFlipTextCoords()+")...");
				FloatBuffer textureCoordsBuffer = createNativeByteBuffer(texCoords.size() * 2 * 4).asFloatBuffer();
				for (Tuple3 texCor : texCoords) {
					textureCoordsBuffer.put(texCor.getX());
					textureCoordsBuffer.put(obj.isFlipTextCoords() ? 1 - texCor.getY() : texCor.getY());
				}

				Log.i("Object3DBuilder", "Populating texture array buffer...");
				FloatBuffer textureCoordsArraysBuffer = createNativeByteBuffer(2 * faces.getVerticesReferencesCount() * 4).asFloatBuffer();
				obj.setTextureCoordsArrayBuffer(textureCoordsArraysBuffer);

				try {

					boolean anyTextureOk = false;
					String currentTexture = null;

					Log.i("Object3DBuilder", "Populating texture array buffer...");
					int counter = 0;
					for (int i = 0; i < faces.facesTexIdxs.size(); i++) {

						// get current texture
						if (!faceMats.isEmpty() && faceMats.findMaterial(i) != null) {
							Material mat = materials.getMaterial(faceMats.findMaterial(i));
							if (mat != null && mat.getTexture() != null) {
								currentTexture = mat.getTexture();
							}
						}

						// check if texture is ok (Because we only support 1 texture currently)
						boolean textureOk = false;
						if (currentTexture != null && currentTexture.equals(texture)) {
							textureOk = true;
						}

						// populate texture coords if ok (in case we have more than 1 texture and 1 is missing. see face.obj example)
						int[] text = faces.facesTexIdxs.get(i);
						for (int j = 0; j < text.length; j++) {
							if (textureData == null || textureOk) {
								if (text[j] * 2 >= 0 && text[j] * 2 < textureCoordsBuffer.limit()) {
									anyTextureOk = true;
									textureCoordsArraysBuffer.put(counter++, textureCoordsBuffer.get(text[j] * 2));
									textureCoordsArraysBuffer.put(counter++, textureCoordsBuffer.get(text[j] * 2 + 1));
								} else{
									Log.v("Object3DBuilder","Wrong texture for face "+i);
									textureCoordsArraysBuffer.put(counter++, 0f);
									textureCoordsArraysBuffer.put(counter++, 0f);
								}
							} else {
								textureCoordsArraysBuffer.put(counter++, 0f);
								textureCoordsArraysBuffer.put(counter++, 0f);
							}
						}
					}

					if (!anyTextureOk) {
						Log.i("Object3DBuilder", "Texture is wrong. Applying global texture");
						counter = 0;
						for (int j=0; j<faces.facesTexIdxs.size(); j++) {
							int[] text = faces.facesTexIdxs.get(j);
							for (int i = 0; i < text.length; i++) {
								textureCoordsArraysBuffer.put(counter++, textureCoordsBuffer.get(text[i] * 2));
								textureCoordsArraysBuffer.put(counter++, textureCoordsBuffer.get(text[i] * 2 + 1));
							}
						}
					}
				} catch (Exception ex) {
					Log.e("Object3DBuilder", "Failure to load texture coordinates", ex);
				}
			}
		//}
		obj.setTextureData(textureData);

		return obj;
	}

	private static ByteBuffer createNativeByteBuffer(int length) {
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(length);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());
		return bb;
	}
}


