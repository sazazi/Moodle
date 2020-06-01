package com.example.moodle;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.Joiner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCall;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.ibm.watson.natural_language_understanding.v1.model.SentimentOptions;
import com.ibm.watson.natural_language_understanding.v1.model.SentimentResult;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "Comments Activity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Mood mood;
    private String [] activities;
    private String comment;
    private float moodRating;
    private EditText commentsEditText;
    private TextView characterCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String moodName = intent.getStringExtra(DisplayActivitiesSelectionActivity.MOOD);
        activities = intent.getStringArrayExtra(DisplayActivitiesSelectionActivity.ACTIVITIES);

        mood = Mood.getByName(moodName);
        moodRating = mood.getRating();

        String delim = ", ";
        String activitiesStr = Joiner.on(delim).join(Objects.requireNonNull(activities));
        TextView activitiesText = findViewById(R.id.activitiesText);
        activitiesText.setText("Activities: " + activitiesStr);

        commentsEditText = findViewById(R.id.commentsEditText);
        commentsEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        commentsEditText.setRawInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);

        characterCountText = findViewById(R.id.characterCountText);

        commentsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = commentsEditText.length();
                characterCountText.setText(String.valueOf(length));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    /** Called when the user taps the Save button */
    public void save(View view){
        //called to sanitise the comments text in the edit text
        comment = sanitiseText();

        //check if comment is valid, if not, show corresponding toasts
        if(comment.isEmpty()){
            Toast.makeText(CommentsActivity.this, "Empty. Please enter a comment.", Toast.LENGTH_SHORT).show();
        }
        else if(comment.length()<30){
            Toast.makeText(CommentsActivity.this, "Please tell us a little more.\n(Minimum 30 characters)", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(CommentsActivity.this, "Saving mood entry...", Toast.LENGTH_LONG).show();
            //performs Sentiment Analysis on comment
            analyseText();
            //updates the Database with the new mood entry
            updateDB();
        }
    }

    private String sanitiseText(){
        String comment = commentsEditText.getText().toString();
        comment = comment.trim();

        return comment;
    }

    private void analyseText(){
        AskWatsonTask task = new AskWatsonTask();
        try {
            task.execute(comment).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateDB(){
        //if user entry for today doesn't exist, add it to db
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(user).getUid();

        final Date date = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(date);

        final DocumentReference todayEntryRef = db.collection("users").document(uid).collection("dailyEntries").document(formattedDate);

        todayEntryRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Task is successful");
                    DocumentSnapshot document = task.getResult();

                    Map<String, Object> dailyEntry = new HashMap<>();
                    dailyEntry.put("date", date);
                    dailyEntry.put("mood", getResources().getString(mood.getNameResID()));
                    dailyEntry.put("moodRating", moodRating);
                    dailyEntry.put("activities", Arrays.asList(activities));
                    dailyEntry.put ("comment", comment);

                    if (document != null) {
                        //if user entry for today exists, replace it with current input
                        if (document.exists()) {
                            Log.d(TAG, "Document exists! Overwriting.");
                        }
                        //else create today's entry
                        else {
                            Log.d(TAG, "Document does not exist! Creating.");
                        }
                    }

                    todayEntryRef.set(dailyEntry)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(CommentsActivity.this, "Mood entry saved!", Toast.LENGTH_SHORT).show();
                                    //start Visualisations Activity
                                    Intent intent = new Intent(CommentsActivity.this, VisualisationsActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error setting daily entry document", e);
                                }
                            });
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }


    /** Enable back button in top-left of action bar for going back  **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** IBM Watson Task to return sentiment score using Natural Language Understanding */
    private class AskWatsonTask extends AsyncTask<String, Void, SentimentResult> {

        @Override
        protected SentimentResult doInBackground(String... textsToAnalyse){
            try {
                String text = textsToAnalyse[0];

                Authenticator authenticator = new IamAuthenticator("tQRqOi-_eNzxqD6HQ5dGRLG3yabr0RMfrxIBbc6p0vof");
                NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding("2019-07-12", authenticator);
                service.setServiceUrl("https://api.eu-gb.natural-language-understanding.watson.cloud.ibm.com/instances/9405429c-b8ed-4023-92c2-e8a8478bda11");

                SentimentOptions sentiment = new SentimentOptions.Builder()
                        .document(true)
                        .build();
                Features features = new Features.Builder()
                        .sentiment(sentiment)
                        .build();
                AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                        .text(text)
                        .features(features)
                        .language("en")
                        .build();

                // Make API call
                ServiceCall<AnalysisResults> call = service.analyze(parameters);
                // Receive Response
                Response<AnalysisResults> response = service.analyze(parameters).execute();
                // Get results
                AnalysisResults results = response.getResult();

                SentimentResult sentimentResult = results.getSentiment();

                Log.d(TAG, "IBM Analysis Results: " + results);
                return sentimentResult;
            }
            catch (Exception e) {
                Log.d(TAG, "IBM Exception: " + e);
                return null;
            }
        }

        /** Gets sentiment score from result, normalises it and updates the mood rating*/
        @Override
        protected void onPostExecute(SentimentResult sentimentResult) {
            float sentimentScore = 0f;

            if(sentimentResult!=null){
                double sScore = sentimentResult.getDocument().getScore();
                sScore = sScore/2;
                sentimentScore = (float) sScore;
            }

            moodRating+=sentimentScore;
        }
    }
}
