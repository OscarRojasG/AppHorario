package com.darklove.appcalendario;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenRequest {
    private final String url;

    public TokenRequest() {
        JSONObject env = Util.readJsonFromAssets("env.json");

        try {
            url = env.getString("login_url");
        } catch (JSONException e) {
            throw new RuntimeException("Error al obtener variable login_url", e);
        }
    }

    public String getToken() {
        return url;
    }

}
