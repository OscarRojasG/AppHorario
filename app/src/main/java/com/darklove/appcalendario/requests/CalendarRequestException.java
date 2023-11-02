package com.darklove.appcalendario.requests;

public class CalendarRequestException extends Exception {
    private final static String message = "Error al cargar el calendario";

    public CalendarRequestException() {
        super(message);
    }

}
