package com.sam_chordas.android.stockhawk.rest;

/**
 * Created by asalfo on 11/04/16.
 */
public class ChartVal<X,Y> {
    private final X xVals;
    private final Y yVals;

    public ChartVal(X xVals, Y yVals) {
        this.xVals = xVals;
        this.yVals = yVals;
    }

    public X getxVals() {
        return xVals;
    }

    public Y getyVals() {
        return yVals;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChartVal)) return false;

        ChartVal<?, ?> chartVal = (ChartVal<?, ?>) o;

        if (getxVals() != null ? !getxVals().equals(chartVal.getxVals()) : chartVal.getxVals() != null)
            return false;
        return getyVals() != null ? getyVals().equals(chartVal.getyVals()) : chartVal.getyVals() == null;

    }

    @Override
    public int hashCode() {
        int result = getxVals() != null ? getxVals().hashCode() : 0;
        result = 31 * result + (getyVals() != null ? getyVals().hashCode() : 0);
        return result;
    }
}
