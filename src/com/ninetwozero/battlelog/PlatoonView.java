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
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import com.ninetwozero.battlelog.adapters.FeedListAdapter;
import com.ninetwozero.battlelog.adapters.PlatoonUserListAdapter;
import com.ninetwozero.battlelog.asynctasks.AsyncFeedHooah;
import com.ninetwozero.battlelog.asynctasks.AsyncPostToWall;
import com.ninetwozero.battlelog.datatypes.CommentData;
import com.ninetwozero.battlelog.datatypes.FeedItem;
import com.ninetwozero.battlelog.datatypes.PlatoonData;
import com.ninetwozero.battlelog.datatypes.PlatoonInformation;
import com.ninetwozero.battlelog.datatypes.PlayerData;
import com.ninetwozero.battlelog.datatypes.ProfileData;
import com.ninetwozero.battlelog.datatypes.SerializedCookie;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.PublicUtils;
import com.ninetwozero.battlelog.misc.RequestHandler;
import com.ninetwozero.battlelog.misc.WebsiteHandler;

public class PlatoonView extends TabActivity {

	//Attributes
	private final Context CONTEXT = this;
	private SharedPreferences sharedPreferences;
	private LayoutInflater layoutInflater;
	private PlatoonData platoonData;
	private PlayerData playerData;
	private PlatoonInformation platoonInformation;
	private TabHost mTabHost;
	
	//Elements
	private ListView listFeed;
	private ListView listUsers;
	private FeedListAdapter feedListAdapter;
	private PlatoonUserListAdapter platoonUserListAdapter;
	
	//CONTROLLERS for the users-tab
	private final int VIEW_MEMBERS = 0, VIEW_FANS = 1;
	private boolean isViewingMembers = true;
	
	@Override
    public void onCreate(Bundle icicle) {
    
    	//onCreate - save the instance state
    	super.onCreate(icicle);	
    	
    	//Did it get passed on?
    	if( icicle != null && icicle.containsKey( "serializedCookies" ) ) {
    		
    		RequestHandler.setSerializedCookies( (ArrayList<SerializedCookie> ) icicle.getSerializable("serializedCookies") );
    	
    	}
        
        //Prepare to tango
        this.sharedPreferences = this.getSharedPreferences( Constants.fileSharedPrefs, 0);
        this.layoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        
    	//Get the intent
        if( getIntent().hasExtra( "platoon" ) ) {
        	
        	platoonData = (PlatoonData) getIntent().getSerializableExtra( "platoon" );
        	
        }
        
        //Is the profileData null?!
        if( platoonData == null || platoonData.getId() == 0 ) { finish(); return; }
    	
    	//Set the content view
        setContentView(R.layout.platoon_view);
        
        //Fix the tabs
    	mTabHost = (TabHost) findViewById(android.R.id.tabhost);
    	setupTabs(
    			
    		new String[] { "Home", "Stats", "Users", "Feed" }, 
    		new int[] { 
    				
				R.layout.tab_content_overview_platoon, 
				R.layout.tab_content_platoon_stats, 
				R.layout.tab_content_platoon_users, 
				R.layout.tab_content_feed 
				
    		}
    		
    	);
        
        initLayout();
	}        

	public void initLayout() {
		
		//Eventually get a *cached* version instead    
		new AsyncPlatoonRefresh(this, false, platoonData, sharedPreferences.getLong( "battlelog_profile_id", 0 )).execute();
		
	}
	
    public void reloadLayout() {
    	
    	//ASYNC!!!
    	new AsyncPlatoonRefresh(this, true, platoonData, sharedPreferences.getLong( "battlelog_profile_id", 0 )).execute();
    	
    	
    }
    
