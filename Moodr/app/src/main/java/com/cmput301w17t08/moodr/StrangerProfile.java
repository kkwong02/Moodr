package com.cmput301w17t08.moodr;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * This activity is a stranger's profile. This shows no moods and only allows the user to follow
 * the user if not already pending. This does not share any common methods with Profile, therefore
 * even though it is a profile, it does not inherit from profile.
 */
public class StrangerProfile extends AppCompatActivity {
    private Boolean status;
    private Button follow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String name = getIntent().getStringExtra("name");

        setTitle(name);

        follow = (Button) findViewById(R.id.follow);

        status = false;

        try {
            status  = checkPending(name);
        }
        catch(Exception e){
            Log.d("Error", "Error getting user pending list from Elastic Search");
        }

        if (status){
            follow.setText("Pending");
        }

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!status){
                    try {
                        addPending(name);
                        follow.setText("Pending");
                    }
                    catch(Exception e){
                        Log.d("Error", "Error following user");
                    }

                }
            }
        });

    }

    /**
     * Checks if there is already a pending request for the user.
     * @param name of user pending
     * @return Boolean value. Whether if there is a pending request for the user.
     * @throws Exception
     */

    private Boolean checkPending(String name) throws Exception{ // change to more specific exception later.
        User user2;
        ElasticSearchUserController.GetUserTask getUserTask = new ElasticSearchUserController.GetUserTask();
        getUserTask.execute(name);

        try{
            user2 = getUserTask.get().get(0); // get first user from result
        }
        catch(Exception e){
            Log.d("Error", "Unable to get user from elastic search");
            throw new Exception();
        }

        String currentUsername = CurrentUserSingleton.getInstance().getUser().getName();

        return user2.getPending().contains(currentUsername);
    }

    /**
     * adds pending request for a given user.
     * @param name of user to send request to.
     * @throws Exception
     */

    private void addPending(String name) throws Exception{
        User user2;

        ElasticSearchUserController.GetUserTask getUserTask = new ElasticSearchUserController.GetUserTask();
        getUserTask.execute(name);

        try{
            user2 = getUserTask.get().get(0); // get first user from result
        }
        catch(Exception e){
            Log.d("Error", "Unable to get user from elastic search");
            throw new Exception();
        }

        user2.addPending(CurrentUserSingleton.getInstance().getUser().getName());

        // update on elastic search
    }
}