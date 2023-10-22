package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.darklove.appcalendario.requests.CourseRequest;
import com.darklove.appcalendario.requests.LoginRequest;
import com.darklove.appcalendario.requests.MaxAttemptsException;
import com.darklove.appcalendario.requests.UnauthorizedException;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etRut, etPassword;
        Button btnIngresar;

        etRut = findViewById(R.id.etRut);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);

        // Obtenido de https://es.stackoverflow.com/a/535116
        etRut.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String rut = String.valueOf(etRut.getText());
                if (!isEditing && !rut.isEmpty()) {
                    isEditing = true;
                    etRut.getText().clear();
                    etRut.append(formatearRUT(rut));
                    isEditing = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnIngresar.setOnClickListener(view -> {
            String rut = etRut.getText().toString();
            String password = etPassword.getText().toString();

            if (!validateRut(rut)) {
                String message = "El RUT ingresado no es válido";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                String message = "Ingresa una clave";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 8 || password.length() > 15) {
                String message = "La clave debe tener entre 8 a 15 caracteres";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }

            login(rut, password);
        });

    }

    private void login(String rut, String password) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando");
        progressDialog.setCancelable(false);
        progressDialog.show();

        LoginRequest loginRequest = new LoginRequest(rut, password);

        CompletableFuture.supplyAsync(() -> {
            try {
                return loginRequest.getData();
            } catch (UnauthorizedException e) {
                throw new CompletionException("RUT o clave ingresados incorrectamente", e);
            } catch (MaxAttemptsException e) {
                throw new CompletionException("Demasiados intentos fallidos. Intenta de nuevo más tarde", e);
            }
        }).thenAccept(data -> {
            int id = loginRequest.getUserId(data);
            String token = loginRequest.getToken(data);

            SharedPreferences sharedPreferences = getSharedPreferences("userdata", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_id", id);
            editor.putString("token", token);
            editor.apply();

            runOnUiThread(() -> {
                loadCourses(token, id);
                progressDialog.hide();
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                progressDialog.hide();
            });
            return null;
        });
    }

    private void loadCourses(String token, int id) {
        HashMap<String, String> courses;

        try {
            CourseRequest courseRequest = new CourseRequest(token, id);
            String data = courseRequest.getData();
            courses = courseRequest.getCourses(data);
        } catch(UnauthorizedException e) {
            String message = "Ocurrió un error al validar tus credenciales. Intenta de nuevo más tarde";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, CalendarActivity.class);
        intent.putExtra("courses", courses);
        startActivity(intent);
        finish();
    }

    // Obtenido de https://es.stackoverflow.com/a/156485
    private boolean validateRut(String rut) {
        boolean validation = false;

        try {
            rut = rut.toUpperCase();
            rut = rut.replace(".", "");
            rut = rut.replace("-", "");
            int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));

            char dv = rut.charAt(rut.length() - 1);

            int m = 0, s = 1;
            for (; rutAux != 0; rutAux /= 10) {
                s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;
            }
            if (dv == (char) (s != 0 ? s + 47 : 75)) {
                validation = true;
            }
        } catch(Exception e) {}

        return validation;
    }

    // Obtenido de https://es.stackoverflow.com/a/535116
    public String formatearRUT(String rut) {
        String format;
        int cont = 0;
        rut = rut.replace(".", "");
        rut = rut.replace("-", "");
        if ((rut.length() - 1) != 0) {
            format = "-" + rut.substring(rut.length() - 1);
            for (int i = rut.length() - 2; i >= 0; i--) {
                format = rut.charAt(i) + format;
                cont++;
                if (cont == 3 && i != 0) {
                    format = "." + format;
                    cont = 0;
                }
            }
        } else {
            return rut;
        }
        return format;
    }

}