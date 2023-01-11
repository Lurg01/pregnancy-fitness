package com.example.pregnancyfitness;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.os.Build;


import com.example.pregnancyfitness.databinding.ActivityCalendarBinding;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CalendarActivity extends AppCompatActivity {

    Handler handler = new Handler();
    FirebaseAuth auth;
    DBHelper DB;
    // CALENDAR
    Calendar myCalendar = Calendar.getInstance();
    CalendarView calendarView;
    TextView dueDate, currentDate, trimester, exercises;
    // FOR NAVIGATION DRAWER
    DrawerLayout drawerLayout;
    View notificationIndicator;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    int months = 1;
    String list_of_exercise;
    public static class Global {
        public static String uid;
    }
    ProgressDialog progressDialog;
    private ActivityCalendarBinding binding;
    private MaterialTimePicker picker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String uid;

    {
        assert currentFirebaseUser != null;
        uid = currentFirebaseUser.getUid();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance(); // INIT FIREBASE AUTH

        // CONFIGURE PROGRESS DIALOG
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Resetting Trimester . . .");
        progressDialog.setCanceledOnTouchOutside(false);
        // <

        // GET ID FOR CALENDAR
        calendarView = (CalendarView) findViewById(R.id.calendar);
        dueDate = (TextView) findViewById(R.id.due_date);
        currentDate = (TextView) findViewById(R.id.current_date);
        trimester = (TextView) findViewById(R.id.trimester);
        exercises = (TextView) findViewById(R.id.exercises);
        // <

        // NAVIGATION DRAWER
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_View);
        toggle = new ActionBarDrawerToggle(CalendarActivity.this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Drawer click event
        // Drawer item Click event ------
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_drawer_home:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0,0); // VANISH TRANSITION
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                        // Set title
                        builder.setTitle("Logout");
                        // Set message
                        builder.setMessage("Are you sure you want to logout ?");
                        // Positive yes button
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                Toast.makeText(CalendarActivity.this, "Redirect to login . .", Toast.LENGTH_SHORT).show();
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

        binding.resetDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDueDate();
                DB = new DBHelper(CalendarActivity.this);
                DB.insertResetDueDate("$");
            }
        });
        binding.resetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //  INITIALIZE ALERT DIALOG
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                builder.setTitle("Reset Alarm ?");
                // Set message
                builder.setMessage("Your about to Reset your Alarm . .");
                // Positive yes button
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        binding.setupCalendarContent.setVisibility(View.GONE);
                        binding.setupTimeContent.setVisibility(View.VISIBLE);
                        binding.setViewContent.setVisibility(View.GONE);

                    }
                });
                //  NEGATIVE BUTTON
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });
                builder.show();

            }
        });

        Global.uid = uid;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String str_date1 = sdf.format(Calendar.getInstance().getTime());
        currentDate.setText("Current Date : " + str_date1);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                DB = new DBHelper(CalendarActivity.this);
                DB.insertResetDueDate(" ");

                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                // Set title
                builder.setTitle("Setup Date");
                // Set message
                builder.setMessage("Your about to set your Due date . .");
                // Positive yes button
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Toast.makeText(CalendarActivity.this, "Setting up Due Date. .", Toast.LENGTH_SHORT).show();
                        String str_date2 = year + "/" + ( month + 1) + "/" + day;
                        Cursor reset = DB.getResetDueDate();

                        try
                        {
                            Date current_date = sdf.parse(str_date1);
                            Date due_date = sdf.parse(str_date2);
                            String date_format = sdf.format(due_date);
                            long start_date = current_date.getTime();
                            long end_date = due_date.getTime();

                            if (start_date <= end_date)
                            {
                                Period period = new Period(start_date, end_date, PeriodType.months());

                                months = period.getMonths();
                                String months_to_str = String.valueOf(months);

                                if (reset.moveToFirst()) {

                                    if (months == 0 || months < 0)
                                    {
                                        exercises.setText("The date you have set is not valid!");
                                    }
                                    else if (months <= 3)
                                    {
                                        list_of_exercise = "1 - Pelvic curl \n2 - Pelvic brace \n3 - kneeling push up \n4 - Squats \n5 - Bicep curls";

                                        if (reset.getString(0).equals(" ")) {
                                            uploadCalendarSet(str_date1, date_format, months_to_str, list_of_exercise, reset.getString(0));

                                        } else if (reset.getString(0).equals("$")){
                                            uploadCalendarSet(str_date1, date_format, months_to_str, list_of_exercise, reset.getString(0));
                                        }

                                    }
                                    else if(months <= 6)
                                    {
                                        list_of_exercise = "1 - Incline push ups \n2 - Side-lying leg lifts \n3 - Mermaid Stretch";

                                        if (reset.getString(0).equals(" ")) {
                                            uploadCalendarSet(str_date1, date_format, months_to_str, list_of_exercise, reset.getString(0));

                                        } else if (reset.getString(0).equals("$")){
                                            uploadCalendarSet(str_date1, date_format, months_to_str, list_of_exercise, reset.getString(0));
                                        }

                                    }
                                    else if (months <= 9)
                                    {
                                        list_of_exercise = "1 - Prenatal Yoga \n2 - Pilates \n3 - Pelvic floor exercises \n4 - Body weight moves";

                                        if (reset.getString(0).equals(" ")) {
                                            uploadCalendarSet(str_date1, date_format, months_to_str, list_of_exercise, reset.getString(0));

                                        }  else if (reset.getString(0).equals("$")){
                                            uploadCalendarSet(str_date1, date_format, months_to_str, list_of_exercise, reset.getString(0));
                                        }
                                    }
                                    else
                                    {
                                        exercises.setText("This month is not cover by any trimester");
                                    }

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
                });
                // Negative no button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                // Show dialog
                builder.show();

            }
        });

 // ALARM >>>>>>>>>>>>>

        createNotificationChannel();

        DB = new DBHelper(this);
        Cursor res = DB.getTime();
        if (res.getCount() != 0)
        {
            if (res.moveToFirst()) {
                binding.selectedTime.setText(res.getString(0));
                binding.time.setText(res.getString(0));
            }
            binding.selectTimeBtn.setText("Reset Time");
        }

        binding.selectTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (binding.selectTimeBtn.getText().toString())
                {
                    case "Set Time": showTimePicker() ;break;
                    case "Reset Time": resetAlarm(); break;

                }

            }
        });

