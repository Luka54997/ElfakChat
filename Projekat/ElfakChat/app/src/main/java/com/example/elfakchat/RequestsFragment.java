package com.example.elfakchat;


import android.content.Intent;
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
public class RequestsFragment extends Fragment {


    private View view;
    private RecyclerView requestRecycler;
    private DatabaseReference ref,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private Button acceptRequestButton;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_requests, container, false);

        requestRecycler = view.findViewById(R.id.request_recycler);
        requestRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        acceptRequestButton = view.findViewById(R.id.request_accept);

        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId = mAuth.getCurrentUser().getUid();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ref.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, final int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.request_accept).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel).setVisibility(View.VISIBLE);

                final String usersId = getRef(position).getKey();

                final DatabaseReference requestType = getRef(position).child("request_type").getRef();

                requestType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            String type = dataSnapshot.getValue().toString();

                            if(type.equals("received")){

                                userRef.child(usersId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild("image")){

                                            final String userName = dataSnapshot.child("name").getValue().toString();
                                            final String userStatus = dataSnapshot.child("status").getValue().toString();
                                            final String userImage = dataSnapshot.child("image").getValue().toString();

                                            holder.userName.setText(userName);
                                            holder.userStatus.setText(userStatus);
                                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.userImage);

                                        }
                                        else{

                                            final String userName = dataSnapshot.child("name").getValue().toString();
                                            final String userStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(userName);
                                            holder.userStatus.setText(userStatus);
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String user_id = getRef(position).getKey();

                                                Intent intent = new Intent(getContext(),ProfileActivity.class);
                                                intent.putExtra("user_id",user_id);
                                                startActivity(intent);




                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if(type.equals("sent")){

                                Button requestButton = holder.itemView.findViewById(R.id.request_accept);
                                requestButton.setText("Request Sent");

                                holder.itemView.findViewById(R.id.request_cancel).setVisibility(View.INVISIBLE);


                                userRef.child(usersId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild("image")){

                                            final String userName = dataSnapshot.child("name").getValue().toString();
                                            final String userStatus = dataSnapshot.child("status").getValue().toString();
                                            final String userImage = dataSnapshot.child("image").getValue().toString();

                                            holder.userName.setText(userName);
                                            holder.userStatus.setText(userStatus);
                                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.userImage);

                                        }
                                        else{

                                            final String userName = dataSnapshot.child("name").getValue().toString();
                                            final String userStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(userName);
                                            holder.userStatus.setText(userStatus);
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String user_id = getRef(position).getKey();

                                                Intent intent = new Intent(getContext(),ProfileActivity.class);
                                                intent.putExtra("user_id",user_id);
                                                startActivity(intent);




                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
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
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.find_friends_layout,viewGroup,false);
                RequestsViewHolder viewHolder = new RequestsViewHolder(view);
                return  viewHolder;
            }
        };

        requestRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImage;
        Button acceptButton,cancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.profile_name);
            userStatus = itemView.findViewById(R.id.status);
            userImage = itemView.findViewById(R.id.user_image);
            acceptButton = itemView.findViewById(R.id.request_accept);
            cancelButton = itemView.findViewById(R.id.request_cancel);
        }


    }
}
