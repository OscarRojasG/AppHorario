package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etRut, etPassword;
        Button btnIngresar;

        etRut = findViewById(R.id.etRut);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);

        btnIngresar.setOnClickListener(view -> {
            String rut = etRut.getText().toString();
            String password = etPassword.getText().toString();

            Toast.makeText(getApplicationContext(), "RUT " + rut + ", Clave " + password, Toast.LENGTH_LONG).show();
        });

    }

}