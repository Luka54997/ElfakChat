package com.example.elfakchat;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ContactsFragment extends Fragment {

    private View view;
    private RecyclerView contactsRecyclerView;
    private DatabaseReference ref,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;



    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contacts, container, false);


        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), new LinearLayoutManager(getContext()).getOrientation());
        contactsRecyclerView = view.findViewById(R.id.contacts_recycler);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactsRecyclerView.addItemDecoration(itemDecoration);
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ref,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                String userIds = getRef(position).getKey();


                userRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            if(dataSnapshot.child("state").hasChild("state")){



                                String state = dataSnapshot.child("state").child("state").getValue().toString();
                                String date = dataSnapshot.child("state").child("date").getValue().toString();
                                String time = dataSnapshot.child("state").child("time").getValue().toString();

                                if(state.equals("online")){




                                    holder.onlineImage.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline")){

                                    holder.onlineImage.setVisibility(View.INVISIBLE);
                                }
                            }

                            else{

                                holder.onlineImage.setVisibility(View.INVISIBLE);;
                            }


                            if(dataSnapshot.hasChild("image")){

                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();
                                String userImage = dataSnapshot.child("image").getValue().toString();

                                holder.userName.setText(userName);
                                holder.userStatus.setText(userStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.userImage);

                            }
                            else{
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(userName);
                                holder.userStatus.setText(userStatus);

                            }
                        }





                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.find_friends_layout,viewGroup,false);

                ContactsViewHolder viewHolder = new ContactsViewHolder(view);

                return viewHolder;
            }
        };

        contactsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends  RecyclerView.ViewHolder{

        TextView userName,userStatus;

        CircleImageView userImage;
        ImageView onlineImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.profile_name);
            userStatus = itemView.findViewById(R.id.status);
            userImage = itemView.findViewById(R.id.user_image);
            onlineImage = itemView.findViewById(R.id.online_status);
        }
    }
}


