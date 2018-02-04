package aaronenders.easyredminetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class List extends AppCompatActivity {
    private Button button;
    private ProgressDialog progressDialog;

    @Override
    public  void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        downloadFile("https://bluehouse.easyredmine.com/projects.xml?key=336fc0d087745a7260d7d8294947ff0c13aaa236");


        button = (Button) findViewById(R.id.refreshButton);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                downloadFile("https://bluehouse.easyredmine.com/projects.xml?key=336fc0d087745a7260d7d8294947ff0c13aaa236");


            }
        });
    }


    public boolean downloadFile(final String path)
    {

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.append("Hello");
        return true;
    }


}
