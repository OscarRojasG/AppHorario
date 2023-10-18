package com.darklove.appcalendario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class LoginRequest {
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

    public String getData() {
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

        String test = data.toString();
        byte[] bytes = test.getBytes(StandardCharsets.UTF_8);
        String byteString = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(byteString);

        try {
            OutputStream outputStream = http.getOutputStream();
            outputStream.write(data.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar datos de solicitud", e);
        }

        StringBuilder responseBuilder;
        try {
            System.out.println(http.getResponseCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            responseBuilder = new StringBuilder();
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
            }
        } catch(IOException e) {
            throw new RuntimeException("Error al procesar respuesta", e);
        }

        return responseBuilder.toString();
    }

    // Obtenido de ChatGPT
    private static javax.net.ssl.SSLSocketFactory trustAllCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
        };

        // Configurar la conexión SSL para confiar en todos los certificados
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc.getSocketFactory();
    }

    public int getUserId(String data) {
        return 0;
    }

    public String getToken(String data) {
        return null;
    }

}
