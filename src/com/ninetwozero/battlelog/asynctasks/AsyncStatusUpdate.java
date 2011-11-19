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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.Toast;

import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.datatypes.PostData;
import com.ninetwozero.battlelog.datatypes.RequestHandlerException;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.RequestHandler;


public class AsyncStatusUpdate extends AsyncTask<PostData, Integer, Integer> {

	//Attribute
	ProgressDialog progressDialog;
	Context context;
	boolean fromWidget;
	String httpContent;
	
	//Constructor
	public AsyncStatusUpdate( Context c, boolean w ) { 
		
		this.context = c; 
		this.fromWidget = w;
	
	}	
	
	@Override
	protected void onPreExecute() {
	
		
		//Let's see
		if( !fromWidget ) {
		
			this.progressDialog = new ProgressDialog(this.context);
			this.progressDialog.setTitle("Please wait");
			this.progressDialog.setMessage( "Updating your status..." );
			this.progressDialog.show();
		
		}
	}
	
	@Override
	protected Integer doInBackground( PostData... arg0 ) {
		
		try {
		
			//Let's login everybody!
			RequestHandler wh = new RequestHandler();
    		httpContent = wh.post( Constants.urlStatusSend, arg0, 0);
    		
    		//Did we manage?
    		if( httpContent != null && !httpContent.equals( "" ) ) {
    			
    			//Set the int
    			int startPosition = httpContent.indexOf( Constants.elementStatusOK );
    			
    			//Did we find it?
    			if( startPosition == -1 ) {
    				
    				return 1;

    			}
				
    		}
    		
		} catch ( RequestHandlerException ex ) {
			
			ex.printStackTrace();
			return 1;
		
		} catch( Exception ex ) {
			
			ex.printStackTrace();
			return 1;
			
		}

		return 0;
		
	}
	
	@Override
	protected void onPostExecute(Integer results) {

		if( !fromWidget ) {
			
			this.progressDialog.dismiss();
			
			if( results == 0 ) { 
				
				Toast.makeText(this.context, "Status updated successfully.", Toast.LENGTH_SHORT).show(); 
				((EditText) ((Activity)context).findViewById(R.id.field_status)).setText("");
				
			} else { Toast.makeText( this.context, "Posting the status update...", Toast.LENGTH_SHORT).show(); }
			
		}
		return;
		
	}

	
	
}
