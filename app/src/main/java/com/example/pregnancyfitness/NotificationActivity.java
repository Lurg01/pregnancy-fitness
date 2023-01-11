package com.example.pregnancyfitness;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pregnancyfitness.databinding.ActivityNotificationBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.timepicker.MaterialTimePicker;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;


public class NotificationActivity extends AppCompatActivity {


    private ActivityNotificationBinding binding;
    private MaterialTimePicker picker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    DBHelper DB;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.clearNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB = new DBHelper(NotificationActivity.this);
                Cursor resDelete = DB.getTitle();

                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
                // Set title
                builder.setTitle("Clear All");
                // Set message
                builder.setMessage("Clear all Notifications ?");
                // Positive yes button
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                        if (resDelete.getCount() != 0)
                        {
                            while (resDelete.moveToNext())
                            {
                                Boolean deleted =  DB.deleteTitle(resDelete.getString(0));

                                if (deleted){
                                    Toast.makeText(NotificationActivity.this, "Notification Cleared.", Toast.LENGTH_SHORT).show();
                                    recreate();
                                }
                            }
                        }


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
        });


        final ArrayList<String> list = new ArrayList<>();

        DB = new DBHelper(this);
        Cursor res = DB.getTitle();
        if (res.getCount() != 0)
        {
            binding.empty.setVisibility(View.INVISIBLE);
            binding.emptyDesc.setVisibility(View.INVISIBLE);

            for (int count = 0; count <= res.getCount(); count++)
            {
                if (res.moveToPosition(count))
                {
                    list.add(res.getString(0));

                }

            }

        }

        Collections.reverse(list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.text_listview, list);
        binding.simpleListView.setAdapter(arrayAdapter);

        bottomNav();
    }

    private void bottomNav() {

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.notifications);

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
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        DB = new DBHelper(this);
        Cursor getNotify = DB.getNotification();
        if (getNotify.getCount() != 0)
        {
            if (getNotify.moveToFirst())
            {
                DB.deleteNotification(getNotify.getString(0));
            }

        }

    }

}