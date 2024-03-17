package com.darklove.appcalendario.requests;

import com.darklove.appcalendario.AppCalendario;
import com.darklove.appcalendario.CalendarPeriod;
import com.darklove.appcalendario.Task;
import com.darklove.appcalendario.UserData;
import com.darklove.appcalendario.Util;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CalendarManager {
    private ArrayList<Task> tasks = new ArrayList<>();
    private CalendarPeriod calendarPeriod = CalendarPeriod.ALL;

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

    private void sortTasks() {
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

    private Iterator<Task> getTasksOnPeriod(int timeUnit, int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int currentYear = calendar.get(Calendar.YEAR);
        int currentPeriod = calendar.get(timeUnit);

        ArrayList<Task> tasksPeriod = new ArrayList<>();
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setFirstDayOfWeek(Calendar.MONDAY);
        for(int i = 0; i < tasks.size(); i++) {
            Date date = tasks.get(i).getDate();
            calendarDate.setTime(date);
            int year = calendarDate.get(Calendar.YEAR);
            int period = calendarDate.get(timeUnit);

            if(year == currentYear && period == currentPeriod + offset) {
                tasksPeriod.add(tasks.get(i));
            }
        }

        return tasksPeriod.iterator();
    }

    private Iterator<Task> getTasksOnPeriod(int timeUnit) {
        return getTasksOnPeriod(timeUnit, 0);
    }

    public Iterator<Task> getTasks() {
        switch(calendarPeriod) {
            case ALL:
                return tasks.iterator();
            case WEEKLY:
                return getTasksOnPeriod(Calendar.WEEK_OF_YEAR);
            case NEXT_WEEK:
                return getTasksOnPeriod(Calendar.WEEK_OF_YEAR, 1);
            case MONTHLY:
                return getTasksOnPeriod(Calendar.MONTH);
        }
        return null;
    }

    public void setPeriod(CalendarPeriod calendarPeriod) {
        this.calendarPeriod = calendarPeriod;
    }

}
