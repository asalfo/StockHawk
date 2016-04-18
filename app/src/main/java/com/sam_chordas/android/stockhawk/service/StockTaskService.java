package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {



    private final String LOG_TAG = StockTaskService.class.getSimpleName();

    private final OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private final StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    // Defines and instantiates an object for handling status updates.
    private BroadcastNotifier mBroadcaster;

  public StockTaskService(){}

    public StockTaskService(Context context) {
        mContext = context;
        mBroadcaster = new BroadcastNotifier(mContext);
    }

    private String fetchData(String url) throws IOException {
        Log.d(LOG_TAG, URLDecoder.decode(url));
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append(Constants.YAHOOAPIS_QUERY);
            urlStringBuilder.append(URLEncoder.encode(Constants.SELECT_STATEMENT
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (params.getTag().equals(Constants.INIT) || params.getTag().equals(Constants.PERIODIC_TAG)) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);
            if ((initQueryCursor != null ? initQueryCursor.getCount() : 0) == 0) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode(Constants.DEFAULT_QUOTE, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\"").append(initQueryCursor.getString(initQueryCursor.getColumnIndex(QuoteColumns.SYMBOL))).append("\",");
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else if (params.getTag().equals(Constants.ADD)) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString(QuoteColumns.SYMBOL);
            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append(Constants.YQL_FORMAT);

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        urlString = urlStringBuilder.toString();
        try {
            getResponse = fetchData(urlString);
            Log.d(LOG_TAG, "Response = " + getResponse);
            result = GcmNetworkManager.RESULT_SUCCESS;
            try {
                ContentValues contentValues = new ContentValues();
                // update ISCURRENT to 0 (false) so new data is current
                if (isUpdate) {
                    contentValues.put(QuoteColumns.ISCURRENT, 0);
                    mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                            null, null);
                }
                try {
                    ArrayList batchOperations;
                    batchOperations = Utils.quoteJsonToContentVals(getResponse);
                    //noinspection unchecked
                   ContentProviderResult[] contentProviderResult = mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, batchOperations);
                    if(contentProviderResult.length > 0){
                        Intent dataUpdatedIntent = new Intent(Constants.ACTION_DATA_UPDATED)
                                .setPackage(mContext.getPackageName());
                        mContext.sendBroadcast(dataUpdatedIntent);
                    }

                } catch (NumberFormatException e) {
                    mBroadcaster.broadcastIntentState(Constants.STATE_ERROR);
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }

            } catch (RemoteException | OperationApplicationException e) {
                mBroadcaster.broadcastIntentState(Constants.STATE_ERROR);
                Log.e(LOG_TAG, "Updating quote value", e);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error Fecthing data", e);
            e.printStackTrace();
            mBroadcaster.broadcastIntentState(Constants.STATE_ERROR);
        }

        return result;
    }

}
