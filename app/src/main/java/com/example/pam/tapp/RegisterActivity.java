package com.example.pam.tapp;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAcountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();
//check if the signup link was clicked
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        //create account

        CreateAcountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        //get email and password
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
//check if the email is empty
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"pls enter email...", Toast.LENGTH_SHORT);
        }
        //check is the password is empty
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"pls enter password...", Toast.LENGTH_SHORT);
        }
        //if none are empty
        else
        {
            //set the loading bar to inform user of progress
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait while we create your New Account");
            //prevent the loading bar from getting dismissed when the user touches the screen
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            //creating the new user on firebase
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {  //a listener to check if the creation has been completed
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){  //if successful

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();   //get the phone id of the logged in user
                                String currentUserID = mAuth.getCurrentUser().getUid(); //get current user id
                                RootRef.child("Users").child(currentUserID).setValue(""); // add user id into a users table in the firebase database

                                RootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                SendUserToMainActivity(); //send user to main activity
                                Toast.makeText(RegisterActivity.this,"Account created!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss(); //remove loading bar
                            } else {  //if unsuccessful
                                String message = task.getException().toString(); //get the exception to a string
                                Toast.makeText(RegisterActivity.this," Error: "+message, Toast.LENGTH_SHORT).show(); //display the exception
                                loadingBar.dismiss(); //remove loading bar
                            }
                        }
                    });
        }
    }

    //to initialise the widjets in the register activity
    private void InitializeFields() {
//loading bar for the signup
        loadingBar = new ProgressDialog(this);
        //widgets
        CreateAcountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);

    }

    //intent to change to login activity
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,  LoginActivity.class);
        startActivity(loginIntent);
    }

    //intent to change to main activity
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        //so the user cannot go back if they press the back button
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
