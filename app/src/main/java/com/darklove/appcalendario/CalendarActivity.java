package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    private static final String APPLICATION_NAME = "AppCalendario";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String CREDENTIALS_FILE_NAME = "credentials.json";

    private HashMap<String, String> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        courses = (HashMap<String, String>) getIntent().getSerializableExtra("courses");
        JSONArray activities = getCalendarActivities();
        System.out.println(activities);
    }

    private JSONArray getCalendarActivities() {
        List<List<Object>> values = makeRequest();
        JSONArray activities = new JSONArray();

        if (values == null || values.isEmpty()) return activities;

        for (List row : values) {
            JSONObject activity = new JSONObject();

            try {
                int id = Integer.parseInt((String) row.get(0));
                String courseCode = (String) row.get(1);
                String name = (String) row.get(2);

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
                Date date = dateFormat.parse((String) row.get(3));

                DateFormat timeFormat = new SimpleDateFormat("hh:mm");
                Date time = null;
                if (row.size() == 5) {
                    time = timeFormat.parse((String) row.get(4));
                }

                activity.put("Id", id);
                activity.put("CourseCode", courseCode);
                activity.put("Name", name);
                activity.put("Date", date);
                activity.put("Time", time);
                activities.put(activity);
            } catch(Exception e) { }

        }

        return activities;
    }

    private List<List<Object>> makeRequest() {
        Sheets service;
        try {
            service = getSheetService();

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

    private Sheets getSheetService() throws GeneralSecurityException, IOException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = GoogleCredential.fromStream(getAssets().open(CREDENTIALS_FILE_NAME))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

}