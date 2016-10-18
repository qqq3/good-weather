package org.asdtm.goodweather.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

public class XAxisValueFormatter implements AxisValueFormatter {

    private String[] mValues;

    public XAxisValueFormatter(String[] dates) {
        mValues = dates;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mValues[(int) value];
    }

    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
