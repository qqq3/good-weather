package org.asdtm.goodweather.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.text.DecimalFormat;

public class YAxisValueFormatter implements AxisValueFormatter {

    private DecimalFormat mFormat;

    public YAxisValueFormatter() {
        mFormat = new DecimalFormat("#.##");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value);
    }

    @Override
    public int getDecimalDigits() {
        return 1;
    }
}
