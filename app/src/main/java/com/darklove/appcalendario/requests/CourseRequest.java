package com.darklove.appcalendario.requests;

import com.darklove.appcalendario.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

public class CourseRequest extends Request {
    private final URL url;
    private final String token;
    private final int id;

    public CourseRequest(String token, int id) {
        JSONObject env = Util.readJsonFromAssets("env.json");

        try {
            String strUrl = env.getString("courses_url");

            int year = Calendar.getInstance().get(Calendar.YEAR);
            int month = Calendar.getInstance().get(Calendar.MONTH);
            int semester = (month < 6) ? 1 : 2;

            strUrl = strUrl.replace("{year}", String.valueOf(year));
            strUrl = strUrl.replace("{number}", String.valueOf(semester));
            strUrl = strUrl.replace("{student_id}", String.valueOf(id));

            url = new URL(strUrl);
        } catch (JSONException e) {
            throw new RuntimeException("Error al obtener variable course_url", e);
        } catch(MalformedURLException e) {
            throw new RuntimeException("course_url no es una URL válida", e);
        }

        this.token = token;
        this.id = id;
    }

    public String getData() throws UnauthorizedException {
        HttpsURLConnection http;

        try {
            http = (HttpsURLConnection) url.openConnection();

            String authValue = "Bearer " + token;
            http.setRequestProperty("Authorization", authValue);
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/111.0");
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Accept", "application/json, text/plain, */*");
            http.setRequestMethod("GET");

            http.setSSLSocketFactory(trustAllCertificates());
            http.setHostnameVerifier((hostname, session) -> true);
            http.connect();
        } catch (IOException e) {
            throw new RuntimeException("Error al establecer la conexión con course_url", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al configurar certificados", e);
        }

        try {
            int code = http.getResponseCode();
            if (code == 401) throw new UnauthorizedException();
        } catch (IOException e) {
            throw new RuntimeException("Error al obtener código de respuesta", e);
        }

        return getResponse(http);
    }

}
