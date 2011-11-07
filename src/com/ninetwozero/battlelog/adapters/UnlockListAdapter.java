/*
	This file is part of BF3 Battlelog

    BF3 Battlelog is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Battlelog is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
*/   

package com.ninetwozero.battlelog.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.datatypes.UnlockData;

public class UnlockListAdapter extends BaseAdapter {
	
	//Attributes
	Context context;
	ArrayList<UnlockData> unlockArray;
	LayoutInflater layoutInflater;
	String tempStatus;
	ProgressBar progressBar;
	
	//Construct
	public UnlockListAdapter(Context c, ArrayList<UnlockData> u, LayoutInflater l) {
	
		context = c;
		unlockArray = u;
		layoutInflater = l;
		
	}

	@Override
	public int getCount() {

		return ( unlockArray != null )? unlockArray.size() : 0;
		
	}

	@Override
	public UnlockData getItem( int position ) {

		return unlockArray.get( position );

	}

	@Override
	public long getItemId( int position ) {

		return position;
		
	}

	
	@Override
	public View getView( int position, View convertView, ViewGroup parent ) {

		//Get the current item
		UnlockData currentUnlock = getItem(position);
		String unlockTitle;
		
		//Recycle
		if ( convertView == null ) {

			convertView = layoutInflater.inflate( R.layout.list_item_unlock, parent, false );

		}
		
		//Grab the progressBar
		progressBar = ((ProgressBar) convertView.findViewById( R.id.progress_unlock ));

		//Set the TextViews
		((View) convertView.findViewById(R.id.divider_left)).setBackgroundColor( context.getResources().getColor( getColorForKit(currentUnlock.getKitId()) ) );
		((TextView) convertView.findViewById( R.id.text_unlock_percent )).setText(currentUnlock.getUnlockPercentage() + "%");
		((TextView) convertView.findViewById( R.id.text_unlock_type )).setText( "" );
		
		Log.d("com.ninetwozero.battlelog", "Type: " + currentUnlock.getType());
		
		//Title
		((TextView) convertView.findViewById( R.id.text_unlock_title )).setText( currentUnlock.getName() );
		
		//Description
		((TextView) convertView.findViewById(R.id.text_unlock_desc)).setText( currentUnlock.getObjective() );
		
		//Update the progress
		progressBar.setMax( (int) currentUnlock.getScoreNeeded() );
		progressBar.setProgress( (int) currentUnlock.getScoreCurrent() );
		
		//Tag it!
		convertView.setTag( currentUnlock );

		return convertView;
	}
	
	public int getColorForKit( int kitId ) {
			
		switch( kitId ) {
		
			case 1:
				return R.color.kit_assault;
				
			case 2:
				return R.color.kit_engineer;
				
			case 8:
				return R.color.kit_recon;
				
			case 32:
				return R.color.kit_support;
				
			default:
				return R.color.kit_general;
			
		}
		
	}
	
}