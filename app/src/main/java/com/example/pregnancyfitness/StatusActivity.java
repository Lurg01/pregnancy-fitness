package com.example.pregnancyfitness;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pregnancyfitness.databinding.ActivityStatusBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Date;
import java.util.Objects;



// FOR EXPANDABLE LIST VIEW >>>

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StatusActivity extends AppCompatActivity {

    private ActivityStatusBinding binding;
    // FOR EXPANDABLE LIST VIEW >>>
    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> mobileCollection;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
// FOR EXPANDABLE LIST VIEW <<<
    View notificationIndicator;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String uid = currentFirebaseUser.getUid();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    DBHelper DB;
    final ArrayList<String> weekPercentageList = new ArrayList<>();
    final ArrayList<String> monthPercentageList = new ArrayList<>();
    FirebaseAuth auth;
    // FOR NAVIGATION DRAWER
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    //^
    double total_daily_percentage_for_week = 0, total_daily_percentage_for_month = 0;
    String daily_percentage_for_a_week = "", daily_percentage_for_a_month = "", weekly_percentage_to_list, monthly_percentage_to_list;
    public static class Global {
        public static String uid, weeks, months;

    }
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // INIT FIREBASE AUTH
        auth = FirebaseAuth.getInstance();

        // NAVIGATION DRAWER
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_View);
        toggle = new ActionBarDrawerToggle(StatusActivity.this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        // Drawer click event
        // Drawer item Click event ------
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_drawer_home:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0,0);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(StatusActivity.this);
                        // Set title
                        builder.setTitle("Logout");
                        // Set message
                        builder.setMessage("Are you sure you want to logout ?");
                        // Positive yes button
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Toast.makeText(StatusActivity.this, "Redirect to login . .", Toast.LENGTH_SHORT).show();
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

        // APP BAR CLICK EVENT
        binding.imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code Here
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        bottomNav();

        binding.babyProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.atBaby.setVisibility(View.VISIBLE);
                binding.atExercise.setVisibility(View.INVISIBLE);
                binding.exercisesProgressContent.setVisibility(View.GONE);
                binding.babyProgressContent.setVisibility(View.VISIBLE);
            }
        });
        binding.exercisesProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.atBaby.setVisibility(View.INVISIBLE);
                binding.atExercise.setVisibility(View.VISIBLE);
                binding.exercisesProgressContent.setVisibility(View.VISIBLE);
                binding.babyProgressContent.setVisibility(View.GONE);
            }
        });

        displayWeeklyPercentage();
        displayMonthlyPercentage();

// FOR EXPANDABLE LIST VIEW >>>>

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createGroupList();
                createCollection();
                expandableListView = findViewById(R.id.elvMobiles);
                expandableListAdapter = new MyExpandableListAdapter(StatusActivity.this, groupList, mobileCollection);
                expandableListView.setAdapter(expandableListAdapter);
                expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                    int lastExpandedPosition = -1;
                    @Override
                    public void onGroupExpand(int i) {
                        if(lastExpandedPosition != -1 && i != lastExpandedPosition){
                            expandableListView.collapseGroup(lastExpandedPosition);
                        }
                        lastExpandedPosition = i;
                    }
                });
                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                        expandableListAdapter.getChild(i,i1).toString();
                        return true;
                    }
                });
            }
        }, 1500);


