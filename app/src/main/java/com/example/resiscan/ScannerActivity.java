package com.example.resiscan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScannerActivity extends AppCompatActivity {
    Button scanBtn, logout;
    FirebaseAuth auth;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        scanBtn = findViewById(R.id.scanBtn);
        logout = findViewById(R.id.logout);
        auth = FirebaseAuth.getInstance();

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(ScannerActivity.this);
                intentIntegrator.setPrompt("Scan a barcode or QR Code");
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.initiateScan();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(ScannerActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                text = intentResult.getContents();
                populateTableWithData(text);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String role = dataSnapshot.child("role").getValue(String.class);
                        if ("admin".equals(role) || "user".equals(role)) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        super.onBackPressed();
    }

    private void populateTableWithData(String scannedData) {
        String[] dataItems = scannedData.split(" ");

        if (dataItems.length >= 9) {
            String flatNumber = dataItems[4];

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
            Query query = databaseReference.orderByChild("flatNumber").equalTo(flatNumber);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String status = snapshot.child("status").getValue(String.class);
                            if ("active".equalsIgnoreCase(status)) {
                                updateUIWithData(dataItems);
                            } else if ("inactive".equalsIgnoreCase(status)) {
                                showAlert("Account Inactive", "This account has been disabled.");
                            } else {
                                showAlert("Invalid Status", "Status unknown: " + status);
                            }
                            return;
                        }
                    } else {
                        showAlert("Data Not Found", "No record found for the scanned data.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ScannerActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Invalid data format", Toast.LENGTH_SHORT).show();
        }
    }


    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateUIWithData(String[] dataItems) {
        TextView nameTextView = findViewById(R.id.value1);
        TextView flatTextView = findViewById(R.id.value2);
        TextView residentTypeTextView = findViewById(R.id.value3);
        TextView vehicleTypeTextView = findViewById(R.id.value4);
        TextView vehicleNumberTextView = findViewById(R.id.value5);
        TextView statusTextView = findViewById(R.id.value6);

        if (dataItems.length >= 9) {
            nameTextView.setText(dataItems[0] + " " + dataItems[1] + " " + dataItems[2]);
            flatTextView.setText(dataItems[3] + " " + dataItems[4]);
            residentTypeTextView.setText(dataItems[5]);
            vehicleTypeTextView.setText(dataItems[6]);
            vehicleNumberTextView.setText(dataItems[7]);
            statusTextView.setText(dataItems[8]);
        } else {
            Toast.makeText(this, "Invalid data format", Toast.LENGTH_SHORT).show();
        }
    }
}
