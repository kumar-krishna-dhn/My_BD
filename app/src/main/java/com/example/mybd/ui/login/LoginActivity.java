package com.example.mybd.ui.login;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mybd.MainActivity;
import com.example.mybd.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


    private Button btn_sign_in;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Create GoogleSignInClient with the options
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Get reference to shared preferences
        sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (isLoggedIn()) {
            redirectToMain();
        }


        btn_sign_in = findViewById(R.id.btn_sign_in);

        // Set click listener for the login button
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Create an AuthStateListener to monitor the user authentication state
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !user.isEmailVerified()) {
                    // User is logged in but their account is disabled or not verified
                    Toast.makeText(LoginActivity.this, "Account is disabled or not verified", Toast.LENGTH_SHORT).show();
                    signOut();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start listening for authentication state changes
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening for authentication state changes
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the Google Sign-In result
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String idToken = account.getIdToken();
                    String displayName = account.getDisplayName();
                    String email = account.getEmail();
                    // Perform further actions with the signed-in user's info
                    Log.d("GoogleSignIn", "ID Token: " + idToken);
                    Log.d("GoogleSignIn", "Display Name: " + displayName);
                    Log.d("GoogleSignIn", "Email: " + email);

                    // Save the login status in shared preferences
                    saveLoginStatus(true);

                    // Redirect to MainActivity
                    redirectToMain();

                    // Successful login, display a toast message
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                // Google Sign-In failed, update UI appropriately
                Log.w("GoogleSignIn", "Google sign-in failed", e);
                Toast.makeText(LoginActivity.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLoggedIn() {
        return sharedPref.getBoolean("isLoggedIn", false);
    }

    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    private void redirectToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void signOut() {
        // Sign out from Firebase and Google Sign-In
        firebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Clear the login status from shared preferences
                saveLoginStatus(false);
                // Redirect to login activity
                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
