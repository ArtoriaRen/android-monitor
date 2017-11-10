package ca.uwaterloo.usmmonitor.synchronization;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ca.uwaterloo.usmmonitor.database.ProcessInfoContract;

/**
 * Created by liuyangren on 11/8/17.
 * reference : @see  https://github.com/googlesamples/android-BasicSyncAdapter
 */

public class MonitorSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = MonitorSyncAdapter.class.getSimpleName();

    /**
     * URL to fetch content from during a sync.
     *
     * <p>This points to the Android Developers Blog. (Side note: We highly recommend reading the
     * Android Developer Blog to stay up to date on the latest Android platform developments!)
     */
    private static final String FEED_URL = "http://android-developers.blogspot.com/atom.xml";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[]{
            ProcessInfoContract.ProcessInfoEntry.COLUME_PACKAGE_NAME
    };

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_PACKAGE_NAME = 0;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public MonitorSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        Log.d(LOG_TAG, "Sync adapter is constructed!!!");
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MonitorSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link android.content.AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(LOG_TAG, "Beginning network synchronization");
        try {
            final URL location = new URL(FEED_URL);
            InputStream stream = null;

            try {
                Log.i(LOG_TAG, "Streaming data from network: " + location);
                downloadUrl(location);
//                updateLocalFeedData(stream, syncResult);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
//        } catch (XmlPullParserException e) {
//            Log.e(LOG_TAG, "Error parsing feed: " + e.toString());
//            syncResult.stats.numParseExceptions++;
//            return;
//        } catch (ParseException e) {
//            Log.e(LOG_TAG, "Error parsing feed: " + e.toString());
//            syncResult.stats.numParseExceptions++;
//            return;
//        } catch (RemoteException e) {
//            Log.e(LOG_TAG, "Error updating database: " + e.toString());
//            syncResult.databaseError = true;
//            return;
//        } catch (OperationApplicationException e) {
//            Log.e(LOG_TAG, "Error updating database: " + e.toString());
//            syncResult.databaseError = true;
//            return;
        }
        Log.i(LOG_TAG, "Network synchronization complete");
    }

    /**
     * Read XML from an input stream, storing it into the content provider.
     *
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */
//    public void updateLocalFeedData(final InputStream stream, final SyncResult syncResult)
//            throws IOException, XmlPullParserException, RemoteException,
//            OperationApplicationException, ParseException {
////        final FeedParser feedParser = new FeedParser();
//        final ContentResolver contentResolver = getContext().getContentResolver();
//
//        Log.i(LOG_TAG, "Parsing stream as Atom feed");
//        final List<FeedParser.Entry> entries = feedParser.parse(stream);
//        Log.i(LOG_TAG, "Parsing complete. Found " + entries.size() + " entries");
//
//
//        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
//
//        // Build hash table of incoming entries
//        HashMap<String, FeedParser.Entry> entryMap = new HashMap<String, FeedParser.Entry>();
//        for (FeedParser.Entry e : entries) {
//            entryMap.put(e.id, e);
//        }
//
//        // Get list of all items
//        Log.i(LOG_TAG, "Fetching local entries for merge");
//        Uri uri = FeedContract.Entry.CONTENT_URI; // Get all entries
//        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
//        assert c != null;
//        Log.i(LOG_TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");
//
//        // Find stale data
//        int id;
//        String entryId;
//        String title;
//        String link;
//        long published;
//        while (c.moveToNext()) {
//            syncResult.stats.numEntries++;
//            id = c.getInt(COLUMN_ID);
//            entryId = c.getString(COLUMN_ENTRY_ID);
//            title = c.getString(COLUMN_TITLE);
//            link = c.getString(COLUMN_LINK);
//            published = c.getLong(COLUMN_PUBLISHED);
//            FeedParser.Entry match = entryMap.get(entryId);
//            if (match != null) {
//                // Entry exists. Remove from entry map to prevent insert later.
//                entryMap.remove(entryId);
//                // Check to see if the entry needs to be updated
//                Uri existingUri = FeedContract.Entry.CONTENT_URI.buildUpon()
//                        .appendPath(Integer.toString(id)).build();
//                if ((match.title != null && !match.title.equals(title)) ||
//                        (match.link != null && !match.link.equals(link)) ||
//                        (match.published != published)) {
//                    // Update existing record
//                    Log.i(LOG_TAG, "Scheduling update: " + existingUri);
//                    batch.add(ContentProviderOperation.newUpdate(existingUri)
//                            .withValue(FeedContract.Entry.COLUMN_NAME_TITLE, match.title)
//                            .withValue(FeedContract.Entry.COLUMN_NAME_LINK, match.link)
//                            .withValue(FeedContract.Entry.COLUMN_NAME_PUBLISHED, match.published)
//                            .build());
//                    syncResult.stats.numUpdates++;
//                } else {
//                    Log.i(LOG_TAG, "No action: " + existingUri);
//                }
//            } else {
//                // Entry doesn't exist. Remove it from the database.
//                Uri deleteUri = FeedContract.Entry.CONTENT_URI.buildUpon()
//                        .appendPath(Integer.toString(id)).build();
//                Log.i(LOG_TAG, "Scheduling delete: " + deleteUri);
//                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
//                syncResult.stats.numDeletes++;
//            }
//        }
//        c.close();
//
//        // Add new items
//        for (FeedParser.Entry e : entryMap.values()) {
//            Log.i(LOG_TAG, "Scheduling insert: entry_id=" + e.id);
//            batch.add(ContentProviderOperation.newInsert(FeedContract.Entry.CONTENT_URI)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_ENTRY_ID, e.id)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_TITLE, e.title)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_LINK, e.link)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_PUBLISHED, e.published)
//                    .build());
//            syncResult.stats.numInserts++;
//        }
//        Log.i(LOG_TAG, "Merge solution ready. Applying batch update");
//        mContentResolver.applyBatch(FeedContract.CONTENT_AUTHORITY, batch);
//        mContentResolver.notifyChange(
//                FeedContract.Entry.CONTENT_URI, // URI where data was modified
//                null,                           // No local observer
//                false);                         // IMPORTANT: Do not sync to network
//        // This sample doesn't support uploads, but if *your* code does, make sure you set
//        // syncToNetwork=false in the line above to prevent duplicate syncs.
//    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     */
    private void downloadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        try {
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            byte[] contents = new byte[1024];

            int bytesRead = 0;
            StringBuilder strFileContents = new StringBuilder();
            while((bytesRead = in.read(contents)) != -1) {
                strFileContents.append(new String(contents, 0, bytesRead));
            }
            System.out.print(strFileContents);
    } finally {
        conn.disconnect();
    }
    }
}

