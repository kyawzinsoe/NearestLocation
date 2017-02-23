package com.example.nearestlocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResultListAdapter extends CursorAdapter {

	protected static final String TAG = ResultListAdapter.class.getSimpleName();
	
	public ResultListAdapter(Context context, Cursor c) {
		super(context, c, false);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView text1 = (TextView)view.findViewById(android.R.id.text1);
		String name = cursor.getString(cursor.getColumnIndex("name"));
		text1.setText(name);
		
		TextView text2 = (TextView)view.findViewById(android.R.id.text2);
		String address = cursor.getString(cursor.getColumnIndex("address"));
		text2.setText(address);
	}

	@SuppressLint("InflateParams")
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater layoutInflater = (LayoutInflater)LayoutInflater.from(context);
		return layoutInflater.inflate(R.layout.two_line_list_item, null);
	}

}
