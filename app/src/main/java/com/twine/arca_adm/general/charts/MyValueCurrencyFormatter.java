package com.twine.arca_adm.general.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by Jose Williams Garcia on 14/5/2017.
 */

public class MyValueCurrencyFormatter implements IAxisValueFormatter
{

    private DecimalFormat mFormat;

    public MyValueCurrencyFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value) + " C$";
    }
}
