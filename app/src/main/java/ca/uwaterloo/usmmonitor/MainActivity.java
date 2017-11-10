package ca.uwaterloo.usmmonitor;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Launcher Activity for the USMMonitor app.
 */
public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private ActivityManager activityManager;
    private TextView mGenralCpuUsage, mTotalMem, mAvailMem;
    private NumberFormat formater = NumberFormat.getNumberInstance(Locale.US);
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] generalInfo = intent.getStringExtra("cpuMem").split(ProcFolderParser.DELIMITER);
//            Log.d(LOG_TAG, "received cpu  = " + generalInfo[0]);
            mGenralCpuUsage.setText("Total CPU usage: " + generalInfo[0] + "%");
            mAvailMem.setText("Available Memory: " + generalInfo[1] + "KB, " + generalInfo[2] + "%");
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, RunningProcessesFragment.newInstance())
                    .commit();
        }
        // Start the Parser service to get CPU and Memory info from /proc folder
        Intent intent = new Intent(this, ProcFolderParser.class);
        startService(intent);
        // find all UI widgets
        mTotalMem = (TextView) findViewById(R.id.total_mem);
        mAvailMem = (TextView) findViewById(R.id.avail_mem);
        mGenralCpuUsage = (TextView) findViewById(R.id.general_cpu);

        // set text for total memory and threshold, because they are constant and we don't need
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
        mTotalMem.setText("Total memory: " + formater.format(memInfo.totalMem >> 10) + "KB   Threshold: "
        + formater.format(memInfo.threshold >> 10) + "KB");

        // Create the dummy account
//        SyncUtils.CreateSyncAccount(this);
        // Pass the settings flags by inserting them in a bundle
//        Bundle settingsBundle = new Bundle();
//        settingsBundle.putBoolean(
//                ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        settingsBundle.putBoolean(
//                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
//        Log.i(LOG_TAG, "send sync req from Main");
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("general"));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
//    public static Account CreateSyncAccount(Context context) {
//        // Create the account type and default account
//        Account newAccount = new Account(
//                ACCOUNT, ACCOUNT_TYPE);
//        // Get an instance of the Android account manager
//        AccountManager accountManager =
//                (AccountManager) context.getSystemService(
//                        ACCOUNT_SERVICE);
//        /*
//         * Add the account and account type, no password or user data
//         * If successful, return the Account object, otherwise report an error.
//         */
//        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
//            /*
//             * If you don't set android:syncable="true" in
//             * in your <provider> element in the manifest,
//             * then call context.setIsSyncable(account, AUTHORITY, 1)
//             * here.
//             */
//        } else {
//            /*
//             * The account exists or some other error occurred. Log this, report it,
//             * or handle it internally.
//             */
//        }
//    }
}
