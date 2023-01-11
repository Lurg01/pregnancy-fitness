package com.example.pregnancyfitness;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
    public DBHelper(Context context) {
        super(context, "Pregnancy Fitness.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("Create Table time(time String)");
        DB.execSQL("Create Table title(title String)");
        DB.execSQL("Create Table resetDueDate(resetDueDate String)");
        DB.execSQL("Create Table notification(notification String)");
        DB.execSQL("Create Table percentage(percentage String)");
        DB.execSQL("Create Table total_months(total_months String)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("DROP TABLE IF EXISTS time");
        DB.execSQL("DROP TABLE IF EXISTS title");
        DB.execSQL("DROP TABLE IF EXISTS resetDueDate");
        DB.execSQL("DROP TABLE IF EXISTS notification");
        DB.execSQL("DROP TABLE IF EXISTS percentage");
        DB.execSQL("DROP TABLE IF EXISTS total_months");

    }

    // FOR TOTAL MONTHS
    public Boolean insertTotalMonths(String total_months) {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("total_months", total_months);

        long result = DB.insert("total_months", null,contentValues);

        if (result == -1)
        { return false; }
        else
        { return true;}

    }

    public Boolean deleteTotalMonths(String total_months)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from total_months where total_months = ?", new String[] {total_months});

        if (cursor.getCount() > 0)
        {
            long result = DB.delete("total_months", "total_months=?", new String[] {total_months});
            if (result == -1)
            { return false; }
            else
            { return true;}

        }
        else
        { return false;}

    }

    public Cursor getTotalMonths() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from total_months", null);
        return cursor;
    }
    // <


    // FOR PERCENTAGE
    public Boolean insertPercentage(String percentage) {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("percentage", percentage);

        long result = DB.insert("percentage", null,contentValues);

        if (result == -1)
        { return false; }
        else
        { return true;}

    }

    public Boolean deletePercentage(String percentage)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from percentage where percentage = ?", new String[] {percentage});

        if (cursor.getCount() > 0)
        {
            long result = DB.delete("percentage", "percentage=?", new String[] {percentage});
            if (result == -1)
            { return false; }
            else
            { return true;}

        }
        else
        { return false;}

    }

    public Cursor getPercentage() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from percentage", null);
        return cursor;
    }
    // <



    public Boolean insertNotification(String notification) {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("notification", notification);

        long result = DB.insert("notification", null,contentValues);

        if (result == -1)
        { return false; }
        else
        { return true;}

    }


    public Boolean deleteNotification(String notification)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from notification where notification = ?", new String[] {notification});

        if (cursor.getCount() > 0)
        {
            long result = DB.delete("notification", "notification=?", new String[] {notification});
            if (result == -1)
            { return false; }
            else
            { return true;}

        }
        else
        { return false;}


    }

    public Cursor getNotification() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from notification", null);
        return cursor;
    }



    public Boolean insertTime(String time) {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time",time);

        long result = DB.insert("time", null,contentValues);

        if (result == -1)
        { return false; }
        else
        { return true;}

    }


    public Boolean deleteTime(String time)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from time where time = ?", new String[] {time});

        if (cursor.getCount() > 0)
        {
            long result = DB.delete("time", "time=?", new String[] {time});
            if (result == -1)
            { return false; }
            else
            { return true;}

        }
        else
        { return false;}


    }

    public Cursor getTime() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from time", null);
        return cursor;
    }




    public Boolean insertTitle(String title) {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title",title);

        long result = DB.insert("title", null,contentValues);

        if (result == -1)
        { return false; }
        else
        { return true;}

    }



    public Boolean deleteTitle(String title)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from title where title = ?", new String[] {title});

        if (cursor.getCount() > 0)
        {
            long result = DB.delete("title", "title=?", new String[] {title});
            if (result == -1)
            { return false; }
            else
            { return true;}

        }
        else
        { return false;}


    }



    public Cursor getTitle() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from title",  null);
        return cursor;
    }



    public Boolean insertResetDueDate(String resetDueDate) {

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("resetDueDate",resetDueDate);

        long result = DB.insert("resetDueDate", null,contentValues);

        if (result == -1)
        { return false; }
        else
        { return true;}

    }

    public Boolean deleteResetDueDate(String resetDueDate)
    {

        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from resetDueDate where resetDuedate = ?", new String[] {resetDueDate});

        if (cursor.getCount() > 0)
        {
            long result = DB.delete("resetDueDate", "resetDueDate=?", new String[] {resetDueDate});
            if (result == -1)
            { return false; }
            else
            { return true;}

        }
        else
        { return false;}


    }

    public Cursor getResetDueDate() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from resetDueDate",  null);
        return cursor;
    }


}
