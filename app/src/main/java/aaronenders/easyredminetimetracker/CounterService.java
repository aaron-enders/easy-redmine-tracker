package aaronenders.easyredminetimetracker;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class CounterService  extends Service {

    private Intent intent;
    public static final String BROADCAST_ACTION = "List";

    private Handler handler = new Handler();
    static long startTime;
    long timeInMilliseconds = 0L;

    long updatedTime = 0L;
    long timeSwapBuff = 0L;
    static int currentTime;
    static boolean timerIsRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        startTime = SystemClock.uptimeMillis();
        intent = new Intent(BROADCAST_ACTION);
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            DisplayLoggingInfo();
            if (timerIsRunning){
                handler.postDelayed(this, 1000); // 1 seconds
            }

        }
    };

    private void DisplayLoggingInfo() {
        timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
        updatedTime = timeSwapBuff + timeInMilliseconds;
        int secs = (int) (updatedTime / 1000) + currentTime;
Log.i("time", String.valueOf(secs));
        intent.putExtra("time", secs);
        sendBroadcast(intent);


/*
        timeInMilliseconds = SystemClock.uptimeMillis() - initial_time;

        int timer = (int) timeInMilliseconds / 1000;
        intent.putExtra("time", timer);
        sendBroadcast(intent);
        */

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sendUpdatesToUI);

    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


}