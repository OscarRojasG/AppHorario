package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class SuggestionActivity extends AppCompatActivity {
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        TextInputEditText etDate = findViewById(R.id.activityDate);
        configDateField(etDate);

        TextInputEditText etTime = findViewById(R.id.activityTime);
        configTimeField(etTime);

    }

    private void configDateField(TextInputEditText etDate) {
        etDate.setInputType(InputType.TYPE_NULL);
        etDate.setKeyListener(null);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis()-1000);
        Calendar upperLimit = Calendar.getInstance();
        upperLimit.add(Calendar.YEAR, 1);
        datePickerDialog.getDatePicker().setMaxDate(upperLimit.getTimeInMillis());

        datePickerDialog.setOnDateSetListener((view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            String date = Util.formatDate(calendar.getTime());
            etDate.setText(date);
        });

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
    }

    private void configTimeField(TextInputEditText etTime) {
        etTime.setInputType(InputType.TYPE_NULL);
        etTime.setKeyListener(null);

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            String date = Util.formatTime(calendar.getTime());
            etTime.setText(date);
        };

        Calendar currentTime = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                onTimeSetListener,
                currentTime.get(Calendar.HOUR),
                currentTime.get(Calendar.MINUTE),
                false);

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show();
            }
        });
    }

}