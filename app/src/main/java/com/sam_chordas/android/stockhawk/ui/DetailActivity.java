package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockLineChartTask;

import java.text.ParseException;

/**
 * Created by asalfo on 24/03/16.
 */
public class DetailActivity  extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String SYMBOL_KEY = "symbol";
    private static final int CURSOR_LOADER_ID = 0;
    private LineChartView mLineChartView;
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
    private Uri mUri ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

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
        mLineChartView = (LineChartView) findViewById(R.id.linechart);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);


    }






    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
           String itemId = mUri.getPathSegments().get(1);
            Log.d(LOG_TAG,mUri.toString());

            return new CursorLoader(this,QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.NAME, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.DAYS_LOW,
                            QuoteColumns.DAYS_HIGH, QuoteColumns.YEAR_LOW, QuoteColumns.YEAR_HIGH, QuoteColumns.VOLUME,
                            QuoteColumns.PREVIOUS_CLOSE,QuoteColumns.OPEN,QuoteColumns.CREATED},
                    QuoteColumns._ID + " = ? ",
                    new String[]{itemId},
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      Log.d(LOG_TAG, "" + data.getCount());
        if (data != null && data.moveToFirst()) {
            mSymbol = data.getString(data.getColumnIndex("symbol"));
           int color;
            try {
                mLastUpdate.setText(Utils.formatDate(data.getString(data.getColumnIndex("created")), "MMM dd, h:m a z"));
            } catch (ParseException ex){
                Log.d(LOG_TAG,ex.getMessage());
            }
            mChange.setText(data.getString(data.getColumnIndex("change")));
            mBidPrice.setText(data.getString(data.getColumnIndex("bid_price")));
            mCurrPercentChange.setText(data.getString(data.getColumnIndex("percent_change")));
            mPercentChange.setText(Utils.formatText(this, R.string.percent_change, data.getString(data.getColumnIndex("percent_change"))))
            ;

            mVolume.setText(Utils.formatText(this, R.string.volume, data.getString(data.getColumnIndex("volume"))));
            mOpen.setText(Utils.formatText(this, R.string.open, data.getString(data.getColumnIndex("open"))));
            mPreviousClose.setText(Utils.formatText(this,R.string.last, data.getString(data.getColumnIndex("previous_close"))));
            Float low = data.getFloat(data.getColumnIndex("days_low"));
            Float high = data.getFloat(data.getColumnIndex("days_high"));
            mDaysLow.setText(Utils.formatText(this, R.string.days_low, String.valueOf(low)));
            mDaysHigh.setText(Utils.formatText(this, R.string.days_high, String.valueOf(high)));
            mYearLow.setText(Utils.formatText(this, R.string.year_low, data.getString(data.getColumnIndex("year_low"))));
            mYearHigh.setText(Utils.formatText(this, R.string.year_high, data.getString(data.getColumnIndex("year_high"))));

            if (data.getInt(data.getColumnIndex("is_up")) == 1){
                color = ContextCompat.getColor(this, R.color.material_green_700);
            }else{
                color = ContextCompat.getColor(this, R.color.material_red_700);
            }
            mChange.setTextColor(color);
            mCurrPercentChange.setTextColor(color);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(data.getString(data.getColumnIndex("symbol")));
            actionBar.setSubtitle(data.getString(data.getColumnIndex("name")));




            mLineChartView.setAxisBorderValues(low.intValue(), Math.round(high), Utils.divisor(Math.round(high) - low.intValue()));
        }

        StockLineChartTask chartTask = new StockLineChartTask(this);
        chartTask.execute(mSymbol);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateGraph (LineSet lineSet){

        Log.d(LOG_TAG, lineSet.toString());
        lineSet.setSmooth(true);
        lineSet.setFill(getColor(R.color.material_green_700));
        mLineChartView.addData(lineSet);
        mLineChartView.setGrid(ChartView.GridType.FULL, new Paint(Paint.FILTER_BITMAP_FLAG));
        mLineChartView.show();
    }
}
