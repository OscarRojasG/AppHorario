package com.darklove.appcalendario;

import androidx.annotation.NonNull;

public enum CalendarPeriod {
    WEEKLY ("Semanal"),
    NEXT_WEEK ("Próxima semana"),
    MONTHLY ("Mensual"),
    ALL ("Todo");

    private final String name;

    CalendarPeriod(String name) {
        this.name = name;
    }

    @NonNull
    public String toString() {
        return name;
    }
}
