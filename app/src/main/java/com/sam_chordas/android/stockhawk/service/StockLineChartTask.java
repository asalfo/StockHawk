package com.sam_chordas.android.stockhawk.service;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.db.chart.model.LineSet;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by asalfo on 31/03/16.
 */
public class StockLineChartTask extends AsyncTask<String, Void, LineSet> {

    private String LOG_TAG = StockLineChartTask.class.getSimpleName();
    WeakReference<DetailActivity> mActivity;


    public StockLineChartTask(DetailActivity activity) {
        mActivity = new WeakReference<>(activity);
    }


    @Override
    protected LineSet doInBackground(String... params) {

        String symbol =params[0];
        LineSet lineSet = new LineSet();
        Map<Integer, String> data = new TreeMap<Integer, String>();

        Uri uri = QuoteProvider.Quotes.withSymbol(symbol);
        Cursor stockCursor = mActivity.get().getContentResolver().query(
                uri,
                new String[]{QuoteColumns.BIDPRICE,QuoteColumns.CREATED},
                QuoteColumns.CREATED + " BETWEEN ? AND ?",
                new String[]{Utils.openDay()+" 00:00:00",Utils.openDay()+" 23:59:59"},
                QuoteColumns.CREATED);

        if (stockCursor.moveToFirst()) {

            Float currentValue = 0.0f;
            do {

                try {
                    int label = Integer.valueOf(Utils.formatDate(stockCursor.getString(stockCursor.getColumnIndex("created")), "H"));
                    Float value =  Float.valueOf(stockCursor.getString(stockCursor.getColumnIndex("bid_price")));
                    if( (Math.abs(value - currentValue) > 0.0f)) {
                        currentValue = value;
                        data.put(label, String.valueOf(value));
                       // Log.d(LOG_TAG, label + "=>" +value+" @@@");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } while(stockCursor.moveToNext());

        }
        stockCursor.close();
        for (Map.Entry<Integer,String> entry : data.entrySet()) {
            Log.d(LOG_TAG, entry.getKey() + "=>" +Float.valueOf(entry.getValue())+" @@@");
            lineSet.addPoint(String.valueOf(entry.getKey()),Float.valueOf(entry.getValue()));
        }
        return lineSet;
    }

    @Override
    protected void onPostExecute(LineSet lineSet) {
        if(null != mActivity.get() && lineSet.size() != 0) {
            mActivity.get().updateGraph(lineSet);
        }
    }
}
