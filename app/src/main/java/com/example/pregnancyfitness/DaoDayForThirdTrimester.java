package com.example.pregnancyfitness;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class DaoDayForThirdTrimester {

    private final DatabaseReference databaseReference;

    String uid = ExerciseActivity.Global.uid;
    String days = ExerciseActivity.Global.days;

    public DaoDayForThirdTrimester(){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("Users").child(uid).child("Third Trimester").child("Days").child("Day: " + days);

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
