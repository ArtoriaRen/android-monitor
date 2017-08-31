package ca.uwaterloo.usmmonitor;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.os.Debug;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

import static android.R.attr.process;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * 1. All PIDs are obtained by parsing /proc folder
 * 2. Mem info of each process is get through ActivityManager.getProcessMemoryInfo( int[] pids).
 * 3. CPU info of each process is get through parsing /proc/[pid]/stat file, which is implemented in
 * AppProcess.java.
 */
public class ProcFolderParser extends IntentService {
    private HashMap<Integer, AppProcess> processHashMap = new HashMap<>();
    // To store all PIDs
    private final String LOG_TAG = ProcFolderParser.class.getSimpleName();
    // To store all AppProcess object correspond to all PIDs
    private List<Integer> pidList = new ArrayList<>();
    // we don't have permission to read file with pid<=63
    private final int NO_PERMISSION_PID = 63;
    // Use activity manager to get mem info for each process
    private ActivityManager activityManager;
    // To store mem info of all processes.
    private Debug.MemoryInfo[] processMemArray;
    // how many clock ticks in one second, this is used to calculated cpu usage of each process
    public static int USER_HZ;
    public static int NUM_CPU = 1;
    public static long samplePeriod = 1;
    public float generalCpuUsage = 0.0f;
    private ActivityManager.MemoryInfo generalMem = new ActivityManager.MemoryInfo();
    // total memory of the device, this value will also be used to calculate mem percentage of each proc by AppProcess.class
    public static long totalMem;
    private NumberFormat formater = NumberFormat.getNumberInstance(Locale.US);
    private long totalTime = 1, busyTime = 1, totalTimeLast = 0, busyTimeLast = 0;
    public static final String DELIMITER = "#";
    private int[] keysArray;


    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ca.uwaterloo.usmmonitor.action.FOO";
    private static final String ACTION_BAZ = "ca.uwaterloo.usmmonitor.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ca.uwaterloo.usmmonitor.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ca.uwaterloo.usmmonitor.extra.PARAM2";

    public ProcFolderParser() {
        super("ProcFolderParser");
//        getUserHz();
//        getCpuNumber();
    }

//    private void getCpuNumber() {
//        BufferedReader reader = null;
//        String line;
//        String[] lineSplit;
//
//        int cpuNum = 0;
//        try {
//            reader = new BufferedReader(new FileReader("/proc/cpuinfo"));
//            line = reader.readLine();
//            do {
//                lineSplit = line.split(":");
//                if (lineSplit[0].trim().startsWith("processor")){
//                    cpuNum ++;
//                }
//                line = reader.readLine();
//            } while (line!= null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (cpuNum != 0) {
//            this.NUM_CPU = cpuNum;
//        }
//
//    }


