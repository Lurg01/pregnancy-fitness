package com.example.pregnancyfitness;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.pregnancyfitness.databinding.ActivitySettingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    private ActivitySettingBinding binding;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String uid = currentFirebaseUser.getUid();
    Handler handler = new Handler();
    DBHelper DB;
    View notificationIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bottomNav();

        // CONFIGURE PROGRESS DIALOG
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Updating your Information . . .");
        progressDialog.setCanceledOnTouchOutside(false);


        databaseReference.child("Users")
                .child(uid)
                .child("User Private Info")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String k = childSnapshot.getKey();

                            databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("firstname").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String firstname = dataSnapshot.getValue(String.class);
                                            binding.fetchFirstname.setText(firstname);
                                            binding.editFirstname.setText(firstname);

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });

                            databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("lastname").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String lastname = dataSnapshot.getValue(String.class);
                                            binding.fetchLastname.setText(lastname);
                                            binding.editLastname.setText(lastname);

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });


                            databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("email").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String email = dataSnapshot.getValue(String.class);
                                            binding.fetchEmail.setText(email);

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });

                            databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("password").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String password = dataSnapshot.getValue(String.class);
                                            binding.fetchPassword.setText(password);

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.fetchInfoContent.setVisibility(View.VISIBLE);
                binding.editInfoContent.setVisibility(View.GONE);
                binding.back.setVisibility(View.GONE);
            }
        });

        binding.editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.fetchInfoContent.setVisibility(View.GONE);
                binding.editInfoContent.setVisibility(View.VISIBLE);
                binding.back.setVisibility(View.VISIBLE);

            }
        });

        binding.onChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.onChangeEmailContent.setVisibility(View.GONE);
                binding.changeEmailContent.setVisibility(View.VISIBLE);
            }
        });

        binding.onCancelEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.onChangeEmailContent.setVisibility(View.VISIBLE);
                binding.changeEmailContent.setVisibility(View.GONE);
                binding.changeEmail.setText("");
            }
        });

        binding.onChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.onChangePasswordContent.setVisibility(View.GONE);
                binding.changePasswordContent.setVisibility(View.VISIBLE);

            }
        });

        binding.onCancelPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.onChangePasswordContent.setVisibility(View.VISIBLE);
                binding.changePasswordContent.setVisibility(View.GONE);
                binding.changePassword.setText("");
            }
        });

        binding.updateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // SHOW PROGRESS DIALOG
                progressDialog.show();

                if (binding.fetchPassword.getText().toString().equals(Objects.requireNonNull(binding.currentPassword.getText()).toString()))
                {
                    updateCredential();
                }
                else if (Objects.requireNonNull(binding.editFirstname.getText()).toString().equals("") ||
                                Objects.requireNonNull(binding.editLastname.getText()).toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(), "You have Empty Credentials!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else if (binding.currentPassword.getText().toString().equals("") )
                {
                    Toast.makeText(getApplicationContext(), "Enter your current Password!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
                else{ Toast.makeText(getApplicationContext(), "Incorrect Confirmation!", Toast.LENGTH_SHORT).show();    progressDialog.dismiss();}

            }
        });

    }

    private void updateCredential() {
        // lurgino.buerano@lspu.edu.ph

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(binding.fetchEmail.getText().toString(), binding.fetchPassword.getText().toString());


        if (binding.changePassword.getText().toString().equals("") && binding.changeEmail.getText().toString().equals(""))
        {
            changeFullName();
            Toast.makeText(SettingActivity.this, "Updated Successful", Toast.LENGTH_SHORT).show();
            binding.fetchInfoContent.setVisibility(View.VISIBLE);
            binding.editInfoContent.setVisibility(View.GONE);
            progressDialog.dismiss();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            },1000);

        }
        else if (!binding.changeEmail.getText().toString().equals("") && binding.changePassword.getText().toString().equals(""))
        {

            changeFullName();

            databaseReference.child("Users")
                    .child(uid)
                    .child("User Private Info")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String k = childSnapshot.getKey();

                                assert k != null;

                                databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("email")
                                        .setValue(Objects.requireNonNull(binding.changeEmail.getText()).toString());

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });

