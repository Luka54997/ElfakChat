package com.example.elfakchat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ChatsFragment extends Fragment {

    private View view;
    private RecyclerView chatRecycler;
    private DatabaseReference ref,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view =  inflater.inflate(R.layout.fragment_chats, container, false);

        chatRecycler = view.findViewById(R.id.chat_recycler_fragment);
        chatRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), new LinearLayoutManager(getContext()).getOrientation());
        chatRecycler.addItemDecoration(itemDecoration);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ref,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, final int position, @NonNull Contacts model) {

                String usersId = getRef(position).getKey();
                final String[] userImage = {"default_image"};


                userRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            if(dataSnapshot.hasChild("image")){

                                userImage[0] = dataSnapshot.child("image").getValue().toString();

                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.userProfileImage);

                            }

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            String userStatus = dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(userName);


                            if(dataSnapshot.child("state").hasChild("state")){

                                String state = dataSnapshot.child("state").child("state").getValue().toString();
                                String date = dataSnapshot.child("state").child("date").getValue().toString();
                                String time = dataSnapshot.child("state").child("time").getValue().toString();

                                if(state.equals("online")){

                                    holder.userStatus.setText("online");
                                }
                                else if(state.equals("offline")){

                                    holder.userStatus.setText("Last Seen:" + date + " " + time);
                                }
                            }

                            else{

                                holder.userStatus.setText("offline");
                            }



                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String userId = getRef(position).getKey();

                                    Intent intent = new Intent(getContext(),ChatActivity.class);
                                    intent.putExtra("user_id",userId);
                                    intent.putExtra("user_name",userName);
                                    intent.putExtra("user_image", userImage[0]);
                                    startActivity(intent);
                                }
                            });
                        }




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.find_friends_layout,viewGroup,false);

                return new ChatViewHolder(view);
            }
        };

        chatRecycler.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{


        CircleImageView userProfileImage;
        TextView userName,userStatus;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.profile_name);
            userStatus = itemView.findViewById(R.id.status);
        }
    }
}