    private void setupTabs(final String[] tags, final int[] layouts) {

		//Init
    	TabHost.TabSpec spec;
    	
    	//Iterate them tabs
    	for(int i = 0; i < tags.length; i++) {

    		//Num
    		final int num = i;
			View tabview = createTabView(mTabHost.getContext(), tags[num]);
			
			//Let's set the content
			spec = mTabHost.newTabSpec(tags[num]).setIndicator(tabview).setContent(
	        		
	    		new TabContentFactory() {
	    			
	            	public View createTabContent(String tag) {
	            		
	            		return layoutInflater.inflate( layouts[num], null );
	    
	            	}
	            
	            }
	    		
	        );
			
			//Add the tab
			mTabHost.addTab( spec ); 
    	
    	}
    	
    }

    private static View createTabView(final Context context, final String text) {
    	
    	View view = LayoutInflater.from(context).inflate(R.layout.profile_tab_layout, null);
    	TextView tv = (TextView) view.findViewById(R.id.tabsText);
    	tv.setText(text);
    	return view;
    
    }
    
    public void doFinish() {}
    
    public class AsyncPlatoonRefresh extends AsyncTask<Void, Void, Boolean> {
    
    	//Attributes
    	private Context context;
    	private ProgressDialog progressDialog;
    	private PlatoonData platoonData;
    	private long activeProfileId;
    	private boolean hideDialog;
    	
    	public AsyncPlatoonRefresh(Context c, boolean f, PlatoonData pd, long pId) {
    		
    		this.context = c;
    		this.hideDialog = f;
    		this.platoonData = pd;
    		this.progressDialog = null;
    		this.activeProfileId = pId;
    		
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		
    		//Do we?
    		if( !hideDialog ) {

    			//Let's see
				this.progressDialog = new ProgressDialog(this.context);
				this.progressDialog.setTitle("Please wait");
				this.progressDialog.setMessage( "Downloading the data..." );
				this.progressDialog.show();
    		
    		}	
    	
    	}

		@Override
		protected Boolean doInBackground( Void... arg0 ) {
			
			try {
				
				//Get...
				platoonInformation = WebsiteHandler.getProfileInformationForPlatoon( this.platoonData, this.activeProfileId);
				
				//...validate!
				if( platoonInformation == null ) { 
					
					return false; 
				
				} else {
					
					return true;
				
				}
				
			} catch ( WebsiteHandlerException ex ) {
				
				ex.printStackTrace();
				return false;
				
			}

		}
		
		@Override
		protected void onPostExecute(Boolean result) {
					
			//Fail?
			if( !result ) { 
				
				if ( !hideDialog ) { if( this.progressDialog != null ) this.progressDialog.dismiss(); }
				Toast.makeText( this.context, "No data found.", Toast.LENGTH_SHORT).show(); 
				((Activity) this.context).finish();
				return; 
			
			}

			//Assign values
			mTabHost.setOnTabChangedListener(
					
				new OnTabChangeListener() {

					@Override
					public void onTabChanged(String tabId) {
	
						switch( getTabHost().getCurrentTab() ) {
							
							case 0:
								drawHome(platoonInformation);
								break;
								
							case 1:
								drawStats(playerData);
								break;
								
							case 2:
								drawUsers(platoonInformation);
								break;
								
							case 3:
								drawFeed(platoonInformation);
								break;
								
							default:
								break;
					
						}
		
					}
					
				}
				
			);

			//Let's see what we need to update *directly*
			switch( mTabHost.getCurrentTab() ) {
				
				case 0:
					drawHome(platoonInformation);
					break;
					
				case 1:
					drawStats(playerData);
					break;
				
				case 2:
					drawUsers(platoonInformation);
					break;
					
				case 3:
					drawFeed(platoonInformation);
					break;
					
				default:
					break;
		
			}
			
			//Done!
	        if( this.progressDialog != null && !hideDialog ) this.progressDialog.dismiss();
	        
	        //Get back here!
	        return;
		        
		}
		
    }
    
