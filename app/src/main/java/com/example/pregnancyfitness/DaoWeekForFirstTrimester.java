package com.example.pregnancyfitness;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import java.util.HashMap;


public class DaoWeekForFirstTrimester {


    private final DatabaseReference databaseReference;

    String uid = StatusActivity.Global.uid;
    String weeks = StatusActivity.Global.weeks;

    public DaoWeekForFirstTrimester(){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("Users").child(uid).child("First Trimester").child("Weeks").child("Week: " + weeks);

    }

    public Task<Void> add(GetTrimester getTrimester )
    {
        return databaseReference.push().setValue(getTrimester);
    }
    public Task<Void> update(String key, HashMap<String, Object> hashMap) { return databaseReference.child(key).updateChildren(hashMap); }
    public Task<Void> remove(String key)
    {
        return databaseReference.child(key).removeValue();
    }
    public Query get(String key)
    { if(key == null) { return databaseReference.orderByKey().limitToFirst(8); } return databaseReference.orderByKey().startAfter(key).limitToFirst(8); }


    public Query get()
    {
        return databaseReference;
    }
}
