package com.example.moodle;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public enum Activity {
    /** Activity values (id, imageResID, nameResID) */
    TRAVEL(1, R.drawable.travel, R.string.activity_label_travel),
    WORK(2, R.drawable.work, R.string.activity_label_work),
    FAMILY(3, R.drawable.family, R.string.activity_label_family),
    FRIENDS(4, R.drawable.friends, R.string.activity_label_friends),
    DATE(5, R.drawable.date,  R.string.activity_label_date),
    SPORT(6, R.drawable.sport, R.string.activity_label_sport),
    PARTY(7, R.drawable.party, R.string.activity_label_party),
    TV(8, R.drawable.tv, R.string.activity_label_tv),
    READING(9, R.drawable.reading,R.string.activity_label_reading),
    GAMING(10, R.drawable.gaming,R.string.activity_label_gaming),
    SHOPPING(11, R.drawable.shopping, R.string.activity_label_shopping),
    MEAL(12, R.drawable.meal, R.string.activity_label_meal),
    COOKING(13, R.drawable.cooking,R.string.activity_label_cooking),
    CLEANING(14, R.drawable.cleaning,R.string.activity_label_cleaning),
    STUDYING(15, R.drawable.studying, R.string.activity_label_studying),
    RELAX(16, R.drawable.relax, R.string.activity_label_relax),
    GYM(17, R.drawable.gym, R.string.activity_label_gym),
    SICK(18, R.drawable.sick,R.string.activity_label_sick),
    OTHER(19, R.drawable.other,R.string.activity_label_other);

    /** Map used to get an activity by its id */
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, Activity> byID = new HashMap<>();

    // static block to fill the byID map
    static {
        for (Activity e : Activity.values()) {
            if (byID.put(e.getID(), e) != null) {
                throw new IllegalArgumentException("duplicate id: " + e.getID());
            }
        }
    }

    /** Map used to get a activity by its name */
    @SuppressLint("UseSparseArrays")
    private static final Map<String, Activity> byName = new HashMap<>();

    /** Initialise Activity from context*/
    public static void init(Context context) {
        // fill the byName map using context to resolve resource IDs
        for (Activity activity : Activity.values()) {
            String name = context.getResources().getString(activity.getNameResID());
            if (byName.put(name, activity) != null) {
                throw new IllegalArgumentException("duplicate name: " + name);
            }
        }
    }

    /** Returns activity corresponding to the id */
    public static Activity getById(int id) {
        return byID.get(id);
    }

    /** Returns activity corresponding to the name */
    public static Activity getByName(String name) {
        return byName.get(name);
    }

    /** Mood id */
    private int id;
    private int imageResID;
    private int nameResID;

    Activity(int id, int imageResID, int nameResID) {
        this.id = id;
        this.imageResID = imageResID;
        this.nameResID = nameResID;
    }

    public int getID(){
        return id;
    }

    public int getImageResID() { return imageResID; }

    public int getNameResID() { return nameResID; }
}
