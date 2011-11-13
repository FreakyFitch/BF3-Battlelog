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

package com.ninetwozero.battlelog.asynctasks;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.adapters.FriendListAdapter;
import com.ninetwozero.battlelog.adapters.RequestListAdapter;
import com.ninetwozero.battlelog.datatypes.ProfileData;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.WebsiteHandler;

public class AsyncComRefresh extends AsyncTask<Void, Integer, Boolean> {

	//Attribute
	Context context;
	SharedPreferences sharedPreferences;
	ArrayList<ArrayList<ProfileData>> profileArray = new ArrayList<ArrayList<ProfileData>>();
	ListView listRequests, listFriendsOnline, listFriendsOffline;
	LayoutInflater layoutInflater;
	Button buttonRefresh;
	
	//Constructor
	public AsyncComRefresh( Context c, ListView r, ListView fon, ListView fof, LayoutInflater l, Button b ) { 
		
		this.context = c;
		this.listRequests = r;
		this.listFriendsOnline = fon;
		this.listFriendsOffline = fof;
		this.layoutInflater = l;
		this.sharedPreferences = context.getSharedPreferences(Constants.fileSharedPrefs, 0);
		this.buttonRefresh = b;
		
	}	
	
	@Override
	protected void onPreExecute() {
		
		this.buttonRefresh.setEnabled(false);
		this.buttonRefresh.setText( "Please wait..." );
		
	}
	
	@Override
	protected Boolean doInBackground( Void... arg0) {
		
		try {
		
			//Let's get this!!
			profileArray = WebsiteHandler.getFriendsCOM( sharedPreferences.getString( "battlelog_post_checksum", "") );
			return true;
			
		} catch ( WebsiteHandlerException e ) {
			
			return false;
			
		}
		
	}
	
	@Override
	protected void onPostExecute(Boolean results) {

		//Boolean
		int emptyLists = 0;
		
		//Fill the listviews!!
		if( profileArray.size() > 0 ) {
			
			if( profileArray.get( 0 ) == null || profileArray.get( 0 ).size() == 0 ) {
			
				((Activity)context).findViewById(R.id.wrap_friends_requests).setVisibility( View.GONE );
			
			} else {
				
				//VISIBILITY!!!
				((Activity)context).findViewById(R.id.wrap_friends_requests).setVisibility( View.VISIBLE );
				
				//Set the adapter
				listRequests.setAdapter( new RequestListAdapter(context, profileArray.get(0), layoutInflater) );
				
			}

			if( profileArray.get( 1 ) == null || profileArray.get( 1 ).size() > 0 ) {

				//Set the visibilty (could've been hidden)
				((Activity)context).findViewById(R.id.wrap_friends_online).setVisibility( View.VISIBLE );
				
				//Set the adapter
				listFriendsOnline.setAdapter( new FriendListAdapter(context, profileArray.get(1), layoutInflater) );
				
				
			} else {
				
				//No online friends found :-(
				emptyLists++;
				((Activity)context).findViewById(R.id.wrap_friends_online).setVisibility( View.GONE );
				
			}
			
			if( profileArray.get( 2 ) == null || profileArray.get( 2 ).size() > 0 ) {

				//Set the visibilty (could've been hidden)
				((Activity)context).findViewById(R.id.wrap_friends_offline).setVisibility( View.VISIBLE );
				
				//Set the adapter
				listFriendsOffline.setAdapter( new FriendListAdapter(context, profileArray.get(2), layoutInflater) );
				
				
			} else {
				
				//No offline friends found :-( and :-) at the same time
				emptyLists++;
				((Activity)context).findViewById(R.id.wrap_friends_offline).setVisibility( View.GONE );
				
				//No friends at all? What the fork... :-(
				if( emptyLists > 1 ) { ((Activity) context).findViewById( R.id.text_empty_com ).setVisibility( View.VISIBLE ); }
			
			}
		
		} else {
			
			results = false;
			
		}
		

		//Update the button y'all
		this.buttonRefresh.setEnabled(true);
		this.buttonRefresh.setText( "Refresh now" );
		
		//How did go?
		if( results ) Toast.makeText( context, "COM CENTER up to date.", Toast.LENGTH_SHORT).show();
		else Toast.makeText( context, "COM CENTER could not be refreshed.", Toast.LENGTH_SHORT).show();				
		return;
		
	}	

}