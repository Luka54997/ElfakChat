package com.example.elfakchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateSettings;
    private EditText username,userStatus;
    private CircleImageView profileImage;
    private String userID;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private TextInputLayout usernameLayout;
    private static final int galleryPick = 1;
    private StorageReference userImageRef;
    private ProgressDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference();
        userImageRef = FirebaseStorage.getInstance().getReference().child("Profile image");

        BindViews();

        username.setVisibility(View.INVISIBLE);
        usernameLayout.setVisibility(View.INVISIBLE);

        updateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateUserInfo();
            }
        });

        FetchUserInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickImage();
            }
        });
    }

    private void PickImage() {
        CropImage.startPickImageActivity(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if(requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK ){

            Uri imageUri = CropImage.getPickImageResultUri(this,data);
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);





        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                dialog.setTitle("Set profile image");
                dialog.setMessage("Please wait, your profile image is being updated");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                Uri resultUri = result.getUri();

                final StorageReference reference = userImageRef.child(userID + ".jpg");
                reference.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            String downloadedUrl = task.getResult().getDownloadUrl().toString();
                            Log.d("Url",downloadedUrl);

                            ref.child("Users").child(userID).child("image").setValue(downloadedUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        dialog.dismiss();
                                    }
                                    else{
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });

                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
            }

        }


    }

    private void FetchUserInfo() {

        ref.child("Users").child(userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                            String fetchUserName = dataSnapshot.child("name").getValue().toString();
                            String fetchStatus = dataSnapshot.child("status").getValue().toString();
                            String fetchProfileImage = dataSnapshot.child("image").getValue().toString();


                            username.setText(fetchUserName);
                            userStatus.setText(fetchStatus);
                            Picasso.get().load(fetchProfileImage).into(profileImage);

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                            String fetchUserName = dataSnapshot.child("name").getValue().toString();
                            String fetchStatus = dataSnapshot.child("status").getValue().toString();


                            username.setText(fetchUserName);
                            userStatus.setText(fetchStatus);

                        }
                        else {
                            usernameLayout.setVisibility(View.VISIBLE);
                            username.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Update your profile information", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void UpdateUserInfo() {

        String userName = username.getText().toString();
        String userstatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(this,"Username cannot be empty!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(userstatus))
        {
            Toast.makeText(this,"Status cannot be empty!",Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            HashMap<String,Object> profileInfo = new HashMap<>();
            profileInfo.put("uid",userID);
            profileInfo.put("name",userName);
            profileInfo.put("status",userstatus);

            ref.child("Users").child(userID).updateChildren(profileInfo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,message.substring(message.lastIndexOf(":")+1),Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void BindViews() {
        updateSettings = findViewById(R.id.btn_update_user);
        username = findViewById(R.id.user_name);
        userStatus = findViewById(R.id.user_status);
        profileImage = findViewById(R.id.profile_image);
        usernameLayout = findViewById(R.id.username_layout);
        dialog = new ProgressDialog(this);
    }


}
