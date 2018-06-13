package com.julie.letstalkart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText username, enter_country, enter_full_name, description;
    private Button btn_save;
    private CircleImageView profile_image;
    private ImageView cover_photo;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    String currentUserID;

    private StorageReference UserProfileImageRef;

    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        enter_country = (EditText)findViewById(R.id.enter_county);
        enter_full_name = (EditText)findViewById(R.id.enter_full_name);
        description = (EditText)findViewById(R.id.description);
        username = (EditText)findViewById(R.id.username);
        btn_save = (Button) findViewById(R.id.btn_save);
        profile_image = (CircleImageView)findViewById(R.id.profile_image);
        cover_photo = (ImageView)findViewById(R.id.cover_photo);

        //get firebase instance
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profileimages");




        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInfo();
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //check if snapshot exists
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.child("profileimages").getValue().toString();
                    //to display the profile photo using the picasso library;
                    // providing a placeholder as the profile image if the photo is not availabe
                    Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profile_image);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(requestCode == RESULT_OK){
                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Profile Stored successfully",Toast.LENGTH_SHORT).show();
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            //store link to the database
                            UsersRef.child("profileimages").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                   if(task.isSuccessful()){
                                       //intent to start the very same activity
                                       Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                       startActivity(selfIntent);
                                       Toast.makeText(SetupActivity.this,"Profile stored to the db successfully", Toast.LENGTH_SHORT).show();

                                   }else{
                                       String message = task.getException().getMessage();
                                       Toast.makeText(SetupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                   }
                                }
                            });
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void saveAccountSetupInfo() {
        String user_name = username.getText().toString();
        String full_name = enter_full_name.getText().toString();
        String setup_country = enter_country.getText().toString();
        String art_description = description.getText().toString();

        if(TextUtils.isEmpty(user_name)){
            Toast.makeText(SetupActivity.this, "Enter your username", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(full_name)){
            Toast.makeText(SetupActivity.this, "Enter your Full name", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(setup_country)){
            Toast.makeText(SetupActivity.this, "Enter your country name", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(art_description)){
            Toast.makeText(SetupActivity.this, "Enter description", Toast.LENGTH_SHORT).show();
        }else{
            HashMap userMap = new HashMap();
             userMap.put("Username", user_name);
             userMap.put("FullName", full_name);
             userMap.put("Country", setup_country);
             userMap.put("Status", "Hey There;Lets Talk Art");
             userMap.put("dob", "");




             UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                 @Override
                 public void onComplete(@NonNull Task task) {
                     if(task.isSuccessful()){
                         sendUserToMainActivity();
                         Toast.makeText(SetupActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                     }else{
                         //gets the error that is causing the account creation to fail and stores it in the variable message
                         //which is then displayed by the toast message
                         String message = task.getException().getMessage();
                         Toast.makeText(SetupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();


                     }
                 }
             });
        }

    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
