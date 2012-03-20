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

package com.ninetwozero.battlelog.fragments;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ninetwozero.battlelog.AssignmentView;
import com.ninetwozero.battlelog.ProfileSettingsView;
import com.ninetwozero.battlelog.ProfileView;
import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.SearchView;
import com.ninetwozero.battlelog.UnlockView;
import com.ninetwozero.battlelog.datatypes.DefaultFragment;
import com.ninetwozero.battlelog.datatypes.PlatoonData;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.DataBank;
import com.ninetwozero.battlelog.misc.SessionKeeper;

public class MenuPlatoonFragment extends Fragment implements DefaultFragment {

    // Attributes
    private Context context;
    private LayoutInflater layoutInflater;
    private Map<Integer, Intent> MENU_INTENTS;
    private SharedPreferences sharedPreferences;
    
    //Elements
    private RelativeLayout wrapPlatoon;
    private TextView textPlatoon;
    private ImageView imagePlatoon;
    
    //Let's store the position & platoon
    private PlatoonData[] platoon;
    private long[] platoonId;
    private String[] platoonName;
    private long selectedPlatoon;
    private int selectedPosition;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Set our attributes
        context = getActivity();
        layoutInflater = inflater;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        // Let's inflate & return the view
        View view = layoutInflater.inflate(R.layout.tab_content_dashboard_platoon,
                container, false);

        initFragment(view);
        
        return view;

    }
    
    public void initFragment(View view) {
        
        //Let's set the vars
        
        //Set up the Platoon box
        wrapPlatoon = (RelativeLayout) view.findViewById(R.id.wrap_platoon);
        textPlatoon = (TextView) wrapPlatoon.findViewById(R.id.text_platoon);
        textPlatoon.setSelected(true);
        imagePlatoon = (ImageView) wrapPlatoon.findViewById(R.id.image_platoon);
        wrapPlatoon.setOnClickListener( 
                
            new OnClickListener() {

                @Override
                public void onClick(View v) {
          
                    generateDialogPlatoonList().show();

                    
                }
                
            }
            
        );
        
        //Setup the "platoon box"
        setupPlatoonBox();
        
        //Set up the intents
        /*MENU_INTENTS = new HashMap<Integer, Intent>();
        MENU_INTENTS.put(R.id.button_unlocks,
                        new Intent(context, UnlockView.class).putExtra("profile",
                                SessionKeeper.getProfileData()));
        MENU_INTENTS.put(R.id.button_assignments,
                        new Intent(context, AssignmentView.class).putExtra("profile",
                                SessionKeeper.getProfileData()));
        MENU_INTENTS.put(R.id.button_search, new Intent(context, SearchView.class));
        MENU_INTENTS.put(R.id.button_self,
                new Intent(context, ProfileView.class).putExtra("profile",
                        SessionKeeper.getProfileData()));        
        
        MENU_INTENTS.put(R.id.button_settings,
                new Intent(context, ProfileSettingsView.class));

        //Add the OnClickListeners
        for( int key : MENU_INTENTS.keySet() ) {
            
            view.findViewById(key).setOnClickListener( new OnClickListener() {

                @Override
                public void onClick(View v) {

                    startActivity(MENU_INTENTS.get(v.getId()));
                    
                }} );
        
        }*/
        
    }

    @Override
    public void reload() {
    }

    @Override
    public Menu prepareOptionsMenu(Menu menu) {
        return menu;
    }

    @Override
    public boolean handleSelectedOption(MenuItem item) {
        return false;
    }

    public Dialog generateDialogPlatoonList() {

        // Attributes
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        // Set the title and the view
        builder.setTitle(R.string.info_dialog_soldierselect);

        //Do we have items to show?
        if( platoonId == null ) {

            //Init
            platoonId = new long[platoon.length];
            platoonName = new String[platoon.length];
            
            //Iterate
            for( int count = 0, max = platoon.length; count < max; count++ ) {

                platoonId[count] = platoon[count].getId();
                platoonName[count] = platoon[count].getName();
                
            }
            
        }
        
        //Set it up
        builder.setSingleChoiceItems(

                platoonName, -1, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        if (platoonId[item] != selectedPlatoon) {

                            // Update it
                            selectedPlatoon = platoonId[item];
                            
                            // Store selectedPlatoonPos
                            selectedPosition = item;
                            
                            // Load the new!
                            setupPlatoonBox();
                            
                        }

                        dialog.dismiss();

                    }

                }

                );

        // CREATE
        return builder.create();

    }
    
    public void setupPlatoonBox() {
        
        //Let's see...
        //textPlatoon.setText( platoon[selectedPosition].getName() );
        //imagePlatoon.setImageBitmap( BitmapFactory.decodeFile(platoon[selectedPosition].getImage()) );
        
    }
    
}
