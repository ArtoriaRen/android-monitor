package ca.uwaterloo.usmmonitor;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import ca.uwaterloo.usmmonitor.database.ProcessInfoContract;
import ca.uwaterloo.usmmonitor.database.ProcessInfoDbHelper;

/**
 * Created by liuyangren on 11/9/17.
 */

public class Provider extends ContentProvider {
    ProcessInfoDbHelper mDbHelper;
    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using uriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through uriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_ENTRIES = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_ENTRIES_ID = 2;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(ProcessInfoContract.CONTENT_AUTHORITY, "entries", ROUTE_ENTRIES);
        uriMatcher.addURI(ProcessInfoContract.CONTENT_AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
    }

    /**
         * Always return true, indicating that the
         * provider loaded correctly.
         */
    @Override
    public boolean onCreate() {
        mDbHelper = new ProcessInfoDbHelper(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return ProcessInfoContract.ProcessInfoEntry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return ProcessInfoContract.ProcessInfoEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c;
        switch (uriMatcher.match(uri)) {
            // Query for all entries
            case ROUTE_ENTRIES:
                c = db.query(ProcessInfoContract.ProcessInfoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            // Query for single entry
            case ROUTE_ENTRIES_ID:
                long _id = ContentUris.parseId(uri);
                c = db.query(ProcessInfoContract.ProcessInfoEntry.TABLE_NAME,
                        projection,
                        ProcessInfoContract.ProcessInfoEntry._ID + "=?",
                        new String[] { String.valueOf(_id) },
                        null,
                        null,
                        sortOrder);
                break;
            default: throw new IllegalArgumentException("Invalid URI!");
        }
        return c;
    }
    /**
     * insert() always returns null (no URI)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assert db != null;
        Uri result;
        switch (uriMatcher.match(uri)) {
            case ROUTE_ENTRIES:
                long id = db.insertOrThrow(ProcessInfoContract.ProcessInfoEntry.TABLE_NAME, null, values);
                result = Uri.parse(ProcessInfoContract.ProcessInfoEntry.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return result;
    }
    /**
     * delete() always returns "no rows affected" (0)
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ROUTE_ENTRIES:
                return db.delete(ProcessInfoContract.ProcessInfoEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                        );
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    /**
     * update() always returns "no rows affected" (0)
     */
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rows;
        switch (uriMatcher.match(uri)) {
            case ROUTE_ENTRIES:
                rows = db.update(ProcessInfoContract.ProcessInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default: throw new IllegalArgumentException("Invalid URI!");
        }
        return rows;
    }
}