    public final void drawHome(PlatoonInformation data) {
    	
    	//Let's start drawing the... layout
    	((TextView) findViewById(R.id.text_name)).setText( data.getName() );
    	
    	//Set the *created*
    	((TextView) findViewById(R.id.text_date)).setText( 
    			
    		PublicUtils.getDate( data.getDate(), "Created on" ) + " (" +
    		PublicUtils.getRelativeDate( data.getDate() ) + ")"
    	);
    	
    	//Do we have a link?!
    	if( data.getWebsite() != null && !data.getWebsite().equals( "" ) ) {
    		
    		((TextView) findViewById(R.id.text_web)).setText( data.getWebsite() );
    		((View) findViewById(R.id.wrap_web)).setTag( data.getWebsite() );
    		
    	} else {
    		
    		((View) findViewById(R.id.wrap_web)).setVisibility( View.GONE );
    		
    	}
    	//Do we have a presentation?
    	if( data.getPresentation() != null && !data.getPresentation().equals( "" ) ) {
    		
    		((TextView) findViewById(R.id.text_presentation)).setText( data.getPresentation() );
		
    	} else {
    		
    		((TextView) findViewById(R.id.text_presentation)).setText( "No presentation found." );

    	}
    	
    }
    
    public void drawStats(PlayerData pd) {
    	
		Toast.makeText( this, "This is not implemented yet!", Toast.LENGTH_SHORT).show();
		
    }
    
    public final void drawUsers(PlatoonInformation data) {
    	
    	//Do we have the ListView?
    	if( listUsers == null ) {
    		
    		listUsers = (ListView) findViewById(R.id.list_users);
    		registerForContextMenu(listUsers);
    		
    	}
    	
    	//Do we have an adapter?
    	if( listUsers.getAdapter() == null ) {
    		
    		//Create new adapter & set it onto our ListView
    		platoonUserListAdapter = new PlatoonUserListAdapter(this, data.getMembers(), layoutInflater);
    		listUsers.setAdapter( platoonUserListAdapter );
			
    		//Do we have the onClick?
			if( listUsers.getOnItemClickListener() == null ) {
				
				listUsers.setOnItemClickListener( 
						
					new OnItemClickListener() {
	
						@Override
						public void onItemClick( AdapterView<?> a, View v, int pos, long id ) {
	
							startActivity(
									
								new Intent(CONTEXT, ProfileView.class).putExtra(
										
									"profile", 
									(ProfileData) v.getTag() 
									
								)
								
							);
							
						}
						
					}
						
				);
			
			}
    		
    	} else {
    		
			//Get the appropriate data
			if( isViewingMembers ) { 
				
				((PlatoonUserListAdapter)listUsers.getAdapter()).setProfileArray( platoonInformation.getMembers());
			
			} else {
				
				((PlatoonUserListAdapter)listUsers.getAdapter()).setProfileArray( platoonInformation.getFans());
			
			}
    			
    		//Update it!
			((PlatoonUserListAdapter)listUsers.getAdapter()).notifyDataSetChanged();
			
    	}
    	
    	//Which view are we on?
    	if( isViewingMembers ) { 
    		
    		((TextView) findViewById(R.id.feed_title)).setText( "MEMBERS" );
    		
    	} else { 
    		
    		((TextView) findViewById(R.id.feed_title)).setText( "FANS");
    		
    	}
    	
    }
    
    public void drawFeed(PlatoonInformation data) {
    	
    	//Do we have it already?
		if( listFeed == null ) { 
			
			listFeed = ((ListView) findViewById(R.id.list_feed)); 
			registerForContextMenu(listFeed);
			
		}
        
		((TextView) findViewById(R.id.feed_username)).setText( data.getName() );
        
		//If we don't have it defined, then we need to set it
		if( listFeed.getAdapter() == null ) {
			
			//Create a new FeedListAdapter
			feedListAdapter = new FeedListAdapter(this, data.getFeedItems(), layoutInflater);
			listFeed.setAdapter( feedListAdapter );
			
			//Do we have the onClick?
			if( listFeed.getOnItemClickListener() == null ) {
				
				listFeed.setOnItemClickListener( 
						
					new OnItemClickListener() {
	
						@Override
						public void onItemClick( AdapterView<?> a, View v, int pos, long id ) {
	
							if( !((FeedItem) a.getItemAtPosition( pos ) ).getContent().equals( "" ) ) {
								
								View viewContainer = (View) v.findViewById(R.id.wrap_contentbox);
								viewContainer.setVisibility( ( viewContainer.getVisibility() == View.GONE ) ? View.VISIBLE : View.GONE );
							
							}
							
						}
						
						
						
					}
						
				);
			
			}
			
		} else {
			
			
			feedListAdapter.notifyDataSetChanged();
		}
    }

