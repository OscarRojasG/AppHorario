package com.darklove.appcalendario.requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.HttpsURLConnection;

public class Request {

    public Request() {}

    // Obtenido de ChatGPT
    public static javax.net.ssl.SSLSocketFactory trustAllCertificates() throws Exception {
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

    public String getResponse(HttpsURLConnection http) {
        StringBuilder responseBuilder;
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

}
