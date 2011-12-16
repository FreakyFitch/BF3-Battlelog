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
package com.ninetwozero.battlelog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ninetwozero.battlelog.datatypes.ProfileData;
import com.ninetwozero.battlelog.datatypes.ShareableCookie;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.AssignmentData;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.RequestHandler;
import com.ninetwozero.battlelog.misc.WebsiteHandler;

public class AssignmentView extends Activity {

	//SharedPreferences for shizzle
	private final Context CONTEXT = this;
	private SharedPreferences sharedPreferences;
	private TableLayout tableAssignments;
	private LayoutInflater layoutInflater;
	private ArrayList<AssignmentData> assignments;
	
	@Override
    public void onCreate(Bundle icicle) {
    
    	//onCreate - save the instance state
    	super.onCreate(icicle);
    	
    	//Did it get passed on?
    	if( icicle != null && icicle.containsKey( Constants.SUPER_COOKIES ) ) {
    		
    		RequestHandler.setCookies( (ArrayList<ShareableCookie> ) icicle.getParcelable(Constants.SUPER_COOKIES) );
    	
    	}

    	//Set the content view
        setContentView(R.layout.assignment_view);

        //Prepare to tango
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.reloadLayout();
        
	}        

	public void setupList(ArrayList<AssignmentData> data) {
		
		//Is it empty?
		if( data == null ) { return; }
		
		//Do we have the TableLayout?
		if( tableAssignments == null ) { 
			
			tableAssignments = (TableLayout) findViewById(R.id.table_assignments);
			
		}
		
		//Let's clear the table
		tableAssignments.removeAllViews();
		
		//Loop & create
		for( int i = 0, max = data.size(); i < max; i += 2 ) {
		
			//Init the elements
			TableRow tableRow = (TableRow) layoutInflater.inflate( R.layout.list_item_assignment, null );
			ProgressBar progressLeft = (ProgressBar) tableRow.findViewById( R.id.progress_left);
			ProgressBar progressRight = (ProgressBar) tableRow.findViewById( R.id.progress_right);
			ImageView imageLeft = (ImageView) tableRow.findViewById( R.id.image_leftassignment );
			ImageView imageRight = (ImageView) tableRow.findViewById( R.id.image_rightassignment );

			//Add the table row
			tableAssignments.addView( tableRow );
			
			//Get the values
			AssignmentData ass1 = data.get( i );
			AssignmentData ass2 = data.get( i + 1 );
									
			//Set the images
			imageLeft.setImageResource( ass1.getResourceId() );
			imageLeft.setTag( i );
			imageRight.setImageResource( ass2.getResourceId() );
			imageRight.setTag( i+1 );

			//Set the progressbars
			progressLeft.setMax( ass1.getMax() );
			progressLeft.setProgress( ass1.getCurrent() );
			progressRight.setMax( ass2.getMax() );
			progressRight.setProgress( ass2.getCurrent() );
			
		}
		
	}
	
    public void reloadLayout() {
    	
    	//ASYNC!!!
    	new GetDataSelfAsync(this).execute(
    		
    		new ProfileData(
				this.sharedPreferences.getString( Constants.SP_BL_USERNAME, "" ),
				this.sharedPreferences.getString( Constants.SP_BL_PERSONA, "" ),
				this.sharedPreferences.getLong( Constants.SP_BL_PERSONA_ID, 0 ),
				this.sharedPreferences.getLong( Constants.SP_BL_PERSONA_ID, 0 ),
				this.sharedPreferences.getLong( Constants.SP_BL_PLATFORM_ID, 1),
				sharedPreferences.getString( Constants.SP_BL_GRAVATAR, "" )
			)
		
		);
    	
    	
    }
    
    public void doFinish() {}
    
    private class GetDataSelfAsync extends AsyncTask<ProfileData, Void, Boolean> {
    
    	//Attributes
    	Context context;
    	ProgressDialog progressDialog;
    	
    	public GetDataSelfAsync(Context c) {
    		
    		this.context = c;
    		this.progressDialog = null;
    		
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		
    		//Let's see
			this.progressDialog = new ProgressDialog(this.context);
			this.progressDialog.setTitle(context.getString( R.string.general_wait ));
			this.progressDialog.setMessage( getString(R.string.general_downloading ) );
			this.progressDialog.show();
    		
    	}
    	

		@Override
		protected Boolean doInBackground( ProfileData... arg0 ) {
			
			try {
				
				assignments = WebsiteHandler.getAssignments( context, arg0[0] );
				return true;
				
			} catch ( WebsiteHandlerException ex ) {
				
				ex.printStackTrace();
				return false;
				
			}

		}
		
		@Override
		protected void onPostExecute(Boolean result) {
		
			//Fail?
			if( !result ) { 
				
				if( this.progressDialog != null ) this.progressDialog.dismiss();
				Toast.makeText( this.context, R.string.general_no_data, Toast.LENGTH_SHORT).show(); 
				((Activity) this.context).finish();
				return; 
			
			}

			//Do actual stuff	
			setupList(assignments);
			
			//Go go go
	        if( this.progressDialog != null ) this.progressDialog.dismiss();
			return;
		}
    	
    }
    
    @Override
	public boolean onCreateOptionsMenu( Menu menu ) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.option_basic, menu );
		return super.onCreateOptionsMenu( menu );
	
    }
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {

		//Let's act!
		if( item.getItemId() == R.id.option_reload ) {

	    	
	    	showDialog(0);
			this.reloadLayout();
			
		} else if( item.getItemId() == R.id.option_back ) {
			
			((Activity) this).finish();
			
		}
		
		// Return true yo
		return true;

	}  
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){        
        super.onConfigurationChanged(newConfig);
    }  
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(Constants.SUPER_COOKIES, RequestHandler.getCookies());
	
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
        
		//Init
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        AssignmentData assignment = assignments.get( id );
        AssignmentData.Unlock unlocks = assignment.getUnlocks().get( 0 );
        
        View dialog = layoutInflater.inflate( R.layout.popup_dialog_view, null);
        LinearLayout wrapObjectives = (LinearLayout) dialog.findViewById( R.id.wrap_objectives );
        
        //Set the title
        builder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            
	        	public void onClick(DialogInterface dialog, int id) {
	                
	        		dialog.dismiss();
	            	
	            }
	        	
        	}
        );
        
        //Create the dialog and set the contentView
        builder.setView( dialog );
        builder.setCancelable( true );
        
        //Set the actual fields too
        ((ImageView) dialog.findViewById(R.id.image_assignment)).setImageResource( assignment.getResourceId() );
        ((TextView) dialog.findViewById(R.id.text_title)).setText( assignment.getId() );
        
        //Loop over the criterias
        for( AssignmentData.Objective objective : assignment.getObjectives() ) {
        	        	
        	//Inflate a layout...
        	View v = layoutInflater.inflate( R.layout.list_item_assignment_popup, null);
        	
        	//...and set the fields
        	((TextView) v.findViewById( R.id.text_obj_title )).setText( objective.getDescription() );
        	((TextView) v.findViewById( R.id.text_obj_values )).setText( 
        			
        		objective.getCurrentValue() + "/" + objective.getGoalValue() 
        		
        	);
        	
        	wrapObjectives.addView(v);
        	
        }
        
        ((ImageView) dialog.findViewById( R.id.image_reward )).setImageResource( assignment.getResourceId() );
        ((TextView) dialog.findViewById( R.id.text_rew_name )).setText( unlocks.getId() );

    	return builder.create();

    }
	
	public void onPopupClick(View v) {
		
		showDialog(Integer.parseInt( v.getTag().toString() ));
		return;
	
	}
	
}
