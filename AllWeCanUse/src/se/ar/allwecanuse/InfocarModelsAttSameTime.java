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

public class InfocarModelsAttSameTime extends ARViewActivity {

	private MetaioSDKCallbackHandler mCallbackHandler;
	private IGeometry mMan;
	private IGeometry mEngine;
	
	private Vector2d mMidPoint;
	private GestureHandlerAndroid mGestureHandler;
	private int mGestureMask;

	private boolean mIsCloseToModel;

	/*
	 * This method is regularly called, calculates the distance between phone
	 * and target and performs actions based on the distance
	 */
	private void checkDistanceToTarget() {

		// get tracing values for COS 1
		final TrackingValues tv = metaioSDK.getTrackingValues(1);
		
		if (tv.isTrackingState()) {
			
			final Vector3d translation = tv.getTranslation();			
			final float distanceToTarget = translation.norm();			
			final float threshold = 800;

			// if we are already close to the model
			if (mIsCloseToModel) {
				// if our distance is larger than our threshold (+ a little)
				if (distanceToTarget > (threshold + 10)) {
					// we flip this variable again
					mIsCloseToModel = false;
					// and start the close_up animation
					mMan.startAnimation("close_up", false);
				}
			} else {
				// we're not close yet, let's check if we are now
				if (distanceToTarget < threshold) {
					// flip the variable
					mIsCloseToModel = true;
					// and start an animation
					mMan.startAnimation("close_down", false);
				}
			}

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mMidPoint = new Vector2d();
		mGestureMask = GestureHandler.GESTURE_ALL;
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);

		mCallbackHandler = new MetaioSDKCallbackHandler();
		mIsCloseToModel = false;

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
		// TODO: return 0 in case of no GUI overlay
		return R.layout.infocar_models_sametime;
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	@Override
	public void onDrawFrame() {
		super.onDrawFrame();

		try {
			checkDistanceToTarget();
		} catch (Exception e) {
		}
	}

	public void onButtonClick(View v) {
		finish();
	}

	@Override
	protected void loadContents() {
		try {

			// TODO: Load desired tracking data for planar marker tracking
//			File filepath = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarModelsAttSameTime/Assets/TrackingData_MarkerlessFast.xml");
			File filepath = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarModelsAttSameTime/Assets/TrackingData_PictureMarker.xml");
			boolean result = metaioSDK.setTrackingConfiguration(filepath);
			MetaioDebug.log("Tracking data loaded: " + result);

			// Load all the geometries
//			final File mManModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarModelsAttSameTime/Assets/TestBild/static_engine.md2");	
//			final File mManModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarModelsAttSameTime/Assets/TestBild/md2_animation_test.md2");
			final File mManModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarModelsAttSameTime/Assets/metaioman.md2");
			
			
			if (mManModel != null) {
				mMan = metaioSDK.createGeometry(mManModel);
				if (mMan != null) {
					// Set geometry properties
					mMan.setScale(4f);
					mMan.setCoordinateSystemID(1);
					mGestureHandler.addObject(mMan, 1); 
					// Start first animation
					mMan.startAnimation("idle", true);
					mMan.setAnimationSpeed(50f); 
					

				} else
					MetaioDebug.log(Log.ERROR, "Error loading geometry: " + mManModel);
			}
			
			final File mEngineModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "InfocarModelsAttSameTime/Assets/TestBild/rotating_engine_01.md2");			
			if(mEngineModel!= null){
				mEngine = metaioSDK.createGeometry(mEngineModel);
				if(mEngine != null){
					mEngine.setScale(new Vector3d(0.1f, 0.1f, 0.1f));
					mEngine.setCoordinateSystemID(2);
					mEngine.startAnimation("rotate", true);
					mEngine.setAnimationSpeed(60f); 
					mGestureHandler.addObject(mEngine, 2); 
					Log.d("mEngine ", "" + mEngineModel);
				}
			}
			

		} catch (Exception e) {
			MetaioDebug.log(Log.ERROR, "Error loading contents!");
			MetaioDebug.printStackTrace(Log.ERROR, e);
		}
	}

	@Override
	protected void onGeometryTouched(final IGeometry geometry) {		
		
			
			MetaioDebug.log("UnifeyeCallbackHandler.onGeometryTouched: " + geometry);
			
				geometry.startAnimation("rotate",true);
				geometry.startAnimation("shock_down", false);
			
				
						
	}
	

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {
		
		
		@Override
		public void onSDKReady() {
			runOnUiThread(new Runnable() {				
				public void run() {
					mGUIView.setVisibility(View.VISIBLE);					
				}
			});
		}
		

		@Override
		public void onAnimationEnd(IGeometry geometry, String animationName) {
			MetaioDebug.log("UnifeyeCallbackHandler.onAnimationEnd: " + animationName.toString());
			
			if(animationName.equalsIgnoreCase(animationName)){
				geometry.startAnimation("rotation", true);
			}
			
			if (animationName.equalsIgnoreCase("shock_down")) {
				geometry.startAnimation("shock_idle", false);				
			} 
			else if (animationName.equalsIgnoreCase("shock_idle")) {
				geometry.startAnimation("shock_up", false);
				
			} else if (animationName.equalsIgnoreCase("shock_up") || animationName.equalsIgnoreCase("close_up")) {
				
				if (mIsCloseToModel)
					geometry.startAnimation("close_idle", true);
				else
					geometry.startAnimation("idle", true);
				
			} else if (animationName.equalsIgnoreCase("close_down"))
				geometry.startAnimation("close_idle", true);

		}

		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
			for (int i = 0; i < trackingValues.size(); i++) {
				final TrackingValues v = trackingValues.get(i);
				MetaioDebug.log("Tracking state for COS " + v.getCoordinateSystemID() + " is " + v.getState());

			}
		}

	}

}
