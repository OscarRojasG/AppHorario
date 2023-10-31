package com.darklove.appcalendario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserData {
    private final static UserData instance = new UserData();

    private HashMap<String, String> courses;

    private UserData() {}

    public static UserData getInstance() {
        return instance;
    }

    public void setCourses(HashMap<String, String> courses) {
        this.courses = courses;
    }

    public boolean isEnrolled(String courseCode) {
        return courses.containsKey(courseCode);
    }

    public String getCourseName(String courseCode) {
        return courses.get(courseCode);
    }

    public ArrayList<Course> getCourses() {
        ArrayList<Course> courseList = new ArrayList<>();

        Iterator it = courses.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = (Map.Entry) it.next();
            Course course = new Course(pair.getValue(), pair.getKey());
            courseList.add(course);
            it.remove();
        }

        return courseList;
    }

}
