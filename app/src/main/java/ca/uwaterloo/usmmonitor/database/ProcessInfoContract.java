package ca.uwaterloo.usmmonitor.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by liuyangren on 2017-09-01.
 */

public class ProcessInfoContract {
    // content provider information
    public static final String CONTENT_AUTHORITY = "ca.uwaterloo.usmmonitor.provider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_ENTRIES = "processes";

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

        // ContentProvider information for table "processInfo"
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_URI ;
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.ANY_CURSOR_ITEM_TYPE + CONTENT_URI + "/item" ;
    }

}
