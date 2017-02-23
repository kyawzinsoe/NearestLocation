package com.example.nearestlocation;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	protected static final String TAG = MainActivity.class.getSimpleName();
	
	protected ListView mListView;
	protected TextView mTextView;
	protected CheckBox mCheckUsedGps;
	private LocationManager mLocationManager;
	private ResultListAdapter mListAdapter;
	
	private LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			mLocationManager.removeUpdates(this);
			if (location != null) {
				if (mTextView != null) {
					mTextView.setText("Lat : " + location.getLatitude() + " - Lng : " + location.getLongitude());
				}
				findNearest(location.getLatitude(), location.getLongitude());
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
			if (mTextView != null) {
				mTextView.setText("GPS is enabled!");
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			if (mTextView != null) {
				mTextView.setText("GPS is not enabled!");
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mListView = (ListView)findViewById(R.id.listView);
		
		Cursor cursor = getContentResolver().query(DataProvider.URI_LOCATION, null, null, null, null);
		mListAdapter = new ResultListAdapter(this, cursor);
		mListView.setAdapter(mListAdapter);
		
		mTextView = (TextView)findViewById(R.id.textLocation);
		mCheckUsedGps = (CheckBox)findViewById(R.id.checkGpsOnly);
		
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		Button btnCheck = (Button)findViewById(R.id.buttonCheck);
		btnCheck.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkLocation();
				findNearest(17.002113,96.092414);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void checkLocation() {
		if (mCheckUsedGps != null && mCheckUsedGps.isChecked()) {
			if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			} else {
				mTextView.setText("GPS is not enabled!");
			}	
		} else {
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
			} else {
				mTextView.setText("Can't connect to internet!");
			}	
		}
	}
	
	protected void findNearest(double x, double y) {
		float radius = 10000.0f; // meter
		PointF center = new PointF((float)x, (float)y);
		final double mult = 1; // mult = 1.1; is more reliable
		PointF p1 = calculateDerivedPosition(center, mult * radius, 0);
		PointF p2 = calculateDerivedPosition(center, mult * radius, 90);
		PointF p3 = calculateDerivedPosition(center, mult * radius, 180);
		PointF p4 = calculateDerivedPosition(center, mult * radius, 270);

		String selection =  " "
		        + "`lat` > " + String.valueOf(p3.x) + " AND "
		        + "`lat` < " + String.valueOf(p1.x) + " AND "
		        + "`lng` < " + String.valueOf(p2.y) + " AND "
		        + "`lng` > " + String.valueOf(p4.y);
		
		Cursor cursor = getContentResolver().query(DataProvider.URI_LOCATION, null, selection, null, null);
		if (cursor != null) {
			Log.i(TAG, "Result=" + cursor.getCount());
			mListAdapter.changeCursor(cursor);
			mListAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	* Calculates the end-point from a given source at a given range (meters)
	* and bearing (degrees). This methods uses simple geometry equations to
	* calculate the end-point.
	* 
	* @param point
	*            Point of origin
	* @param range
	*            Range in meters
	* @param bearing
	*            Bearing in degrees
	* @return End-point from the source given the desired range and bearing.
	*/
	protected PointF calculateDerivedPosition(PointF point, double range, double bearing) {
        double earthRadius = 6371000; // meter

        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range / earthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);
        
        Log.i("LAT : ", ""+lat);
        Log.i("LON : ", ""+lon);

        return new PointF((float)lat, (float)lon);

    }
}
