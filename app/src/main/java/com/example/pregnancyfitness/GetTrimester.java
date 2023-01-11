package com.example.pregnancyfitness;
import com.google.firebase.database.Exclude;
public class GetTrimester {


    @Exclude
    private String key;
    private String percentage;

    public GetTrimester(){}
    public GetTrimester( String percentage)
    {
        this.percentage = percentage;

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
}
