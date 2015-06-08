// Copyright 2007-2014 metaio GmbH. All rights reserved.
package se.ar.allwecanuse;

import java.io.File;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.EENV_MAP_FORMAT;
import com.metaio.sdk.jni.EPLAYBACK_STATUS;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.MovieTextureStatus;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.tools.io.AssetsManager;

public class InfocarChanging3DModels extends ARViewActivity {

	private IGeometry mMetaioMan;
	private IGeometry mBoat;
	private IGeometry mMoviePlane;
	private IGeometry mTruck;
	private int mSelectedModel;

	private Vector2d mMidPoint;
	private GestureHandlerAndroid mGestureHandler;
	private int mGestureMask;

	private MetaioSDKCallbackHandler mCallbackHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMetaioMan = null;
		mBoat = null;
		mMoviePlane = null;
		mTruck = null;

		mMidPoint = new Vector2d();
		mGestureMask = GestureHandler.GESTURE_ALL;
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);

		mCallbackHandler = new MetaioSDKCallbackHandler();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		super.onTouch(v, event);
		mGestureHandler.onTouch(v, event);
		return true;
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		super.onSurfaceChanged(width, height);

		// Update mid point of the view
		mMidPoint.setX(width / 2f);
		mMidPoint.setY(height / 2f);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCallbackHandler.delete();
		mCallbackHandler = null;
	}

	@Override
	protected int getGUILayout() {
		return R.layout.infocar_changing_models;
	}

	public void onButtonClick(View v) {
		finish();
	}

	public void onModelButtonClick(View v) {
		setActiveModel(0);
	}

	public void onImageButtonClick(View v) {
		setActiveModel(1);
	}

	public void onMovieButtonClick(View v) {
		setActiveModel(3);
	}

	public void onTruckButtonClick(View v) {
		setActiveModel(2);
	}

	@Override
	protected void loadContents() {
		try {
			// Load desired tracking data for planar marker tracking
			final File trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(),
											"InfocarChanging3DModels/Assets/TrackingData_MarkerlessFast.xml");

			final boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
			MetaioDebug.log("Tracking data loaded: " + result);

			// Load all the geometries. First - Model
			final File modelPath = AssetsManager.getAssetPathAsFile(getApplicationContext(),
																	"InfocarChanging3DModels/Assets/metaioman.md2");
			if (modelPath != null) {
				mMetaioMan = metaioSDK.createGeometry(modelPath);
				if (mMetaioMan != null) {
					// Set geometry properties
					mMetaioMan.setScale(4.0f);
					mGestureHandler.addObject(mMetaioMan, 1);
					MetaioDebug.log("Loaded geometry " + modelPath);
				} else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: " + modelPath);
				}
			}

			// Loading image geometry
			final File imagePath = AssetsManager.getAssetPathAsFile(getApplicationContext(),
																	"InfocarChanging3DModels/Assets/Boat/sailboat.zip");
			if (imagePath != null) {
				mBoat = metaioSDK.createGeometryFromImage(imagePath);
				if (mBoat != null) {
					mBoat.setScale(8.0f);
					mGestureHandler.addObject(mBoat, 2);
					MetaioDebug.log("Loaded geometry " + imagePath);
				} else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ imagePath);
				}
			}

			// Loading movie geometry
			final File moviePath = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarChanging3DModels/Assets/demo_movie.3g2");
			if (moviePath != null) {
				mMoviePlane = metaioSDK
						.createGeometryFromMovie(moviePath, true);
				if (mMoviePlane != null) {
					mMoviePlane.setScale(2.0f);
					mGestureHandler.addObject(mMoviePlane, 3);
					mMoviePlane.setRotation(new Rotation(0f, 0f,
							(float) -Math.PI / 2));
					MetaioDebug.log("Loaded geometry " + moviePath);
				} else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: " + moviePath);
				}
			}

			// loading truck geometry
			final File truckPath = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarChanging3DModels/Assets/truck/truck.obj");
			if (truckPath != null) {
				mTruck = metaioSDK.createGeometry(truckPath);
				if (mTruck != null) {
					mTruck.setScale(2.0f);
					mGestureHandler.addObject(mTruck, 4);
					mTruck.setRotation(new Rotation((float) Math.PI / 2, 0f,
							(float) Math.PI));
					MetaioDebug.log("Loaded geometry " + truckPath);
				} else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "
							+ truckPath);
				}
			}

			// Load environment map
			final File envMapPath = AssetsManager.getAssetPathAsFile(getApplicationContext(),"InfocarChanging3DModels/Assets/env_map.png");

			if (envMapPath != null) {
				final boolean loaded = metaioSDK.loadEnvironmentMap(envMapPath,	EENV_MAP_FORMAT.EEMF_LATLONG);
				MetaioDebug.log("Environment map loaded: " + loaded);
			} else {
				MetaioDebug.log(Log.ERROR, "Environment map not found at: " + envMapPath);
			}

			// Start by displaying metaio man (hide other models)
			setActiveModel(0);
		} catch (Exception e) {
			e.printStackTrace();
			MetaioDebug.log(Log.ERROR, "loadContents failed, see stack trace");
		}
	}

	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		if (geometry.equals(mMoviePlane)) {
			final MovieTextureStatus status = mMoviePlane.getMovieTextureStatus();
			if (status.getPlaybackStatus() == EPLAYBACK_STATUS.EPLAYBACK_STATUS_PLAYING) {
				mMoviePlane.pauseMovieTexture();
			} else {
				mMoviePlane.startMovieTexture(true);
			}
		}
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	private void setActiveModel(int modelIndex) {
		mSelectedModel = modelIndex;

		mMetaioMan.setVisible(modelIndex == 0);
		mBoat.setVisible(modelIndex == 1);
		mTruck.setVisible(modelIndex == 2);
		mMoviePlane.setVisible(modelIndex == 3);

		if (modelIndex != 3) {
			mMoviePlane.stopMovieTexture();
		}

		// Start or pause movie according to tracking state
		mCallbackHandler.onTrackingEvent(metaioSDK.getTrackingValues());
	}

	final private class MetaioSDKCallbackHandler extends IMetaioSDKCallback {
		@Override
		public void onSDKReady() {
			// show GUI after SDK is ready
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}

		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
			super.onTrackingEvent(trackingValues);

			// We only have one COS, so there can only ever be one
			// TrackingValues structure passed.
			// Play movie if the movie button was selected and we're currently
			// tracking.
			if (trackingValues.isEmpty()
					|| !trackingValues.get(0).isTrackingState()) {
				if (mMoviePlane != null) {
					mMoviePlane.pauseMovieTexture();
				}
			} else {
				if (mMoviePlane != null && mSelectedModel == 3) {
					mMoviePlane.startMovieTexture(true);
				}
			}
		}
	}
}
