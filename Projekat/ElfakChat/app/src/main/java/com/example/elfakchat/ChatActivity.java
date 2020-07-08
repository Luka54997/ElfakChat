package com.example.elfakchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderId;
    private TextView userName,userLastSeen;
    private CircleImageView profileImage;
    private Toolbar chatToolbar;
    private ImageButton sendButton,recordButton;
    private EditText messageText;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter adapter;
    private RecyclerView messagesRecycler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();

        messageSenderId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getExtras().get("user_id").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("user_image").toString();

        BindViews();

        userName.setText(messageReceiverName);

        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(profileImage);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               SendMessage();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSpeechInput();
            }
        });

    }


    private void GetSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(ChatActivity.this, "Your device doesnt support speech recognition", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10){

            if(resultCode == Activity.RESULT_OK && data != null){
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                messageText.setText(results.get(0));
            }
        }
    }






    private void BindViews() {


        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        final LayoutInflater inflater = this.getLayoutInflater();

        final View actionBarView = inflater.inflate(R.layout.chat_layout,null);

        actionBar.setCustomView(actionBarView);

        userName = actionBarView.findViewById(R.id.chat_user_name);
        profileImage = actionBarView.findViewById(R.id.chat_profile_image);
        userLastSeen = findViewById(R.id.chat_profile_last_seen);
        sendButton = findViewById(R.id.btn_chat_send);
        messageText = findViewById(R.id.input_chat_message);
        recordButton = findViewById(R.id.btn_chat_record);
        messagesRecycler = findViewById(R.id.chat_recycler);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesRecycler.setLayoutManager(linearLayoutManager);



        adapter = new MessagesAdapter(messagesList);

        messagesRecycler.setAdapter(adapter);







    }

    @Override
    protected void onStart() {
        super.onStart();

        ref.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);

                adapter.notifyDataSetChanged();

                messagesRecycler.smoothScrollToPosition(messagesRecycler.getAdapter().getItemCount());


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage(){

        final String messageInputText = messageText.getText().toString();

        if(TextUtils.isEmpty(messageInputText)){
            Toast.makeText(ChatActivity.this, "Enter message", Toast.LENGTH_SHORT).show();
            return;
        }
        else{

            String senderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;

            String receiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference messageRef = ref.child("Messages").child(messageSenderId).child(messageReceiverId).push();

            String messageId = messageRef.getKey();

            Map message = new HashMap();

            message.put("message",messageInputText);
            message.put("type","text");
            message.put("from",messageSenderId);

            Map messageDetails = new HashMap();

            messageDetails.put(senderRef + "/" + messageId,message);

            messageDetails.put(receiverRef + "/" + messageId,message);

            ref.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                   messageText.setText("");

                }
            });


        }
    }
}
