package org.asdtm.goodweather.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class XAxisValueFormatter implements IAxisValueFormatter {

    private String[] mValues;

    public XAxisValueFormatter(String[] dates) {
        mValues = dates;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mValues[(int) value];
    }

    public int getDecimalDigits() {
        return 0;
    }
}
