package com.bullzer.cordova.emdkscanner;

import android.util.Log;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class ZebraScanner extends CordovaPlugin implements Serializable, EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener, BarcodeManager.ScannerConnectionListener {

	private static final String LOG_TAG = "ZebraScanner";

	private EMDKManager mEmdkManager = null;
	private Scanner mScanner = null;
	private CallbackContext initialisationCallbackContext = null;
	private CallbackContext scanCallbackContext = null;
	private BarcodeManager barcodeManager = null;

	public ZebraScanner() {}

	@Override
	public void onDestroy() {
		Log.i(LOG_TAG, "onDestroy");

		// De-initialize mScanner
		deInitScanner();

		// Remove connection listener
		if (barcodeManager != null) {
			barcodeManager.removeConnectionListener(this);
			barcodeManager = null;
		}

		// Release all the resources
		if (mEmdkManager != null) {
			mEmdkManager.release();
			mEmdkManager = null;
		}
	}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		Log.d(LOG_TAG, "JS-Action: " + action);

		if (action.equals("init")) {
			if (mScanner != null && mScanner.isEnabled()) {
				callbackContext.success();
			} else {
				final ZebraScanner myZebraScanner = this;
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						initialisationCallbackContext = callbackContext;

						try {
							EMDKResults results = EMDKManager.getEMDKManager(cordova.getActivity().getApplicationContext(), myZebraScanner);
							if(results != null) {
								if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
									Log.i(LOG_TAG, "EMDK manager has been successfully created");
									callbackContext.success();
								} else {
									Log.w(LOG_TAG, "Some error has occurred creating the EMDK manager.  EMDK functionality will not be available");
									FailureCallback(callbackContext, "Creating the EMDK manager failed");
								}
							}else {
								FailureCallback(callbackContext, "Creating the EMDK manager failed");
							} 
							
						} catch (NoClassDefFoundError e) {
							Log.w(LOG_TAG, "EMDK is not available on this device");
							FailureCallback(callbackContext, "EMDK is not available on this device");
						}
					}
				});
			}
		}

		else if (action.equalsIgnoreCase("startScanning")) {
			Log.d(LOG_TAG, "Start Scanning");
			cordova.getThreadPool().execute(new Runnable() {
				public void run() { StartScanning(callbackContext); }
			});
		}

		else if (action.equalsIgnoreCase("stopScanning")) {
			Log.d(LOG_TAG, "Stop Scanning");
			cordova.getThreadPool().execute(new Runnable() {
				public void run() {
					StopScanning();
				}
			});
		}

		else {
			return false;
		}

		return true;
	}


	@Override
	public void onOpened(EMDKManager emdkManager) {
		Log.i(LOG_TAG, "onOpened");

		if (mScanner == null || !mScanner.isEnabled()) {
			Log.i(LOG_TAG, "Initializing");

			this.mEmdkManager = emdkManager;

			// Acquire the barcode manager resources
			barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);

			// Add connection listener
			if (barcodeManager != null) {
				barcodeManager.addConnectionListener(this);
			}
		} else {
			Log.i(LOG_TAG, "Already initialized");
		}
	}

	@Override
	public void onClosed() {
		Log.i(LOG_TAG, "onClosed");

		if (mEmdkManager != null) {

			// Remove connection listener
			if (barcodeManager != null){
				barcodeManager.removeConnectionListener(this);
				barcodeManager = null;
			}

			// Release all the resources
			mEmdkManager.release();
			mEmdkManager = null;
		}
	}

	private void StartScanning(CallbackContext callbackContext) {
		Log.e(LOG_TAG, "StartScanning");

		if(mScanner == null) {
			initScanner();
		}

		if (mScanner != null) {
			try {

				if(mScanner.isEnabled())
				{
					scanCallbackContext = callbackContext;

					// Submit a new read.
					mScanner.read();

				}
				else
				{
					Log.e(LOG_TAG, "error: " + "Scanner is not enabled");
					FailureCallback(callbackContext, "Scanner is not enabled");
				}

			} catch (ScannerException e) {
				Log.e(LOG_TAG, "error: " + e.getMessage());
				FailureCallback(callbackContext, e.getMessage());
			}
		}

	}

	private void StopScanning() {
		Log.e(LOG_TAG, "Stop Scanning");
		scanCallbackContext = null;
		if (mScanner != null) {
			try {
				mScanner.cancelRead();
			} catch (ScannerException e) {
				Log.e(LOG_TAG, "Error stopping read");
			}
		}
	}

	private void initScanner() {
		Log.i(LOG_TAG, "Init Scanner");

		if (mScanner == null) {

			mScanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);


			if (mScanner != null) {

				mScanner.addDataListener(this);
				mScanner.addStatusListener(this);

				mScanner.triggerType = Scanner.TriggerType.HARD;


				try {
					mScanner.enable();
				} catch (ScannerException e) {
					Log.e(LOG_TAG, e.getMessage());
					FailureCallback(initialisationCallbackContext, e.getMessage());
				}
			}else{
				Log.e(LOG_TAG, "Failed to initialize the Scanner device.");
				FailureCallback(initialisationCallbackContext, "Failed to initialize the Scanner device.");
			}
		}
	}

	private void deInitScanner() {
		Log.i(LOG_TAG, "De-Init Scanner");

		if (mScanner != null) {

			try {

				mScanner.cancelRead();


			} catch (ScannerException e) {

				Log.e(LOG_TAG, e.getMessage());
			}
			mScanner.removeDataListener(this);
			mScanner.removeStatusListener(this);
			try{
				mScanner.release();
			} catch (ScannerException e) {

				Log.e(LOG_TAG, e.getMessage());

			}

			mScanner = null;
		}
	}

	@Override
	public void onData(ScanDataCollection scanDataCollection) {
		if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
			ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
			if (scanData.size() > 0) {
				Log.d(LOG_TAG, "Data scanned: " + scanData.get(0).getData());
				String scanDataResponse = scanData.get(0).getData();
				PluginResult result = new PluginResult(PluginResult.Status.OK, scanDataResponse);
				result.setKeepCallback(true);
				scanCallbackContext.sendPluginResult(result);
			}
		}
	}

	@Override
	public void onStatus(StatusData statusData) {
		StatusData.ScannerStates state = statusData.getState();
		Log.d(LOG_TAG, "onStatus Change: " + state);
		if (state.equals(StatusData.ScannerStates.IDLE) && scanCallbackContext != null) {
			try {

				Thread.sleep(500);

				if(!mScanner.isReadPending()){
					mScanner.read();
				}

			} catch (ScannerException e) {
				Log.e(LOG_TAG, "Error re-initializing read: " + e.getMessage());
				FailureCallback(scanCallbackContext, "Error re-initializing read: " + e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPause(boolean multitasking) {
		Log.i(LOG_TAG, "onPause");

		// The application is in background

		// De-initialize mScanner
		deInitScanner();

		// Remove connection listener
		if (barcodeManager != null) {
			barcodeManager.removeConnectionListener(this);
			barcodeManager = null;
		}

		// Release the barcode manager resources
		if (mEmdkManager != null) {
			mEmdkManager.release(EMDKManager.FEATURE_TYPE.BARCODE);
		}
	}

	@Override
	public void onResume(boolean multitasking) {
		Log.i(LOG_TAG, "onResume");

		// The application is in foreground

		// Acquire the barcode manager resources
		if (mEmdkManager != null) {
			barcodeManager = (BarcodeManager) mEmdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);

			// Add connection listener
			if (barcodeManager != null) {
				barcodeManager.addConnectionListener(this);
			}

			// Initialize mScanner
			initScanner();
		}
	}

	private void FailureCallback(CallbackContext callbackContext, String message) {
		if (callbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);
		}
	}


	@Override
	public void onConnectionChange(ScannerInfo scannerInfo, BarcodeManager.ConnectionState connectionState) {

	}

}
