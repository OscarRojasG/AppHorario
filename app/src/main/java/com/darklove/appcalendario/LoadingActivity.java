package com.darklove.appcalendario;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.darklove.appcalendario.requests.CourseRequest;
import com.darklove.appcalendario.requests.UnauthorizedException;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        SharedPreferences sharedPreferences = getSharedPreferences("userdata", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        int userId = sharedPreferences.getInt("user_id", 0);

        if (token == null || userId == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        try {
            CourseRequest courseRequest = new CourseRequest(token, userId);
            String data = courseRequest.getData();

            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
        } catch(UnauthorizedException e) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

}