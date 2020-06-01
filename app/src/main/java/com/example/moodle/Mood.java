package com.example.moodle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public enum Mood {
    /** Mood values (default rating, nameResID, text) */
    VERY_SAD(0.5f, R.drawable.very_sad,  R.string.mood_label_very_sad),
    SAD(1.5f, R.drawable.sad,  R.string.mood_label_sad),
    NEUTRAL(2.5f, R.drawable.neutral,  R.string.mood_label_neutral),
    HAPPY(3.5f, R.drawable.happy,  R.string.mood_label_happy),
    VERY_HAPPY(4.5f, R.drawable.very_happy,  R.string.mood_label_very_happy);

    private static final String TAG = "MOOD ENUM";

    /** Map used to get a mood by its rating */
    @SuppressLint("UseSparseArrays")
    private static final Map<Float, Mood> byRating = new HashMap<>();

    // static block to fill the byRating map
    static {
        for (Mood mood : Mood.values()) {
            if (byRating.put(mood.getRating(), mood) != null) {
                throw new IllegalArgumentException("duplicate rating: " + mood.getRating());
            }
        }
    }

    /** Map used to get a mood by its id */
    @SuppressLint("UseSparseArrays")
    private static final Map<String, Mood> byName = new HashMap<>();

    /** Initialise Mood from context*/
    public static void init(Context context) {
        //fills byName map using context to resolve resource IDs
        for (Mood mood : Mood.values()) {
            String name = context.getResources().getString(mood.getNameResID());
            if (byName.put(name, mood) != null) {
                throw new IllegalArgumentException("duplicate name: " + name);
            }
        }
    }

    /** Returns mood corresponding to the rating */
    public static Mood getByRating(float rating) {
        return byRating.get(rating);
    }

    /** Returns mood corresponding to the name */
    public static Mood getByName(String name) {
        return byName.get(name);
    }

    /** Returns true if the rating given has a corresponding mood value*/
    public static boolean moodExists(float rating) {
        for(Mood mood : values()){
            if(mood.getRating()==rating){
                return true;
            }
        }
        return false;
    }

    /** Returns mood within the rating range */
    public static Mood getByRatingRange(float rating){
        int roundedRating = (int) rating;
        float r = (float) roundedRating + 0.5f;
        return byRating.get(r);
    }

    /** Mood id */
    private float rating;
    private int imageResID;
    private int nameResID;

    Mood(float rating, int imageResID, int nameResID) {
        this.rating = rating;
        this.imageResID = imageResID;
        this.nameResID = nameResID;
    }

    public float getRating(){
        return rating;
    }

    public int getImageResID() { return imageResID; }

    public int getNameResID() { return nameResID; }
}
