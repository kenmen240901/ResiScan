package com.example.resiscan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<DataClass> dataList;
    private Context context;

    public MyAdapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    public void setSearchList(ArrayList<DataClass> dataSearchList){
        this.dataList = dataSearchList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataClass data = dataList.get(position);

        Glide.with(context).load(data.getImageURL()).into(holder.recyclerImage);
        holder.Wing.setText(data.getWing());
        holder.Flat.setText(data.getFlatNumber());
        holder.Vtype.setText(data.getVehicleType());

        int statusImageResource = data.getStatus().equalsIgnoreCase("active") ?
                R.drawable.baseline_done_all_24px : R.drawable.baseline_block_24;
        holder.btnStatus.setImageResource(statusImageResource);

        int statusBackgroundResource = data.getStatus().equalsIgnoreCase("active") ?
                R.drawable.btn_active : R.drawable.btn_inactive;
        holder.btnStatus.setBackgroundResource(statusBackgroundResource);

        holder.btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.changeAccountStatus(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView recyclerImage, btnDelete, btnStatus;
        TextView Wing, Flat, Vtype;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerImage = itemView.findViewById(R.id.recyclerImage);
            Wing = itemView.findViewById(R.id.Wing);
            Flat = itemView.findViewById(R.id.Flat);
            Vtype = itemView.findViewById(R.id.Vtype);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnStatus = itemView.findViewById(R.id.btnStatus);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View view = inflater.inflate(R.layout.custom_dialog_box, null);
                    TextInputLayout textInputLayout = view.findViewById(R.id.textInputLayout);

                    alertDialogBuilder.setView(view)
                            .setTitle("Delete Record")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String userInput = textInputLayout.getEditText().getText().toString();
                                    if (userInput.trim().equals("DELETE")) {
                                        int adapterPosition = getAdapterPosition();
                                        if (adapterPosition != RecyclerView.NO_POSITION) {
                                            String key = dataList.get(adapterPosition).getKey();
                                            deleteItemFromDatabase();
                                            String imageUrl = dataList.get(adapterPosition).getImageURL();
                                            deleteImageFromStorage(imageUrl);
                                            dataList.remove(adapterPosition);
                                            notifyItemRemoved(adapterPosition);
                                            dialog.cancel();
                                        }
                                    } else {
                                        Toast.makeText(context, "Wrong input", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .show();
                }
            });

            btnStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataClass data = dataList.get(getAdapterPosition());
                    String newStatus = data.getStatus().equalsIgnoreCase("active") ? "inactive" : "active";
                    data.setStatus(newStatus);
                    if (newStatus.equalsIgnoreCase("active")) {
                        btnStatus.setImageResource(R.drawable.baseline_done_all_24px);
                        btnStatus.setBackgroundResource(R.drawable.btn_active);
                    } else {
                        btnStatus.setImageResource(R.drawable.baseline_block_24);
                        btnStatus.setBackgroundResource(R.drawable.btn_inactive);
                    }

                    changeAccountStatus(data);
                }
            });
        }

        public void deleteItemFromDatabase() {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
            int adapterPosition = getLayoutPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {
                String key = dataList.get(adapterPosition).getKey();
                if (key != null) {
                    databaseReference.child(key).removeValue();
                } else {
                    Log.e("MyAdapter", "Key is null");
                }
            }
        }

        private void deleteImageFromStorage(String imageUrl) {
            String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.indexOf('?'));
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(imageName);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Deletion Successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Deletion Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void changeAccountStatus(DataClass data) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");

            String newStatus = data.getStatus().equalsIgnoreCase("active") ? "inactive" : "active";

            databaseReference.child(data.getKey()).child("status").setValue(newStatus)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            data.setStatus(newStatus);
                            int statusImageResource = newStatus.equalsIgnoreCase("active") ?
                                    R.drawable.baseline_done_all_24px : R.drawable.baseline_block_24;
                            btnStatus.setImageResource(statusImageResource);

                            int statusBackgroundResource = newStatus.equalsIgnoreCase("active") ?
                                    R.drawable.btn_active : R.drawable.btn_inactive;
                            btnStatus.setBackgroundResource(statusBackgroundResource);

                            Toast.makeText(context, "Account status updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to update account status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}
