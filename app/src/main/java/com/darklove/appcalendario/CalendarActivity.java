package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CalendarActivity extends AppCompatActivity {

    private UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando calendario");
        progressDialog.setCancelable(false);
        progressDialog.show();

        CompletableFuture.supplyAsync(() -> {
            userData = UserData.getInstance();
            JSONArray activities = getCalendarActivities();
            return sortActivities(activities);
        }).thenAccept((activities) -> {
            runOnUiThread(() -> {
                progressDialog.hide();

                LinearLayout parentLayout = findViewById(R.id.bubble_container);
                for (int i = 0; i < activities.length(); i++) {
                    View bubbleLayout = getLayoutInflater().inflate(R.layout.calendar_bubble, parentLayout, false);
                    TextView txtName = bubbleLayout.findViewById(R.id.calendar_bubble_name);
                    TextView txtCourse = bubbleLayout.findViewById(R.id.calendar_bubble_course);
                    TextView txtDatetime = bubbleLayout.findViewById(R.id.calendar_bubble_datetime);

                    try {
                        JSONObject activity = activities.getJSONObject(i);
                        String name = activity.getString("name");
                        String courseCode = activity.getString("course_code");
                        String courseName = userData.getCourseName(courseCode);
                        String date = Util.customFormatDate((Date) activity.get("date"));

                        String time = "";
                        if (activity.has("time")) {
                            time = Util.formatTime((Date) activity.get("time"));
                        }

                        txtName.setText(name);
                        txtCourse.setText(courseCode + " " + courseName);
                        txtDatetime.setText(date + " " + time);
                    } catch (JSONException e) {
                        String message = "Error al mostrar actividades";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }

                    parentLayout.addView(bubbleLayout);
                }
            });
        });

        FloatingActionButton btnSuggestion =  findViewById(R.id.btnSuggestion);
        btnSuggestion.setOnClickListener(view -> {
            Intent intent = new Intent(this, SuggestionActivity.class);
            startActivity(intent);
        });
    }

    private JSONArray getCalendarActivities() {
        List<List<Object>> values = makeRequest();
        JSONArray activities = new JSONArray();

        if (values == null || values.isEmpty()) return activities;

        Date currentDate = null;
        Date currentTime = null;
        try {
            currentDate = Util.parseDate(Util.formatDate(new Date()));
            currentTime = Util.parseTime(Util.formatTime(new Date()));
        } catch (ParseException e) {}

        for (List row : values) {
            JSONObject activity = new JSONObject();

            try {
                int id = Integer.parseInt((String) row.get(0));
                String courseCode = (String) row.get(1);
                if (!userData.isEnrolled(courseCode)) continue;

                String name = (String) row.get(2);

                Date date = Util.parseDate((String) row.get(3));
                if (date.compareTo(currentDate) < 0) continue;

                Date time = null;
                if (row.size() == 5) {
                    time = Util.parseTime((String) row.get(4));
                    if (date.compareTo(currentDate) == 0 && time.compareTo(currentTime) < 0) {
                        continue;
                    }
                }

                activity.put("id", id);
                activity.put("course_code", courseCode);
                activity.put("name", name);
                activity.put("date", date);
                activity.put("time", time);
                activities.put(activity);
            } catch(Exception e) { }

        }

        return activities;
    }

    private List<List<Object>> makeRequest() {
        Sheets service;
        try {
            service = AppCalendario.getSheetService();

            JSONObject env = Util.readJsonFromAssets("env.json");
            String spreadsheetId = env.getString("spreadsheet_id");
            String range = "Actividades!A2:E";

            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            return response.getValues();
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No fue posible cargar el calendario", Toast.LENGTH_LONG).show();
        }

        return null;
    }

    private JSONArray sortActivities(JSONArray activities) {
        List<JSONObject> activityList = new ArrayList<>();
        for (int i = 0; i < activities.length(); i++) {
            try {
                activityList.add(activities.getJSONObject(i));
            } catch (JSONException e) {}
        }

        Collections.sort(activityList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                try {
                    Date dateA = (Date) a.get("date");
                    Date dateB = (Date) b.get("date");

                    int compare = dateA.compareTo(dateB);
                    if (compare != 0) return compare;
                    if (!a.has("time")) return 1;
                    if (!b.has("time")) return -1;

                    Date timeA = (Date) a.get("time");
                    Date timeB = (Date) b.get("time");
                    return timeA.compareTo(timeB);
                } catch (JSONException e) {}

                return 0;
            }
        });

        JSONArray sortedArray = new JSONArray();
        for (int i = 0; i < activityList.size(); i++) {
            sortedArray.put(activityList.get(i));
        }

        return sortedArray;
    }

}