package ca.uwaterloo.usmmonitor;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
            Log.d(LOG_TAG, "received cpu  = " + generalInfo[0]);
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
}
