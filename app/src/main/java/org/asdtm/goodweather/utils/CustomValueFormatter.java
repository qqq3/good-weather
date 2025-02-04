package org.asdtm.goodweather.utils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class CustomValueFormatter implements IValueFormatter {

    private DecimalFormat mFormat;

    public CustomValueFormatter() {
        mFormat = new DecimalFormat("#.##");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                    ViewPortHandler viewPortHandler) {
        return mFormat.format(value);
    }
}
