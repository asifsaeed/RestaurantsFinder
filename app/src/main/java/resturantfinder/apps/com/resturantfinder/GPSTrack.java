package resturantfinder.apps.com.resturantfinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;


@SuppressWarnings("unused")
@SuppressLint("Registered")
public class GPSTrack extends Service implements LocationListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationSource{
	private GoogleApiClient mGoogleApiClient;
	private OnLocationChangedListener mMapLocationListener = null;
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	private final Context mContext;
	// flag for GPS status
	private boolean isGPS = false;
	// flag for network status
	private boolean isNetwork = false;
	// flag for GPS status
	private boolean canGetLocation = false;
	private Location location; // location
	private double latitude; // latitude
	private double longitude; // longitude
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //1 meter ; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES =  1000 * 60; //1 sec,  * 60 * 1; // 1 minute
	// Declaring a Location Manager
	private LocationManager locationManager;
	private final String TAG = "GPS";
	private MainActivity mActivity;
	public GPSTrack(final Context context, final MainActivity activity) {
		this.mContext = context;
		this.mActivity=activity;
		String[] permissions = new String[2];
		permissions[0]=Manifest.permission.ACCESS_FINE_LOCATION;
		permissions[1]=Manifest.permission.ACCESS_COARSE_LOCATION;
		//permissionsToRequest = findUnAskedPermissions(permissions);
		locationManager = (LocationManager) this.mContext.getSystemService(Service.LOCATION_SERVICE);
		isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (!isGPS && !isNetwork) {
			Log.d(TAG, "Connection off");
			showSettingsAlert();
			getLastLocation();
		} else {
			Log.d(TAG, "Connection on");
			// check permissions
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					canGetLocation = false;
					Permissions.check(this.mContext, permissions,
							"Access Fine Location & Access Coarse Location are required because...", new Permissions.Options()
									.setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
							new PermissionHandler() {
								@Override
								public void onGranted() {
									//do your task
									canGetLocation = true;
									getLocation();
                                    activity.callMethod();
								}
							});
			}else
			getLocation();
		}
	}

	private void getLastLocation() {
		try {
			Criteria criteria = new Criteria();
			String provider = locationManager.getBestProvider(criteria, false);
			 location = locationManager.getLastKnownLocation(provider);
			Log.d(TAG, provider);
			Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	private String[] findUnAskedPermissions(ArrayList<String> wanted) {
		String[] result = new String[wanted.size()];
		for (String perm : wanted) {
			Log.i("PEM",perm);
			if (!hasPermission(perm)) {
				result[0]=perm;
			}
		}
		return result;
	}

	private boolean hasPermission(String permission) {
		if (canAskPermission()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
			}
		}
		return true;
	}

	private boolean canAskPermission() {
		return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
	}

	public void getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);
			// getting GPS status
			isGPS = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// getting network status
			isNetwork = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (!isGPS && !isNetwork) {
				// no network provider is enabled
				showSettingsAlert();
			} else {
				this.canGetLocation = true;
				// First get location from Network Provider
				if (isNetwork) {
					try {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("Network", "Network");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}catch (SecurityException se){
						se.printStackTrace();
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPS) {
					if (location == null) {
						try{
							locationManager.requestLocationUpdates(
									LocationManager.GPS_PROVIDER,
									MIN_TIME_BW_UPDATES,
									MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
							Log.d("GPS Enabled", "GPS Enabled");
							if (locationManager != null) {
								location = locationManager
										.getLastKnownLocation(LocationManager.GPS_PROVIDER);
								if (location != null) {
									latitude = location.getLatitude();
									longitude = location.getLongitude();
								}
							}
						}catch (SecurityException se){
							se.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			return;
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient,
				REQUEST,
				(com.google.android.gms.location.LocationListener) this);  // LocationListener
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// Do nothing
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result) {
		// Do nothing
	}

	@Override
	public void activate(OnLocationChangedListener onLocationChangedListener) {
		mMapLocationListener = onLocationChangedListener;
	}

	@Override
	public void deactivate() {
		mMapLocationListener = null;
	}


	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 * */
	public void stopUsingGPS(){
		if(locationManager != null){
			try{
				locationManager.removeUpdates(GPSTrack.this);
			}catch (SecurityException se){
				se.printStackTrace();
			}

		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude(){
		if(location != null){
			latitude = location.getLatitude();
		}

		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude(){
		if(location != null){
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will lauch Settings Options
	 * */
	private void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");
		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mMapLocationListener != null) {
			mMapLocationListener.onLocationChanged(location);
			mActivity.callMethod();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}