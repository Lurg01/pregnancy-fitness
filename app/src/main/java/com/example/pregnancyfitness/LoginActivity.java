package com.example.pregnancyfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.database.Cursor;
import android.media.tv.TvContract;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.nfc.NfcAdapter;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.JobIntentService;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.concurrent.HandlerExecutor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    static int RC_SIGN_IN = 0;
    private static final String TAG = "Google Account";

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    LinearLayout background;
    RadioGroup radioGroup;
    RadioButton sk, participant;
    //    TextView onLoading;
    TextInputEditText email, pass;
    Button login, goto_signup;
    ImageView logo;
    CardView loginCardView;
    ProgressBar progressBar;
    String get, str, txtUsername, txtEmail, txtPass, txtConfPass;
    ProgressDialog progressDialog;
    private FirebaseAuth auth;
    int clickcount = 0;
//    DBHelper DB;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        // configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Logging in to your account . . .");
        progressDialog.setCanceledOnTouchOutside(false);

        // get data
        email = (TextInputEditText) findViewById(R.id.edit_emailAdd);
        pass = (TextInputEditText) findViewById(R.id.edit_pass);
        login = (Button) findViewById(R.id.login_btn);
        goto_signup = (Button) findViewById(R.id.signup_btn);
        background = (LinearLayout) findViewById(R.id.background);
        logo = (ImageView) findViewById(R.id.logo_img);
        loginCardView = (CardView) findViewById(R.id.login_cardView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

//        onLoading = (TextView) findViewById(R.id.on_loading);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { login();}
        });

        goto_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));

            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);   - NOT A COMMENT !!!

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.signIn_btn);
        //signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.signIn_btn) {
                    toSignIn();
                }
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // goto previous activity when back button of action bar clicked
        return super.onSupportNavigateUp();
    }

    private void toSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity()); - NOT A COMMENT !!!
            //GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(Login.this);


            Toast.makeText(this, "Sign-in Successful ! ", Toast.LENGTH_SHORT).show();

/*            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);*/

            // Signed in successfully, show authenticated UI.
            //updateUI(account); - NOT A COMMENT !!!
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // Log.w(TAG, "signInResult:failed code=" + e.getStatusCode()); - NOT A COMMENT !!!
            //Log.d("Failed", e.getMessage());
            Log.w("Error","signInResult:failed code = " + e.getStatusCode());

            // updateUI(null); - NOT A COMMENT !!!
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        Handler h = new Handler();
//        DB = new DBHelper(this);

        // check if the user already logged in using Google Sign in
        // if already logged in then open NavHomeActivity
        // get current Gmail user
