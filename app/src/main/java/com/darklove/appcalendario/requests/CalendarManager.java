package com.darklove.appcalendario.requests;

import com.darklove.appcalendario.AppCalendario;
import com.darklove.appcalendario.Task;
import com.darklove.appcalendario.UserData;
import com.darklove.appcalendario.Util;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CalendarManager {
    private ArrayList<Task> tasks = new ArrayList<>();

    public CalendarManager() throws CalendarRequestException {
        List<List<Object>> values = makeRequest();
        initializeTasks(values);
        sortTasks();
    }

    private List<List<Object>> makeRequest() throws CalendarRequestException {
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
            throw new CalendarRequestException();
        }
    }

    public void initializeTasks(List<List<Object>> values) {
        for (List row : values) {
            try {
                Task task = createTask(row);
                if(validateTask(task)) {
                    tasks.add(task);
                }
            } catch (Exception e) {}
        }
    }

    private Task createTask(List row) throws Exception {
        int id = Integer.parseInt((String) row.get(0));
        String courseCode = (String) row.get(1);
        String name = (String) row.get(2);
        Date date = Util.parseDate((String) row.get(3));
        Date time = null;
        if (row.size() == 5) {
            time = Util.parseTime((String) row.get(4));
        }

        return new Task(id, courseCode, name, date, time);
    }

    private boolean validateTask(Task task) {
        UserData userData = UserData.getInstance();
        if (!userData.isEnrolled(task.getCourseCode()))
            return false;

        Date currentDate = null;
        Date currentTime = null;
        try {
            currentDate = Util.parseDate(Util.formatDate(new Date()));
            currentTime = Util.parseTime(Util.formatTime(new Date()));
        } catch (ParseException e) {}

        if (task.getDate().compareTo(currentDate) < 0)
            return false;
        if (task.getDate().compareTo(currentDate) == 0 && task.getDate().compareTo(currentTime) < 0) {
            return false;
        }

        return true;
    }

    public void sortTasks() {
        Collections.sort(tasks, (a, b) -> {
            Date dateA = (Date) a.getDate();
            Date dateB = (Date) b.getDate();

            int compare = dateA.compareTo(dateB);
            if (compare != 0) return compare;
            if (a.getTime() == null) return 1;
            if (b.getTime() == null) return -1;

            Date timeA = (Date) a.getTime();
            Date timeB = (Date) b.getTime();
            return timeA.compareTo(timeB);
        });
    }

    public Iterator<Task> getTasks() {
        return tasks.iterator();
    }

}
