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
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

public class InfocarChangingTrackers extends ARViewActivity {

	/**
	 * Reference to loaded metaioman geometry
	 */
	private IGeometry mBoat;
	
	private GestureHandlerAndroid mGestureHandler;
	private int mGestureMask;
	private Vector2d mMidlePoint;

	/**
	 * Currently loaded tracking configuration file
	 */
	File trackingConfigFile;

	private MetaioSDKCallbackHandler mCallbackHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mMidlePoint = new Vector2d();
		mGestureMask = GestureHandler.GESTURE_ALL;
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);

		mCallbackHandler = new MetaioSDKCallbackHandler();
	}
	
	
	@Override
	public void onSurfaceChanged(int width, int height) {
		super.onSurfaceChanged(width, height);

		// Update mid point of the view
		mMidlePoint.setX(width / 2f);
		mMidlePoint.setY(height / 2f);
	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCallbackHandler.delete();
		mCallbackHandler = null;
	}

	@Override
	protected int getGUILayout() {
		return R.layout.infocar_changing_trackers;
	}

	public void onButtonClick(View v) {
		finish();
	}

	public void onIdButtonClick(View v) {
		trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(),"InfocarChangingTrackers/Assets/TrackingData_Marker.xml");
		MetaioDebug.log("Tracking Config path = " + trackingConfigFile);

		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
		MetaioDebug.log("Id Marker tracking data loaded: " + result);
		mBoat.setScale(10f);
	}

	public void onPictureButtonClick(View v) {
		trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(),"InfocarChangingTrackers/Assets/TrackingData_PictureMarker.xml");
		MetaioDebug.log("Tracking Config path = " + trackingConfigFile);

		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
		MetaioDebug.log("Picture Marker tracking data loaded: " + result);
		mBoat.setScale(8f);

	}

	public void onMarkerlessButtonClick(View v) {
		trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(),"InfocarChangingTrackers/Assets/TrackingData_MarkerlessFast.xml");
		MetaioDebug.log("Tracking Config path = " + trackingConfigFile);

		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
		MetaioDebug.log("Markerless tracking data loaded: " + result);
		mBoat.setScale(8f);
	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		super.onTouch(v, event);

		mGestureHandler.onTouch(v, event);
		return true;
	}
	
	

	@Override
	protected void loadContents() {
		try {

			// Load the desired tracking configuration
			trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(),"InfocarChangingTrackers/Assets/TrackingData_MarkerlessFast.xml");
			final boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
			MetaioDebug.log("Tracking configuration loaded: " + result);

			// Load all the geometries. First - Model
			final File metaioManModel = AssetsManager.getAssetPathAsFile(getApplicationContext(),"InfocarChangingTrackers/Assets/Boat/sailboat.zip");
			if (metaioManModel != null) {
				mBoat = metaioSDK.createGeometry(metaioManModel);
				if (mBoat != null) {
					// Set geometry properties
					mBoat.setScale(8f);
					mGestureHandler.addObject(mBoat, 1);
					MetaioDebug.log("Loaded geometry " + metaioManModel);
				} else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: " + metaioManModel);
			}

		} catch (Exception e) {
			MetaioDebug.log(Log.ERROR, "Error loading contents!");
			MetaioDebug.printStackTrace(Log.ERROR, e);
		}
	}

	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {

		@Override
		public void onSDKReady() {
			// show GUI
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}

		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
			// if we detect any target, we bind the loaded geometry to this
			// target
			if (mBoat != null) {
				for (int i = 0; i < trackingValues.size(); i++) {
					final TrackingValues tv = trackingValues.get(i);
					if (tv.isTrackingState()) {
						mBoat.setCoordinateSystemID(tv.getCoordinateSystemID());
						break;
					}
				}
			}

		}
	}

}
