package com.example.resiscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    EditText fname, lname, fno, vno;
    Button submit;
    Spinner wing, title, rtype, vtype;
    String[] w = {"A", "B", "C"};
    String[] t = {"Mr.", "Mrs.", "Ms."};
    String[] rt = {"Owner", "Tenant"};
    String[] vt = {"Two-Wheeler", "Four-Wheeler"};
    String status = "Active";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wing = findViewById(R.id.wing);
        title = findViewById(R.id.title);
        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        fno = findViewById(R.id.fno);
        rtype = findViewById(R.id.res);
        vtype = findViewById(R.id.vtype);
        vno = findViewById(R.id.vno);

        submit = findViewById(R.id.generateBtn);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> a1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, w);
        ArrayAdapter<String> a2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, t);
        ArrayAdapter<String> a3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rt);
        ArrayAdapter<String> a4 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, vt);

        a1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wing.setAdapter(a1);
        a2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        title.setAdapter(a2);
        a3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rtype.setAdapter(a3);
        a4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vtype.setAdapter(a4);

        rtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedResidentType = rt[position];
                updateVehicleTypeOptions(selectedResidentType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    final String titleValue = title.getSelectedItem().toString();
                    final String firstNameValue = fname.getText().toString();
                    final String lastNameValue = lname.getText().toString();
                    final String wingValue = wing.getSelectedItem().toString();
                    final String flatNumberValue = fno.getText().toString();
                    final String residentTypeValue = rtype.getSelectedItem().toString();
                    final String vehicleTypeValue = vtype.getSelectedItem().toString();
                    final String vehicleNumberValue = vno.getText().toString();

                    String dataToEncode = titleValue + " " + firstNameValue + " " + lastNameValue + " "
                            + wingValue + " " + flatNumberValue + " " + residentTypeValue + " "
                            + vehicleTypeValue + " " + vehicleNumberValue + " " + status;
                    String caption = wingValue + " " + flatNumberValue + " " + residentTypeValue;
                    Bitmap barcodeBitmap = TextToImageEncode(dataToEncode, caption);
                    DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("Images");
                    Query query = tenantRef.orderByChild("wing").equalTo(wingValue);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int vehicleCount = 0;
                            int twoWheelerCount = 0;
                            boolean canProceed = true;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String flatNumberInDB = snapshot.child("flatNumber").getValue(String.class);
                                String residentTypeInDB = snapshot.child("residentType").getValue(String.class);
                                String vehicleType = snapshot.child("vehicleType").getValue(String.class);

                                if (flatNumberInDB.equals(flatNumberValue) && residentTypeInDB.equals(residentTypeValue)) {
                                    vehicleCount++;

                                    if ("Owner".equals(residentTypeValue)) {
                                        int countTwoWheelers = countTwoWheelers(dataSnapshot, flatNumberValue, residentTypeValue);
                                        if ("Four-Wheeler".equals(vehicleType) && vehicleCount >= 1) {
                                            canProceed = false;
                                            Toast.makeText(MainActivity.this, "Owner already has a two-wheeler, cannot add a four-wheeler", Toast.LENGTH_SHORT).show();
                                            break;
                                        }

                                        if ("Two-Wheeler".equals(vehicleType)) {
                                            twoWheelerCount++;
                                            if (twoWheelerCount >= 2) {
                                                canProceed = false;
                                                Toast.makeText(MainActivity.this, "Owner cannot have more than 2 two-wheelers", Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                            if (countTwoWheelers >= 1) {
                                                canProceed = false;
                                                Toast.makeText(MainActivity.this, "Owner already has a two-wheeler, cannot add a four-wheeler", Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                        }
                                    } else if ("Tenant".equals(residentTypeValue)) {
                                        if ("Four-Wheeler".equals(vehicleType)) {
                                            canProceed = false;
                                            Toast.makeText(MainActivity.this, "Tenant cannot have a four-wheeler", Toast.LENGTH_SHORT).show();
                                            break;
                                        }

                                        if ("Two-Wheeler".equals(vehicleType) && vehicleCount >= 1) {
                                            canProceed = false;
                                            Toast.makeText(MainActivity.this, "Tenant cannot have more than 1 two-wheeler", Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                }
                            }

                            if (canProceed) {
                                uploadToFirebase(barcodeBitmap, title.getSelectedItem().toString(),
                                        fname.getText().toString(), lname.getText().toString(),
                                        wingValue, flatNumberValue, residentTypeValue,
                                        vtype.getSelectedItem().toString(), vno.getText().toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private int countTwoWheelers(DataSnapshot dataSnapshot, String flatNumberValue, String residentTypeValue) {
        int count = 0;
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String flatNumberInDB = snapshot.child("flatNumber").getValue(String.class);
            String residentTypeInDB = snapshot.child("residentType").getValue(String.class);
            String vehicleType = snapshot.child("vehicleType").getValue(String.class);

            if (flatNumberInDB.equals(flatNumberValue) && residentTypeInDB.equals(residentTypeValue) && "Two-Wheeler".equals(vehicleType)) {
                count++;
            }
        }
        return count;
    }

    private boolean validateFields() {
        boolean valid = true;

        String firstNameValue = fname.getText().toString();
        String lastNameValue = lname.getText().toString();
        String flatNumberValue = fno.getText().toString();
        String vehicleNumberValue = vno.getText().toString();

        if (TextUtils.isEmpty(firstNameValue)) {
            fname.setError("Please enter first name");
            valid = false;
        } else {
            fname.setError(null);
        }

        if (TextUtils.isEmpty(lastNameValue)) {
            lname.setError("Please enter last name");
            valid = false;
        } else {
            lname.setError(null);
        }

        if (TextUtils.isEmpty(flatNumberValue)) {
            fno.setError("Please enter flat number");
            valid = false;
        } else {
            fno.setError(null);
        }

        if (TextUtils.isEmpty(vehicleNumberValue)) {
            vno.setError("Please enter vehicle number");
            valid = false;
        } else {
            vno.setError(null);
        }

        return valid;
    }


    Bitmap TextToImageEncode(String QRText, String imageName) {
        MultiFormatWriter multiFormatWriter;
        BitMatrix bitMatrix;
        Bitmap bitmap = null;
        int textWidth = 10;

        multiFormatWriter = new MultiFormatWriter();

        try {
            bitMatrix = multiFormatWriter.encode(QRText, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);

            Canvas canvas = new Canvas(bitmap);
            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(0, 0, 0));
            paint.setTextSize(textWidth);
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            float x = 3 * textWidth;
            float y = (bitmap.getHeight() - textWidth);
            canvas.drawText(imageName, x, y, paint);
            canvas.save();
            canvas.restore();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void uploadToFirebase(Bitmap barcodeBitmap, String title, String firstName, String lastName,
                                  String wing, String flatNumber, String residentType,
                                  String vehicleType, String vehicleNumber) {
        if (barcodeBitmap != null && isDataValid(title, firstName, lastName, wing, flatNumber, residentType, vehicleType, vehicleNumber)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            barcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] barcodeImageData = baos.toByteArray();
            final StorageReference barcodeImageRef = storageReference.child(System.currentTimeMillis() + "_barcode.png");
            barcodeImageRef.putBytes(barcodeImageData)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            barcodeImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri barcodeUri) {
                                    DataClass dataClass = new DataClass(title, firstName, lastName, wing, flatNumber,
                                            residentType, vehicleType, vehicleNumber, status,
                                            barcodeUri.toString());


                                    String key = databaseReference.push().getKey();
                                    databaseReference.child(key).setValue(dataClass)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(MainActivity.this, HomePage.class));
                                                    overridePendingTransition(R.anim.slide_in_anim, R.anim.slide_out_anim);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(MainActivity.this, "Failed to upload data", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Failed to upload barcode image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "Invalid data or failed to generate barcode", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDataValid(String title, String firstName, String lastName,
                                String wing, String flatNumber, String residentType,
                                String vehicleType, String vehicleNumber) {
        return !title.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty()
                && !wing.isEmpty() && !flatNumber.isEmpty() && !residentType.isEmpty()
                && !vehicleType.isEmpty() && !vehicleNumber.isEmpty();
    }

    private void updateVehicleTypeOptions(String residentType) {
        ArrayAdapter<String> adapter;
        if ("Owner".equals(residentType)) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, vt);
        } else {
            String[] vtWithoutFourWheeler = {"Two-Wheeler"};
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, vtWithoutFourWheeler);
        }
        vtype.setAdapter(adapter);
    }


    public void onBackPressed() {
        startActivity(new Intent(MainActivity.this, HomePage.class));
        overridePendingTransition(R.anim.slide_in_anim, R.anim.slide_out_anim);
        super.onBackPressed();
    }
}