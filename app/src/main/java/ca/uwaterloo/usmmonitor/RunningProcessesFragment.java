package ca.uwaterloo.usmmonitor;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.uwaterloo.usmmonitor.synchronization.SyncUtils;

/**
 * Fragment that use UsageStatsManager API to get current running processes.
 */
public class RunningProcessesFragment extends Fragment {
    private static final String LOG_TAG = RunningProcessesFragment.class.getSimpleName();

    private UsageStatsManager mUsageStatsManager;
    private ListAdapter mListAdapter;
    private RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    private List<AppStats> mAppStatsList;
    private PackageManager mPackageManager;
    /**
     * Options menu used to populate ActionBar.
     */
    private Menu mOptionsMenu;



    /**
     * handler for received Intents. This will be called whenever an Intent
     * with an action named "custom-event-name" is broadcasted.
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //clear the former process info stored in the list
            if (!mAppStatsList.isEmpty()) {
                mAppStatsList.clear();
            }
            String msg = intent.getStringExtra("processInfo");
//            Log.d(LOG_TAG, "Received msg = " + msg);
            if (msg != null) {
                String[] infoArray = msg.split(";");
                for (String s : infoArray) {
                    parseInfo(s);
                }
            }
            Collections.sort(mAppStatsList, new LastTimeUsedComparator());
            mListAdapter.setAppStatList(mAppStatsList);
            mListAdapter.notifyDataSetChanged();
//            mRecyclerView.scrollToPosition(0);

        }
    };

    private void parseInfo(String processInfo) {
        // fields = [pid], [packageName], [mem usage in KB], [mem usage in %], [cpu usage]
        String[] fields = processInfo.split(ProcFolderParser.DELIMITER);
        AppStats appStats = new AppStats();
        appStats.pid = fields[0];
        appStats.packageName = fields[1];
        appStats.memUsageKB = fields[2];
        appStats.memUsagePercent = fields[3];
        appStats.cpuUsage = fields[4];
        try {
            appStats.appIcon = mPackageManager.getApplicationIcon(appStats.packageName);
            appStats.appLabel = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(appStats.packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
//            Log.d(LOG_TAG, String.format("App Icon is not found for %s, use default icon.", appStats.packageName));
            appStats.appIcon = getActivity().getDrawable(R.mipmap.ic_default_app_launcher);
        }
        mAppStatsList.add(appStats);
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link RunningProcessesFragment}
     */
    public static RunningProcessesFragment newInstance() {
        return new RunningProcessesFragment();
    }

    public RunningProcessesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // add a refresh menu
        setHasOptionsMenu(true);
        mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
        mAppStatsList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_running_process, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListAdapter = new ListAdapter();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mListAdapter);
        mPackageManager = getContext().getPackageManager();

//        List<UsageStats> usageStatsList = getAppStatistics();
//        Collections.sort(usageStatsList, new LastTimeUsedComparator());
//        updateAppsList(usageStatsList);
    }

    @Override
    public void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter("processes"));
        super.onResume();
    }

    @Override
    public void onPause() {
        // Unregister since the fragment is paused.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        super.onPause();
    }

//    /**
//     * @return A list of {@link android.app.usage.UsageStats}.
//     */
//    public List<UsageStats> getAppStatistics() {
//        // Want to get the app statistics since one minute ago from the current time,
//        // but queryUsageStats() will return all apps used today.
//        Calendar calStart = Calendar.getInstance();
//        calStart.add(Calendar.MINUTE, -1);
//
//        List<UsageStats> queryUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calStart.getTimeInMillis(), System.currentTimeMillis());
//        if (queryUsageStats.size() == 0) {
//            Log.i(LOG_TAG, "The user may not allow the access to apps usage.");
//            Toast.makeText(getActivity(), getString(R.string.explaination_permission_to_grant), Toast.LENGTH_LONG).show();
//            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//        }
//        return queryUsageStats;
//    }

    /**
     * Create the ActionBar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    /**
     * Respond to user gestures on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                Log.i(LOG_TAG, "user clicked the refresh button.");
                SyncUtils.TriggerRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occuring, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }



    /**
     * Updates the {@link #mRecyclerView} with the list of {@link UsageStats} passed as an argument.
     *
     * @param usageStatsList A list of {@link UsageStats} from which update the
     *                       {@link #mRecyclerView}.
     */
    public void updateAppsList(List<UsageStats> usageStatsList) {
        mAppStatsList = new ArrayList<>();
        PackageManager packageManager = getContext().getPackageManager();
        for (int i = 0; i < usageStatsList.size(); i++) {
            AppStats appStats = new AppStats();
            appStats.usageStats = usageStatsList.get(i);
            if (appStats.usageStats.getLastTimeUsed() == 0) continue;
            try {
                appStats.appIcon = packageManager.getApplicationIcon(appStats.usageStats.getPackageName());
                appStats.appLabel = packageManager.getApplicationLabel(packageManager.getApplicationInfo(appStats.usageStats.getPackageName(), 0)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(LOG_TAG, String.format("App Icon is not found for %s, use default icon.", appStats.usageStats.getPackageName()));
                appStats.appIcon = getActivity().getDrawable(R.mipmap.ic_default_app_launcher);
            }
//            Log.d(LOG_TAG, appStats.usageStats.getPackageName() + ": \n LastTimeStamp =" + new Date(appStats.usageStats.getLastTimeStamp()) + "\n LastTimeUsed = " + new Date(appStats.usageStats.getLastTimeUsed()) + "\n FirstTimeStamp = " + new Date(appStats.usageStats.getFirstTimeStamp()));
            mAppStatsList.add(appStats);


        }

        mListAdapter.setAppStatList(mAppStatsList);
        mListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }


    /**
     * The {@link Comparator} to sort a collection of {@link UsageStats} sorted by the timestamp
     * last time the app was used in the descendant order.
     */
    private static class LastTimeUsedComparator implements Comparator<AppStats> {
        @Override
        public int compare(AppStats o1, AppStats o2) {
            return Float.compare(Float.parseFloat(o2.cpuUsage), Float.parseFloat(o1.cpuUsage));
        }
    }
}
