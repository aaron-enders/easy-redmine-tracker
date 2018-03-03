package aaronenders.easyredminetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class List extends AppCompatActivity {
    private ImageButton button;
    private ProgressDialog progressDialog;


    public String companyName;
    public String apiKey;
    public String userId;
    public String avatar = "https://www.easyredmine.com/images/stories/easy_logo.png";
    public String userName = "";
    Api Api = new Api();
    String[][] issues = new String[25][4];
    final Map <Integer, Integer> timeEntryIds = new HashMap<Integer, Integer>();

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.login) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (id == R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultCompanyName = getResources().getString(R.string.pref_default_companyname_text);
        companyName = preferences.getString("companyName", defaultCompanyName);
        String defaultApiKey = getResources().getString(R.string.pref_default_apikey_text);
        apiKey = preferences.getString("apiKey", defaultApiKey);
        String defaultUserId = getResources().getString(R.string.pref_default_userid_text);
        userId = preferences.getString("userId", defaultUserId);
        if((!preferences.contains("companyName")) || (!preferences.contains("apiKey")) || (!preferences.contains("userId")
        || companyName.isEmpty() ||  apiKey.isEmpty() || userId.isEmpty())){
            Snackbar snacky = Snackbar.make(findViewById(R.id.ListContainer), R.string.makeSettingsFirst,
                    Snackbar.LENGTH_INDEFINITE);
            View snackyView = snacky.getView();
            TextView snackyTextView = (TextView) snackyView.findViewById(android.support.design.R.id.snackbar_text);
            snackyTextView.setMaxLines(3);
            snacky.setAction(R.string.makeSettingsButton, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(List.this, SettingsActivity.class);
                    startActivity(i);
                }
            }).show();

        }else{
            String[] userInfos = Api.getUserInfos(companyName, userId, apiKey);
            setAvatarAndName(userInfos[1], userInfos[0]);

            issues = Api.getIssues(companyName, apiKey, userId);
            if (issues.length == 0){
                Snackbar snacky = Snackbar.make(findViewById(R.id.ListContainer), R.string.nothingFoundCheckSettings,
                        Snackbar.LENGTH_INDEFINITE);
                View snackyView = snacky.getView();
                TextView snackyTextView = (TextView) snackyView.findViewById(android.support.design.R.id.snackbar_text);
                snackyTextView.setMaxLines(3);
                snacky.setAction(R.string.makeSettingsButton, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(List.this, SettingsActivity.class);
                        startActivity(i);
                    }
                }).show();
            }else{
                addIssueButtons(issues);
            }
        }
    }



    public void addIssueButtons(String[][] issues){
        LinearLayout layoutWrapper = (LinearLayout) findViewById(R.id.linearMain);
        if(((LinearLayout) layoutWrapper).getChildCount() > 0)
            ((LinearLayout) layoutWrapper).removeAllViews();
        for (String[] issue : issues) {
            if (issue[0] != null) {
                String issueTimeEntryHours = "";
                Integer issueId = Integer.parseInt(issue[0]);
                String issueSubject = issue[1];
                if (issue[2] != null){
                    Integer issueTimeEntryId = Integer.parseInt(issue[2]);
                    issueTimeEntryHours = issue[3];

                    timeEntryIds.put(issueId, issueTimeEntryId);
                }


                LinearLayout issueRow = new LinearLayout(this);
                issueRow.setBackgroundColor(Color.parseColor("#ffffff"));
                issueRow.setOrientation(LinearLayout.HORIZONTAL);
                issueRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                LinearLayout.LayoutParams issueRowParams = (LinearLayout.LayoutParams) issueRow.getLayoutParams();
                issueRowParams.setMargins(0, 2, 0, 0);
                layoutWrapper.addView(issueRow);

                // --------------

                DisplayMetrics dm = new DisplayMetrics();
                this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels;

                // -----------------

                final Button issueButtonTrack = new Button(this);
                issueButtonTrack.setId(issueId);
                issueButtonTrack.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        try {
                            countIssueTime(issueButtonTrack);
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //LinearLayout.LayoutParams forButtonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                issueButtonTrack.setBackgroundColor(0x00000000);
                issueButtonTrack.setText(issueTimeEntryHours);
                issueRow.addView(issueButtonTrack);
                issueButtonTrack.getLayoutParams().width = (width / 100) * 20;

                // --------------

                final Button issueButtonName = new Button(this);
                issueButtonName.setGravity(Gravity.LEFT);

                issueButtonName.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        try {
                            countIssueTime(issueButtonTrack);
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }
                });
                issueButtonName.setGravity(Gravity.CENTER);
                issueButtonName.setBackgroundColor(0x00000000);
                issueButtonName.setText(issueSubject);
                issueRow.addView(issueButtonName);
                issueButtonName.getLayoutParams().width = (width / 100) * 70;
            }

        }
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }
    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultCompanyName = getResources().getString(R.string.pref_default_companyname_text);
        companyName = preferences.getString("companyName", defaultCompanyName);
        String defaultApiKey = getResources().getString(R.string.pref_default_apikey_text);
        apiKey = preferences.getString("apiKey", defaultApiKey);
        String defaultUserId = getResources().getString(R.string.pref_default_userid_text);
        userId = preferences.getString("userId", defaultUserId);
        if((!preferences.contains("companyName")) || (!preferences.contains("apiKey")) || (!preferences.contains("userId")
                || companyName.isEmpty() ||  apiKey.isEmpty() || userId.isEmpty())){
            Intent i = new Intent(List.this, LoginActivity.class);
            startActivity(i);
        }


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        FloatingActionButton pauseButton = findViewById(R.id.pauseButton);
    }

    public boolean setAvatarAndName(String avatar, String name){
        if (avatar != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            Drawable avatarDrawable = LoadImageFromWebOperations(avatar);
            getSupportActionBar().setIcon(avatarDrawable);
        }
        if (name != null){
            setTitle(" "+name);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String countIssueTime(final Button clickedButtonTrack) throws SAXException {
        Integer lastIssueId = 0;
        Integer newIssueId = clickedButtonTrack.getId();
        if (currentIssueButtonTrack != null){
            lastIssueId = currentIssueButtonTrack.getId();
            String lastTimeString = currentIssueButtonTrack.getText().toString();
            String[] lastTimeParts=lastTimeString.split(":");
            int lastTimeHours=Integer.parseInt(lastTimeParts[0]);
            int lastTimeMinutes=Integer.parseInt(lastTimeParts[1]);
            int lastTimeSeconds=Integer.parseInt(lastTimeParts[2]);
            int lastTime = (lastTimeSeconds + (60 * lastTimeMinutes) + (3600 * lastTimeHours));
            if (lastTime > 60){
                BigDecimal lastTimeDec = new BigDecimal(lastTime);
                BigDecimal divideBy =  new BigDecimal(100);
                BigDecimal lastHours = lastTimeDec.divide(BigDecimal.valueOf(3600), 5, RoundingMode.HALF_UP);
                try {
                    if (timeEntryIds.containsKey(lastIssueId)){
                        int timeEntryId = timeEntryIds.get(lastIssueId);
                        if (Api.updateTimeRecord(timeEntryId, lastHours.toString(), companyName, apiKey)){
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.updatedTimeEntry), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.couldNotupdateTimeEntry), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        int timeEntryId = Api.createTimeRecord(lastIssueId, lastHours.toString(), companyName, userId, apiKey);
                        if (timeEntryId != 0){
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.createdTimeEntry)+": "+timeEntryId, Toast.LENGTH_SHORT).show();
                            timeEntryIds.put(lastIssueId, timeEntryId);
                        }else{
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.couldNotCreate), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.notMinTime), Toast.LENGTH_SHORT).show();
            }
            currentIssueButtonTrack.setBackgroundColor(0x00000000);
        }
        String currentTimeString = clickedButtonTrack.getText().toString();
        if (currentTimeString != ""){
            String[] currentTimeParts=currentTimeString.split(":");
            int currentTimeHours=Integer.parseInt(currentTimeParts[0]);
            int currentTimeMinutes=Integer.parseInt(currentTimeParts[1]);
            int currentTimeSeconds=Integer.parseInt(currentTimeParts[2]);

            currentTime = currentTimeSeconds + (60 * currentTimeMinutes) + (3600 * currentTimeHours);
        }else{
            currentTime = 0;
        }
        String currentHours = Integer.toString(currentTime / 60 / 60);
        currentIssueButtonTrack = clickedButtonTrack;
        currentIssueButtonTrack.setBackgroundColor(Color.parseColor("#efa126"));
        startTime = SystemClock.uptimeMillis();
        if (Objects.equals(newIssueId, lastIssueId) && timerIsRunning){
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.trackingStopped), Toast.LENGTH_SHORT).show();
            timerHandler.removeCallbacks(updateTimerThread);
            timerHandler.postDelayed(updateTimerThread, 1);
            timerIsRunning = false;

            return "stopped";
        }else{
            timerIsRunning = true;
            timerHandler.postDelayed(updateTimerThread, 0);
        }
        return "saved";
    }



    int currentTime;
    long timeInMilliseconds = 0L;
    private long startTime = 0L;
    long updatedTime = 0L;
    long timeSwapBuff = 0L;
    private Button currentIssueButtonTrack;
    final Handler timerHandler = new Handler();
    boolean timerIsRunning = false;

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000) + currentTime;
            int mins = secs / 60;
            int hours = secs / 60 / 60;
            secs = secs % 60;
            currentIssueButtonTrack.setText("" + hours + ":"
                    + "" + mins + ":"
                    + String.format("%02d", secs));
            if (timerIsRunning){
                timerHandler.postDelayed(this, 0);
            }
        }
    };
}
