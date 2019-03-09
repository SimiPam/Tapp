package com.example.pam.tapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter <String> arrayAdapter;
    private ArrayList <String> list_of_groups = new ArrayList<>();
    private DatabaseReference GroupRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");   //reference to the group node

        InitializeFields();

        RetrieveAndDisplayGroups();
// a listener that will check if any item on the list view is clicked
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String currentGroupName = adapterView.getItemAtPosition(position).toString();   //get the name from the position and store in the string

                //move user to the group chat activity were the messages are sent and received
                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                //send the group name clicked to the next activity
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        return groupFragmentView;
    }



    private void InitializeFields() {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);  //we use group fragment view since we are using fragment views here
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);   //get context is our group fragment, simple list item is the layout ot display output
        list_view.setAdapter(arrayAdapter); //set the arrayadapter on the list view
    }

//to retrieve and display groups
    private void RetrieveAndDisplayGroups() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator(); //iterator that will go thru line by line through each group node

                while (iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey()); //to append the list, meaning to store qithout duplicates, the get key method will get all the group names
                }
                list_of_groups.clear(); //clear current list
                list_of_groups.addAll(set); //add all the group names
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
