package ca.uwaterloo.usmmonitor.details;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.util.regex.Pattern;

import ca.uwaterloo.usmmonitor.ListAdapter;
import ca.uwaterloo.usmmonitor.R;
import ca.uwaterloo.usmmonitor.database.ProcessInfoContract.ProcessInfoEntry;
import ca.uwaterloo.usmmonitor.database.ProcessInfoDbHelper;
import ca.uwaterloo.usmmonitor.ProcFolderParser;

import static ca.uwaterloo.usmmonitor.ProcFolderParser.formatter;

/**
 * Created by liuyangren on 2017-08-31.
 */

public class DetailActivity extends AppCompatActivity{
    private int pid;
    private String packageName;
    private float cpuUsage;
    private int memoryUsageKb;
    private float memoryUsagePercent;
    private TextView pidTextView;
    private ProcessInfoDbHelper mDbHelper;
    private SQLiteDatabase mDb;
    // activity manager is needed to kill background processes associated with a package
    private ActivityManager am;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        pidTextView = (TextView) findViewById(R.id.pid);
        Button killButton = (Button) findViewById(R.id.kill_button);
        parsingIntent(getIntent());
        // Create a DB helper (this will create the DB if run for the first time)
        mDbHelper = new ProcessInfoDbHelper(this);
        // Keep a reference to the mDb until this activity is paused. Get a writable database
        // because the process info will be added to DB once user click the "killButton".
        mDb = mDbHelper.getWritableDatabase();
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);




    }

    /**
     * This method is called when user clicks on the "Kill This Process" button.
     * First save the data into database and then kill the corresponding process.
     * @param view The calling view (button)
     */
    public void addToDatabase(View view) {
        ContentValues cv = new ContentValues();
        cv.put(ProcessInfoEntry.COLUME_PACKAGE_NAME, packageName);
        cv.put(ProcessInfoEntry.COLUME_CPU_USAGE, cpuUsage);
        cv.put(ProcessInfoEntry.COLUME_MEMORY_USAGE_KB, memoryUsageKb);
        cv.put(ProcessInfoEntry.COLUME_MEMORY_USAGE_PERCENT, memoryUsagePercent);
        // This insert method returns the id of new record added
        mDb.insert(ProcessInfoEntry.TABLE_NAME, null, cv);
        // cannot kill process in other packages
//        android.os.Process.killProcess(pid);
        am.killBackgroundProcesses(packageName);
    }


    /**
     * This method get cpu and memory usage info from the intent.
     * @param intent The intent starting this "detail" activity.
     */
    private void parsingIntent(Intent intent){
        pid = Integer.parseInt(intent.getStringExtra(ListAdapter.PID));
        pidTextView.setText("PID = " + pid);
        packageName = intent.getStringExtra(ProcessInfoEntry.COLUME_PACKAGE_NAME);
        cpuUsage = Float.parseFloat(intent.getStringExtra(ProcessInfoEntry.COLUME_CPU_USAGE));
        try {
            memoryUsageKb = formatter.parse(intent.getStringExtra(ProcessInfoEntry.COLUME_MEMORY_USAGE_KB)).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        memoryUsagePercent = Float.parseFloat(intent.getStringExtra(ProcessInfoEntry.COLUME_MEMORY_USAGE_PERCENT));

    }

    @Override
    protected void onDestroy() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
        if (mDb != null) {
            mDb.close();
        }
        super.onDestroy();
    }
}
