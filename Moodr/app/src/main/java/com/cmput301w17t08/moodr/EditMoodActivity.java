package com.cmput301w17t08.moodr;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;

/*
*
* EditMoodActivity class edits pre-existing mood and updates it onto elasticsearch server
*  You also can edit your location of the mood
*
 */


public class EditMoodActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {


    int day, month, year, hour, minute;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;
    Mood mood;
    int index;
    private ImageView imageView;
    private Button locationButton;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView locationText;
    private ArrayAdapter<String> emotionAdapter;
    private ArrayAdapter<String> situationAdapter;
    private EditText editTrigger;
    private Button editDate;
    private InputFilter filter;
    private Date date;
    private Date editDate_copy;
    private Coordinate editCoordinate = null;
    private int id;
    private String selected_emotion;
    private Emotion emotion;
    private String trigger;
    private String situation;
    private String encodedImage = "";
    Bitmap imageToDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new NavDrawerSetup(this, toolbar).setupNav();

        Intent intent = getIntent();
        index = intent.getIntExtra("index", -1);
        try {
            mood = CurrentUserSingleton.getInstance().getMyMoodList().getMood(index);
            encodedImage = mood.getImgUrl();
        } catch (Exception e) {
            Log.d("Error", "Invalid mood index");
        }

        // set image
        imageView = (ImageView) findViewById(R.id.iv_imageview);

        if (getIntent().getSerializableExtra("editcam") != null) {
            mood = (Mood) getIntent().getSerializableExtra("editcam");
            index = getIntent().getIntExtra("edit_index_cam", -1);
        }

        encodedImage = mood.getImgUrl();

        if (encodedImage != "") {
            imageToDisplay = decodeImage(encodedImage);
            imageView.setImageBitmap(imageToDisplay);
        }

        editTrigger = (EditText) findViewById(R.id.et_trigger);
        editTrigger.setText(mood.getTrigger());

        // Create the spinner drop-down
        Spinner emotion_spinner = (Spinner) findViewById(R.id.sp_emotion);
        List<String> emotion_categories = new ArrayList<String>();
        // Add the strings to the drop-down for mood
        emotion_categories.add("Happy");
        emotion_categories.add("Sad");
        emotion_categories.add("Angry");
        emotion_categories.add("Confused");
        emotion_categories.add("Disgust");
        emotion_categories.add("Scared");
        emotion_categories.add("Shame");
        emotion_categories.add("Surprised");

        // Create the spinner drop-down
        Spinner situation_spinner = (Spinner) findViewById(R.id.et_social_situation);
        List<String> situation_categories = new ArrayList<String>();
        // Strings for situations
        situation_categories.add("");
        situation_categories.add("Alone");
        situation_categories.add("1 other person");
        situation_categories.add("2 to several people");
        situation_categories.add("Crowd");

        emotionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, emotion_categories);
        situationAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, situation_categories);
        emotionAdapter.setDropDownViewResource(android.R.layout.
                simple_spinner_dropdown_item);
        situationAdapter.setDropDownViewResource(android.R.layout.
                simple_spinner_dropdown_item);
        emotion_spinner.setAdapter(emotionAdapter);
        situation_spinner.setAdapter(situationAdapter);

        // Do something when user selects an emotion
        emotion_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_emotion = parent.getItemAtPosition(position).toString();
                switch (selected_emotion) {
                    case "Happy":
                        emotion = Emotion.happy;
                        break;
                    case "Sad":
                        emotion = Emotion.sad;
                        break;
                    case "Angry":
                        emotion = Emotion.angry;
                        break;
                    case "Confused":
                        emotion = Emotion.confused;
                        break;
                    case "Disgust":
                        emotion = Emotion.disgust;
                        break;
                    case "Scared":
                        emotion = Emotion.fear;
                        break;
                    case "Shame":
                        emotion = Emotion.shame;
                        break;
                    case "Surprised":
                        emotion = Emotion.surprise;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int emotion_spinner_position = emotionAdapter.getPosition(mood.getEmotion().getName());
        emotion_spinner.setSelection(emotion_spinner_position);

        // Do something when user selects a situation
        situation_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                situation = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int situation_spinner_position = situationAdapter.getPosition(mood.getSituation());
        situation_spinner.setSelection(situation_spinner_position);

        // set Date to button
        editDate = (Button) findViewById(R.id.date);

        // date needs to be converted to a string
        editDate_copy = mood.getDate();
        java.text.DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.US);
        editDate.setText(dateFormat.format(editDate_copy));

        // Date&Time Dialog - https://www.youtube.com/watch?v=a_Ap6T4RlYU - by Tihomir RAdeff
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                year = editDate_copy.getYear() + 1900;
                month = editDate_copy.getMonth();
                day = editDate_copy.getDate();

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditMoodActivity.this, EditMoodActivity.this, year, month, day);
                datePickerDialog.show();
            }
        });


        // Open camera on button click and use for the picture
        Button btnOpenCamera = (Button) findViewById(R.id.btn_camera);
        btnOpenCamera.setOnClickListener(btnOpenCameraPressed);



        // Get user location on button click
        locationButton = (Button) findViewById(R.id.btn_location);
        locationText = (TextView) findViewById(R.id.tv_location);

        if (mood.getLocation() != null) {
            editCoordinate = mood.getLocation();
            locationText.setText(editCoordinate.getLat().toString() + " " + editCoordinate.getLon().toString());
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationText.setText(location.getLatitude() + " " + location.getLongitude());

                if (editCoordinate != null) {
                    editCoordinate.setLat(location.getLatitude());
                    editCoordinate.setLon(location.getLongitude());
                } else {
                    editCoordinate = new Coordinate(location.getLatitude(), location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }

        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, 10);
                return;
            }
        } else {
            acquireLocation();
        }
    }

    // setDate - https://www.youtube.com/watch?v=a_Ap6T4RlYU - by Tihomir RAdeff
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        yearFinal = year;
        monthFinal = month + 1;
        dayFinal = day;

        hour = editDate_copy.getHours();
        minute = editDate_copy.getMinutes();

        TimePickerDialog timePickerDialog = new TimePickerDialog(EditMoodActivity.this, EditMoodActivity.this, hour, minute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    // setTime - https://www.youtube.com/watch?v=a_Ap6T4RlYU - by Tihomir RAdeff
    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        hourFinal = hour;
        minuteFinal = minute;

        editDate_copy = new Date(yearFinal - 1900, monthFinal - 1, dayFinal, hourFinal, minuteFinal);

        java.text.DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm", Locale.US);
        editDate.setText(dateFormat.format(editDate_copy));

    }


    /* Gets new location and makes changex to the old one if need be */
    public void acquireLocation() {
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(EditMoodActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EditMoodActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            }
        });
    }

    // Creates the actionbar at the top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds the icons to the action bar is it present
        getMenuInflater().inflate(R.menu.menu_edit_mood, menu);
        return true;
    }

    // When one of the buttons are selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // X button
            case R.id.action_edit_cancel:
                Intent intent = new Intent(EditMoodActivity.this, MyProfileActivity.class);
                startActivity(intent);
                return true;
            // Checkmark button
            case R.id.action_edit_complete:
                // Edit mood and send it right to elasticSearch
                editMood();
                setResult(RESULT_OK);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Edit the mood
    public void editMood() {
        mood.setDate(editDate_copy);
        mood.setEmotion(emotion);
        mood.setSituation(situation);
        if (encodedImage != "") {
            mood.setImgUrl(encodedImage);
        }
        if (editCoordinate != null) {
            mood.setLocation(editCoordinate);
        }
        trigger = editTrigger.getText().toString();
        boolean checkLimit = countLimit();
        mood.setTrigger(trigger);


        if (checkLimit) {
            // Check if app is connected to a network.
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            ArrayList<Mood> moods = CurrentUserSingleton.getInstance().getMyMoodList().getListOfMoods();

            CurrentUserSingleton.getInstance().getMyMoodList().edit(index, mood);
            if (null == activeNetwork) {
                Toast.makeText(getApplicationContext(), "This mood will be edited in database once Moodr has internet connection.", Toast.LENGTH_SHORT).show();
                CurrentUserSingleton.getInstance().getMyOfflineActions().addAction(2, mood);
            } else {
                ElasticSearchMoodController.UpdateMoodTask updateMoodTask = new ElasticSearchMoodController.UpdateMoodTask();
                updateMoodTask.execute(mood);
            }
            new SaveSingleton(getApplicationContext()).SaveSingletons(); // save singleton to disk.
            Intent intent = new Intent(EditMoodActivity.this, MyProfileActivity.class);
            startActivity(intent);
        }
        else {
            triggerError();
        }

    }


    /* Functions for character limit on trigger, 20 characters or 3 words */
    /* ------------------------------------------------------------------- */
    /* ------------------------------------------------------------------- */
    /* ------------------------------------------------------------------- */
    /* ------------------------------------------------------------------- */
    public boolean countLimit() {
        trigger = editTrigger.getText().toString();
        int triggerLength = trigger.length();
        int triggerWords = wordCount(trigger);
        boolean flag = true;
        if (triggerLength > 20 || triggerWords > 3) {
            flag = false;
        }
        return flag;

    }

    public int wordCount(String s) {
        String input = s.trim();
        int words = input.isEmpty() ? 0 : input.split("\\s+").length;
        return words;
    }

    public void triggerError() {
        new AlertDialog.Builder(EditMoodActivity.this)
                .setTitle("Limit Reached")
                .setMessage("Please use only 3 words or 20 characters")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /* ------------------- Functions for image addition ------------------ */
    /* ------------------------------------------------------------------- */
    /* ------------------------------------------------------------------- */
    /* ------------------------------------------------------------------- */
    /* ------------------------------------------------------------------- */


    public View.OnClickListener btnOpenCameraPressed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openCamera();
        }
    };

    public void openCamera() {
        Intent intent = new Intent(EditMoodActivity.this,Camera.class);
        Mood mood = new Mood(CurrentUserSingleton.getInstance().getUser().getName(),emotion);
        mood.setDate(editDate_copy);
        mood.setSituation(situation);
        if (editCoordinate != null) {
            mood.setLocation(editCoordinate);
        }
        trigger = editTrigger.getText().toString();
        mood.setTrigger(trigger);
        intent.putExtra("edit", mood);
        intent.putExtra("edit_index", index);
        startActivity(intent);
    }


    public static Bitmap decodeImage(String imageString) {
        try {
            byte[] encodeByte = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
