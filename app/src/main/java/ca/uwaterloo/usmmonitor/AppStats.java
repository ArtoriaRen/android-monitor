package ca.uwaterloo.usmmonitor;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

/**
 * Statistics info about a process.
 */

public class AppStats {
    public UsageStats usageStats;
    public Drawable appIcon;
    public String appLabel;
    public String cpuUsage;
    public String memUsageKB;
    public String memUsagePercent;
    public String pid;
    public String packageName;
}
