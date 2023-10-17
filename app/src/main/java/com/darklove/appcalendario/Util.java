package com.darklove.appcalendario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class Util {

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

}
