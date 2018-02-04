package aaronenders.easyredminetracker;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class List extends AppCompatActivity {
    private Button button;
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
        try
        {
            Toast.makeText(getApplicationContext(),
                    path, Toast.LENGTH_LONG).show();
            URL url;
            url = new URL(path);

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            //File file = new File(CONTEXT.getDir("filesdir", Context.MODE_PRIVATE) + "/yourfile.png");
            File file = new File("/sdcard/downloadedfile.jpg");
            if (file.exists())
            {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            int len;
            while ((len = inStream.read(buff)) != -1)
            {
                outStream.write(buff, 0, len);
            }

            outStream.flush();
            outStream.close();
            inStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
