package com.example.pam.tapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment
{

    View RequestsFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference ChatRequestRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    String currentUserID;

    public RequestFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        myRequestList = (RecyclerView) RequestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return RequestsFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID), Contacts.class)
                .build();
// to get user name from the ids that sent requests to the current user
        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder > adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model)
            {
                //making buttons visible
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final  String list_user_id = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("received"))
                            {
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild("image"))
                                        {

                                            final String requestUserImage = dataSnapshot.child("image").getValue().toString();


                                            Picasso.get().load(requestUserImage).into(holder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("Wants to connect with you");

                                    //to remove thhe user that sent a request from the request to contact of both contacts
                                        //when the receiver of the request accepts it
                                        holder.itemView.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName+" Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        if (which ==0)
                                                        {
                                                            //to store the new contact into the current users id
                                                            ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                    .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        ContactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                                .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                //to remove the requests from both the sender and receiver of the request
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                            {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                        {
                                                                                             if (task.isSuccessful())
                                                                                             {
                                                                                                 ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                         .removeValue()
                                                                                                         .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                         {
                                                                                                             @Override
                                                                                                             public void onComplete(@NonNull Task<Void> task)
                                                                                                             {
                                                                                                                 if (task.isSuccessful())
                                                                                                                 {
                                                                                                                     Toast.makeText(getContext(),
                                                                                                                             "Contact Added successfully"
                                                                                                                             , Toast.LENGTH_SHORT).show(); //notify user
                                                                                                                 }
                                                                                                             }
                                                                                                         });
                                                                                             }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if (which ==1)
                                                        {
                                                                ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                            {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                {
                                                                                                    if (task.isSuccessful())
                                                                                                    {
                                                                                                        Toast.makeText(getContext(),
                                                                                                                "Request Removed successfully"
                                                                                                                , Toast.LENGTH_SHORT).show(); //notify user
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });
                                                        }//if ends
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });
                            }//sent requests
                            else if (type.equals("sent"))
                            {
                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Request Sent");
                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                //receive data of the user the request was sent to
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.hasChild("image"))
                                        {

                                            final String requestUserImage = dataSnapshot.child("image").getValue().toString();


                                            Picasso.get().load(requestUserImage).into(holder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("You have sent a request to"+requestUserName);

                                        //to remove thhe user that sent a request from the request to contact of both contacts
                                        //when the receiver of the request accepts it
                                        holder.itemView.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Cancel Chat Request"
                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(" Already Sent Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {

                                                            ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if (task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(),
                                                                                                            "Request Cancelled successfully"
                                                                                                            , Toast.LENGTH_SHORT).show(); //notify user
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                RequestsViewHolder holder = new RequestsViewHolder(view);
                return holder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public RequestsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
