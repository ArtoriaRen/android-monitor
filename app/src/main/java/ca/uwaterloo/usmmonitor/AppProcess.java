package ca.uwaterloo.usmmonitor;

import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.R.attr.process;
import static android.R.attr.start;

/**
 * Created by liuyangren on 2017-08-11.
 */

public class AppProcess {
    private final String LOG_TAG = AppProcess.class.getSimpleName();
    private final int pid;
    private String packageName;
    public float cpuUsage;
    private BufferedReader reader;
    // To avoid divided-by-zero error, set 1 as the initial value of second element
    private long cpuTime = 0, cpuTimeLast = 0;

    public AppProcess(int pid) throws IOException {
        this.pid = pid;
        readPackageName();
    }

    public void readPackageName() throws IOException {
        reader = new BufferedReader(new FileReader("/proc/" + this.pid + "/cmdline"));
        this.packageName = reader.readLine().trim();
        reader.close();
        return;
//        // We can also get package name fro the first lin of /proc/pid/status
//        if (TextUtils.isEmpty(this.packageName)){
//                reader = new BufferedReader(new FileReader(String.format("/proc/%d/status", this.pid)));
//                // the first line in /proc/pid/status is "Name: <package name>"
//                this.packageName = reader.readLine().split(":")[1].trim();
//                reader.close();
//        }
    }


    /**
     * we need sample twice and use the difference to calculate cpu usage.
     */
    public void readCpuUsage() throws IOException {
        String[] fieldsCpu;
        //---The first sample--
        // process CPU time is measured in clock ticks
        this.reader = new BufferedReader(new FileReader(String.format("/proc/%d/stat", this.pid)));
        fieldsCpu = reader.readLine().split("[ ]+", 23);
        this.cpuTime = Long.parseLong(fieldsCpu[13]) + Long.parseLong(fieldsCpu[14]) + Long.parseLong(fieldsCpu[15]) + Long.parseLong(fieldsCpu[16]);
        Log.d(LOG_TAG, "packageName = " + packageName + ",fieldsCpu[13] = " + fieldsCpu[13] + ", current time =" + System.currentTimeMillis() / 1000);
        this.reader.close();
        this.cpuUsage = (this.cpuTime - this.cpuTimeLast) / (float) ProcFolderParser.samplePeriod;
        Log.d(LOG_TAG, packageName + "cpu usage = " + cpuUsage + ": process cpuTime = " + cpuTime + ", Last time = " + cpuTimeLast);
        this.cpuTimeLast = this.cpuTime;
    }

//    /**
//     * Bad idea: cannot reflect instant burst cpu usage of a process
//     */
//    private void readCpuUsage(){
//        String[] fields = null;
//        try {
//            reader = new BufferedReader(new FileReader(String.format("/proc/%d/stat", this.pid)));
//            fields = reader.readLine().split("[ ]+", 23);
//            reader.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (fields != null) {
//            // calculate total cpu time consumed by this process,
//            // refer to https://stackoverflow.com/questions/16726779/how-do-i-get-the-total-cpu-usage-of-an-application-from-proc-pid-stat
//            long cpuTime = Long.parseLong(fields[13])
//            +Long.parseLong(fields[14]) + Long.parseLong(fields[15]) + Long.parseLong(fields[16]);
//            long startTime = Long.parseLong(fields[21]);
//            // cpu percentage
//            double cpuUptime = getCpuUptime();
//            cpuUsage = String.format("%.2f", cpuTime * 100 / (getCpuUptime() * ProcFolderParser.USER_HZ - startTime));
//
//            Log.d(LOG_TAG, "process cpuTime = "+ cpuTime + ", cpuUptime = " + cpuUptime
//                    + ", USER_HZ = " + ProcFolderParser.USER_HZ
//                    + ", process startTime = " + startTime );
//        }
//
//
//    }

//    /**
//     *  Get CPU uptime is measured in seconds
//     */
//    private double getCpuUptime(){
//        String line = null;
//        try {
//            reader = new BufferedReader(new FileReader("/proc/uptime"));
//            line = reader.readLine();
//            reader.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (line != null){
//            String[] fields = line.split("[ ]+");
//            return Double.parseDouble(fields[0]);
//        }
//        return 1; // don't return 0 in case of divided-by-0 error
//    }

    public int getPid() {
        return pid;
    }

    public String getPackageName() {
        return packageName;
    }

}
