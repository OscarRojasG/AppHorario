package com.darklove.appcalendario;

import java.util.Date;

public class Task {
    private UserData userData = UserData.getInstance();
    private int id;
    private String courseCode;
    private String name;
    private Date date;
    private Date time;

    public Task(int id, String courseCode, String name, Date date, Date time) {
        this.id = id;
        this.courseCode = courseCode;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public Date getTime() {
        return time;
    }

    public String getCourseName() {
        return userData.getCourseName(courseCode);
    }
}
