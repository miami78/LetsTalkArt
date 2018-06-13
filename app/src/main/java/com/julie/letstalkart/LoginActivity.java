package com.julie.letstalkart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText login_email, login_password;
    private Button btn_login, btn_reset_password, btn_signup;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_email = (EditText)findViewById(R.id.login_email);
        login_password = (EditText)findViewById(R.id.login_password);
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_reset_password = (Button)findViewById(R.id.btn_reset_password);
        btn_signup = (Button)findViewById(R.id.btn_signup);

        //get firebase Instance
        mAuth = FirebaseAuth.getInstance();
        //loading progress instance
        loadingBar = new ProgressDialog(this);

        btn_signup.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                userLogin();
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

    private void userLogin() {
        String email = login_email.getText().toString();
        String password = login_password.getText().toString();

        //adding validations
        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Please provide an email to login", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "Please provide a password to login", Toast.LENGTH_SHORT).show();
        }else{
            //displays a progress bar while the account is being created
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait while we log you in.....");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "You logged in Successfully", Toast.LENGTH_SHORT).show();
                        //dismisses the loading bar
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        //dismisses the loading bar
                        loadingBar.dismiss();

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

    private void sendUserToRegisterActivity(){
        Intent registerIntent = new Intent (LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        //finish();
    }


}
