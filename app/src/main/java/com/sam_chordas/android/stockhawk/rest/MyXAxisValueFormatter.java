package com.sam_chordas.android.stockhawk.rest;

import android.util.Log;

import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sam_chordas.android.stockhawk.service.Constants;

import java.text.ParseException;

/**
 * Created by asalfo on 16/04/16.
 */
public class MyXAxisValueFormatter implements XAxisValueFormatter {

    private final Utils.Option mOption;
    public MyXAxisValueFormatter(Utils.Option option) {
        mOption = option;
    }

    @Override
    public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
        String format;
        String inFormat = Utils.DATE_DEFAULT_FORMAT;
        String xValue = null;
        switch (mOption){
            case ONEDAY:
                format = Utils.HOUR_FORMAT;
                inFormat =Utils.DATE_HOUR_FORMAT;
                break;
            case FIVEDAYS:
                format = Utils.CHART_LABEL_DATE_FORMAT;
                break;
            default:
                format = Utils.YEAR_MONTH_FORMAT;
        }

        try {
            xValue = Utils.formatDate(original,inFormat,format);
            Log.d("SALIF",xValue +" "+original);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return xValue;
    }
}
