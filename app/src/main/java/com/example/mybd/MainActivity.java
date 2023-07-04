package com.example.mybd;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mybd.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {

    private TextView textViewData;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final String API_URL = "https://carrierguru.in/aconfig.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);
        textViewData = findViewById(R.id.textViewData);
        new RetrieveDataTask().execute(API_URL);

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFormDialog();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_reachout)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void showFormDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Leads");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_form, null);
        builder.setView(dialogView);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            EditText editTextName = dialogView.findViewById(R.id.editTextName);
            EditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);
            EditText editTextEmail = dialogView.findViewById(R.id.editTextCity);
            String name = editTextName.getText().toString();
            String email = editTextEmail.getText().toString();
            String phone = editTextPhone.getText().toString();
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "On-boarding Failed ||Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            sendDataToWebsite(name, phone);

            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void sendDataToWebsite(String editTextName, String editTextPhone) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("param1", editTextName)
                .add("param2", editTextPhone)
                .build();
        Request request = new Request.Builder()
                .url("https://carrierguru.in/aconfig.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Handle network error
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error sending data.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // Handle response from the PHP website (if needed)
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    // Request was successful
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Lead added successfully.", Toast.LENGTH_LONG).show());
                    // Process the response as needed
                } else {
                    // Request failed
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error sending data.", Toast.LENGTH_SHORT).show());
                    // Handle the error response
                }
                response.close();
            }
        });
    }

    private class RetrieveDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }
                    reader.close();
                } else {
                    result = "Error: " + responseCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Response: " + result);
            Toast.makeText(MainActivity.this, "Retrieved data: " + result, Toast.LENGTH_SHORT).show();
        }
    }
}
