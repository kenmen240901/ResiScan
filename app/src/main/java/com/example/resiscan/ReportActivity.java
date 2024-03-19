package com.example.resiscan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private Spinner spinnerAttribute;
    private Spinner spinnerValue;
    private Button applyButton;
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    private DatabaseReference databaseReference;

    private Map<String, String> attributeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        databaseReference = FirebaseDatabase.getInstance().getReference("Images");
        spinnerAttribute = findViewById(R.id.attribute);
        spinnerValue = findViewById(R.id.value);
        applyButton = findViewById(R.id.applyBtn);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(this, new ArrayList<DataClass>());
        recyclerView.setAdapter(adapter);

        ArrayAdapter<CharSequence> attributeAdapter = ArrayAdapter.createFromResource(this,
                R.array.attributes_array, android.R.layout.simple_spinner_item);
        attributeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAttribute.setAdapter(attributeAdapter);

        initializeAttributeMap();

        spinnerAttribute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedAttribute = parent.getItemAtPosition(position).toString();
                populateValueSpinner(selectedAttribute);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter();
            }
        });
    }

    private void populateValueSpinner(String selectedAttribute) {
        spinnerValue.setAdapter(null);

        int valuesArrayResource = 0;
        switch (selectedAttribute) {
            case "Wing":
                valuesArrayResource = R.array.wing_values;
                break;
            case "Resident Type":
                valuesArrayResource = R.array.resident_type_values;
                break;
            case "Vehicle Type":
                valuesArrayResource = R.array.vehicle_type_values;
                break;
        }

        if (valuesArrayResource != 0) {
            ArrayAdapter<CharSequence> valueAdapter = ArrayAdapter.createFromResource(this,
                    valuesArrayResource, android.R.layout.simple_spinner_item);
            valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerValue.setAdapter(valueAdapter);
        }
    }

    private void initializeAttributeMap() {
        attributeMap = new HashMap<>();
        attributeMap.put("Wing", "wing");
        attributeMap.put("Resident Type", "residentType");
        attributeMap.put("Vehicle Type", "vehicleType");
    }

    private void applyFilter() {
        String attributeSpinnerValue = spinnerAttribute.getSelectedItem().toString();
        String attributeDatabaseName = attributeMap.get(attributeSpinnerValue);

        String value = spinnerValue.getSelectedItem().toString();

        if (attributeDatabaseName != null) {
            Query query = databaseReference.orderByChild(attributeDatabaseName).equalTo(value);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<DataClass> dataList = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DataClass data = snapshot.getValue(DataClass.class);
                        if (data != null) {
                            data.setKey(snapshot.getKey());
                            dataList.add(data);
                        }
                    }
                    adapter.setSearchList(dataList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ReportActivity", "Error fetching data", databaseError.toException());
                }
            });
        } else {
            Log.e("ReportActivity", "No database attribute name found for spinner value: " + attributeSpinnerValue);
        }
    }
}
