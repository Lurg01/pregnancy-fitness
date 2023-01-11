package com.example.pregnancyfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;


import android.app.ProgressDialog;
import android.graphics.Color;

import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.net.ssl.SSLEngineResult;


public class SignUpActivity extends AppCompatActivity {

    // INITIALIZE
    TextInputEditText firstname, lastname, email, pass, confPass;
    Button sign_up, goto_login;
    RadioGroup radioGroup;
    RadioButton sk, participant;
    String txtFirstName, txtLastName, txtEmail, txtPass, txtConfPass;
    FirebaseAuth auth;
//    DBHelper DB;
    int click_count = 0;
    ProgressDialog progressDialog;
    ActionBar actionBar;

    public static class Global {
        public static String uid;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_sign_up);


        // CONFIGURE ACTION BAR
        actionBar = getSupportActionBar();
        actionBar.setTitle("SignUp");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // INIT FIREBASE AUTH
        auth = FirebaseAuth.getInstance();

        // CONFIGURE PROGRESS DIALOG
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Creating you account . . .");
        progressDialog.setCanceledOnTouchOutside(false);

        firstname = (TextInputEditText) findViewById(R.id.editTxt_fName);
        lastname = (TextInputEditText) findViewById(R.id.editTxt_lName);
        email = (TextInputEditText) findViewById(R.id.editTxt_email);
        pass = (TextInputEditText) findViewById(R.id.editTxt_pass);
        confPass = (TextInputEditText) findViewById(R.id.editTxt_confPass);
        sign_up = findViewById(R.id.signUp_btn);

        goto_login = findViewById(R.id.login_btn);



        // BUTTON SIGN UP
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // VALIDATE USER INPUT
                validateData();


            }
        });

        // BUTTON TO LOGIN ACTIVITY
        goto_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // goto previous activity when back button of action bar clicked
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Handler h = new Handler();
//        DB = new DBHelper(this);

        // get current user
        FirebaseUser firebaseUser = auth.getCurrentUser();
        // check if user is already logged in

        if (firebaseUser != null)
        {

            startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
            finish();


        }


    }
    private void validateData()
    {

        Handler h = new Handler();
        txtFirstName = firstname.getText().toString();
        txtLastName= lastname.getText().toString();
        txtEmail = email.getText().toString();
        txtPass = pass.getText().toString();
        txtConfPass = confPass.getText().toString();


        // validate data
        if(TextUtils.isEmpty(txtFirstName) || TextUtils.isEmpty(txtLastName) || TextUtils.isEmpty(txtEmail) ||
                TextUtils.isEmpty(txtPass) || TextUtils.isEmpty(txtConfPass)){
            Toast.makeText(SignUpActivity.this, "Please complete the required credentials !", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(txtConfPass))
        {

            Toast.makeText(SignUpActivity.this, "Confirm password !", Toast.LENGTH_SHORT).show();

        }
        else if(txtPass.length() < 6)
        {
            Toast.makeText(SignUpActivity.this,"Your password is too short !", Toast.LENGTH_SHORT).show();
            pass.setTextColor(Color.RED);

            if(click_count  < 2 )
            {
                click_count += 1;
            }
            else
            {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(SignUpActivity.this);
                dlgAlert.setMessage("Your password is too short !");
                dlgAlert.setTitle("Error Message...");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pass.setTextColor(Color.BLACK);
                }
            },1000);

        }else if(!txtPass.equals(txtConfPass))
        {

            Toast.makeText(SignUpActivity.this,"Your password did not match !", Toast.LENGTH_SHORT).show();
            confPass.setTextColor(Color.RED);

            if(click_count  < 2 )
            {
                click_count += 1;
            }
            else
            {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(SignUpActivity.this);
                dlgAlert.setMessage("Your password did not match ");
                dlgAlert.setTitle("Error Message...");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
            }

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    confPass.setTextColor(Color.BLACK);
                }
            },1000);

        }
        else
        {

            signUp(txtEmail,txtPass);

        }

    }

    private void signUp(String userEmail, String userPass) {

        // SHOW PROGRESS DIALOG
        progressDialog.show();
        final Handler h = new Handler();

//        DB = new DBHelper(this);


        auth.createUserWithEmailAndPassword(userEmail, userPass).addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // sigup success
                progressDialog.dismiss();
                // get user info
                FirebaseUser firebaseUser = auth.getCurrentUser();

                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                assert currentFirebaseUser != null;
                Global.uid = currentFirebaseUser.getUid();

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        DaoUsername dao = new DaoUsername();
                        GetUsername getU =  new GetUsername(txtFirstName, txtLastName, txtEmail, txtPass); // Convert input to int  >   Integer.parseInt(coins.getText().toString())
                        dao.add(getU).addOnSuccessListener(suc ->
                        {

                            assert firebaseUser != null;
                            String email = firebaseUser.getEmail();

                            Toast.makeText(SignUpActivity.this, "You're Successfully Sign up.\n" + email, Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                            finish();

                        });

                    }
                },1000);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // SIGNUP FAILED
                progressDialog.dismiss();
                Toast.makeText(SignUpActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


}