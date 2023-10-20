package com.darklove.appcalendario.requests;

import com.darklove.appcalendario.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class LoginRequest extends Request {
    private final URL url;
    private final JSONObject data;

    public LoginRequest(String rut, String password) {
        JSONObject env = Util.readJsonFromAssets("env.json");

        try {
            String strUrl = env.getString("login_url");
            url = new URL(strUrl);
        } catch (JSONException e) {
            throw new RuntimeException("Error al obtener variable login_url", e);
        } catch(MalformedURLException e) {
            throw new RuntimeException("login_url no es una URL válida", e);
        }

        try {
            data = new JSONObject();
            data.put("rut", rut);
            data.put("password", password);
            data.put("device_name", "web");
            data.put("token_fcm", "sin_token");
        } catch (JSONException e) {
            throw new RuntimeException("Error al convertir data a JSON", e);
        }
    }

    public String getData() throws UnauthorizedException, MaxAttemptsException {
        HttpsURLConnection http;

        try {
            http = (HttpsURLConnection) url.openConnection();
            http.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/111.0");
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Accept", "application/json, text/plain, */*");
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            http.setSSLSocketFactory(trustAllCertificates());
            http.setHostnameVerifier((hostname, session) -> true);
            http.connect();
        } catch (IOException e) {
            throw new RuntimeException("Error al establecer la conexión con login_url", e);
        } catch (Exception e) {
            throw new RuntimeException("Error al configurar certificados", e);
        }

        try {
            OutputStream outputStream = http.getOutputStream();
            outputStream.write(data.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar datos de solicitud", e);
        }

        try {
            int code = http.getResponseCode();
            System.out.println(code);
            if (code == 401 | code == 422) throw new UnauthorizedException();
            if (code == 429) throw new MaxAttemptsException();
        } catch (IOException e) {
            throw new RuntimeException("Error al obtener código de respuesta", e);
        }

        return getResponse(http);
    }

    public int getUserId(String data) {
        int userId;

        try {
            JSONObject jsonObject = new JSONObject(data);
            userId = jsonObject.getJSONObject("user").getInt("id");
        } catch(JSONException e) {
            throw new RuntimeException("No se pudo obtener la ID", e);
        }

        return userId;
    }

    public String getToken(String data) {
        String token;

        try {
            JSONObject jsonObject = new JSONObject(data);
            token = jsonObject.getString("token");
        } catch(JSONException e) {
            throw new RuntimeException("No se pudo obtener el token", e);
        }

        return token;
    }

}
