package ca.uwaterloo.usmmonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ca.uwaterloo.usmmonitor.database.ProcessInfoContract.ProcessInfoEntry;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by liuyangren on 2017-09-01.
 */

public class ProcessInfoDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "killedProcesses.db";
    private static final int DATABASE_VERSION = 1;

    public ProcessInfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // string to create table
        final String SQL_CREATE_KILLED_PROCESSES_TABLE = "CREATE TABLE " + ProcessInfoEntry.TABLE_NAME
                + " (" + ProcessInfoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProcessInfoEntry.COLUME_PACKAGE_NAME + " TEXT NOT NULL, "
                + ProcessInfoEntry.COLUME_CPU_USAGE + " REAL NOT NULL, "
                + ProcessInfoEntry.COLUME_MEMORY_USAGE_KB + " INTEGER NOT NULL, "
                // Don't add a "," at the end of SQL query as this is the last attribute.
                + ProcessInfoEntry.COLUME_MEMORY_USAGE_PERCENT + " REAL NOT NULL"
//                + ProcessInfoEntry.COLUME_TOTAL_CPU_USAGE + " INTEGER NOT NULL, "
//                + ProcessInfoEntry.COLUME_TOTAL_MEMORY_USAGE + " INTEGER NOT NULL,"
                + " );";

        db.execSQL(SQL_CREATE_KILLED_PROCESSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2){
            final String SQL_ALTER_ADD_TIMESTAMP_COLUMN
                    = "ALTER TABLE " + ProcessInfoEntry.TABLE_NAME
                    + " ADD COLUMN " + ProcessInfoEntry.COLUME_TIMESTAMP
                    + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP;";
        }
    }
}
