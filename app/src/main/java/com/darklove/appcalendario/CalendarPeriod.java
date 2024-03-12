package com.darklove.appcalendario;

public enum CalendarPeriod {
    WEEKLY ("Semanal"),
    MONTHLY ("Mensual"),
    ALL ("Todo");

    private final String name;

    CalendarPeriod(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
