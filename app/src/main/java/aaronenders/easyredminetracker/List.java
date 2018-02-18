package aaronenders.easyredminetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class List extends AppCompatActivity {
    private ImageButton button;
    private ProgressDialog progressDialog;


    public String companyName;
    public String apiKey;
    public String userId;

    final Map <Integer, Integer> timeEntryIds = new HashMap<Integer, Integer>();

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));

        }
        return true;
    }



    @Override
    public void onResume() {
        super.onResume();
        // To use the preferences when the activity starts and when the user navigates back from the settings activity.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String defaultCompanyName = getResources().getString(R.string.pref_default_companyname_text);
        companyName = preferences.getString("companyName", defaultCompanyName);
        String defaultApiKey = getResources().getString(R.string.pref_default_apikey_text);
        apiKey = preferences.getString("apiKey", defaultApiKey);
        String defaultUserId = getResources().getString(R.string.pref_default_userid_text);
        userId = preferences.getString("userId", defaultUserId);


        downloadFile();
        Toast.makeText(getApplicationContext(), "Name: "+companyName, Toast.LENGTH_SHORT).show();

    }


    @Override
    public  void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //System.out.println("Current array list is:"+timeEntryIds);




        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }









        downloadFile();


        button = findViewById(R.id.pauseButton);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                timerHandler.removeCallbacks(updateTimerThread);
                timerHandler.postDelayed(updateTimerThread, 5000);

            }
        });
    }


    public boolean downloadFile()
    {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        LinearLayout layoutWrapper = (LinearLayout) findViewById(R.id.linearMain);
        if(((LinearLayout) layoutWrapper).getChildCount() > 0)
            ((LinearLayout) layoutWrapper).removeAllViews();

        for(int offset=0; offset<=5; offset++) {
            String path = "https://"+companyName+".easyredmine.com/issues.xml?key="+apiKey+"&sort=updated_on:desc&offset="+(offset * 50)+"&limit=50&page=&sort=priority:desc";
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            try {
                InputStream xml = new URL(path).openStream();
                Document dom = db.parse(xml);
                //Element docEle = dom.getDocumentElement();
                Node projects = dom.getElementsByTagName("issues").item(0);
                NodeList nl = projects.getChildNodes();
                if (nl != null) {
                    int length = nl.getLength();
                    for (int i = 0; i < length; i++) {
                        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element el = (Element) nl.item(i);
                            if (el.getNodeName().contains("issue")) {
                                final String issueId = el.getElementsByTagName("id").item(0).getTextContent();
                                String issueSubject = el.getElementsByTagName("subject").item(0).getTextContent();

                                NodeList issueAssignedTo = el.getElementsByTagName("assigned_to");
                                String issueAssignedToId;
                                if (issueAssignedTo.getLength() > 0) {
                                    issueAssignedToId = issueAssignedTo.item(0).getAttributes().getNamedItem("id").getNodeValue();
                                } else {
                                    issueAssignedToId = "-";
                                }
                                if (issueAssignedToId.equals(userId)) {


                                    // ------------

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
                                    issueButtonTrack.setId(Integer.parseInt(issueId));
                                    issueButtonTrack.setOnClickListener(new View.OnClickListener() {
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
                                    issueButtonTrack.setText("");
                                    issueRow.addView(issueButtonTrack);
                                    issueButtonTrack.getLayoutParams().width = (width / 100) * 20;

                                    // --------------

                                    final Button issueButtonName = new Button(this);
                                    issueButtonName.setGravity(Gravity.LEFT);

                                    issueButtonName.setOnClickListener(new View.OnClickListener() {
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
                    }
                }
                xml.close();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    public boolean countIssueTime(final Button clickedButtonTrack) throws SAXException {

        if (currentIssueButtonTrack != null){
            Integer lastIssueId = currentIssueButtonTrack.getId();
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

                Log.i("Stunden", lastHours.toString());

                try {
                    if (timeEntryIds.containsKey(lastIssueId)){
                        Log.i("Test GEFUNDEN?!:", timeEntryIds.get(lastIssueId).toString());
                        int timeEntryId = timeEntryIds.get(lastIssueId);
                        updateTimeRecord(timeEntryId, lastHours.toString());
                        Toast.makeText(getApplicationContext(), "Zeieintrag "+timeEntryId + " aktualisiert!", Toast.LENGTH_SHORT).show();

                    }else{
                        int timeEntryId = createTimeRecord(lastIssueId, lastHours.toString());
                        if (timeEntryId != 0){
                            Toast.makeText(getApplicationContext(), "Zeieintrag "+timeEntryId + " erstellt!", Toast.LENGTH_SHORT).show();
                            timeEntryIds.put(lastIssueId, timeEntryId);
                            Log.i("Zeiteintrag 2", String.valueOf(timeEntryId));
                        }else{
                            Toast.makeText(getApplicationContext(), "Zeieintrag konnte nicht erstellt werden!", Toast.LENGTH_SHORT).show();
                        }

                    }
                    //button.setText(button.getText() + String.valueOf(timeEntryId));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Zeit muss mindesten 1min sein, um getrackt zu werden.", Toast.LENGTH_SHORT).show();
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
        currentIssueButtonTrack.setBackgroundColor(Color.parseColor("#82b91e"));
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(updateTimerThread, 0);
        return true;
    }

    public int createTimeRecord(Integer currentIssueId, String currentHours) throws IOException, SAXException {
        URL url = new URL("https://"+companyName+".easyredmine.com/time_entries.xml?key="+apiKey);
        String body = "<time_entry><issue_id>"+currentIssueId.toString()+"</issue_id><user_id>"+userId+"</user_id><activity_id>13</activity_id><hours>"+currentHours+"</hours><comments>EasyRedmine Time Tracker record</comments>  <spent_on>2018-02-10</spent_on></time_entry>";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            //conn.setReadTimeout(10000);
            //conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/xml");
            conn.setRequestProperty("Content-Type", "application/xml");
            OutputStream output = new BufferedOutputStream(conn.getOutputStream());
            output.write(body.getBytes());
            output.flush();
            output.close();
        }finally {
            //conn.disconnect();
        }
        BufferedReader br;
        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        System.out.print("ANTWORT: "+br);
        String response = new String();
        for (String line; (line = br.readLine()) != null; response += line);
        Log.i("Antworten vom Erstellen", response);
        br.close();
        conn.disconnect();
        return getTimeEntryIdFromXml(response);
    }
    public int updateTimeRecord(Integer timeEntryId, String currentHours) throws IOException, SAXException {
        Log.i("URL", "https://"+companyName+".easyredmine.com/time_entries/"+timeEntryId+".xml?key="+apiKey);
        URL url = new URL("https://"+companyName+".easyredmine.com/time_entries/"+timeEntryId+".xml?key="+apiKey);
        String body = "<time_entry><comments>updated time entry comment</comments></time_entry>";
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/xml");
            conn.setRequestProperty("Content-Type", "application/xml");
            OutputStream output = new BufferedOutputStream(conn.getOutputStream());
            output.write(body.getBytes());
            output.flush();
            output.close();
        }finally {
        }
        BufferedReader br;
        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        System.out.print("ANTWORT: "+br);
        String response = new String();
        for (String line; (line = br.readLine()) != null; response += line);
        Log.i("Antwort vom updaten", response);
        br.close();
        conn.disconnect();
        return timeEntryId;
    }

    public int getTimeEntryIdFromXml(String xml) throws IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        InputSource is = new InputSource(new StringReader(xml));
        Document dom = builder.parse(is);
        if (dom.getElementsByTagName("id").item(0) != null){
            int timeEntry = Integer.parseInt(dom.getElementsByTagName("id").item(0).getTextContent());
            return timeEntry;
        }else{
            return 0;
        }

        //Log.i("Zeiteintrag", String.valueOf(timeEntry));

    }

    int currentTime;
    long timeInMilliseconds = 0L;
    private long startTime = 0L;
    long updatedTime = 0L;
    long timeSwapBuff = 0L;
    private Button currentIssueButtonTrack;
    final Handler timerHandler = new Handler();
    boolean isPaused = true;

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
            timerHandler.postDelayed(this, 0);
        }
    };

}