//        Cursor res = DB.getUserTypeData();

   /*     if (res.moveToFirst()) {
            switch (res.getString(0))
            {
                case "Leader":
                    sk.setChecked(true);
                    break;
                case "Participant":
                    participant.setChecked(true);
                    break;
            }
        }
*/

        // check if user is already logged in using the email provided
        // if already logged in then open NavHomeActivity
        // get current user
        if (firebaseUser != null)
        {
            background.setBackgroundColor(getResources().getColor(R.color.white));
            logo.setVisibility(View.VISIBLE);
            loginCardView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            Toast.makeText(getApplicationContext(), "You're currently Login.", Toast.LENGTH_SHORT).show();

            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            String uid = currentFirebaseUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
/*
            databaseReference.child("Users")
                    .child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String k = childSnapshot.getKey();

                                assert k != null;
                                if (k.equals("User Profile"))
                                {
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                }
                                else
                                {
                                    startActivity(new Intent(getApplicationContext(), SignUpActivity.class));

                                }
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });*/


          /*  if (res.moveToFirst()) {
                switch (res.getString(0))
                {
                    case "Leader":

                        databaseReference.child("Users")
                                .child("SK")
                                .child(uid)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            String k = childSnapshot.getKey();

                                            assert k != null;
                                            if (k.equals("User Profile"))
                                            {
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            }
                                            else
                                            {
                                                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));

                                            }
                                            finish();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });

                        break;

                    case "Participant":
          *//*              DB = new DBHelper(this);
                        databaseReference.child("Users").child("Participant").child(uid).child("Total Coins").addValueEventListener(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String total_coins = dataSnapshot.getValue(String.class);
                                        DB.insertTotalCoinsData(total_coins);
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) { }
                                });
*//*
                        databaseReference.child("Users")
                                .child("Participant")
                                .child(uid)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            String k = childSnapshot.getKey();

                                            assert k != null;
                                            if (k.equals("User Profile"))
                                            {
                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            }
                                            // TO FIXXX !!!!!!!!!!!!
                                        *//*    else
                                            {
                                                startActivity(new Intent(getApplicationContext(), UserSignup.class));

                                            }*//*
                                            finish();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });


                        break;
                }
            }*/


        }
    }

    private void login()
    {

//        DB = new DBHelper(this);
        final Handler h = new Handler();
        //txtUsername = username.getText().toString();
        txtEmail = email.getText().toString();
        txtPass = pass.getText().toString();

        if( TextUtils.isEmpty(txtEmail) ||
                TextUtils.isEmpty(txtPass))
        {

            // When the user input empty credentials
            Toast.makeText(LoginActivity.this, "Email/Username and Password required !", Toast.LENGTH_SHORT).show();

        }
        else if(txtPass.length() < 6)
        {

            // when password is less than 6
            Toast.makeText(LoginActivity.this,"Your password is too short !", Toast.LENGTH_SHORT).show();
            pass.setTextColor(Color.RED);

            if(clickcount  < 2 )
            {
                clickcount += 1;
            }
            else
            {
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(LoginActivity.this);
                dlgAlert.setMessage("Please Enter Password atleast 6 character ...");
                dlgAlert.setTitle("Your password is too short !");
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

        }
        else
        {
            checkUserType();

            /*Cursor res = DB.getUserTypeData();
            int rG = radioGroup.getCheckedRadioButtonId();
            switch (rG) {

                case R.id.sk:

                    UserSignup.Global.user = "SK";
                    if (res.moveToFirst()) {
                        if (res.getString(0).equals("Participant")) {
                            DB.deleteUserTypeData(res.getString(0));
                            DB.insertUserTypeData(sk.getText().toString());
                            checkUserType();
                        }
                    }

                    checkUserType();

                    break;

                case R.id.participant:

                    UserSignup.Global.user = "Participant";
                    if (res.moveToFirst()) {
                        if (res.getString(0).equals("Leader")) {
                            DB.deleteUserTypeData(res.getString(0));
                            DB.insertUserTypeData(participant.getText().toString());
                            checkUserType();
                        }
                    }
                    checkUserType();
                    break;

                default:
                    Toast.makeText(UserLogin.this, "Please select user type !", Toast.LENGTH_SHORT).show();
                    break;
            }*/

        }

    }

    private void checkUserType() {

        progressDialog.show();

//        DB = new DBHelper(this);

        auth.signInWithEmailAndPassword(txtEmail, txtPass) .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressDialog.dismiss();
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();

//                Cursor res = DB.getUserTypeData();
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                String uid = currentFirebaseUser.getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
/*
                if (res.moveToFirst()) {
                    switch (res.getString(0))
                    {
                        case "Leader":
*/

                            // In case !!!!!!!!!
                    /*        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.hasChild("Users")) {

                                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                    }else
                                    {
                                        startActivity(new Intent(getApplicationContext(), UserSignup.class));
                                    }
                                    finish();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
*/

                            /*databaseReference.child("Users")
                                    .child("SK")
                                    .child(uid)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                String k = childSnapshot.getKey();

                                                assert k != null;
                                                if (k.equals("User Profile"))
                                                {
                                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                    finish();
                                                }
                                                else if (k.equals(""))
                                                {
                                                    startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                                                    finish();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
*/

                        /*    Toast.makeText(UserLogin.this, "Login Successful !", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();*/
              /*              break;

                        case "Participant":
*/
                         /*   databaseReference.child("Users")
                                    .child("Participant")
                                    .child(uid)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                String k = childSnapshot.getKey();

                                                assert k != null;
                                                if (k.equals("User Profile"))
                                                {
                                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                                    finish();
                                                }
                                                else if (k.equals(""))
                                                {
                                                    startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                                                    finish();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
*/

          /*                  break;
                    }
                }*/

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // login failed, get and show error message
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}