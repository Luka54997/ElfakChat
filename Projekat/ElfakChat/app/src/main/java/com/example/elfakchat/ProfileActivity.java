package com.example.elfakchat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.DatabaseMetaData;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,currentStat,senderUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequest,cancelMessageRequest;
    private DatabaseReference ref,requestRef,notificationRef;
    private FirebaseAuth mAuth;
    private DatabaseReference contactsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        receiverUserId = getIntent().getExtras().get("user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        userProfileImage = findViewById(R.id.prof_image);
        userProfileName = findViewById(R.id.user_name);
        userProfileStatus = findViewById(R.id.user_status);
        sendMessageRequest = findViewById(R.id.friend_request_btn);
        cancelMessageRequest = findViewById(R.id.friend_request_btn_cancel);
        currentStat = "new";
        requestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Friend Request Notifications");



        FetchUserData();
    }

    private void FetchUserData() {

        ref.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.hasChild("image")){

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageFriendsRequest();
                }
                else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageFriendsRequest();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ManageFriendsRequest() {

        requestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(receiverUserId)){

                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent") ){

                        currentStat = "request_sent";

                        sendMessageRequest.setText("Cancel Request");
                    }
                    else if(request_type.equals("received")){

                        currentStat = "request_received";
                        sendMessageRequest.setText("Accept Chat Request");

                        cancelMessageRequest.setVisibility(View.VISIBLE);
                        cancelMessageRequest.setEnabled(true);

                        cancelMessageRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CancelFriendRequest();

                            }
                        });
                    }
                }
                else{
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild(receiverUserId)){

                                currentStat = "friends";
                                sendMessageRequest.setText("remove Contact");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(!senderUserId.equals(receiverUserId)){

            sendMessageRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendMessageRequest.setEnabled(false);

                    if(currentStat.equals("new")){

                        SendFriendRequest();
                    }
                    if(currentStat.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(currentStat.equals("request_received")){

                        AcceptChatRequest();
                    }
                    if(currentStat.equals("friends")){

                        RemoveContact();
                    }
                }
            });
        }
        else{
            sendMessageRequest.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveContact() {

        contactsRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    contactsRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendMessageRequest.setEnabled(true);
                                currentStat = "new";
                                sendMessageRequest.setText("Send Message");
                                cancelMessageRequest.setVisibility(View.INVISIBLE);
                                cancelMessageRequest.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void AcceptChatRequest() {

        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved")
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){

                                                                        requestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if(task.isSuccessful()){

                                                                                    requestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            sendMessageRequest.setEnabled(true);
                                                                                            currentStat = "friends";
                                                                                            sendMessageRequest.setText("Remove Contact");

                                                                                            cancelMessageRequest.setVisibility(View.INVISIBLE);
                                                                                            cancelMessageRequest.setEnabled(false);

                                                                                        }
                                                                                    }) ;
                                                                                }

                                                                            }
                                                                        }) ;
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
    }


    private void CancelFriendRequest() {

        requestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    requestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                sendMessageRequest.setEnabled(true);
                                currentStat = "new";
                                sendMessageRequest.setText("Send Message");
                                cancelMessageRequest.setVisibility(View.INVISIBLE);
                                cancelMessageRequest.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendFriendRequest() {

        requestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        requestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received")
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){

                                                                                        HashMap<String,String> requestNotification = new HashMap<>();

                                                                                        requestNotification.put("from",senderUserId);
                                                                                        requestNotification.put("type","request");

                                                                                        notificationRef.child(receiverUserId).push().setValue(requestNotification)
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if(task.isSuccessful()){

                                                                                                                    sendMessageRequest.setEnabled(true);
                                                                                                                    currentStat = "request_sent";

                                                                                                                    sendMessageRequest.setText("Cancel request");
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
