package com.darklove.appcalendario;

import android.content.Context;

import java.util.Arrays;

public class CalendarPeriodAdapter extends CustomArrayAdapter {

    public CalendarPeriodAdapter(Context context) {
        super(context, Arrays.asList(CalendarPeriod.values()));
    }
}
