package aaronenders.easyredminetracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Settings activity that contains a fragment displaying the preferences.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the preferences fragment as the content of the activity
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }
}