package com.example.audioplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StopService extends Service
{


    @Override
    public int onStartCommand(Intent data, int sid, int flag)
    {
        GlobalMedia.mp.stop();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}