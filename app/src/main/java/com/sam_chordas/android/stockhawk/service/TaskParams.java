package com.sam_chordas.android.stockhawk.service;

import com.sam_chordas.android.stockhawk.rest.Utils.Option;

/**
 * Created by asalfo on 16/04/16.
 */
public class TaskParams {
    private String mSymbol;
    private Option mOption;

    public TaskParams(String symbol, Option option) {
        this.mSymbol = symbol;
        this.mOption = option;
    }


    public String getSymbol() {
        return mSymbol;
    }

    public void setSymbol(String mSymbol) {
        this.mSymbol = mSymbol;
    }

    public Option getOption() {
        return mOption;
    }

    public void setOption(Option mOption) {
        this.mOption = mOption;
    }
}