    @Override
    protected void onHandleIntent(Intent intent) {
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(generalMem);
        // right shift by 10 bits, convert to KB, this is used by AppProcess.java
        this.totalMem = generalMem.totalMem >> 10;
        while (true) {
            // getAllPid() must be executed first because get memory info need the pid.
            getAllPid();
            Set<Integer> keys = processHashMap.keySet();
            keysArray = ArrayUtils.toPrimitive(keys.toArray(new Integer[keys.size()]));
            // getProcesMemoryInfo() needs an int array as parameter.
            processMemArray = activityManager.getProcessMemoryInfo(keysArray);
            calculateCpuUsageOfEachProc();
            // send general usage msg back to MainActivity
            StringBuilder generalInfo = new StringBuilder(String.format("%.2f", generalCpuUsage * 100) + DELIMITER);
            activityManager.getMemoryInfo(generalMem);
            generalInfo.append(formater.format(generalMem.availMem >> 10) + DELIMITER + String.format("%.2f", generalMem.availMem * 100 / (double) generalMem.totalMem));
            sendIntent("general", "cpuMem", generalInfo.toString());
            // send per-process usage to RunningProcesesFragment
            StringBuilder processesInfo = new StringBuilder("");
            if (processHashMap.size() != 0) {
                /* Elements in processHashMap may be removed during CPU usage calculation.
                   However, to get correspondent memory info stored in an array, we cannot use other
                   method of iteration. */
                for (int i = 0; i < keysArray.length; i++) {
                    if (!processHashMap.containsKey(keysArray[i])){
                        // This process no longer exists
                        continue;
                    }
                    processesInfo.append(processHashMap.get(keysArray[i]).getPid() + DELIMITER);
                    processesInfo.append(processHashMap.get(keysArray[i]).getPackageName() + DELIMITER);
                    processesInfo.append(formater.format(processMemArray[i].getTotalPss()) + DELIMITER);
                    processesInfo.append(String.format(Locale.getDefault(), "%.2f", processMemArray[i].getTotalPss() * 100 / (float) totalMem) + DELIMITER);
                    processesInfo.append(String.format(Locale.getDefault(), "%.2f", processHashMap.get(keysArray[i]).cpuUsage * 100) + ";");
//                    Log.d(LOG_TAG, "process pid=" + processHashMap.get(i).getPid() + ", names = " + processHashMap.get(i).getPackageName() + ", memory usage = " + processMemArray[i].getTotalPss() + ", " + String.format("%.2f", processMemArray[i].getTotalPss() * 100 / (double) totalMem) + " %" + ", " + processHashMap.get(i).cpuUsage + "%");
                }
            } else {
                Log.d(LOG_TAG, "processHashMap size = 0 !!!");
            }
            // send result back to RunningProcessFragment
            sendIntent("processes", "processInfo", processesInfo.toString());
            // Sleep for a second to reduce CPU usage of this monitoring app
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendIntent(String action, String extraKey, String extraValue) {
        Intent localIntent = new Intent(action);
        localIntent.putExtra(extraKey, extraValue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Sample the CPU-related file, and then calculate the difference between this time and last time.
     * How to calculate total CPU usage: https://github.com/Leo-G/DevopsWiki/wiki/How-Linux-CPU-Usage-Time-and-Percentage-is-calculated
     */
    private void calculateCpuUsageOfEachProc() {
        String[] fields;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("/proc/stat"));
            fields = reader.readLine().split("[ ]+");
            this.busyTime = Long.parseLong(fields[1]) + Long.parseLong(fields[2]) + Long.parseLong(fields[3]) + Long.parseLong(fields[6]) + Long.parseLong(fields[7]) + Long.parseLong(fields[8]);
            this.totalTime = this.busyTime + Long.parseLong(fields[4]) + Long.parseLong(fields[5]);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        this.samplePeriod = totalTime - totalTimeLast;
        // In case of ConcurrentModificationException, do not use for each loop
        for(int i = 0; i <keysArray.length; i++) {
            try {
                processHashMap.get(keysArray[i]).readCpuUsage();
            } catch (IOException e) {
                // This process no longer exists, may be killed by Android or user,
                // so remove the current process
                processHashMap.remove(keysArray[i]);
                continue;
            }

        }
        // general CPU usage = DiffBusyTime/DiffTotalTime.
        this.generalCpuUsage = (busyTime - busyTimeLast) / (float) this.samplePeriod;
        this.totalTimeLast = this.totalTime;
        this.busyTimeLast = this.busyTime;
    }


    private void getAllPid() {
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory() && file.getName().matches("[0-9]+")) {
                int pid = Integer.parseInt(file.getName());
                if (pid <= NO_PERMISSION_PID) {
                    // we don't have permission to read, go to next iteration
                    continue;
                }
                try {
                    if (!processHashMap.containsKey(pid)) {
                        // This is a new process, create a new process object for it.
                        AppProcess process = new AppProcess(pid);
                        processHashMap.put(pid, process);
                    }
                } catch (IOException e) {
                    // IOException happens will the file corresponding to this process no longer exist
                    continue;
                }
            }
        }
    }


//    /**
//     * Get USER_HZ (clockticks/seconds) to calculate CPU usage
//     * Bad idea: because cannot reflect the instant burst usage of one process
//     */
//    private void getUserHz() {
//        BufferedReader reader;
//        String lineUptime = null;
//        String lineStat = null;
//        try {
//            reader = new BufferedReader(new FileReader("/proc/uptime"));
//            lineUptime = reader.readLine();
//            reader = new BufferedReader(new FileReader("/proc/stat"));
//            lineStat = reader.readLine();
//            reader.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // refer to this to see the fields of each file: http://man7.org/linux/man-pages/man5/proc.5.html
//        String[] fieldsUptime = lineUptime.split("[ ]+");
//        double idleSeconds = Double.parseDouble(fieldsUptime[1]);
//        String[] fieldsStat = lineStat.split("[ ]+", 6);
//        long idleTicks = Long.parseLong(fieldsStat[4]);
//        USER_HZ = (int) (idleTicks / idleSeconds);
//        Log.d(LOG_TAG, "idleTicks = " + idleTicks + ", idleSeconds = " + idleSeconds);
//    }


}
