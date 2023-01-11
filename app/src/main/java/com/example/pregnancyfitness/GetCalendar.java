package com.example.pregnancyfitness;
import com.google.firebase.database.Exclude;

public class GetCalendar {

    @Exclude
    private String key;
    private String start_date;
    private String end_date;
    private String total_months;
    private String list_of_exercise;


    public GetCalendar(){}
    public GetCalendar( String start_date, String end_date, String total_months, String list_of_exercise)
    {
        this.start_date = start_date;
        this.end_date = end_date;
        this.total_months = total_months;
        this.list_of_exercise = list_of_exercise;

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getTotal_months() {
        return total_months;
    }

    public void setTotal_months(String total_months) {
        this.total_months = total_months;
    }

    public String getList_of_exercise() {
        return list_of_exercise;
    }

    public void setList_of_exercise(String list_of_exercise) {
        this.list_of_exercise = list_of_exercise;
    }
}
