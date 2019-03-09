package com.example.pam.tapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {


    private Button LoginButton, PhoneLoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgetPasswordLink;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//get current user
        mAuth = FirebaseAuth.getInstance();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        InitializeFields();

        //check if the signup link was clicked
        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
//when a user decide to login in with phone
        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
    }

    private void AllowUserToLogin() {
        //get email and password
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
//check if the email is empty
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(LoginActivity.this,"pls enter email...", Toast.LENGTH_SHORT).show();
        }
        //check is the password is empty
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(LoginActivity.this,"pls enter password...", Toast.LENGTH_SHORT).show();
        }
        //if none are empty
        else
        {
            //set the loading bar to inform user of progress
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait while we sign you in ");
            //prevent the loading bar from getting dismissed when the user touches the screen
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)        //sign user in
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {       //check if the sign in has been completed
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){       //if successful

                        String currentUserID = mAuth.getCurrentUser().getUid(); //get user id
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();   //get the phone id of the logged in user
                        //save phone id as part of current users attributes
                        UsersRef.child(currentUserID).child("device_token")
                                .setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            SendUserToMainActivity();       //send user to the main page
                                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });


                    } else {        //if unsuccessful
                        String message = task.getException().toString(); //get the exception to a string
                        Toast.makeText(LoginActivity.this," Error: "+message, Toast.LENGTH_SHORT).show(); //display the exception
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    //to initialise the widjets in the register activity
    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        PhoneLoginButton = (Button) findViewById(R.id.phone_login_button);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        NeedNewAccountLink = (TextView) findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        //loading bar for the signup
        loadingBar = new ProgressDialog(this);
    }

    //intent to change to main activity
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        //so the user cannot go back if they press the back button
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    //intent to change to register activity
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