// Prompt the user to re-provide their sign-in credentials
            assert user != null;
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updateEmail(Objects.requireNonNull(binding.changeEmail.getText()).toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                        if (task.isSuccessful()) {
                                            //                                        Log.d(TAG, "Password updated");
                                            Toast.makeText(SettingActivity.this, "Updated Successful", Toast.LENGTH_SHORT).show();
                                            binding.fetchInfoContent.setVisibility(View.VISIBLE);
                                            binding.editInfoContent.setVisibility(View.GONE);
                                            progressDialog.dismiss();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    recreate();
                                                }
                                            },1000);

                                        } else {
                                            //                                        Log.d(TAG, "Error password not updated");
                                            Toast.makeText(SettingActivity.this, "Error update", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }


                                    }
                                });


                            } else {
//                            Log.d(TAG, "Error auth failed");
                                Toast.makeText(SettingActivity.this,  "Error auth for Email", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }
                        }
                    });

        }

        else if (binding.changeEmail.getText().toString().equals("") && !binding.changePassword.getText().toString().equals(""))
        {

            changeFullName();

            databaseReference.child("Users")
                    .child(uid)
                    .child("User Private Info")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String k = childSnapshot.getKey();

                                assert k != null;

                                databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("password")
                                        .setValue(Objects.requireNonNull(binding.changePassword.getText()).toString());

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });

// Prompt the user to re-provide their sign-in credentials
            assert user != null;
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(Objects.requireNonNull(binding.changePassword.getText()).toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {


                                        if (task.isSuccessful()) {
                                            //                                        Log.d(TAG, "Password updated");
                                            Toast.makeText(SettingActivity.this, "Updated Successful", Toast.LENGTH_SHORT).show();
                                            binding.fetchInfoContent.setVisibility(View.VISIBLE);
                                            binding.editInfoContent.setVisibility(View.GONE);
                                            progressDialog.dismiss();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    recreate();
                                                }
                                            },1000);

                                        } else {
                                            //                                        Log.d(TAG, "Error password not updated");
                                            Toast.makeText(SettingActivity.this, "Error update", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }


                                    }
                                });


                            } else {
//                            Log.d(TAG, "Error auth failed");
                                Toast.makeText(SettingActivity.this,  "Error auth for Password", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }
                        }
                    });

        }

        else
        {

            changeFullName();

            databaseReference.child("Users")
                    .child(uid)
                    .child("User Private Info")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String k = childSnapshot.getKey();
                                assert k != null;
                                databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("email")
                                        .setValue(Objects.requireNonNull(binding.changeEmail.getText()).toString());
                                databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("password")
                                        .setValue(Objects.requireNonNull(binding.changePassword.getText()).toString());

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });

// Prompt the user to re-provide their sign-in credentials
            assert user != null;
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updateEmail(Objects.requireNonNull(binding.changeEmail.getText()).toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        user.updatePassword(Objects.requireNonNull(binding.changePassword.getText()).toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
//                                        Log.d(TAG, "Password updated");
                                                    Toast.makeText(SettingActivity.this, "Updated Successful", Toast.LENGTH_SHORT).show();
                                                    binding.fetchInfoContent.setVisibility(View.VISIBLE);
                                                    binding.editInfoContent.setVisibility(View.GONE);
                                                    progressDialog.dismiss();
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            recreate();
                                                        }
                                                    },1000);

                                                } else {
//                                        Log.d(TAG, "Error password not updated");
                                                    Toast.makeText(SettingActivity.this, "Error update", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });

                                    }
                                });


                            } else {
//                            Log.d(TAG, "Error auth failed");
                                Toast.makeText(SettingActivity.this,  "Error auth for Email and Password", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }
                        }
                    });

        }

    }

    private void changeFullName()
    {
        databaseReference.child("Users")
                .child(uid)
                .child("User Private Info")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String k = childSnapshot.getKey();

                            assert k != null;
                            databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("firstname")
                                    .setValue(Objects.requireNonNull(binding.editFirstname.getText()).toString());

                            databaseReference.child("Users").child(uid).child("User Private Info").child(k).child("lastname")
                                    .setValue(Objects.requireNonNull(binding.editLastname.getText()).toString());

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

    }

    private void bottomNav() {

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.settings);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.calendar:
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.status:
                        startActivity(new Intent(getApplicationContext(), StatusActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.notifications:
                        startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        return true;

                }
                return false;
            }
        });

        notificationIndicator = LayoutInflater.from(this).inflate(R.layout.notification_action_item,bottomNavigationView, false);
        bottomNavigationView.addView(notificationIndicator);

        DB = new DBHelper(this);
        Cursor getNotify = DB.getNotification();
        if (getNotify.getCount() == 0)
        {
            notificationIndicator.setVisibility(View.INVISIBLE);
        }

    }
}