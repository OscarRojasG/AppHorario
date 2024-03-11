package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.darklove.appcalendario.requests.CalendarManager;
import com.darklove.appcalendario.requests.CalendarRequestException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CalendarActivity extends AppCompatActivity {
    private CalendarManager calendarManager;
    private LinearLayout bubbleContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        bubbleContainer = findViewById(R.id.bubble_container);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando calendario");
        progressDialog.setCancelable(false);
        progressDialog.show();

        CompletableFuture.runAsync(() -> {
            try {
                calendarManager = new CalendarManager();
                runOnUiThread(() -> showTasks());
            } catch (CalendarRequestException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                runOnUiThread(() -> progressDialog.dismiss());
            }
        }).thenRun(() -> {
            SwipeRefreshLayout swipeRefresh = findViewById(R.id.calendarSwipe);
            swipeRefresh.setOnRefreshListener(() -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        calendarManager = new CalendarManager();
                        runOnUiThread(() -> {
                            clearTasks();
                            showTasks();
                        });
                    } catch (CalendarRequestException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        runOnUiThread(() -> swipeRefresh.setRefreshing(false));
                    }
                });
            });
        });

        FloatingActionButton btnSuggestion = findViewById(R.id.btnSuggestion);
        btnSuggestion.setOnClickListener(view -> {
            Intent intent = new Intent(this, SuggestionActivity.class);
            startActivity(intent);
        });
    }

    private void clearTasks() {
        bubbleContainer.removeAllViews();
    }

    private void showTasks() {
        Iterator<Task> tasks = calendarManager.getTasks();

        while(tasks.hasNext()) {
            Task task = tasks.next();
            showTask(task);
        }
    }

    private void showTask(Task task) {
        View bubbleLayout = getLayoutInflater().inflate(R.layout.calendar_bubble, bubbleContainer, false);
        TextView txtName = bubbleLayout.findViewById(R.id.calendar_bubble_name);
        TextView txtCourse = bubbleLayout.findViewById(R.id.calendar_bubble_course);
        TextView txtDatetime = bubbleLayout.findViewById(R.id.calendar_bubble_datetime);

        String name = task.getName();
        String courseCode = task.getCourseCode();
        String courseName = task.getCourseName();
        String date = Util.customFormatDate(task.getDate());

        String time = "";
        if(task.getTime() != null) {
            time = Util.formatTime(task.getTime());
        }

        txtName.setText(name);
        txtCourse.setText(courseCode + " " + courseName);
        txtDatetime.setText(date + " " + time);

        bubbleContainer.addView(bubbleLayout);
    }

}