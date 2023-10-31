package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;

public class SuggestionActivity extends AppCompatActivity {
    private final Calendar calendar = Calendar.getInstance();
    private Course selectedCourse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        Button btnSend = findViewById(R.id.btnSendSuggest);

        TextInputEditText etDate = findViewById(R.id.activityDate);
        configDateField(etDate);

        TextInputEditText etTime = findViewById(R.id.activityTime);
        configTimeField(etTime);

        MaterialAutoCompleteTextView courseSpinner = findViewById(R.id.activityCourse);
        configCourseSpinner(courseSpinner);

        btnSend.setOnClickListener(view -> {
            Toast.makeText(this, selectedCourse.getCode(), Toast.LENGTH_LONG).show();
        });

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

    private void configCourseSpinner(MaterialAutoCompleteTextView courseSpinner) {
        courseSpinner.setKeyListener(null);

        ArrayList<Course> courses = UserData.getInstance().getCourses();
        ArrayAdapter courseAdapter = new CustomArrayAdapter(this, courses);
        courseSpinner.setAdapter(courseAdapter);

        courseSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course course = (Course) parent.getItemAtPosition(position);
                selectedCourse = course;
            }
        });
    }

}