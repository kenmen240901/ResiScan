package com.example.resiscan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText;
    private Button loginButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String role = dataSnapshot.child("role").getValue(String.class);

                        if ("admin".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, HomePage.class));
                        } else if ("user".equals(role)) {
                            startActivity(new Intent(LoginActivity.this, ScannerActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Unknown role", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(LoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            setContentView(R.layout.activity_login);
            loginEmail = findViewById(R.id.login_email);
            loginPassword = findViewById(R.id.login_password);
            loginButton = findViewById(R.id.login_button);
            signupRedirectText = findViewById(R.id.signUpRedirectText);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = loginEmail.getText().toString();
                    String pass = loginPassword.getText().toString();

                    if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        if (!pass.isEmpty()) {
                            auth.signInWithEmailAndPassword(email, pass)
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            String userId = authResult.getUser().getUid();

                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        String role = dataSnapshot.child("role").getValue(String.class);

                                                        if ("admin".equals(role)) {
                                                            startActivity(new Intent(LoginActivity.this, HomePage.class));
                                                        } else if ("user".equals(role)) {
                                                            startActivity(new Intent(LoginActivity.this, ScannerActivity.class));
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, "Unknown role", Toast.LENGTH_SHORT).show();
                                                        }

                                                        finish();
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    Toast.makeText(LoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            loginPassword.setError("Empty fields are not allowed");
                        }
                    } else if (email.isEmpty()) {
                        loginEmail.setError("Empty fields are not allowed");
                    } else {
                        loginEmail.setError("Please enter correct email");
                    }
                }
            });

            signupRedirectText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                }
            });
        }
    }
}