// ALARM <<<<<<<<<


        setViewContent();
        bottomNav();

    }

    private void resetDueDate() {

        databaseReference.child("Users")
                .child(uid)
                .child("Calendar")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {

                            // Initialize alert dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                            // Set title
                            builder.setTitle("Reset Due Date ?");
                            // Set message
                            builder.setMessage("Your about to Reset your Due Date . .");
                            // Positive yes button
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    // FUNCTION FOR OFFLINE MODE
                                    DB = new DBHelper(CalendarActivity.this);
                                    Cursor get_total_months = DB.getTotalMonths();
                                    if (get_total_months.getCount() != 0){
                                        while (get_total_months.moveToNext())
                                        {
                                            DB.deleteTotalMonths(get_total_months.getString(0));
                                        }
                                    }
                                    // <

                                    databaseReference.child("Users")
                                            .child(uid)
                                            .child("Calendar")
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                        childSnapshot.getRef().removeValue().addOnSuccessListener(suc ->{
                                                            finish();
                                                            startActivity(getIntent());
                                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                                        });

                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) { }
                                            });

                                }
                            });
                            // Negative no button
                            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Dismiss dialog
                                    dialogInterface.dismiss();
                                }
                            });
                            // Show dialog
                            builder.show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setViewContent()
    {
        DB = new DBHelper(this);
        databaseReference.child("Users")
                .child(uid)
                .child("Calendar")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            String k = childSnapshot.getKey();
                            assert k != null;
                            databaseReference.child("Users").child(uid).child("Calendar").child(k).child("end_date").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String due_date = dataSnapshot.getValue(String.class);
                                            binding.dueDate.setText(due_date);
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });

                            databaseReference.child("Users").child(uid).child("Calendar").child(k).child("total_months").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String trimester = dataSnapshot.getValue(String.class);
                                            binding.trimester.setText("Months before Due Date : " + trimester);
                                            DB.insertTotalMonths(trimester);

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });

                            databaseReference.child("Users").child(uid).child("Calendar").child(k).child("list_of_exercise").addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String trimester = dataSnapshot.getValue(String.class);
                                            binding.exercises.setText(trimester);
                                            assert trimester != null;
                                            if (!trimester.equals(""))
                                            { binding.setupCalendarContent.setVisibility(View.GONE); binding.setViewContent.setVisibility(View.VISIBLE); }
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

    private void uploadCalendarSet(String str_date1, String date_format, String months_to_str, String list_of_exercise, String get_reset)
    {
        Toast.makeText(getApplicationContext(),"GUMAGANA > " + get_reset, Toast.LENGTH_LONG).show();

        DaoCalendar daoCalendar = new DaoCalendar();
        GetCalendar getCalendar =  new GetCalendar(str_date1, date_format, months_to_str, list_of_exercise); // Convert input to int  >   Integer.parseInt(coins.getText().toString())
// Convert input to int  >   Integer.parseInt(coins.getText().toString())
        if (get_reset.equals(" "))
        {

            daoCalendar.add(getCalendar).addOnSuccessListener(suc ->
            {
//                Toast.makeText(getApplicationContext(), "Date Set Successfully.", Toast.LENGTH_SHORT).show();

                binding.setupCalendarContent.setVisibility(View.GONE);
                binding.setupTimeContent.setVisibility(View.VISIBLE);
                binding.setViewContent.setVisibility(View.GONE);
                DB = new DBHelper(CalendarActivity.this);
                Cursor del_reset = DB.getResetDueDate();
                if (del_reset.moveToFirst() ) { DB.deleteResetDueDate(del_reset.getString(0)); }


            }).addOnFailureListener(er->
            {
                Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();
            });

        }

        else if (get_reset.equals("$"))
        {

            daoCalendar.add(getCalendar).addOnSuccessListener(suc ->
            {
                Toast.makeText(getApplicationContext(), "Date Reset Successfully.", Toast.LENGTH_SHORT).show();
                binding.setupCalendarContent.setVisibility(View.GONE);
                binding.setupTimeContent.setVisibility(View.GONE);
                binding.setViewContent.setVisibility(View.VISIBLE);
                DB = new DBHelper(CalendarActivity.this);
                Cursor del_reset = DB.getResetDueDate();
                if (del_reset.moveToFirst() ) { DB.deleteResetDueDate(del_reset.getString(0)); }

            }).addOnFailureListener(er->
            {
                Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();
            });
        }

    }


    private void showTimePicker() {
        DB = new DBHelper(this);

        picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Time of Exercise")
                .build();

        picker.show(getSupportFragmentManager(),"foxandroid");


        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (picker.getHour() > 12){

                    DB.insertTime(String.format("%02d",(picker.getHour()-12)) + " : " + String.format("%02d",picker.getMinute()) + " PM");

                }else {

                    DB.insertTime(String.format("%02d",picker.getHour()) + " : " + String.format("%02d",picker.getMinute()) + " AM");

                }

                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY,picker.getHour());
                calendar.set(Calendar.MINUTE,picker.getMinute());
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);

                setAlarm();

            }
        });

    }

    private void resetAlarm() {
        DB = new DBHelper(CalendarActivity.this);
        Cursor resTime = DB.getTime();
        if (resTime.moveToFirst() ) { DB.deleteTime(resTime.getString(0));  }
        Intent intent = new Intent(this,AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_IMMUTABLE); // 0 have changed to PendingIntent.FLAG_IMMUTABLE

        if (alarmManager == null){

            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            showTimePicker();

        }

        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Alarm Cancelled", Toast.LENGTH_SHORT).show();

    }

    private void setAlarm() {

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this,AlarmReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_IMMUTABLE); // 0 have changed to PendingIntent.FLAG_IMMUTABLE
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,pendingIntent);

        Toast.makeText(this, "Alarm set Successfully", Toast.LENGTH_SHORT).show();
        binding.setupTimeContent.setVisibility(View.GONE);
        binding.setViewContent.setVisibility(View.VISIBLE);
        recreate();

    }


    private void createNotificationChannel() {
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.sound);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "ExerciseReminderChannel";
            String description = "Channel For Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("foxandroid",name,importance);
            channel.setDescription(description);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }

    }

    private void bottomNav() {


        // INITIALIZE AND ASSIGN VARIABLE
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.calendar);

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