// FOR EXPANDABLE LIST VIEW <<<<

        Global.uid = uid;

        String current_date = sdf.format(Calendar.getInstance().getTime());

        databaseReference.child("Users")
                .child(uid)
                .child("Calendar")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String k = childSnapshot.getKey();
                            assert k != null;
                            databaseReference.child("Users").child(uid).child("Calendar").child(k).child("start_date").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String date_started = dataSnapshot.getValue(String.class);
                                            getDate(date_started, current_date);

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

    private void bottomNav() {

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.status);

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
                        return true;
                    case R.id.notifications:
                        startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                        overridePendingTransition(0,0);
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

    private void getDate(String dt, String cd)
    {

        try
        {
            Date date_started_parse = sdf.parse(dt);
            Date current_date_parse = sdf.parse(cd);
//            String date_format = sdf.format(due_date_parse);
            long start_date = date_started_parse.getTime();
            long current_date = current_date_parse.getTime();
            int weeks, months;

            String weeks_to_str, months_to_str;
            if (start_date <= current_date)
            {
                Period periodWeeks = new Period(start_date, current_date, PeriodType.weeks());

                weeks =  periodWeeks.getWeeks();
                weeks_to_str = String.valueOf(weeks);

                Global.weeks = weeks_to_str; // TO DAO WEEK TRIMESTER

                int dw1 , dw7, dm1, dm30  ;

                switch (weeks_to_str)
                {
                    case "1":  dw1 = 1; dw7 = 7;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;
                    case "2":  dw1 = 8; dw7 = 14;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;
                    case "3":  dw1 = 15; dw7 = 21;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;

                    case "4":  dw1 = 22; dw7 = 28;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;
                    case "5":  dw1 = 29; dw7 = 35;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;
                    case "6":  dw1 = 36; dw7 = 42;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;

                    case "7":  dw1 = 43; dw7 = 49;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;

                    case "8":  dw1 = 50; dw7 = 56;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;
                    case "9":  dw1 = 57; dw7 = 63;
                        sumOfDayInAWeek(dw1, dw7, weeks_to_str);
//                        sumOfDayInAWeekForFirstTrimester(dw1, dw7, weeks_to_str);
                        break;

                    case "10":  dw1 = 64; dw7 = 70; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "11":  dw1 = 71; dw7 = 77;sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;     case "12":  dw1 = 78; dw7 = 84; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "13":  dw1 = 85; dw7 = 91; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;    case "14":  dw1 = 92; dw7 = 98; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "15":  dw1 = 99; dw7 = 105;sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;    case "16":  dw1 = 106; dw7 = 112; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "17":  dw1 = 113; dw7 = 119; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "18":  dw1 = 120; dw7 = 126; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "19":  dw1 = 127; dw7 = 133; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "20":  dw1 = 134; dw7 = 140; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "21":  dw1 = 141; dw7 = 147; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "22":  dw1 = 148; dw7 = 154; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "23":  dw1 = 155; dw7 = 161; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "24":  dw1 = 162; dw7 = 168; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "25":  dw1 = 169; dw7 = 175; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "26":  dw1 = 176; dw7 = 182; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "27":  dw1 = 183; dw7 = 189; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "28":  dw1 = 190; dw7 = 196; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "29":  dw1 = 197; dw7 = 203; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "30":  dw1 = 204; dw7 = 210; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "31":  dw1 = 211; dw7 = 217; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "32":  dw1 = 218; dw7 = 224; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;
                    case "33":  dw1 = 225; dw7 = 231; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;  case "34":  dw1 = 232; dw7 = 238; sumOfDayInAWeek(dw1, dw7, weeks_to_str); break;

                }

                Period periodMonths = new Period(start_date, current_date, PeriodType.months());
                months =  periodMonths.getMonths();
                getTrimester(weeks); // FOR GETTING TOTAL MONTH

                months_to_str = String.valueOf(months);
                Global.months = months_to_str; // TO DAO MONTH TRIMESTER

                switch (months_to_str)
                {

                    case "1": dm1 = 1; dm30 = 30; sumOfDayInAMonth(dm1, dm30, months_to_str); break;    case "2": dm1 = 31; dm30 = 60; sumOfDayInAMonth(dm1, dm30, months_to_str); break;
                    case "3": dm1 = 61; dm30 = 90;  sumOfDayInAMonth(dm1, dm30, months_to_str); break;
                }

            }

            else
            {
                Toast.makeText(getApplicationContext()
                        , "The date you have set is not valid"
                        , Toast.LENGTH_SHORT).show();
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }


    }

    private void getTrimester(int weeks) {

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
                                            babyProgress(trimester_to_int, weeks); // FOR BABY IMAGES PROGRESS
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

    private void babyProgress(int months, int weeks) {

        int total_weeks;
        switch (months)
        {

            case 1:
                total_weeks = 34 + weeks;
                switch (total_weeks)
                {
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 2:
                total_weeks = 30 + weeks;
                switch (total_weeks)
                {
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 3:
                total_weeks = 25 + weeks;
                switch (total_weeks)
                {

                    case 25: binding.baby.setImageResource(R.drawable.week25); break;
                    case 26: binding.baby.setImageResource(R.drawable.week26); break;
                    case 27: binding.baby.setImageResource(R.drawable.week27); break;
                    case 28: binding.baby.setImageResource(R.drawable.week28); break;
                    case 29: binding.baby.setImageResource(R.drawable.week29); break;
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 4:
                total_weeks = 21 + weeks;
                switch (total_weeks)
                {

                    case 21: binding.baby.setImageResource(R.drawable.week21); break;
                    case 22: binding.baby.setImageResource(R.drawable.week22); break;
                    case 23: binding.baby.setImageResource(R.drawable.week23); break;
                    case 24: binding.baby.setImageResource(R.drawable.week24); break;
                    case 25: binding.baby.setImageResource(R.drawable.week25); break;
                    case 26: binding.baby.setImageResource(R.drawable.week26); break;
                    case 27: binding.baby.setImageResource(R.drawable.week27); break;
                    case 28: binding.baby.setImageResource(R.drawable.week28); break;
                    case 29: binding.baby.setImageResource(R.drawable.week29); break;
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 5:
                total_weeks = 17 + weeks;
                switch (total_weeks)
                {
                    case 17: binding.baby.setImageResource(R.drawable.week17); break;
                    case 18: binding.baby.setImageResource(R.drawable.week18); break;
                    case 19: binding.baby.setImageResource(R.drawable.week19); break;
                    case 20: binding.baby.setImageResource(R.drawable.week20); break;
                    case 21: binding.baby.setImageResource(R.drawable.week21); break;
                    case 22: binding.baby.setImageResource(R.drawable.week22); break;
                    case 23: binding.baby.setImageResource(R.drawable.week23); break;
                    case 24: binding.baby.setImageResource(R.drawable.week24); break;
                    case 25: binding.baby.setImageResource(R.drawable.week25); break;
                    case 26: binding.baby.setImageResource(R.drawable.week26); break;
                    case 27: binding.baby.setImageResource(R.drawable.week27); break;
                    case 28: binding.baby.setImageResource(R.drawable.week28); break;
                    case 29: binding.baby.setImageResource(R.drawable.week29); break;
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 6:
                total_weeks = 12 + weeks;
                switch (total_weeks)
                {
                    case 12: binding.baby.setImageResource(R.drawable.week12); break;
                    case 13: binding.baby.setImageResource(R.drawable.week13); break;
                    case 14: binding.baby.setImageResource(R.drawable.week14); break;
                    case 15: binding.baby.setImageResource(R.drawable.week15); break;
                    case 16: binding.baby.setImageResource(R.drawable.week16); break;
                    case 17: binding.baby.setImageResource(R.drawable.week17); break;
                    case 18: binding.baby.setImageResource(R.drawable.week18); break;
                    case 19: binding.baby.setImageResource(R.drawable.week19); break;
                    case 20: binding.baby.setImageResource(R.drawable.week20); break;
                    case 21: binding.baby.setImageResource(R.drawable.week21); break;
                    case 22: binding.baby.setImageResource(R.drawable.week22); break;
                    case 23: binding.baby.setImageResource(R.drawable.week23); break;
                    case 24: binding.baby.setImageResource(R.drawable.week24); break;
                    case 25: binding.baby.setImageResource(R.drawable.week25); break;
                    case 26: binding.baby.setImageResource(R.drawable.week26); break;
                    case 27: binding.baby.setImageResource(R.drawable.week27); break;
                    case 28: binding.baby.setImageResource(R.drawable.week28); break;
                    case 29: binding.baby.setImageResource(R.drawable.week29); break;
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 7:
                total_weeks = 8 + weeks;
                switch (total_weeks)
                {

                    case 8: binding.baby.setImageResource(R.drawable.week8); break;
                    case 9: binding.baby.setImageResource(R.drawable.week9); break;
                    case 10: binding.baby.setImageResource(R.drawable.week10); break;
                    case 11: binding.baby.setImageResource(R.drawable.week11); break;
                    case 12: binding.baby.setImageResource(R.drawable.week12); break;
                    case 13: binding.baby.setImageResource(R.drawable.week13); break;
                    case 14: binding.baby.setImageResource(R.drawable.week14); break;
                    case 15: binding.baby.setImageResource(R.drawable.week15); break;
                    case 16: binding.baby.setImageResource(R.drawable.week16); break;
                    case 17: binding.baby.setImageResource(R.drawable.week17); break;
                    case 18: binding.baby.setImageResource(R.drawable.week18); break;
                    case 19: binding.baby.setImageResource(R.drawable.week19); break;
                    case 20: binding.baby.setImageResource(R.drawable.week20); break;
                    case 21: binding.baby.setImageResource(R.drawable.week21); break;
                    case 22: binding.baby.setImageResource(R.drawable.week22); break;
                    case 23: binding.baby.setImageResource(R.drawable.week23); break;
                    case 24: binding.baby.setImageResource(R.drawable.week24); break;
                    case 25: binding.baby.setImageResource(R.drawable.week25); break;
                    case 26: binding.baby.setImageResource(R.drawable.week26); break;
                    case 27: binding.baby.setImageResource(R.drawable.week27); break;
                    case 28: binding.baby.setImageResource(R.drawable.week28); break;
                    case 29: binding.baby.setImageResource(R.drawable.week29); break;
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 8:
                total_weeks = 4 + weeks;
                switch (total_weeks)
                {
                    case 4: binding.baby.setImageResource(R.drawable.week4); break;
                    case 5: binding.baby.setImageResource(R.drawable.week5); break;
                    case 6: binding.baby.setImageResource(R.drawable.week6); break;
                    case 7: binding.baby.setImageResource(R.drawable.week7); break;
                    case 8: binding.baby.setImageResource(R.drawable.week8); break;
                    case 9: binding.baby.setImageResource(R.drawable.week9); break;
                    case 10: binding.baby.setImageResource(R.drawable.week10); break;
                    case 11: binding.baby.setImageResource(R.drawable.week11); break;
                    case 12: binding.baby.setImageResource(R.drawable.week12); break;
                    case 13: binding.baby.setImageResource(R.drawable.week13); break;
                    case 14: binding.baby.setImageResource(R.drawable.week14); break;
                    case 15: binding.baby.setImageResource(R.drawable.week15); break;
                    case 16: binding.baby.setImageResource(R.drawable.week16); break;
                    case 17: binding.baby.setImageResource(R.drawable.week17); break;
                    case 18: binding.baby.setImageResource(R.drawable.week18); break;
                    case 19: binding.baby.setImageResource(R.drawable.week19); break;
                    case 20: binding.baby.setImageResource(R.drawable.week20); break;
                    case 21: binding.baby.setImageResource(R.drawable.week21); break;
                    case 22: binding.baby.setImageResource(R.drawable.week22); break;
                    case 23: binding.baby.setImageResource(R.drawable.week23); break;
                    case 24: binding.baby.setImageResource(R.drawable.week24); break;
                    case 25: binding.baby.setImageResource(R.drawable.week25); break;
                    case 26: binding.baby.setImageResource(R.drawable.week26); break;
                    case 27: binding.baby.setImageResource(R.drawable.week27); break;
                    case 28: binding.baby.setImageResource(R.drawable.week28); break;
                    case 29: binding.baby.setImageResource(R.drawable.week29); break;
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;

            case 9:
                switch (weeks)
                {
                    case 2: binding.baby.setImageResource(R.drawable.week2); break;
                    case 3: binding.baby.setImageResource(R.drawable.week3); break;
                    case 4: binding.baby.setImageResource(R.drawable.week4); break;
                    case 5: binding.baby.setImageResource(R.drawable.week5); break;
                    case 6: binding.baby.setImageResource(R.drawable.week6); break;
                    case 7: binding.baby.setImageResource(R.drawable.week7); break;
                    case 8: binding.baby.setImageResource(R.drawable.week8); break;
                    case 9: binding.baby.setImageResource(R.drawable.week9); break;
                    case 10: binding.baby.setImageResource(R.drawable.week10); break;
                    case 11: binding.baby.setImageResource(R.drawable.week11); break;
                    case 12: binding.baby.setImageResource(R.drawable.week12); break;
                    case 13: binding.baby.setImageResource(R.drawable.week13); break;
                    case 14: binding.baby.setImageResource(R.drawable.week14); break;
                    case 15: binding.baby.setImageResource(R.drawable.week15); break;
                    case 16: binding.baby.setImageResource(R.drawable.week16); break;
                    case 17: binding.baby.setImageResource(R.drawable.week17); break;
                    case 18: binding.baby.setImageResource(R.drawable.week18); break;
                    case 19: binding.baby.setImageResource(R.drawable.week19); break;
                    case 20: binding.baby.setImageResource(R.drawable.week20); break;
                    case 21: binding.baby.setImageResource(R.drawable.week21); break;
                    case 22: binding.baby.setImageResource(R.drawable.week22); break;
                    case 23: binding.baby.setImageResource(R.drawable.week23); break;
                    case 24: binding.baby.setImageResource(R.drawable.week24); break;
                    case 25: binding.baby.setImageResource(R.drawable.week25); break;
                    case 26: binding.baby.setImageResource(R.drawable.week26); break;
                    case 27: binding.baby.setImageResource(R.drawable.week27); break;
                    case 28: binding.baby.setImageResource(R.drawable.week28); break;
                    case 29: binding.baby.setImageResource(R.drawable.week29); break;
                    case 30: binding.baby.setImageResource(R.drawable.week30); break;
                    case 31: binding.baby.setImageResource(R.drawable.week31); break;
                    case 32: binding.baby.setImageResource(R.drawable.week32); break;
                    case 33: binding.baby.setImageResource(R.drawable.week33); break;
                    case 34: binding.baby.setImageResource(R.drawable.week34); break;
                    case 35: binding.baby.setImageResource(R.drawable.week35); break;
                    case 36: binding.baby.setImageResource(R.drawable.week36); break;
                    case 37: binding.baby.setImageResource(R.drawable.week37); break;
                    case 38: binding.baby.setImageResource(R.drawable.week38); break;
                    case 39: binding.baby.setImageResource(R.drawable.week39); break;
                    case 40: binding.baby.setImageResource(R.drawable.week40); break;
                    case 41: binding.baby.setImageResource(R.drawable.week41); break;

                }
                break;
            default:
        }

    }

    private void sumOfDayInAWeek(int dw1, int dw7, String weeks_to_str)
    {


        for (int i = dw1 ; i <= dw7; i++)
        {

            int finalI = i;
            databaseReference.child("Users")
                    .child(uid)
                    .child("Cardio for All Trimesters")
                    .child("Days")
                    .child("Day: " + i)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String k = childSnapshot.getKey();
                                assert k != null;
                                databaseReference.child("Users")
                                        .child(uid)
                                        .child("Cardio for All Trimesters")
                                        .child("Days")
                                        .child("Day: " + finalI).child(k).child("percentage").addValueEventListener(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        daily_percentage_for_a_week = dataSnapshot.getValue(String.class);
                                                        assert daily_percentage_for_a_week != null;
                                                        double to_double = Double.parseDouble(daily_percentage_for_a_week);
                                                        total_daily_percentage_for_week += to_double;
                                                        insertTotalWeekPercentage(weeks_to_str, calculatePercentage(total_daily_percentage_for_week, 700));


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

    }

   /* private void sumOfDayInAWeekForFirstTrimester(int dw1, int dw7, String weeks_to_str)
    {

        for (int i = dw1 ; i <= dw7; i++)
        {

            int finalI = i;
            databaseReference.child("Users")
                    .child(uid)
                    .child("Cardio for All Trimesters")
                    .child("Days")
                    .child("Day: " + i)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String k = childSnapshot.getKey();
                                assert k != null;
                                databaseReference.child("Users")
                                        .child(uid)
                                        .child("Cardio for All Trimesters")
                                        .child("Days")
                                        .child("Day: " + finalI).child(k).child("percentage").addValueEventListener(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        daily_percentage_for_a_week = dataSnapshot.getValue(String.class);
                                                        assert daily_percentage_for_a_week != null;
                                                        double to_double = Double.parseDouble(daily_percentage_for_a_week);
                                                        total_daily_percentage_for_week += to_double;
                                                        insertTotalWeekPercentage(weeks_to_str, calculatePercentage(total_daily_percentage_for_week, 700));

//                                                        Toast.makeText(HomeActivity.this, "" + total_daily_percentage, Toast.LENGTH_SHORT).show();

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


    }*/

    private void sumOfDayInAMonth(int dm1, int dm30, String months_to_str)
    {

        for (int i = dm1 ; i <= dm30; i++)
        {

            int finalI = i;
            databaseReference.child("Users")
                    .child(uid)
                    .child("Cardio for All Trimesters")
                    .child("Days")
                    .child("Day: " + i)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String k = childSnapshot.getKey();
                                databaseReference.child("Users")
                                        .child(uid)
                                        .child("Cardio for All Trimesters")
                                        .child("Days")
                                        .child("Day: " + finalI).child(k).child("percentage").addValueEventListener(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        daily_percentage_for_a_month = dataSnapshot.getValue(String.class);
                                                        assert daily_percentage_for_a_month != null;
                                                        double to_double = Double.parseDouble(daily_percentage_for_a_month);
                                                        total_daily_percentage_for_month += to_double;
                                                        insertTotalMonthPercentage(months_to_str, calculatePercentage(total_daily_percentage_for_month, 3000));

//                                                        Toast.makeText(HomeActivity.this, "" + total_daily_percentage, Toast.LENGTH_SHORT).show();

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




    }


    public double calculatePercentage(double value, double total) {
        return value * 100 / total;
    }


    private void insertTotalWeekPercentage(String wks_to_str, double ttl_wk_prc)
    {

        binding.refreshImageButtonForWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.exercisesProgressContent.setVisibility(View.VISIBLE);
                binding.babyProgressContent.setVisibility(View.GONE);
                // CHECK IF EACH WEEK HAS ALREADY HAVE A TOTAL PERCENTAGE
                databaseReference.child("Users")
                        .child(uid)
                        .child("Cardio for All Trimesters")
                        .child("Weeks")
                        .child("Week: " + wks_to_str)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() == null) {

                                    DaoWeekTrimester daoWeekTrimester = new DaoWeekTrimester();
                                    GetTrimester getTrimester = new GetTrimester(String.valueOf(ttl_wk_prc));
                                    daoWeekTrimester.add(getTrimester).addOnSuccessListener(suc ->
                                    {
                                        displayWeeklyPercentage();
                                        Toast.makeText(StatusActivity.this, "Data Updated.", Toast.LENGTH_SHORT).show();
                                        recreate();

                                    }).addOnFailureListener(er->

                                    {
                                        Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();

                                    });


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            }
        });

    }

    private void displayWeeklyPercentage()
    {
                databaseReference.child("Users")
                        .child(uid)
                        .child("Cardio for All Trimesters")
                        .child("Weeks")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                    String week = childSnapshot.getKey();

                                    assert week != null;
                                    databaseReference.child("Users")
                                            .child(uid)
                                            .child("Cardio for All Trimesters")
                                            .child("Weeks")
                                            .child(week)

                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                        String k = childSnapshot.getKey();

                                                        databaseReference.child("Users")
                                                                .child(uid)
                                                                .child("Cardio for All Trimesters")
                                                                .child("Weeks")
                                                                .child(week).child(k).child("percentage").addValueEventListener(
                                                                        new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                weekly_percentage_to_list = dataSnapshot.getValue(String.class);
                                                                                weekPercentageList.add(weekly_percentage_to_list);

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
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });



    }

    private void insertTotalMonthPercentage(String mth_to_str, double ttl_mth_prc)
    {

        binding.refreshImageButtonForMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                expandableListView.setVisibility(View.INVISIBLE);
                // CHECK IF EACH WEEK HAS ALREADY HAVE A TOTAL PERCENTAGE
                databaseReference.child("Users")
                        .child(uid)
                        .child("Cardio for All Trimesters")
                        .child("Months")
                        .child("Month: " + mth_to_str)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() == null) {

                                    DaoMonthTrimester daoMonthTrimester = new DaoMonthTrimester();
                                    GetTrimester getTrimester = new GetTrimester(String.valueOf(ttl_mth_prc));
                                    daoMonthTrimester.add(getTrimester).addOnSuccessListener(suc ->
                                    {
                                        displayMonthlyPercentage();
                                        Toast.makeText(StatusActivity.this, "Data Updated.", Toast.LENGTH_SHORT).show();
                                        recreate();
                                    }).addOnFailureListener(er->

                                    {
                                        Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();

                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });
    }

    private void displayMonthlyPercentage()
    {

        databaseReference.child("Users")
                .child(uid)
                .child("Cardio for All Trimesters")
                .child("Months")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String month = childSnapshot.getKey();

                            assert month != null;
                            databaseReference.child("Users")
                                    .child(uid)
                                    .child("Cardio for All Trimesters")
                                    .child("Months")
                                    .child(month)

                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                String k = childSnapshot.getKey();

                                                databaseReference.child("Users")
                                                        .child(uid)
                                                        .child("Cardio for All Trimesters")
                                                        .child("Months")
                                                        .child(month).child(k).child("percentage").addValueEventListener(
                                                                new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        monthly_percentage_to_list = dataSnapshot.getValue(String.class);
                                                                        monthPercentageList.add(monthly_percentage_to_list);

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
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

    }

    private void createCollection() {



        String weekly_percentage =  weekPercentageList.toString();
        String monthly_percentage =  monthPercentageList.toString();

        weekly_percentage = weekly_percentage.substring(1, weekly_percentage.length() - 1);
        monthly_percentage = monthly_percentage.substring(1, monthly_percentage.length() - 1);

        String[] weekly = weekly_percentage.split(",");

        String[] monthly = monthly_percentage.split(",");

        mobileCollection = new HashMap<String, List<String>>();

        for(String group : groupList){

            switch (group)
            {
                case "Weekly": loadChild(weekly);break;
                case "Monthly": loadChild(monthly);break;
                default:
            }


            mobileCollection.put(group, childList);
        }

    }


    private void loadChild(String[] mobileModels) {
        childList = new ArrayList<>();
        childList.addAll(Arrays.asList(mobileModels));

    }

    private void createGroupList() {
        groupList = new ArrayList<>();
        groupList.add("Weekly");
        groupList.add("Monthly");

    }
// ^

    private void signOut() {
        auth.signOut();

        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();

    }

}