package com.example.elfakchat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;

    public MessagesAdapter(List<Messages> userMessages){

        this.userMessages = userMessages;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessage,receiverMessage;
        public CircleImageView receiverImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.sender_message);
            receiverMessage = itemView.findViewById(R.id.receiver_message);
            receiverImage = itemView.findViewById(R.id.profile_image_chat);


        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {

                String messageSenderId = mAuth.getCurrentUser().getUid();

                Messages messages = userMessages.get(i);

                String fromUserId = messages.getFrom();
                String fromMessageType = messages.getType();

                ref = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("image")){

                            String profileImage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverImage);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if(fromMessageType.equals("text")){

                    messageViewHolder.receiverMessage.setVisibility(View.INVISIBLE);
                    messageViewHolder.receiverImage.setVisibility(View.INVISIBLE);
                    messageViewHolder.senderMessage.setVisibility(View.INVISIBLE);

                    if(fromUserId.equals(messageSenderId)){

                        messageViewHolder.senderMessage.setVisibility(View.VISIBLE);
                        messageViewHolder.senderMessage.setBackgroundResource(R.drawable.sender_message);
                        messageViewHolder.senderMessage.setText(messages.getMessage());
                    }
                    else{


                        messageViewHolder.receiverImage.setVisibility(View.VISIBLE);
                        messageViewHolder.receiverMessage.setVisibility(View.VISIBLE);


                        messageViewHolder.receiverMessage.setBackgroundResource(R.drawable.receiver_message);
                        messageViewHolder.receiverMessage.setText(messages.getMessage());

                    }

                }

    }



    @Override
    public int getItemCount() {
        return userMessages.size();
    }


}
