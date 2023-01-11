package com.example.pregnancyfitness;


import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class DaoDayTrimester {

    private final DatabaseReference databaseReference;

    String uid = ExerciseActivity.Global.uid;
    String days = ExerciseActivity.Global.days;

    public DaoDayTrimester(){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("Users").child(uid).child("Cardio for All Trimesters").child("Days").child("Day: " + days);

    }

    public Task<Void> add(GetTrimester getTrimester )
    {
        return databaseReference.push().setValue(getTrimester);
    }


    public Query get()
    {
        return databaseReference;
    }

}
