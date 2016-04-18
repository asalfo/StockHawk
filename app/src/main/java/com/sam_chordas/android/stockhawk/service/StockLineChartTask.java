package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.ChartVal;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asalfo on 31/03/16.
 */
public class StockLineChartTask extends AsyncTask<TaskParams, Void, LineData> {


    public static final String SORT_FIELD = "| sort(field=\"Date\", descending=\"false\")";
    private final OkHttpClient client = new OkHttpClient();
    private final String LOG_TAG = StockLineChartTask.class.getSimpleName();
    private final WeakReference<DetailActivity> mActivity;
    private Utils.Option mOption;


    public StockLineChartTask(DetailActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mActivity.get().showProgress();
    }

    @Override
    protected void onCancelled(LineData lineData) {
        super.onCancelled(lineData);

    }

    @Override
    protected LineData doInBackground(TaskParams... params) {

        ChartVal chartVal;
        LineData data = null;
        if (!isCancelled()) {
            String symbol = params[0].getSymbol();
            mOption = params[0].getOption();

            if (mOption == Utils.Option.ONEDAY) {
                chartVal = buildChartValsFromCursor(mActivity.get(), symbol);
            } else {
                String json = fecthData(symbol, mOption);
                chartVal = buildChartValsJson(json);

            }
            if(null != chartVal) {
                LineDataSet lineDataSet;
                @SuppressWarnings("unchecked") List<Entry> entry = (List<Entry>) chartVal.getyVals();
                lineDataSet = new LineDataSet(entry, "DataSet");
                lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                lineDataSet.setColor(Color.DKGRAY);

                lineDataSet.setFillAlpha(60);
                lineDataSet.setFillColor(Color.CYAN);
                lineDataSet.setDrawFilled(true);
                lineDataSet.setDrawCircleHole(false);
                lineDataSet.setDrawCircles(false);
                lineDataSet.setDrawValues(false);
                lineDataSet.setHighLightColor(Color.rgb(244, 117, 117));

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(lineDataSet);
                data = new LineData((List<String>) chartVal.getxVals(), dataSets);
                data.setValueTextColor(Color.WHITE);
                data.setValueTextSize(9f);
            }
        }

        return data;


    }


    private ChartVal buildChartValsFromCursor(Context context, String symbol) {

        ChartVal<ArrayList<String>, ArrayList<Entry>> chartVal = null;
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();

        Uri uri = QuoteProvider.Quotes.withSymbol(symbol);
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{QuoteColumns.BIDPRICE, QuoteColumns.CREATED},
                QuoteColumns.CREATED + " BETWEEN ? AND ?",
                new String[]{Utils.openDay() + " 00:00:00", Utils.openDay() + " 23:59:59"},
                QuoteColumns.CREATED);

        if (cursor != null ? cursor.moveToFirst() : false) {

            int xIndex = 0;
            do {

                xVals.add(cursor.getString(cursor.getColumnIndex(QuoteColumns.CREATED)));
                Float value = Float.valueOf(cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));
                yVals.add(new Entry(value, xIndex));
                xIndex++;

            } while (cursor.moveToNext() && !isCancelled());

            cursor.close();

            chartVal = new ChartVal<>(xVals, yVals);

        }
        return chartVal;
    }


    private ChartVal buildChartValsJson(String JSON) {
        ChartVal<ArrayList<String>, ArrayList<Entry>> chartVal = null;
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        try {

            if (!isCancelled()) {
                jsonObject = new JSONObject(JSON);

                if (jsonObject.length() != 0) {
                    jsonObject = jsonObject.getJSONObject("query");
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                                jsonObject = resultsArray.getJSONObject(i);
                                xVals.add(jsonObject.getString("Date"));
                            Log.d(LOG_TAG, "Date"+jsonObject.getString("Date"));
                                Float value = Float.valueOf(jsonObject.getString("Close"));
                                yVals.add(new Entry(value, i));
                            if (isCancelled()) {
                                break;
                            }
                        }
                        chartVal = new ChartVal<>(xVals, yVals);
                    }

                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return chartVal;
    }

    private String fecthData(String symbol, Utils.Option option) {

        StringBuilder urlStringBuilder = new StringBuilder();
        String getResponse = null;
        try {
            String starDate = Utils.startDate(option);

            // Base URL for the Yahoo query
            urlStringBuilder.append(Constants.YAHOOAPIS_QUERY);
            urlStringBuilder.append(URLEncoder.encode(Constants.SELECT_HISTORICALDATA
                    + "in (" + "\"" + symbol + "\"" + " )", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("and startDate = \"" + starDate + "\" and endDate = \"" + Utils.openDay() + "\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode(SORT_FIELD, "UTF-8"));
            urlStringBuilder.append(Constants.YQL_FORMAT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!isCancelled()) {
            try {
                String url = urlStringBuilder.toString();

                Log.d(LOG_TAG, "Url" + URLDecoder.decode(url));
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                getResponse = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return getResponse;

    }

    @Override
    protected void onPostExecute(LineData data) {
        if (null != mActivity.get() && data != null) {
            mActivity.get().updateGraph(data,mOption);
        }else{
            mActivity.get().hideProgress();
        }
    }
}
