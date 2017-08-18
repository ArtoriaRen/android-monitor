package ca.uwaterloo.usmmonitor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
    private List<AppStats> mAppStatList = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView mAppLabel;
        private final TextView mPackageName;
        private final TextView mLastTimeUsed;
        private final ImageView mAppIcon;
        private TextView mCpuUsage, mMemUsage;

        public ViewHolder(View v) {
            super(v);
            mAppLabel = (TextView) v.findViewById(R.id.app_label);
            mPackageName = (TextView) v.findViewById(R.id.package_name);
            mLastTimeUsed = (TextView) v.findViewById(R.id.last_time_used);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
            mCpuUsage = (TextView) v.findViewById(R.id.cpu_usage);
            mMemUsage = (TextView) v.findViewById(R.id.mem_usage);
        }

        public TextView getAppLabel() {return mAppLabel;}
        public TextView getPackageName() {return mPackageName;}
        public TextView getLastTimeUsed() {return mLastTimeUsed;}
        public ImageView getAppIcon() {return mAppIcon;}
        public TextView getmCpuUsage() {return mCpuUsage;}
        public TextView getmMemUsage() {return mMemUsage;}

    }
    public ListAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.getAppLabel().setText(mAppStatList.get(position).appLabel + "(pid=" + mAppStatList.get(position).pid + ")");
        holder.getPackageName().setText(mAppStatList.get(position).packageName);
        holder.getmCpuUsage().setText("CPU: " + mAppStatList.get(position).cpuUsage + "%");
        holder.getmMemUsage().setText("Memory: " + mAppStatList.get(position).memUsageKB + "KB, " + mAppStatList.get(position).memUsagePercent + "%");
//        long lastTimeUsed = mAppStatList.get(position).usageStats.getLastTimeUsed();
//        holder.getLastTimeUsed().setText(mDataFormat.format(new Date(lastTimeUsed)));
        holder.getAppIcon().setImageDrawable(mAppStatList.get(position).appIcon);
    }

    @Override
    public int getItemCount() {
        return mAppStatList.size();
    }

    public void setAppStatList(List<AppStats> appStatsList){
        mAppStatList = appStatsList;
    }
}
