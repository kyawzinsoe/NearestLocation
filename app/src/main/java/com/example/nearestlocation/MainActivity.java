package com.example.nearestlocation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final String TAG = MainActivity.class.getSimpleName();
	public static Context appContext;
	protected ListView mListView;
	protected TextView mTextView;
	protected CheckBox mCheckUsedGps;
	private ResultListAdapter mListAdapter;
	public static void runOnUI(Runnable runnable){
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(runnable);
	}

	
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
		

		
		Button btnCheck = (Button)findViewById(R.id.buttonCheck);
		btnCheck.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.appContext=MainActivity.this.getApplicationContext();
			//	checkLocation();
			//	findNearest(17.002113,96.092414);
				Toast.makeText(MainActivity.this,"started",Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(MainActivity.this, LocService.class);
				startService(intent);
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
