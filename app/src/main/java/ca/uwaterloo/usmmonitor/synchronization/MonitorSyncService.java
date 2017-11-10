package ca.uwaterloo.usmmonitor.synchronization;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by liuyangren on 11/8/17.
 */

public class MonitorSyncService extends Service {
    private static final String LOG_TAG = MonitorSyncAdapter.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static MonitorSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.i("USM-"+LOG_TAG, "sync service created.");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null)
                sSyncAdapter = new MonitorSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    /**
     * Logging-only destructor.
     */
    public void onDestroy() {
        super.onDestroy();
        Log.i("USM-"+LOG_TAG, "sync service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}