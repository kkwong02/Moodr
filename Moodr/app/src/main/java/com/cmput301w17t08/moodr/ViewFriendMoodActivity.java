package com.cmput301w17t08.moodr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ViewFriendMoodActivity extends ViewMoodActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend_mood);

        Intent intent = getIntent();
        Mood mood = (Mood) intent.getSerializableExtra("mood"); // ???

        loadMood(mood);
    }
}