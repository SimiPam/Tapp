package com.example.pam.tapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RootRef = FirebaseDatabase.getInstance().getReference();

//to load and show the toolbar on the load of the project
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        //set the name of app in toolbar
        getSupportActionBar().setTitle("Tapp");
//get current user
        mAuth = FirebaseAuth.getInstance();

//to set the tabs for the contacts, chats and groups
        myViewPager =(ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);


        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }
//when the app is started
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
//if user is not logged in then send the user to the login activity
        if (currentUser == null) {
            SendUserToLoginActivity();
        }
        else { //if user is logged in
            VerifyUserExistence();
            updateUserStatus( "online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus( "offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus( "offline");
        }
    }

    private void VerifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid(); //get user id of current user
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {   //check if the value has been entered into child
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()) //if the user has their name value and name node in the database
                {
                    Toast.makeText(MainActivity.this,"welcome",Toast.LENGTH_SHORT).show();
                }
                else { //if no, then the user is a new user
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //making the menu to show on the app main page
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //assess menu xml file
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.main_logout_option) //if the logout option is selected
        {
            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if (item.getItemId()==R.id.main_settings_option)    //if the setting option is selected
        {
            SendUserToSettingsActivity();
        }
        if (item.getItemId()==R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }
        if (item.getItemId()==R.id.main_creaete_group_option)   //if group is selected
        {
            RequestNewGroup();
        }

        return true;
    }
// method to name and create new group
    private void RequestNewGroup() {
        // a dialog box to prompt user to enter group name
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter group name: ");
        final EditText groupNameField = new EditText(MainActivity.this); //textfield to be added to dialog box
        groupNameField.setHint("e.g Friendzoned");
        builder.setView(groupNameField); //adding text field to dialog box

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {     //to listen if the positive button called "Create" was clicked
            @Override
            public void onClick(DialogInterface dialog, int which) {    //when clicked
                String groupName = groupNameField.getText().toString(); //get the name from the textfield

                if (TextUtils.isEmpty(groupName)) {     //check if the name is empty
                    Toast.makeText(MainActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();     //notfiy user that its empty
                } else { //if not empty
                    CreateNewGroup(groupName); //create group
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { //the cancel button click listener
            @Override
            public void onClick(DialogInterface dialog, int which) {    //if clicked
                dialog.cancel();    //close the dialog
            }
        });

        builder.show(); //show the dialog box

    }
//method to create the group
    private void CreateNewGroup(String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")   //create the groups table in the database with a null value
                .addOnCompleteListener(new OnCompleteListener<Void>() {     //check if the addition has been completed
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {      //if successful
                            Toast.makeText(MainActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show(); //notify user
                        }
                    }
                });
    }

    //method to send the user to login activity
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        //so the user cannot go back if they press the back button
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    //method to send the user to login activity
    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
    private void SendUserToFindFriendsActivity() {
        Intent friendIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(friendIntent);

    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        //get date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy"); // set the date format
        saveCurrentDate = currentDateFormat.format(calendar.getTime()); //get the dte in the format and assign it to a string

        //get time
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");   //get time in 12 hour format
        saveCurrentTime = currentTimeFormat.format(calendar.getTime()); //save time to string


        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }

}
