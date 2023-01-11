package com.example.pregnancyfitness;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.security.Signature;
import java.util.HashMap;

public class DaoCalendar {

    private DatabaseReference databaseReference;

    String uid = CalendarActivity.Global.uid;
    public DaoCalendar(){

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        databaseReference = db.getReference("Users").child(uid).child("Calendar");

/*
        databaseReference.child(last_name).get().addOnSuccessListener(dataSnapshot -> {
            databaseReference.child(new_user_name).setValue(dataSnapshot.getValue());
            databaseReference.child(last_name).removeValue();

        });

 */

    }
    public Task<Void> add(GetCalendar getCalendar)
    {
        return databaseReference.push().setValue(getCalendar);
    }


    public Query get()
    {
        return databaseReference;
    }
}
