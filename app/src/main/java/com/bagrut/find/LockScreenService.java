package com.bagrut.find;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * a service that listens when phone screen is off
 */
public class LockScreenService extends Service
{
    /**
     * power manager gives info about status of screen
     */
    private PowerManager pm;
    /**
     * a timer to check status every specified amount of seconds
     */
    private Timer timer;
    /**
     * used for checking if screen was off
     */
    private boolean deactivated = false;

    /**
     * constructor
     */
    public LockScreenService()
    {
    }

    /**
     * on bind service
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;

    }

    /**
     * on create service
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

    }

    /**
     * initializes timer
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                if(!pm.isInteractive())
                {
                    deactivated = true;
                }
                else if(deactivated)
                {
                    Intent intentOut = new Intent();
                    intentOut.setAction("com.example.Broadcast");
                    intentOut.putExtra("lockScreen", true);
                    sendBroadcast(intentOut);
                    this.cancel();
                }
            }
        }, 0, 3000);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * destroys service and stops timer
     */
    @Override
    public void onDestroy()
    {
        timer.cancel();
        super.onDestroy();
    }
}
