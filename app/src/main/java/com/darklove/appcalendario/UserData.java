package com.darklove.appcalendario;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserData {
    private final static UserData instance = new UserData();

    private HashMap<String, String> courses;
    private String username;

    private UserData() {}

    public static UserData getInstance() {
        return instance;
    }

    public void setCourses(HashMap<String, String> courses) {
        this.courses = courses;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isEnrolled(String courseCode) {
        return courses.containsKey(courseCode);
    }

    public String getCourseName(String courseCode) {
        return courses.get(courseCode);
    }

    public Iterator<Map.Entry<String, String>> getCourses() {
        return courses.entrySet().iterator();
    }

    public String getUsername() {
        return username;
    }

}
