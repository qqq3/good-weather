package org.asdtm.goodweather.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

public class YAxisValueFormatter implements IAxisValueFormatter {

    private DecimalFormat mFormat;

    public YAxisValueFormatter() {
        mFormat = new DecimalFormat("#.##");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value);
    }

    public int getDecimalDigits() {
        return 1;
    }
}
