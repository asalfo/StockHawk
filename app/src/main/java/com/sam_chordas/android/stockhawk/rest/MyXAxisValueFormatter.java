package com.sam_chordas.android.stockhawk.rest;

import android.util.Log;

import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

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
        String inFormat = "yyyy-MM-dd";
        String xValue = null;
        switch (mOption){
            case ONEDAY:
                format = "H:mm";
                inFormat ="yyyy-MM-dd HH:mm:ss";
                break;
            case FIVEDAYS:
                format = "MMM dd";
                break;
            default:
                format = "yyyy-MM";
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
