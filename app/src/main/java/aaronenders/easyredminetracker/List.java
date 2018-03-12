package aaronenders.easyredminetracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
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

    static Boolean refreshView = false;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_menu, menu);
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.login) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (id == R.id.refresh) {
            initializeView();
        }
        if (id == R.id.home) {
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        final View  mProgressView = findViewById(R.id.login_progress);
        final View  mMainView = findViewById(R.id.linearMain);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultCompanyName = "";
        companyName = preferences.getString("companyName", defaultCompanyName);
        String defaultApiKey = "";
        apiKey = preferences.getString("apiKey", defaultApiKey);
        String defaultUserId = "";
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
            if (refreshView) {
                initializeView();
                refreshView = false;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void initializeView(){
        showProgress(true);
        issues = Api.getIssues(companyName, apiKey, userId);
        if (issues.length == 0) {
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
        } else {
            addIssueButtons(issues);
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
        showProgress(false);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultCompanyName = "";
        companyName = preferences.getString("companyName", defaultCompanyName);
        String defaultApiKey = "";
        apiKey = preferences.getString("apiKey", defaultApiKey);
        String defaultUserId = "";
        userId = preferences.getString("userId", defaultUserId);
        if((!preferences.contains("companyName")) || (!preferences.contains("apiKey")) || (!preferences.contains("userId")
                || companyName.isEmpty() ||  apiKey.isEmpty() || userId.isEmpty())){
            Intent i = new Intent(List.this, LoginActivity.class);
            startActivity(i);
        }else{
            refreshView = true;
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

            CounterService.currentTime = currentTimeSeconds + (60 * currentTimeMinutes) + (3600 * currentTimeHours);
        }else{
            CounterService.currentTime = 0;
        }
        String currentHours = Integer.toString(CounterService.currentTime / 60 / 60);
        currentIssueButtonTrack = clickedButtonTrack;
        currentIssueButtonTrack.setBackgroundColor(Color.parseColor("#efa126"));
        CounterService.startTime = SystemClock.uptimeMillis();
        if (Objects.equals(newIssueId, lastIssueId) && CounterService.timerIsRunning){
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.trackingStopped), Toast.LENGTH_SHORT).show();
            CounterService.timerIsRunning = false;
            unregisterReceiver(broadcastReceiver);
            stopService(intent);
            return "stopped";
        }else{
            intent = new Intent(List.this, CounterService.class);
            startService(intent);
            registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));
            CounterService.timerIsRunning = true;
        }
        return "saved";
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };
    private void updateUI(Intent intent) {
        int secs = intent.getIntExtra("time", 0);
        int mins = secs / 60;
        int hours = secs / 60 / 60;
        secs = secs % 60;
        String timerValue = ("" + hours + ":"
                + "" + mins + ":"
                + String.format("%02d", secs));
        Log.i("kommt an", timerValue);
        currentIssueButtonTrack.setText(timerValue);
    }


    Intent intent;

    private Button currentIssueButtonTrack;

}
