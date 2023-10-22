package com.darklove.appcalendario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class Util {
    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
    private static final DateFormat timeFormat = new SimpleDateFormat("hh:mm");
    private static final String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private static final String[] daysOfWeek = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    public static JSONObject readJsonFromAssets(String filename) {
        JSONObject jsonObject;

        try {
            InputStream is = AppCalendario.getContext().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            jsonObject = new JSONObject(jsonString);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer variables de entorno", e);
        } catch (JSONException e) {
            throw new RuntimeException("Error al convertir string a JSON", e);
        }

        return jsonObject;
    }

    public static String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        return daysOfWeek[(dayOfWeek+5)%7] + " " + dayOfMonth + " de " + months[month].toLowerCase();
    }

    public static String formatTime(Date time) {
        return timeFormat.format(time);
    }

    public static Date parseDate(String date) throws ParseException {
        return dateFormat.parse(date);
    }

    public static Date parseTime(String time) throws ParseException {
        return timeFormat.parse(time);
    }

}
