package com.example.tpvolley.projetws;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.tpvolley.projetws.Etudiant ;
import com.example.tpvolley.projetws.EtudiantService;
import com.example.tpvolley.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";
    private ListView listView;
    private EtudiantService es;
    private List<Etudiant> etudiants;
    private EtudiantAdapter adapter;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private String currentImagePath;
    private Etudiant currentEtudiant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        es = new EtudiantService(this);
        listView = findViewById(R.id.listView);

        // Fetch all students from the database
        etudiants = es.findAll();

        // Log all students data
        logEtudiantsData(etudiants);

        adapter = new EtudiantAdapter(this, etudiants);
        listView.setAdapter(adapter);

        // Set item click listener for handling updates and deletion
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Etudiant selectedEtudiant = etudiants.get(position);

                // Log the selected student
                Log.d(TAG, "Selected student - ID: " + selectedEtudiant.getId() +
                        ", Nom: " + selectedEtudiant.getNom() +
                        ", Prenom: " + selectedEtudiant.getPrenom() +
                        ", DateNaiss: " + new SimpleDateFormat("dd/MM/yyyy").format(selectedEtudiant.getDateNaiss()));


                showOptionsDialog(selectedEtudiant);
            }
        });
    }

    // Method to log all students data
    private void logEtudiantsData(List<Etudiant> etudiants) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Log.d(TAG, "===== List of Students =====");
        for (Etudiant etudiant : etudiants) {
            String dateStr = (etudiant.getDateNaiss() != null) ?
                    dateFormat.format(etudiant.getDateNaiss()) :
                    "N/A";

            Log.d(TAG, "ID: " + etudiant.getId() +
                    " | Nom: " + etudiant.getNom() +
                    " | Prenom: " + etudiant.getPrenom() +
                    " | DateNaiss: " + dateStr);
        }
        Log.d(TAG, "===========================");
    }

    // Dialog to choose between update or delete
    private void showOptionsDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir une action");

        builder.setPositiveButton("Mettre à jour", (dialog, which) -> {
            Log.d(TAG, "Update option selected for student ID: " + etudiant.getId());
            showUpdateDialog(etudiant);
        });

        builder.setNegativeButton("Supprimer", (dialog, which) -> {
            Log.d(TAG, "Delete option selected for student ID: " + etudiant.getId());
            showDeleteConfirmationDialog(etudiant);
        });

        builder.show();
    }

    private void showUpdateDialog(final Etudiant etudiant) {
        currentEtudiant = etudiant;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier Étudiant");
        Button uploadImageButton = new Button(this);
        uploadImageButton.setText("Choisir une image");
        uploadImageButton.setOnClickListener(v -> showImageSelectionDialog());

        // Add Image Preview
        ImageView imagePreview = new ImageView(this);
        imagePreview.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        if (etudiant.getImagePath() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(etudiant.getImagePath());
            if (bitmap != null) {
                imagePreview.setImageBitmap(bitmap);
            }
        }
        // Create the dialog content programmatically
        final EditText inputNom = new EditText(this);
        inputNom.setHint("Nom");
        inputNom.setText(etudiant.getNom());

        final EditText inputPrenom = new EditText(this);
        inputPrenom.setHint("Prénom");
        inputPrenom.setText(etudiant.getPrenom());

        // Date picker setup
        final TextView dateDisplay = new TextView(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateDisplay.setText(dateFormat.format(etudiant.getDateNaiss()));
        dateDisplay.setPadding(0, 20, 0, 20);

        Button datePickerButton = new Button(this);
        datePickerButton.setText("Choisir la date");
        datePickerButton.setOnClickListener(v -> showDatePicker(dateDisplay));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(inputNom);
        layout.addView(inputPrenom);
        layout.addView(dateDisplay);
        layout.addView(datePickerButton);
        layout.addView(uploadImageButton);
        layout.addView(imagePreview);
        builder.setView(layout);

        builder.setPositiveButton("Mettre à jour", (dialog, which) -> {
            String newNom = inputNom.getText().toString();
            String newPrenom = inputPrenom.getText().toString();
            String newDateStr = dateDisplay.getText().toString();

            if (!newNom.isEmpty() && !newPrenom.isEmpty() && !newDateStr.isEmpty()) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    etudiant.setNom(newNom);
                    etudiant.setPrenom(newPrenom);
                    etudiant.setDateNaiss(format.parse(newDateStr));

                    // Update the student in the database
                    es.update(etudiant);

                    // Full refresh of the list
                    refreshStudentList();

                    Toast.makeText(ListActivity.this, "Étudiant mis à jour!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error updating student: " + e.getMessage());
                    Toast.makeText(ListActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ListActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDatePicker(final TextView dateDisplay) {
        final var calendar = Calendar.getInstance();
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            calendar.setTime(format.parse(dateDisplay.getText().toString()));
        } catch (Exception e) {
            calendar.setTime(new Date());
        }

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    dateDisplay.setText(format.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    // Delete confirmation dialog
    private void showDeleteConfirmationDialog(final Etudiant etudiant) {
        new AlertDialog.Builder(ListActivity.this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer " + etudiant.getNom() + " " + etudiant.getPrenom() + "?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    Log.d(TAG, "Deleting student ID: " + etudiant.getId());
                    deleteEtudiant(etudiant);
                })
                .setNegativeButton("Non", (dialog, which) -> {
                    Log.d(TAG, "Delete cancelled for student ID: " + etudiant.getId());
                    dialog.dismiss();
                })
                .show();
    }



    // Helper method to refresh the list
    private void refreshStudentList() {
        etudiants = es.findAll();
        logEtudiantsData(etudiants);
        adapter.clear();
        adapter.addAll(etudiants);
        adapter.notifyDataSetChanged();
    }

    private void deleteEtudiant(Etudiant etudiant) {
        // Delete the student from the database
        es.delete(etudiant);
        refreshStudentList();
        // Remove the student from the list and notify the adapter
        etudiants.remove(etudiant);
        adapter.notifyDataSetChanged();

        // Log the remaining students
        logEtudiantsData(etudiants);

        Toast.makeText(ListActivity.this, "Étudiant supprimé!", Toast.LENGTH_SHORT).show();
    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir une image");
        builder.setItems(new CharSequence[]{"Prendre une photo", "Choisir depuis la galerie"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            try {
                                dispatchTakePictureIntent();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case 1:
                            dispatchPickPictureIntent();
                            break;
                    }
                });
        builder.show();
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.sqlite.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && currentEtudiant != null) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (currentImagePath != null) {
                        currentEtudiant.setImagePath(currentImagePath);
                        adapter.notifyDataSetChanged();
                    }
                    break;

                case REQUEST_IMAGE_PICK:
                    if (data != null && data.getData() != null) {
                        Uri selectedImage = data.getData();
                        try {
                            // Use ContentResolver to safely access the image
                            InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                                // Save to internal storage and get new path
                                currentImagePath = saveImageToInternalStorage(bitmap);
                                currentEtudiant.setImagePath(currentImagePath);
                                adapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }
    private String saveImageToInternalStorage(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);

        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }}

}
