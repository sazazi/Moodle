package com.example.moodle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";
    public static final String MOODNAME = "com.example.myfirstapp.MOODNAME";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //initialise Mood with the current context
        Mood.init(this);

        //initialise Activity with the current context
        Activity.init(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and sign in anonymously if not.
        user = mAuth.getCurrentUser();

        if(user==null){
            Log.d(TAG, "NEW USER");
            signInAnonymously();
        }
        else {
            Log.d(TAG, "User: (" + user + ") with uid: " + Objects.requireNonNull(user).getUid());
        }

        Intent intent = new Intent(this, MoodSelectionActivity.class);
        startActivity(intent);
    }

    private void signInAnonymously() {
        // [START signin_anonymously]
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success
                        user = mAuth.getCurrentUser();
                        String uid = Objects.requireNonNull(user).getUid();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("signedIn", true);
                        // Add user to Firestore DB
                        db.collection("users").document(uid)
                                .set(userMap);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
        // [END signin_anonymously]
    }
}
