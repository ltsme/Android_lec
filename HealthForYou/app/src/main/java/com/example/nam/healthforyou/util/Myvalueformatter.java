package com.example.nam.healthforyou.util;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by NAM on 2017-08-08.
 */

public class Myvalueformatter implements IValueFormatter {
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return ""+(int)value;
    }
}