    @Override
	public boolean onCreateOptionsMenu( Menu menu ) {

    	//Inflate!!
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.option_platoonview, menu );		
		return super.onCreateOptionsMenu( menu );
	
    }
	
    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
    	
    	//Our own profile, no need to show the "extra" buttons
		if( mTabHost.getCurrentTab() == 0 ) {			
					
			if( platoonInformation.isOpenForNewMembers() ) {
					
				if( platoonInformation.isOpenForNewMembers() ) {

					((MenuItem) menu.findItem( R.id.option_join )).setVisible( false );
					((MenuItem) menu.findItem( R.id.option_leave )).setVisible( true );
					((MenuItem) menu.findItem( R.id.option_fans )).setVisible( false );
					((MenuItem) menu.findItem( R.id.option_members )).setVisible( false );
					((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( false );
				
				} else {

					((MenuItem) menu.findItem( R.id.option_join )).setVisible( true );
					((MenuItem) menu.findItem( R.id.option_leave )).setVisible( false );
					((MenuItem) menu.findItem( R.id.option_fans )).setVisible( false );
					((MenuItem) menu.findItem( R.id.option_members )).setVisible( false );
					((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( false );
				}
					
			} else {

				((MenuItem) menu.findItem( R.id.option_join )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_leave )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_fans )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_members )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( false );
				
			}
		
		} else if( mTabHost.getCurrentTab() == 1 ) {

			((MenuItem) menu.findItem( R.id.option_join )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_leave )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_fans )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_members )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( false );
			
		} else if( mTabHost.getCurrentTab() == 2 ) {
			
			if( isViewingMembers ) {
				
				((MenuItem) menu.findItem( R.id.option_join )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_leave )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_fans )).setVisible( true );
				((MenuItem) menu.findItem( R.id.option_members )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( false );
				
			} else {

				((MenuItem) menu.findItem( R.id.option_join )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_leave )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_fans )).setVisible( false );
				((MenuItem) menu.findItem( R.id.option_members )).setVisible( true );
				((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( false );
				
			}
			
		} else if( mTabHost.getCurrentTab() == 3 ) {

			((MenuItem) menu.findItem( R.id.option_join )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_leave )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_fans )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_members )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( true );
			
		} else {
			
			((MenuItem) menu.findItem( R.id.option_join )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_leave )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_fans )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_members )).setVisible( false );
			((MenuItem) menu.findItem( R.id.option_newpost )).setVisible( false );
			
		}
		
    	return super.onPrepareOptionsMenu( menu );
    	
    }
    
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {

		//Let's act!
		if( item.getItemId() == R.id.option_reload ) {
	
			this.reloadLayout();
			
		} else if( item.getItemId() == R.id.option_back ) {
			
			((Activity) this).finish();
			
		} else if( item.getItemId() == R.id.option_members || item.getItemId() == R.id.option_fans ) {
			
			isViewingMembers = !isViewingMembers;
			drawUsers( platoonInformation );
			
		} else if( item.getItemId() == R.id.option_compare ) {
			
			Toast.makeText( this, "You can't compare platoons... duh", Toast.LENGTH_SHORT).show();
			
		} else if( item.getItemId() == R.id.option_newpost ) {
			
			generateDialogPost(this).show();
			
		} else if( item.getItemId() == R.id.option_join ) {
			
			Toast.makeText( this, "So you want to join the platoon ey?", Toast.LENGTH_SHORT).show();
			
		} else if( item.getItemId() == R.id.option_leave ) {
			
			Toast.makeText( this, "So you want to leave the platoon ey?", Toast.LENGTH_SHORT).show();
			
		}
	
		// Return true yo
		return true;

	}  
	
	@Override
	public void onResume() {
		
		super.onResume();
		this.reloadLayout();
		
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig){        
        super.onConfigurationChanged(newConfig);
    }  
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		outState.putSerializable("serializedCookies", RequestHandler.getSerializedCookies());
	
	}
	
	public void onClick(View v) {
		
		//Check which view we clicked
		if( v.getId() == R.id.wrap_web ) {
			
			startActivity(
			
				new Intent(Intent.ACTION_VIEW).setData(
						
					Uri.parse(
							
						String.valueOf( v.getTag() ) 
						
					)
					
				)
					
			);
			
		}
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

		//Grab the info
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		//Show the menu
		if( !((FeedItem) ((View) info.targetView).getTag()).isLiked() ) {
			
			menu.add( 0, 0, 0, "Hooah!");
		
		} else {
			
			menu.add( 0, 0, 0, "Un-hooah!");
			
		}
		menu.add( 0, 1, 0, "View comments");

		return;
	
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		//Declare...
		AdapterView.AdapterContextMenuInfo info;
		
		//Let's try to get some menu information via a try/catch
		try {
			
		    info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		} catch (ClassCastException e) {
		
			e.printStackTrace();
			return false;
		
		}
		
		try {
			
			//Divide & conquer 
			if( item.getGroupId() == 0 ) {
				
				//REQUESTS
				if( item.getItemId() == 0 ) {
						
					new AsyncFeedHooah(
							
						this, 
						info.id, 
						false,
						( (FeedItem)info.targetView.getTag()).isLiked(),
						new AsyncPlatoonRefresh(
								
							this, 
							true, 
							platoonData,
							sharedPreferences.getLong( "battlelog_profile_id", 0 )
							
						)
					
					).execute( 
							
						sharedPreferences.getString( 
								
							"battlelog_post_checksum", 
							""
							
						) 
					
					);
				
				} else if( item.getItemId() == 1 ){
					
					//Yeah
					startActivity(
							
						new Intent(
								
							this, 
							CommentView.class
							
						).putExtra(
								
							"comments", 
							(ArrayList<CommentData>) ((FeedItem) info.targetView.getTag()).getComments()
					
						).putExtra( 

							"postId", 
							((FeedItem) info.targetView.getTag()).getId()
							
						).putExtra( 
								
							"platoonId",
							platoonInformation.getId()
							
						)
						
					);
					
				}
				
			}
			
		} catch( Exception ex ) {
		
			ex.printStackTrace();
			return false;
			
		}

		return true;
	}
	
	public Dialog generateDialogPost(final Context context) {
		
		//Attributes
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	    final View layout = inflater.inflate(R.layout.dialog_newpost, (ViewGroup) findViewById(R.id.dialog_root));
		
	    //Set the title and the view
		builder.setTitle("New wall post");
		builder.setView(layout);

		//Grab the fields
		final EditText fieldMessage = (EditText) layout.findViewById(R.id.field_message);
		
		//Dialog options
		builder.setNegativeButton(
				
			android.R.string.cancel, 
			
			new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int whichButton) { 
					
					dialog.dismiss(); 
					
				}
				
			}
			
		);
			 
		builder.setPositiveButton(
				
			android.R.string.ok, 
			new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
			      
					
					new AsyncPostToWall(
							
						context, 
						platoonData.getId()
						
					).execute(
							
						sharedPreferences.getString( "battlelog_post_checksum", "" ),
						fieldMessage.getText().toString()
						
					);
			   
				}
				
			}
			
		);
		
		//CREATE
		return builder.create();
		
	}
}