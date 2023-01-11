package com.example.pregnancyfitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.example.pregnancyfitness.databinding.ActivityExerciseBinding;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class ExerciseActivity extends AppCompatActivity {

    DBHelper DB;
    TextView timeBound;
    TextToSpeech textToSpeech;
    GifImageView gifImageView;
    ProgressDialog progressDialog;
    private ActivityExerciseBinding binding;
    private final Handler h = new Handler();
    private int progressStatus = 6;
    private int ready_set_int  = 6;
    private int time_bound = 15;
    int maxVolume = 10;
    long seconds;
    private int exercise_index = 0, accomplish = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    public static class Global {
        public static String uid, days;
    }
    Handler handler = new Handler();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String uid = currentFirebaseUser.getUid();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityExerciseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // CHECK NETWORK CONNECTION
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); // USE CONNECTIVITY MANAGER
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        // <

        // FUNCTION FOR OFFLINE MODE
        if (!connected){

            DB = new DBHelper(this);
            Cursor get_percentage = DB.getPercentage();
            if (get_percentage.getCount() != 0){

               binding.restart.setVisibility(View.VISIBLE);
               binding.startAndStop.setVisibility(View.GONE);
               binding.restart.setText("Restart");

            }
            else
            {
                binding.startAndStop.setText("Start");
            }
        }
        // <

        // CONFIGURE PROGRESS DIALOG
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Saving your progress . . .");
        progressDialog.setCanceledOnTouchOutside(false);
        // <

        timeBound = (TextView) findViewById(R.id.time_bound);
        gifImageView = (GifImageView) findViewById(R.id.gif);

        // create an object textToSpeech and adding features into it
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                // Set title
                builder.setTitle("Exit Exercise ?");
                // Positive yes button
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        onBackPressed();
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
            }
        });

        Global.uid = uid; // PASS UID FOR UPLOADING DATA
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
                                            getTrimester(date_started, current_date);

                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) { }
                                    });

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });


        String intent_val = getIntent().getStringExtra("val");
        int int_val = Integer.parseInt(intent_val);
        switch (int_val){
            case 0: case 3: gifImageView.setImageResource(R.drawable.walking); ((GifDrawable)gifImageView.getDrawable()).stop();
                binding.exerciseType.setText("Walking"); break;
            case 1: gifImageView.setImageResource(R.drawable.pelvic_curl); ((GifDrawable)gifImageView.getDrawable()).stop();
                binding.exerciseType.setText("Pelvic Curl"); break;
            case 2: gifImageView.setImageResource(R.drawable.incline_pushup); ((GifDrawable)gifImageView.getDrawable()).stop();
                binding.exerciseType.setText("Incline Push up"); break;
        }
        binding.startAndStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (binding.startAndStop.getText().toString())
                {
                    case "Start":
                        binding.startAndStop.setText("Stop");
                        beginExercise(int_val);
                        break;
                    case "Stop":

                        // Initialize alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                        // Set title
                        builder.setTitle("Save Progress ?");
                        // Positive yes button
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                DB = new DBHelper(ExerciseActivity.this);

                                // SHOW PROGRESS DIALOG
                                progressDialog.show();
                                textToSpeech.shutdown();

                                switch(int_val)
                                {
                                    case 0:

                                        double percentage_for_all_trimester = calculatePercentage(accomplish, 45);

                                        if (!connected){   // IF OFFLINE MODE
                                            DB.insertPercentage(String.valueOf(percentage_for_all_trimester)); // UPLOAD TO LOCAL DB
                                            Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                            recreate();
                                        }
                                        else
                                        {
                                            DaoDayTrimester daoDayTrimesterForAllTrimester = new DaoDayTrimester();
                                            GetTrimester getTrimesterForAllTrimester = new GetTrimester(String.valueOf(percentage_for_all_trimester));
                                            daoDayTrimesterForAllTrimester.add(getTrimesterForAllTrimester).addOnSuccessListener(suc ->
                                            {
                                                Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                                accomplish = 0;
                                                progressDialog.dismiss();
                                                recreate();


                                            }).addOnFailureListener(er ->
                                            {
                                                Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                        break;

                                    case 1:

                                        double percentage_for_first_trimester = calculatePercentage(accomplish, 75);
                                        if (!connected){
                                            DB.insertPercentage(String.valueOf(percentage_for_first_trimester)); // UPLOAD TO LOCAL DB
                                            Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                            recreate();
                                        }
                                        else
                                        {

                                            DaoDayForFirstTrimester daoDayTrimesterForFirstTrimester = new DaoDayForFirstTrimester();
                                            GetTrimester getTrimesterForFirstTrimester = new GetTrimester(String.valueOf(percentage_for_first_trimester));
                                            daoDayTrimesterForFirstTrimester.add(getTrimesterForFirstTrimester).addOnSuccessListener(suc ->
                                            {
                                                Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                                accomplish = 0;
                                                progressDialog.dismiss();
                                                recreate();


                                            }).addOnFailureListener(er ->
                                            {
                                                Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                        break;

                                    case 2:

                                        double percentage_for_second_trimester = calculatePercentage(accomplish, 45);
                                        if (!connected){
                                            DB.insertPercentage(String.valueOf(percentage_for_second_trimester)); // UPLOAD TO LOCAL DB
                                            Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                            recreate();
                                        }
                                        else{
                                            DaoDayForSecondTrimester daoDayTrimesterForSecondTrimester = new DaoDayForSecondTrimester();
                                            GetTrimester getTrimesterForSecondTrimester = new GetTrimester(String.valueOf(percentage_for_second_trimester));

                                            daoDayTrimesterForSecondTrimester.add(getTrimesterForSecondTrimester).addOnSuccessListener(suc ->
                                            {
                                                Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                                accomplish = 0;
                                                progressDialog.dismiss();
                                                recreate();

                                            }).addOnFailureListener(er ->
                                            {
                                                Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                        break;

                                    case 3:

                                        double percentage_for_third_trimester = calculatePercentage(accomplish, 75);
                                        if (!connected){
                                            DB.insertPercentage(String.valueOf(percentage_for_third_trimester)); // UPLOAD TO LOCAL DB
                                            Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                            recreate();
                                        }
                                        else{
                                            DaoDayForThirdTrimester daoDayTrimesterForThirdTrimester = new DaoDayForThirdTrimester();
                                            GetTrimester getTrimesterForThirdTrimester = new GetTrimester(String.valueOf(percentage_for_third_trimester));

                                            daoDayTrimesterForThirdTrimester.add(getTrimesterForThirdTrimester).addOnSuccessListener(suc ->
                                            {
                                                Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                                accomplish = 0;
                                                progressDialog.dismiss();
                                                recreate();

                                            }).addOnFailureListener(er ->
                                            {
                                                Toast.makeText(getApplicationContext(), ""+ er.getMessage(),  Toast.LENGTH_SHORT).show();
                                            });
                                        }

                                        break;

                                    default:

                                }

                            }
                        });


                        // Negative no button
                        builder.setNegativeButton("RESTART", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Dismiss dialog
                                dialogInterface.dismiss();
                                recreate();
                            }
                        });

                        // Show dialog
                        builder.show();
                        textToSpeech.shutdown();
                        break;

                    default:
                }

            }
        });

        binding.restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // FUNCTION FOR OFFLINE MODE
                if (!connected){

                    // Initialize alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                    // Set title
                    builder.setTitle("Restart ?");
                    // Positive yes button
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            DB = new DBHelper(ExerciseActivity.this);
                            Cursor get_percentage = DB.getPercentage();
                            if (get_percentage.moveToFirst()){
                                DB.deletePercentage(get_percentage.getString(0));
                                recreate();
                            }
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
                }
                // <

                else
                {

                    String current_date = sdf.format(Calendar.getInstance().getTime());
                    switch(int_val)
                    {
                        case 0:
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
                                                                restartForAllTrimesterExercise(date_started, current_date);
                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) { }
                                                        });

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });


                            break;

                        case 1:

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
                                                                restartForFirstTrimesterExercise(date_started, current_date);

                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) { }
                                                        });

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });

                            break;

                        case 2:


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

                                                                restartForSecondTrimesterExercise(date_started, current_date);

                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) { }
                                                        });

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                            break;

                        case 3:
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
                                                                restartForThirdTrimesterExercise(date_started, current_date);

                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) { }
                                                        });

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });

                            break;

                        default:


                    }
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


        String intent_val = getIntent().getStringExtra("val");
        int int_val = Integer.parseInt(intent_val);
        String current_date = sdf.format(Calendar.getInstance().getTime());
        switch(int_val)
        {
            case 0:

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

                                                    checkForAllTrimesterExercise(date_started, current_date);

                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) { }
                                            });

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });


                break;

            case 1:

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

                                                    checkForFirstTrimesterExercise(date_started, current_date);

                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) { }
                                            });

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });


                break;

            case 2:

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

                                                    checkForSecondTrimesterExercise(date_started, current_date);

                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) { }
                                            });

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                break;

            case 3:

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

                                                    checkForThirdTrimesterExercise(date_started, current_date);

                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) { }
                                            });

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });

                break;

            default:

        }

    }

    private void checkForAllTrimesterExercise(String date_started, String current_date) {


        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
//            String date_format = sdf.format(due_date_parse);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {

                Period periodDays = new Period(sd, cd, PeriodType.days());

                days =  periodDays.getDays();

                databaseReference.child("Users")
                        .child(uid)
                        .child("Cardio for All Trimesters")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    binding.restart.setVisibility(View.VISIBLE);
                                    binding.startAndStop.setVisibility(View.GONE);
                                    binding.restart.setText("Restart");

                                }
                                else
                                {
                                   binding.startAndStop.setText("Start");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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
    private void restartForAllTrimesterExercise(String date_started, String current_date)
    {

        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
//            String date_format = sdf.format(due_date_parse);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {

                Period periodDays = new Period(sd, cd, PeriodType.days());

                days =  periodDays.getDays();

                databaseReference.child("Users")
                        .child(uid)
                        .child("Cardio for All Trimesters")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {

                                    // Initialize alert dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                                    // Set title
                                    builder.setTitle("Restart ?");
                                    // Positive yes button
                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {

                                            databaseReference.child("Users")
                                                    .child(uid)
                                                    .child("Cardio for All Trimesters")
                                                    .child("Days")
                                                    .child("Day: " + days)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                                childSnapshot.getRef().removeValue().addOnSuccessListener(suc ->{
                                                                    recreate();

                                                                });

                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) { }
                                                    });

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
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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

    private void checkForFirstTrimesterExercise(String date_started, String current_date)
    {
        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {

                Period periodDays = new Period(sd, cd, PeriodType.days());
                days =  periodDays.getDays();
                databaseReference.child("Users")
                        .child(uid)
                        .child("First Trimester")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    binding.restart.setVisibility(View.VISIBLE);
                                    binding.startAndStop.setVisibility(View.GONE);
                                    binding.restart.setText("Restart");

                                }
                                else
                                {
                                    binding.startAndStop.setText("Start");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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

    private void restartForFirstTrimesterExercise(String date_started, String current_date)
    {
        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {

                Period periodDays = new Period(sd, cd, PeriodType.days());

                days =  periodDays.getDays();

                databaseReference.child("Users")
                        .child(uid)
                        .child("First Trimester")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {

                                    // Initialize alert dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                                    // Set title
                                    builder.setTitle("Restart ?");
                                    // Positive yes button
                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {

                                            databaseReference.child("Users")
                                                    .child(uid)
                                                    .child("First Trimester")
                                                    .child("Days")
                                                    .child("Day: " + days)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                                childSnapshot.getRef().removeValue().addOnSuccessListener(suc ->{
                                                                    recreate();

                                                                });

                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) { }
                                                    });

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
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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

    private void checkForSecondTrimesterExercise(String date_started, String current_date)
    {

        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {

                Period periodDays = new Period(sd, cd, PeriodType.days());

                days =  periodDays.getDays();

                databaseReference.child("Users")
                        .child(uid)
                        .child("Second Trimester")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    binding.restart.setVisibility(View.VISIBLE);
                                    binding.startAndStop.setVisibility(View.GONE);
                                    binding.restart.setText("Restart");

                                }
                                else
                                {
                                    binding.startAndStop.setText("Start");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
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

    private void restartForSecondTrimesterExercise(String date_started, String current_date)
    {

        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {
                Period periodDays = new Period(sd, cd, PeriodType.days());
                days =  periodDays.getDays();
                databaseReference.child("Users")
                        .child(uid)
                        .child("Second Trimester")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {

                                    // Initialize alert dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                                    // Set title
                                    builder.setTitle("Restart ?");
                                    // Positive yes button
                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {

                                            databaseReference.child("Users")
                                                    .child(uid)
                                                    .child("Second Trimester")
                                                    .child("Days")
                                                    .child("Day: " + days)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                                childSnapshot.getRef().removeValue().addOnSuccessListener(suc ->{
                                                                    recreate();

                                                                });

                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) { }
                                                    });

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
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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

    private void checkForThirdTrimesterExercise(String date_started, String current_date)
    {
        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {
                Period periodDays = new Period(sd, cd, PeriodType.days());
                days =  periodDays.getDays();
                databaseReference.child("Users")
                        .child(uid)
                        .child("Third Trimester")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    binding.restart.setVisibility(View.VISIBLE);
                                    binding.startAndStop.setVisibility(View.GONE);
                                    binding.restart.setText("Restart");

                                }
                                else
                                {
                                    binding.startAndStop.setText("Start");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
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

    private void restartForThirdTrimesterExercise(String date_started, String current_date)
    {

        try
        {
            Date date_started_parse = sdf.parse(date_started);
            Date current_date_parse = sdf.parse(current_date);
            long sd = date_started_parse.getTime();
            long cd = current_date_parse.getTime();

            int days;

            if (sd <= cd)
            {
                Period periodDays = new Period(sd, cd, PeriodType.days());
                days =  periodDays.getDays();
                databaseReference.child("Users")
                        .child(uid)
                        .child("Third Trimester")
                        .child("Days")
                        .child("Day: " + days)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    // Initialize alert dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                                    // Set title
                                    builder.setTitle("Restart ?");
                                    // Positive yes button
                                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {

                                            databaseReference.child("Users")
                                                    .child(uid)
                                                    .child("Third Trimester")
                                                    .child("Days")
                                                    .child("Day: " + days)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                                childSnapshot.getRef().removeValue().addOnSuccessListener(suc ->{
                                                                    recreate();
                                                                });

                                                            }
                                                        }
                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) { }
                                                    });

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
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
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

    public double calculatePercentage(double value, double total) {
        return value * 100 / total;
    }
    private void getTrimester(String dt, String cd)
    {

        try
        {
            Date date_started_parse = sdf.parse(dt);
            Date current_date_parse = sdf.parse(cd);
//            String date_format = sdf.format(due_date_parse);
            long start_date = date_started_parse.getTime();
            long current_date = current_date_parse.getTime();
            int days, weeks, months;            String days_to_str, weeks_to_str, months_to_str;
            if (start_date <= current_date)
            {

                Period periodDays = new Period(start_date, current_date, PeriodType.days());
                Period periodWeeks = new Period(start_date, current_date, PeriodType.weeks());
                Period periodMonths = new Period(start_date, current_date, PeriodType.months());
                days = periodDays.getDays();
                weeks =  periodWeeks.getWeeks();
                months = periodMonths.getMonths();
                days_to_str = String.valueOf(days); weeks_to_str = String.valueOf(weeks);
                months_to_str = String.valueOf(months);
                Global.days = days_to_str;


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

    private void beginExercise(int int_val)
    {

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (progressStatus > 0) {

                    // Update the progress bar and display the
                    //current value in the text view
                    h.post(new Runnable() {
                        public void run() {
                            ready_set_int -= 1;
                            String ready_set_str = String.valueOf(ready_set_int);
                            textToSpeech.speak(String.valueOf(progressStatus),TextToSpeech.QUEUE_FLUSH,null);
                            progressStatus -= 1;
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    timeBound.setText(ready_set_str);

                                    if (timeBound.getText().toString().equals("0"))
                                    {
                                        exerciseCategory(int_val); // ANIMATION TO DISPLAY

                                        timeBound.setText("Start");
                                        ((GifDrawable)gifImageView.getDrawable()).start();
                                        h.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                timeBoundExercise(int_val);

                                            }
                                        }, 1000);

                                    }
                                }
                            },1000);

                        }
                    });


                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }

                }

            }

        }).start();

    }

    private void timeBoundExercise(int int_val)
    {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        timeBound.setText("seconds remaining: " + millisUntilFinished / 1000);
                        seconds = millisUntilFinished / 1000;
                        if (seconds <= 3)
                        {
                            textToSpeech.speak(String.valueOf(seconds),TextToSpeech.QUEUE_FLUSH,null);
                        }

                    }

                    public void onFinish() {
                        accomplish = accomplish + 15;
                        switch (int_val){

                            case 0: case 2:

                                if (exercise_index <= 2) {
                                    textToSpeech.speak("Take a rest for 5 second.",TextToSpeech.QUEUE_FLUSH,null);
                                    ((GifDrawable)gifImageView.getDrawable()).stop();
                                    timeBoundRest(int_val);
                                }
                                else {
                                    textToSpeech.speak("Take a rest.",TextToSpeech.QUEUE_FLUSH,null);
                                    ((GifDrawable)gifImageView.getDrawable()).stop();
                                    timeBound.setVisibility(View.INVISIBLE);
                                    gifImageView.setVisibility(View.INVISIBLE);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            saveProgress(int_val);
                                        }
                                    },1500);
                                }
                                break;

                            case 1: case 3:

                                if (exercise_index <= 4)
                                {
                                    textToSpeech.speak("Take a rest for 5 second.",TextToSpeech.QUEUE_FLUSH,null);
                                    ((GifDrawable)gifImageView.getDrawable()).stop();
                                    timeBoundRest(int_val);
                                }

                                else {
                                    textToSpeech.speak("Take a rest.",TextToSpeech.QUEUE_FLUSH,null);
                                    ((GifDrawable)gifImageView.getDrawable()).stop();
                                    timeBound.setVisibility(View.INVISIBLE);
                                    gifImageView.setVisibility(View.INVISIBLE);
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            saveProgress(int_val);
                                        }
                                    },1500);
                                }
                                break;
                        }

                    }

                }.start();

            }
        },3000);

    }

    private void timeBoundRest(int int_val)
    {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {

                    public void onTick(long millisUntilFinished) {

                        gifImageView.setVisibility(View.INVISIBLE);
                        timeBound.setText("" + millisUntilFinished / 1000);
                        long seconds = millisUntilFinished / 1000;
                        if (seconds <= 3) {
                            textToSpeech.speak(String.valueOf(seconds), TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }

                    public void onFinish() {
                        timeBound.setText("Start");
                        saveProgress(int_val);

                    }

                }.start();

            /*    if (int_val == 0 || int_val == 2)
                {
                    if (exercise_index > 2 )
                    { countDownTimer.cancel();}
                }
                if (int_val == 1 || int_val == 3)
                {
                    if (exercise_index > 4 )
                    { countDownTimer.cancel();}
                }*/
            }

        }, 3000);


    }

    private void saveProgress(int int_val)
    {
        // CHECK NETWORK CONNECTION
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); // USE CONNECTIVITY MANAGER
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        // <
        DB = new DBHelper(this);
        switch (int_val) {

            case 0:

                if (exercise_index <= 2) {
                    timeBoundExercise(int_val);
                    exerciseCategory(int_val);

                } else {

                    // Initialize alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                    // Set title
                    builder.setTitle("Save Progress. .");
                    // Positive yes button
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            progressDialog.show();  // SHOW PROGRESS DIALOG
                            textToSpeech.shutdown();
                            double percentage_for_all_trimester = calculatePercentage(accomplish, 45);

                            if (!connected){   // IF OFFLINE MODE
                                DB.insertPercentage(String.valueOf(percentage_for_all_trimester)); // UPLOAD TO LOCAL DB
                                Toast.makeText(getApplicationContext(), "Progress Save .", Toast.LENGTH_SHORT).show();
                                recreate();
                            }
                            else
                            {
                                DaoDayTrimester daoDayTrimesterForAllTrimester = new DaoDayTrimester();
                                GetTrimester getTrimesterForAllTrimester = new GetTrimester(String.valueOf(percentage_for_all_trimester));
                                daoDayTrimesterForAllTrimester.add(getTrimesterForAllTrimester).addOnSuccessListener(suc ->
                                {
                                    Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                    accomplish = 0;
                                    progressDialog.dismiss();
                                    recreate();

                                }).addOnFailureListener(er ->
                                {
                                    Toast.makeText(getApplicationContext(), "" + er.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                    // Negative no button
                    builder.setNegativeButton("RESTART", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Dismiss dialog
                            dialogInterface.dismiss();
                            recreate();
                        }
                    });
                    // Show dialog
                    builder.show();
                    textToSpeech.shutdown();

                }


                break;

            case 1:


                if (exercise_index <= 4) {
                    timeBoundExercise(int_val);
                    exerciseCategory(int_val);

                } else {

                    // Initialize alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                    // Set title
                    builder.setTitle("Save Progress. .");
                    // Positive yes button
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // SHOW PROGRESS DIALOG
                            progressDialog.show();
                            textToSpeech.shutdown();
                            double percentage_for_first_trimester = calculatePercentage(accomplish, 75);

                            if (!connected){   // IF OFFLINE MODE
                                DB.insertPercentage(String.valueOf(percentage_for_first_trimester)); // UPLOAD TO LOCAL DB
                                Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                recreate();
                            }
                            else {

                                DaoDayForFirstTrimester daoDayTrimesterForFirstTrimester = new DaoDayForFirstTrimester();
                                GetTrimester getTrimesterForFirstTrimester = new GetTrimester(String.valueOf(percentage_for_first_trimester));
                                daoDayTrimesterForFirstTrimester.add(getTrimesterForFirstTrimester).addOnSuccessListener(suc ->
                                {
                                    Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                    accomplish = 0;
                                    progressDialog.dismiss();
                                    recreate();

                                }).addOnFailureListener(er ->
                                {
                                    Toast.makeText(getApplicationContext(), "" + er.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }

                        }
                    });
                    // Negative no button
                    builder.setNegativeButton("RESTART", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Dismiss dialog
                            dialogInterface.dismiss();
                            recreate();
                        }
                    });
                    // Show dialog
                    builder.show();
                    textToSpeech.shutdown();

                }

                break;

            case 2:

                if (exercise_index <= 2) {
                    timeBoundExercise(int_val);
                    exerciseCategory(int_val);

                } else {

                    // Initialize alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                    // Set title
                    builder.setTitle("Save Progress. .");
                    // Positive yes button
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // SHOW PROGRESS DIALOG
                            progressDialog.show();
                            textToSpeech.shutdown();
                            double percentage_for_second_trimester = calculatePercentage(accomplish, 45);
                            if (!connected){   // IF OFFLINE MODE
                                DB.insertPercentage(String.valueOf(percentage_for_second_trimester)); // UPLOAD TO LOCAL DB
                                Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                recreate();
                            }
                            else
                            {
                                DaoDayForSecondTrimester daoDayTrimesterForSecondTrimester = new DaoDayForSecondTrimester();
                                GetTrimester getTrimesterForSecondTrimester = new GetTrimester(String.valueOf(percentage_for_second_trimester));
                                daoDayTrimesterForSecondTrimester.add(getTrimesterForSecondTrimester).addOnSuccessListener(suc ->
                                {
                                    Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                    accomplish = 0;
                                    progressDialog.dismiss();
                                    recreate();

                                }).addOnFailureListener(er ->
                                {
                                    Toast.makeText(getApplicationContext(), "" + er.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                            }

                        }
                    });
                    // Negative no button
                    builder.setNegativeButton("RESTART", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Dismiss dialog
                            dialogInterface.dismiss();
                            recreate();
                        }
                    });
                    // Show dialog
                    builder.show();
                    textToSpeech.shutdown();

                }

                break;

            case 3:


                if (exercise_index <= 4) {
                    timeBoundExercise(int_val);
                    exerciseCategory(int_val);

                } else {

                    // Initialize alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this);
                    // Set title
                    builder.setTitle("Save Progress. .");
                    // Positive yes button
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // SHOW PROGRESS DIALOG
                            progressDialog.show();
                            textToSpeech.shutdown();
                            double percentage_for_third_trimester = calculatePercentage(accomplish, 75);
                            if (!connected){   // IF OFFLINE MODE
                                DB.insertPercentage(String.valueOf(percentage_for_third_trimester)); // UPLOAD TO LOCAL DB
                                Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                recreate();
                            }
                            else
                            {
                                DaoDayForThirdTrimester daoDayTrimesterForThirdTrimester = new DaoDayForThirdTrimester();
                                GetTrimester getTrimesterForThirdTrimester = new GetTrimester(String.valueOf(percentage_for_third_trimester));

                                daoDayTrimesterForThirdTrimester.add(getTrimesterForThirdTrimester).addOnSuccessListener(suc ->
                                {
                                    Toast.makeText(getApplicationContext(), "Progress Save.", Toast.LENGTH_SHORT).show();
                                    accomplish = 0;
                                    progressDialog.dismiss();
                                    recreate();

                                }).addOnFailureListener(er ->
                                {
                                    Toast.makeText(getApplicationContext(), "" + er.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                    // Negative no button
                    builder.setNegativeButton("RESTART", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Dismiss dialog
                            dialogInterface.dismiss();
                            recreate();
                        }
                    });
                    // Show dialog
                    builder.show();
                    textToSpeech.shutdown();

                }


                break;

            default:
        }
    }

    private void exerciseCategory(int int_val)
    {
        switch (int_val)
        {
            case 0: cardioForAllTrimesters();
                break;
            case 1: firstTrimester();
                break;
            case 2: secondTrimester();
                break;
            case 3: thirdTrimester();
                break;
            default:
        }
    }

    private void cardioForAllTrimesters()
    {

        switch (exercise_index)
        {
            case 0:
                textToSpeech.speak("Start, Do the exercise for 30 second, Walking",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.walking);
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            case 1:
                textToSpeech.speak("Start, Do the exercise for 30 second, Jogging",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.jogging);
                binding.exerciseType.setText("Jogging");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;

                break;
            case 2:
                textToSpeech.speak("Start, Do the exercise for 30 second, Stationary cycling",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.cyling);
                binding.exerciseType.setText("Stationary Cycling");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            default:

        }


    }
    private void firstTrimester()
    {

        switch (exercise_index)
        {
            case 0:
                textToSpeech.speak("Start, Do the exercise for 30 second, Pelvic Curl",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.pelvic_curl);
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            case 1:
                textToSpeech.speak("Start, Do the exercise for 30 second, Pelvic brace",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.pelvic_brace);
                binding.exerciseType.setText("Pelvic Brace");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;
            case 2:
                textToSpeech.speak("Start, Do the exercise for 30 second, Kneeling Push Ups",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.kneeling_pushups);
                
                binding.exerciseType.setText("Kneeling Push Ups");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;

                break;
            case 3:
                textToSpeech.speak("Start, Do the exercise for 30 second, Squats",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.squats);
                binding.exerciseType.setText("Squats");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;


            case 4:
                textToSpeech.speak("Start, Do the exercise for 30 second, Bicep Curls",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.pelvic_brace);
                binding.exerciseType.setText("Bicep Curls");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            default:

        }
    }
    private void secondTrimester()
    {


        switch (exercise_index)
        {
            case 0:
                textToSpeech.speak("Start, Do the exercise for 30 second, Incline Push Ups",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.incline_pushup);
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            case 1:
                textToSpeech.speak("Start, Do the exercise for 30 second, Side Lying Leg Lifts",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.side_lying_leg_lifts);
                binding.exerciseType.setText("Side Lying Leg Lifts");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;
            case 2:
                textToSpeech.speak("Start, Do the exercise for 30 second, Mermaid Stretch",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.mermaid_stretch);
                binding.exerciseType.setText("Mermaid Stretch");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;

                break;


            default:

        }

    }
    private void thirdTrimester()
    {

        switch (exercise_index)
        {
            case 0:
                textToSpeech.speak("Start, Do the exercise for 30 second, Walking",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.walking);
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            case 1:
                textToSpeech.speak("Start, Do the exercise for 30 second, Prenatal Yoga",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.prenatal_yoga);
                binding.exerciseType.setText("Prenatal Yoga");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;
            case 2:
                textToSpeech.speak("Start, Do the exercise for 30 second, Pilates",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.pilates);
                binding.exerciseType.setText("Pilates");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;

                break;
            case 3:
                textToSpeech.speak("Start, Do the exercise for 30 second, Pelvic Floor Exercises",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.pelvic_floor_exercises);
                binding.exerciseType.setText("Pelvic Floor");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            case 4:
                textToSpeech.speak("Start, Do the exercise for 30 second, Body Weight Moves",TextToSpeech.QUEUE_FLUSH,null);
                gifImageView.setVisibility(View.VISIBLE);
                gifImageView.setImageResource(R.drawable.body_weight);
                binding.exerciseType.setText("Body Weight Moves");
                ((GifDrawable)gifImageView.getDrawable()).start();
                exercise_index ++;
                break;

            default:

        }
    }
}