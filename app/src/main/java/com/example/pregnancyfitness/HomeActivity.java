package com.example.pregnancyfitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.DashPathEffect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pregnancyfitness.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;



import androidx.appcompat.app.ActionBar;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.nfc.cardemulation.HostNfcFService;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// import de.hdodenhof.circleimageview.CircleImageView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.ValueEventListener;
// FOR BOTTOM NAVIGATION
import android.view.MenuItem;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.w3c.dom.Text;

public class HomeActivity extends AppCompatActivity {

    // FOR NAVIGATION DRAWER
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    ImageView imageMenu;
    // <
    ProgressDialog progressDialog;
    View notificationIndicator;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    // CARD VIEW
    CardView forAllTrimesters, firstTrimester, secondTrimester, thirdTrimester;
    private ActivityHomeBinding binding;

    // SK USES
    DBHelper DB;
    Handler h = new Handler();

    // INITIALIZE VARIABLE
    FirebaseAuth auth;
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String uid = currentFirebaseUser.getUid();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    String get_due_date = "", k = "";
    int total_months_to_int = 0;
    private final Handler handler = new Handler();
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();   // INIT FIREBASE AUTH

        // CONFIGURE PROGRESS DIALOG
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Your Trimester was not Already Set !.");
        progressDialog.setMessage("Connect to the Internet to set up your Trimester and proceed on Offline Mode . .");
        progressDialog.setCanceledOnTouchOutside(false);
        // <


