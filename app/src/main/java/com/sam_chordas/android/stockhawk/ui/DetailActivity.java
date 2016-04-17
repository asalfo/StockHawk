package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.MyXAxisValueFormatter;
import com.sam_chordas.android.stockhawk.rest.MyYAxisValueFormatter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockLineChartTask;
import com.sam_chordas.android.stockhawk.service.TaskParams;

import java.text.ParseException;

/**
 * Created by asalfo on 24/03/16.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String SYMBOL_KEY = "symbol";
    private static final int CURSOR_LOADER_ID = 0;
    private ProgressBar mLoading;
    private LineChart mLineChartView;
    private Context mContext;
    private TextView mName;
    private String mSymbol;
    private TextView mLastUpdate;
    private TextView mBidPrice;
    private TextView mCurrPercentChange;
    private TextView mPercentChange;
    private TextView mChange;
    private TextView mVolume;
    private TextView mOpen;
    private TextView mPreviousClose;
    private TextView mDaysLow;
    private TextView mDaysHigh;
    private TextView mYearLow;
    private TextView mYearHigh;
    private Uri mUri;

    private StockLineChartTask chartTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mUri = getIntent().getData();
        mLastUpdate = (TextView) findViewById(R.id.date);
        mBidPrice = (TextView) findViewById(R.id.bid_price);
        mChange = (TextView) findViewById(R.id.change);
        mCurrPercentChange = (TextView) findViewById(R.id.cur_per_change);
        mPercentChange = (TextView) findViewById(R.id.percent_change);
        mVolume = (TextView) findViewById(R.id.volume);
        mOpen = (TextView) findViewById(R.id.open);
        mPreviousClose = (TextView) findViewById(R.id.previousClose);
        mDaysLow = (TextView) findViewById(R.id.daysLow);
        mDaysHigh = (TextView) findViewById(R.id.daysHigh);
        mYearLow = (TextView) findViewById(R.id.yearLow);
        mYearHigh = (TextView) findViewById(R.id.yearHigh);
        mLineChartView = (LineChart) findViewById(R.id.linechart);
        mLoading = (ProgressBar) findViewById(R.id.loading);

        chartTask = new StockLineChartTask(this);

        Button day = (Button) findViewById(R.id.one_day);

        if (day != null) {
            day.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           drawChart(mSymbol, Utils.Option.ONEDAY);
                                       }

                                   }
            );
        }

        Button fiveDays = (Button) findViewById(R.id.five_days);
        if (fiveDays != null) {
            fiveDays.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                drawChart(mSymbol, Utils.Option.FIVEDAYS);
                                            }
                                        }
            );
        }

        Button threeMonth = (Button) findViewById(R.id.three_months);
        if (threeMonth != null) {
            threeMonth.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  drawChart(mSymbol, Utils.Option.THREEMONTHS);
                                              }
                                          }
            );
        }


        Button sixMonth = (Button) findViewById(R.id.six_months);
        if (sixMonth != null) {
            sixMonth.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                drawChart(mSymbol, Utils.Option.SIXMONTHS);
                                            }
                                        }
            );
        }


        Button oneYear = (Button) findViewById(R.id.year);
        if (oneYear != null) {
            oneYear.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               drawChart(mSymbol, Utils.Option.YEAR);
                                           }
                                       }
            );
        }


        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);


    }


    private void drawChart(String symbol, Utils.Option option) {
        if (chartTask.getStatus() == AsyncTask.Status.RUNNING || chartTask.getStatus() == AsyncTask.Status.PENDING) {
            chartTask.cancel(true);
            chartTask = new StockLineChartTask(this);
        } else {
            chartTask = new StockLineChartTask(this);
        }

        chartTask.execute(new TaskParams(symbol, option));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            String itemId = mUri.getPathSegments().get(1);
            Log.d(LOG_TAG, mUri.toString());

            return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.NAME, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.DAYS_LOW,
                            QuoteColumns.DAYS_HIGH, QuoteColumns.YEAR_LOW, QuoteColumns.YEAR_HIGH, QuoteColumns.VOLUME,
                            QuoteColumns.PREVIOUS_CLOSE, QuoteColumns.OPEN, QuoteColumns.CREATED},
                    QuoteColumns._ID + " = ? ",
                    new String[]{itemId},
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "" + data.getCount());
        if (data.moveToFirst()) {
            mSymbol = data.getString(data.getColumnIndex("symbol"));
            int color;
            try {
                mLastUpdate.setText(Utils.formatDate(data.getString(data.getColumnIndex("created")), "MMM dd, h:m a z"));
            } catch (ParseException ex) {
                Log.d(LOG_TAG, ex.getMessage());
            }
            mChange.setText(data.getString(data.getColumnIndex("change")));
            mBidPrice.setText(data.getString(data.getColumnIndex("bid_price")));
            mCurrPercentChange.setText(data.getString(data.getColumnIndex("percent_change")));
            mPercentChange.setText(Utils.formatText(this, R.string.percent_change, data.getString(data.getColumnIndex("percent_change"))))
            ;

            mVolume.setText(Utils.formatText(this, R.string.volume, data.getString(data.getColumnIndex("volume"))));
            mOpen.setText(Utils.formatText(this, R.string.open, data.getString(data.getColumnIndex("open"))));
            mPreviousClose.setText(Utils.formatText(this, R.string.last, data.getString(data.getColumnIndex("previous_close"))));
            Float low = data.getFloat(data.getColumnIndex("days_low"));
            Float high = data.getFloat(data.getColumnIndex("days_high"));
            mDaysLow.setText(Utils.formatText(this, R.string.days_low, String.valueOf(low)));
            mDaysHigh.setText(Utils.formatText(this, R.string.days_high, String.valueOf(high)));
            mYearLow.setText(Utils.formatText(this, R.string.year_low, data.getString(data.getColumnIndex("year_low"))));
            mYearHigh.setText(Utils.formatText(this, R.string.year_high, data.getString(data.getColumnIndex("year_high"))));

            if (data.getInt(data.getColumnIndex("is_up")) == 1) {
                color = ContextCompat.getColor(this, R.color.material_green_700);
            } else {
                color = ContextCompat.getColor(this, R.color.material_red_700);
            }
            mChange.setTextColor(color);
            mCurrPercentChange.setTextColor(color);
            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(data.getString(data.getColumnIndex("symbol")).toUpperCase());
                actionBar.setSubtitle(data.getString(data.getColumnIndex("name")));
            }


        }

        drawChart(mSymbol, Utils.Option.ONEDAY);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateGraph(LineData data, Utils.Option option) {


        mLineChartView.setDescription("");
        mLineChartView.setNoDataTextDescription("You need to provide data for the chart.");
        mLineChartView.setData(data);
        mLineChartView.getAxisLeft().setEnabled(false);
        mLineChartView.setDragEnabled(true);
        mLineChartView.setScaleEnabled(true);
        mLineChartView.setDrawGridBackground(false);
        mLineChartView.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChartView.setPinchZoom(true);

        // set an alternative background color
        //mLineChartView.setBackgroundColor(Color.LTGRAY);


        mLineChartView.animateX(2500);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        mLineChartView.getLegend().setEnabled(false);

        XAxis xAxis = mLineChartView.getXAxis();
        xAxis.setTypeface(tf);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(new MyXAxisValueFormatter(option));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = mLineChartView.getAxisRight();
        yAxis.setTextColor(Color.WHITE);
        yAxis.setLabelCount(5, true);
        yAxis.setValueFormatter(new MyYAxisValueFormatter());
        yAxis.setGranularity(1.5f);
        yAxis.setGranularityEnabled(true);

        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);


        mLineChartView.invalidate();

        mLoading.setVisibility(View.GONE);
        mLineChartView.setVisibility(View.VISIBLE);


    }


    public void showProgress() {
        mLoading.setVisibility(View.VISIBLE);
        mLineChartView.setData(new LineData());
        mLineChartView.invalidate();
    }

    public void hideProgress() {
        mLoading.setVisibility(View.GONE);
        mLineChartView.setVisibility(View.GONE);
    }
}
