package com.cmput301w17t08.moodr;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * This activity displays the user's moods. The user has the option to add a mood from this activity.
 * Due to the common load method, this extends Profile.
 * @see Profile
 */
public class MyProfileActivity extends Profile {
    private User user;
    private ArrayList<Mood> moods;
    private ProfileMoodAdapter adapter;
    private ListView moodsListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = CurrentUserSingleton.getInstance().getUser();
        moods = CurrentUserSingleton.getInstance().getMyMoodList().getListOfMoods();

        moodsListview = (ListView) findViewById(R.id.profile_moodlist);

        setTitle(user.getName());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMood();
            }
        });

        moodsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                goToMood(i);
            }
        });

    }








    /**
     * goes to add mood activity to add a mood
     */
    private void addMood(){
        Intent intent  = new Intent(this, AddMoodActivity.class);
        startActivityForResult(intent,1);
    }

    /**
     * To view the details of the mood. Passes an index instead of a mood instance in the parent
     * @param i index of the mood in moodList
     */
    private void goToMood(int i){
        Intent intent = new Intent(this, ViewMyMoodActivity.class);
        intent.putExtra("index", i);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                for (Mood mood : moods){
                    Log.d("Mood", mood.getEmotion().getName());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }




    @Override
    public void onStart() {
        super.onStart();

        moods.clear();
        moods.addAll(loadPosts(user.getName()));

        adapter = new ProfileMoodAdapter(this, moods);

        moodsListview.setAdapter(adapter);
    }


}