        // CHECK NETWORK CONNECTION
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        if (connected){
            trimesterToPerform();
        }else{
            DB = new DBHelper(this);
            Cursor get_total_months = DB.getTotalMonths();
            if (get_total_months.getCount() != 0){
                if (get_total_months.moveToFirst()){
                    int total_months_to_int = Integer.parseInt(get_total_months.getString(0));
                    Toast.makeText(this, ""+total_months_to_int, Toast.LENGTH_SHORT).show();
                    if (total_months_to_int <= 3)
                    {

                        binding.firstTrimester.setEnabled(false);
                        binding.firstTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                        binding.firstTrimesterTxt.setText("---");
                        binding.firstTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                        binding.secondTrimester.setEnabled(false);
                        binding.secondTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                        binding.secondTrimesterTxt.setText("---");
                        binding.secondTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                    }
                    else if(total_months_to_int  <= 6)
                    {
                        binding.firstTrimester.setEnabled(false);
                        binding.firstTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                        binding.firstTrimesterTxt.setText("---");
                        binding.firstTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                        binding.thirdTrimester.setEnabled(false);
                        binding.thirdTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                        binding.thirdTrimesterTxt.setText("---");
                        binding.thirdTrimesterTxt.setTextColor(getResources().getColor(R.color.white));
                    }
                    else if (total_months_to_int  <= 9)
                    {

                        binding.secondTrimester.setEnabled(false);
                        binding.secondTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                        binding.secondTrimesterTxt.setText("---");
                        binding.secondTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                        binding.thirdTrimester.setEnabled(false);
                        binding.thirdTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                        binding.thirdTrimesterTxt.setText("---");
                        binding.thirdTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                    }
                    else
                    {
                        Toast.makeText(HomeActivity.this, "This month is not cover by any trimester", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            else
            {
                progressDialog.show();  // SHOW PROGRESS DIALOG
            }

        }
        // <

        // NAVIGATION DRAWER GETTING ID
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_View);
        // <

        // GET ID OF CARD VIEW
        forAllTrimesters = (CardView) findViewById(R.id.cardio_for_all_trimester);
        firstTrimester = (CardView) findViewById(R.id.first_trimester);
        secondTrimester = (CardView) findViewById(R.id.second_trimester);
        thirdTrimester = (CardView) findViewById(R.id.third_trimester);

        toggle = new ActionBarDrawerToggle(HomeActivity.this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Drawar click event
        // Drawer item Click event ------
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_drawer_home:
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_drawer_user_guide:
                        startActivity(new Intent(getApplicationContext(), UserGuide.class));
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_drawer_about_us:
                        startActivity(new Intent(getApplicationContext(), AboutUs.class));
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.nav_drawer_logout:

                        // Initialize alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        // Set title
                        builder.setTitle("Logout");
                        // Set message
                        builder.setMessage("Are you sure you want to logout ?");
                        // Positive yes button
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Toast.makeText(HomeActivity.this, "Redirect to login . .", Toast.LENGTH_SHORT).show();
                                signOut();

                            }
                        });
                        // Negative no button
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Dismiss dialog
                                dialogInterface.dismiss();
                            }
                        });
                        // Show dialog
                        builder.show();


                        drawerLayout.closeDrawers();
                        break;

                }

                return false;
            }
        });

        // App Bar Click Event
        imageMenu = findViewById(R.id.imageMenu);
        imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code Here
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        // ONCLICK CARD VIEW
        forAllTrimesters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),ExerciseActivity.class).putExtra("val", "0"));
            }
        });

        firstTrimester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ExerciseActivity.class).putExtra("val", "1"));

            }
        });
        secondTrimester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),ExerciseActivity.class).putExtra("val", "2"));

            }
        });

        thirdTrimester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),ExerciseActivity.class).putExtra("val", "3"));
            }
        });

        bottomNav();

        //  CAN BE MODIFY >>>>
     /*   createNotificationChannel(); // ALARM

        DB = new DBHelper(this);

        Cursor res = DB.getTime();
        if (res.moveToFirst())
        {

            String str = res.getString(0);


            char hour = str.charAt(1);
            char minute1 = str.charAt(5);
            char minute2 = str.charAt(6);

            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Calendar.MINUTE, hour);
            calendar.set(Calendar.SECOND,minute1+minute2);
            calendar.set(Calendar.MILLISECOND,0);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setAlarm();
                }
            },1500);
        }*/
        //  CAN BE MODIFY <<<<<<

    }



    @Override
    protected void onStart() {
        super.onStart();

           databaseReference.child("Users")
                .child(uid)
                .child("Calendar")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            k = childSnapshot.getKey();
                            assert k != null;

                            databaseReference.child("Users").child(uid).child("Calendar").child(k).child("end_date").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            get_due_date = dataSnapshot.getValue(String.class);

                                            databaseReference.child("Users").child(uid).child("Calendar").child(k).child("total_months").addValueEventListener(
                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            String total_months = dataSnapshot.getValue(String.class);
                                                            assert total_months != null;
                                                            total_months_to_int = Integer.parseInt(total_months);

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                        }
                                                    });

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeTrimester();
            }
        }, 1500);


        databaseReference.child("Users")
                .child(uid)
                .child("Calendar")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() == null) {

                            binding.firstTrimester.setEnabled(false);
                            binding.firstTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                            binding.firstTrimesterTxt.setText("---");
                            binding.firstTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                            binding.secondTrimester.setEnabled(false);
                            binding.secondTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                            binding.secondTrimesterTxt.setText("---");
                            binding.secondTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                            binding.thirdTrimester.setEnabled(false);
                            binding.thirdTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                            binding.thirdTrimesterTxt.setText("---");
                            binding.thirdTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                            Toast.makeText(HomeActivity.this, "Redirect to Setup . .", Toast.LENGTH_SHORT).show();


                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(HomeActivity.this, CalendarActivity.class));
                                }
                            },1000);


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

      private void changeTrimester()
    {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String str_date1 = sdf.format(Calendar.getInstance().getTime());

        try {
            Date current_date = sdf.parse(str_date1);
            Date due_date = sdf.parse(get_due_date);
            long start_date = current_date.getTime();
            long end_date = due_date.getTime();

            if (start_date <= end_date) {
                Period period = new Period(start_date, end_date, PeriodType.months());

                int months = period.getMonths();
                if (months != total_months_to_int) {
                    String months_to_str = String.valueOf(months);

                    databaseReference.child("Users").child(uid).child("Calendar").child(k).child("total_months")
                            .setValue(months_to_str);
                }

            } else {
                Toast.makeText(getApplicationContext()
                        , "The date you have set is not valid"
                        , Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void trimesterToPerform()
    {

        databaseReference.child("Users")
                .child(uid)
                .child("Calendar")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String k = childSnapshot.getKey();
                            assert k != null;

                            databaseReference.child("Users").child(uid).child("Calendar").child(k).child("total_months").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String trimester = dataSnapshot.getValue(String.class);
                                            assert trimester != null;
                                            int trimester_to_int = Integer.parseInt(trimester);
                                            if (trimester_to_int <= 3)
                                            {

                                                binding.firstTrimester.setEnabled(false);
                                                binding.firstTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                                                binding.firstTrimesterTxt.setText("---");
                                                binding.firstTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                                                binding.secondTrimester.setEnabled(false);
                                                binding.secondTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                                                binding.secondTrimesterTxt.setText("---");
                                                binding.secondTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                                            }
                                            else if(trimester_to_int <= 6)
                                            {
                                                binding.firstTrimester.setEnabled(false);
                                                binding.firstTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                                                binding.firstTrimesterTxt.setText("---");
                                                binding.firstTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                                                binding.thirdTrimester.setEnabled(false);
                                                binding.thirdTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                                                binding.thirdTrimesterTxt.setText("---");
                                                binding.thirdTrimesterTxt.setTextColor(getResources().getColor(R.color.white));
                                            }
                                            else if (trimester_to_int <= 9)
                                            {

                                                binding.secondTrimester.setEnabled(false);
                                                binding.secondTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                                                binding.secondTrimesterTxt.setText("---");
                                                binding.secondTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                                                binding.thirdTrimester.setEnabled(false);
                                                binding.thirdTrimester.setCardBackgroundColor(getResources().getColor(R.color.lightGray));
                                                binding.thirdTrimesterTxt.setText("---");
                                                binding.thirdTrimesterTxt.setTextColor(getResources().getColor(R.color.white));

                                            }
                                            else
                                            {
                                                Toast.makeText(HomeActivity.this, "This month is not cover by any trimester", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void setAlarm() {

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this,AlarmReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_IMMUTABLE); // 0 have changed to PendingIntent.FLAG_IMMUTABLE
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,pendingIntent);

    }

    private void createNotificationChannel() {
        Intent intent = new Intent(this,AlarmReceiver.class);
        if (Build.VERSION.SDK_INT >= 31){ // O have changed to R

            CharSequence name = "PregnancyFitnessReminderChannel";
            String description = "Channel For Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("foxandroid",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            this.startForegroundService(intent);

        }
        else {
            this.startService(intent);
        }

    }

    private void bottomNav()
    {

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId())
                {
                    case R.id.home:
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
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                        overridePendingTransition(0,0);
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

    private void signOut() {
        auth.signOut();

        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();

    }
}