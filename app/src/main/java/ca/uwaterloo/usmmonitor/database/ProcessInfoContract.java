package ca.uwaterloo.usmmonitor.database;

import android.provider.BaseColumns;

/**
 * Created by liuyangren on 2017-09-01.
 */

public class ProcessInfoContract {
    private ProcessInfoContract() {

    }

    public static final class ProcessInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "processInfo";
        public static final String COLUME_PACKAGE_NAME = "package";
        public static final String COLUME_CPU_USAGE = "cpuUsage";
        public static final String COLUME_MEMORY_USAGE_KB= "memoryUsageKb";
        public static final String COLUME_MEMORY_USAGE_PERCENT= "memoryUsagePercent";
//        public static final String COLUME_TOTAL_CPU_USAGE = "totalCpuUsage";
//        public static final String COLUME_TOTAL_MEMORY_USAGE = "totalMemoryUsage";
        public static final String COLUME_TIMESTAMP = "timestamp";
    }

}
