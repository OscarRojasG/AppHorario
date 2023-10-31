package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SuggestionActivity extends AppCompatActivity {
    private final Calendar calendar = Calendar.getInstance();
    private Course selectedCourse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        Button btnSend = findViewById(R.id.btnSendSuggest);
        TextInputEditText etName = findViewById(R.id.activityName);

        TextInputEditText etDate = findViewById(R.id.activityDate);
        configDateField(etDate);

        TextInputEditText etTime = findViewById(R.id.activityTime);
        configTimeField(etTime);

        MaterialAutoCompleteTextView courseSpinner = findViewById(R.id.activityCourse);
        configCourseSpinner(courseSpinner);

        btnSend.setOnClickListener(view -> {
            String name = etName.getText().toString();
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();

            boolean result = validateFields(name, selectedCourse, date, time);
            if (result) {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Enviando");
                progressDialog.setCancelable(false);
                progressDialog.show();

                CompletableFuture.runAsync(() -> {
                    try {
                        sendSuggestion(name, selectedCourse, date, time);
                    } catch(Exception e) {
                        throw new CompletionException("No fue posible enviar la solicitud", e);
                    }
                }).thenRunAsync(() -> {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        finish();
                    });
                }).exceptionally((e) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    });
                    return null;
                });
            }

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

    private boolean validateFields(String name, Course course, String date, String time) {
        if(name.isEmpty()) {
            String message = "El nombre no puede quedar vacío";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }

        if(name.length() > 50) {
            String message = "El nombre no puede tener más de 50 caracteres";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }

        if(course == null) {
            String message = "Por favor selecciona un curso";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }

        if(date.isEmpty()) {
            String message = "Por favor elige una fecha";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            Util.parseDate(date);
            if(!time.isEmpty()) {
                Util.parseTime(time);
            }
        } catch (ParseException e) {
            String message = "Error al procesar la fecha o la hora.";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void sendSuggestion(String name, Course course, String date, String time) throws Exception {
        Sheets service;
        service = AppCalendario.getSheetService();

        JSONObject env = Util.readJsonFromAssets("env.json");
        String spreadsheetId = env.getString("spreadsheet_id");
        String range = "Sugerencias!A2:D";

        List<List<Object>> rows = new ArrayList<>();
        String[] values = {course.getCode(), name, date, time};
        rows.add(Arrays.asList(values));

        ValueRange body = new ValueRange().setValues(rows);
        AppendValuesResponse result = service.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        int updatedRows = result.getUpdates().getUpdatedRows();
        if (updatedRows == 0) throw new RuntimeException();
    }

}