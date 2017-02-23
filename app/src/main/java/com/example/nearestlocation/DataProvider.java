package com.example.nearestlocation;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DataProvider extends ContentProvider {
	
	protected static final String TAG = DataProvider.class.getSimpleName();
	
	protected static final String DATABASE_NAME = "database";
	protected static final int DATABASE_VERSION = 1;
	
	protected static final String TABLE_LOCATION = "location"; 
	protected static final String URL_LOCATION = "content://com.example.nearestlocation.dataprovider/" + TABLE_LOCATION;
	public static final Uri URI_LOCATION = Uri.parse(URL_LOCATION);

	private static class DatabaseHelper extends SQLiteOpenHelper {		

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i("DatabaseHelper : ", "OnCreate");
			
			db.execSQL("CREATE TABLE IF NOT EXISTS `location` "
					+ "(`_id` INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "`name` VARCHAR, `address` TEXT, `lat` NUMERIC, `lng` NUMERIC); ");
			
			for(String str : SampleData.INSERT_DATA) {
				db.execSQL(str);	
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS `location`;");
			onCreate(db);
		}
	}
	
	private DatabaseHelper mDbHelper;
	
	public DataProvider() {
		
	}
	
	public SQLiteDatabase getDatabase() {
		SQLiteDatabase database = null;
		if(mDbHelper == null) return database;
		
		try {
			database = mDbHelper.getWritableDatabase();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		return database;
	}
	
	@Override
	public boolean onCreate() {
		Log.i("DataProvider : ", "OnCreate");
		
		if(mDbHelper == null) {
			mDbHelper = new DatabaseHelper(getContext());
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		Cursor result = null;
		final SQLiteDatabase database = getDatabase();
		if (database == null) return result;
		
		String type = getType(uri);
		if (type != null) {
			if (projection == null) projection = new String[] { "_id", "name", "address", "lat", "lng" };
			if (TextUtils.isEmpty(sortOrder)) sortOrder = "`name` ASC";
			
			Log.i(TAG, "OnQuery, selection = " + selection);
			result = database.query(TABLE_LOCATION, projection, selection, selectionArgs
					, null, null, sortOrder, null);
			
			if (result != null) {
				result.moveToFirst();
				result.setNotificationUri(getContext().getContentResolver(), uri);
			}
			
		}
		return result;
	}

	@Override
	public String getType(Uri uri) {
		if (uri.equals(URI_LOCATION)) {
			return TABLE_LOCATION;
		} 
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return null;
		
		String type = getType(uri);
		if (type != null) {
			long result = database.update(type, values, "`_id` IS ?", new String[] { values.getAsString("_id") });
			if (result < 1) database.insert(type, null, values);
		}

		return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return -1;
		
		String type = getType(uri);
		if (type != null) {
			return database.delete(type, selection, selectionArgs);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		final SQLiteDatabase database = getDatabase();
		if (database == null) return -1;
		
		String type = getType(uri);
		if (type != null) {
			return database.update(type, values, selection, selectionArgs);
		}
		return 0;
	}
}
