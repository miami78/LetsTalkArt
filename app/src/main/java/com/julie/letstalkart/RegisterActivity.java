package com.julie.letstalkart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText register_email, register_password, register_confirm_password;
    private Button sign_up_button, sign_in_button;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Getting views from the XML layout by Id and stoing them in Java objects.
        register_email = (EditText)findViewById(R.id.register_email);
        register_password = (EditText)findViewById(R.id.register_password);
        register_confirm_password = (EditText)findViewById(R.id.register_confirm_password);
        sign_up_button = (Button)findViewById(R.id.sign_up_button);
        sign_in_button = (Button)findViewById(R.id.sign_in_button);

        //get firebase instance
        mAuth = FirebaseAuth.getInstance();

        //loading progress instance
        loadingBar = new ProgressDialog(this);
        //setting up a listener on the signin button that when clicked the createNewAccount method is executed
        sign_up_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendUserToMainActivity();
        }
        super.onStart();
    }
/*
* This method validates that the user has inputed all the fields and that the password matches the confirm password
* If the above is verified then a new account is created for the user and afterwards the user is redirected to the setup Activity
* to setup profile details.
 */
    private void createNewAccount() {
        String email = register_email.getText().toString();
        String password = register_password.getText().toString();
        String confirm_password = register_confirm_password.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this,"Please provide an Email", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this,"Please input a password", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(confirm_password)){
            Toast.makeText(RegisterActivity.this,"Please confirm your password", Toast.LENGTH_SHORT).show();
        }else if(!password.equals(confirm_password)){
            Toast.makeText(RegisterActivity.this, "There is a mismatch in your password", Toast.LENGTH_SHORT).show();
        }else{
            //displays a progress bar while the account is being created
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait while we create your account.....");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful()){
                       sendUserToSetupActivity();
                       Toast.makeText(RegisterActivity.this, "You are authenticated", Toast.LENGTH_SHORT).show();
                       //dismisses the loading bar once the user is authenticated
                       loadingBar.dismiss();
                   }else{
                       //gets the error that is causing the account creation to fail and stores it in the variable message
                       //which is then displayed by the toast message
                       String message = task.getException().getMessage();
                       Toast.makeText(RegisterActivity.this, "Error:" +message, Toast.LENGTH_SHORT).show();
                       //dismisses the loading bar
                       loadingBar.dismiss();
                   }
                }
            });
        }

    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
