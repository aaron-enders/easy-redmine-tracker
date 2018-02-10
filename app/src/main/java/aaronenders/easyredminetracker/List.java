package aaronenders.easyredminetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class List extends AppCompatActivity {
    private Button button;
    private ProgressDialog progressDialog;




    @Override
    public  void onCreate(Bundle savedInstanceState) {

        final String issuesUrl = "https://bluehouse.easyredmine.com/issues.xml?key=336fc0d087745a7260d7d8294947ff0c13aaa236&sort=updated_on:desc&offset=0&limit=100&page=";



        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        downloadFile(issuesUrl);


        button = (Button) findViewById(R.id.refreshButton);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                downloadFile(issuesUrl);


            }
        });
    }


    public boolean downloadFile(final String path)
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            Document dom = db.parse(new URL(path).openStream());
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

                            NodeList  issueAssignedTo = el.getElementsByTagName("assigned_to");
                            String issueAssignedToId;
                            if (issueAssignedTo.getLength() > 0) {
                                issueAssignedToId = issueAssignedTo.item(0).getAttributes().getNamedItem("id").getNodeValue();
                            }else{
                                issueAssignedToId = "-";
                            }
                            if (issueAssignedToId.equals("34")){
                                LinearLayout layoutWrapper = (LinearLayout) findViewById(R.id.linearMain);

                                LinearLayout issueRow = new LinearLayout(this);
                                issueRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


                                Button issueButtonName = new Button(this);
                                Button issueButtonTrack = new Button(this);
                                issueRow.addView(issueButtonName);
                                issueRow.addView(issueButtonTrack);
                                //issueButtonName.setId(issueId);
                                issueButtonName.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        countIssueTime(issueId);
                                    }
                                });
                                issueButtonTrack.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        countIssueTime(issueId);
                                    }
                                });

                                issueRow.setOrientation(LinearLayout.HORIZONTAL);
                                LinearLayout.LayoutParams forButtonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                issueButtonName.setGravity(Gravity.CENTER);
                                DisplayMetrics dm = new DisplayMetrics();
                                this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
                                int width = dm.widthPixels;
                                issueButtonName.getLayoutParams().width = (width / 100)*70;
                                issueButtonTrack.getLayoutParams().width = (width/ 100) * 20;

                                issueButtonName.setText(issueAssignedToId + ": " + issueSubject);
                                issueButtonTrack.setText("0:00");

                                layoutWrapper.addView(issueRow);
                            }


                        }
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean countIssueTime(final String IssueId){
        Toast.makeText(getApplicationContext(), IssueId, Toast.LENGTH_SHORT).show();
        return true;
    }


}
