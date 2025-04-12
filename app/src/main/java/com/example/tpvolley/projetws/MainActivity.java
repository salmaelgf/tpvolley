package com.example.tpvolley.projetws;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.tpvolley.projetws.Etudiant;
import com.example.tpvolley.projetws.EtudiantService;
import com.example.tpvolley.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private EditText nom;
    private EditText prenom;
    private EditText dateNaissEditText;
    private Button add;
    private EditText id;
    private Button rechercher;
    private Button findAll;
    private Button delete;
    private TextView res;
    private ListView listView;
    private EtudiantService es;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private String currentImagePath = "";
    private ImageView imagePreview;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
            };
        } else {
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }
    private boolean allPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, we don't need WRITE_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private DatePickerDialog datePickerDialog;

    void clear() {
        nom.setText("");
        prenom.setText("");
        dateNaissEditText.setText("");
        currentImagePath = "";
        imagePreview.setImageResource(R.drawable.ic_launcher_foreground);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        es = new EtudiantService(this);
        imagePreview = findViewById(R.id.image_preview);
        dateNaissEditText = findViewById(R.id.date_naiss);
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        add = findViewById(R.id.bn);
        id = findViewById(R.id.id);
        rechercher = findViewById(R.id.load);
        findAll = findViewById(R.id.find_all);
        delete = findViewById(R.id.delete);
        res = findViewById(R.id.res);

        Button uploadPhotoBtn = findViewById(R.id.btn_upload_photo);
        uploadPhotoBtn.setOnClickListener(v -> showImageSelectionDialog());
        dateNaissEditText = findViewById(R.id.date_naiss);
        dateNaissEditText.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view1, year1, month1, dayOfMonth) -> {
                        // Set selected date to the EditText field
                        String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        dateNaissEditText.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });

        add.setOnClickListener(v -> {
            String nomText = nom.getText().toString();
            String prenomText = prenom.getText().toString();
            String dateNaissText = dateNaissEditText.getText().toString();

            if (nomText.isEmpty() || prenomText.isEmpty() || dateNaissText.isEmpty()) {
                Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date dateNaiss = dateFormat.parse(dateNaissText);

                // Create student with image path
                Etudiant newEtudiant = new Etudiant(nomText, prenomText, dateNaiss, currentImagePath);
                es.create(newEtudiant);

                clear();
                Toast.makeText(MainActivity.this, "Étudiant ajouté!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Veuillez entrer une date valide (jj/mm/aaaa)", Toast.LENGTH_SHORT).show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if ID field is not empty
                if (id.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // Get the student by ID
                    Etudiant e = es.findById(Integer.parseInt(id.getText().toString()));

                    if (e != null) {
                        // Show confirmation dialog before deleting
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Confirmation")
                                .setMessage("Voulez-vous vraiment supprimer " + e.getNom() + " " + e.getPrenom() + "?")
                                .setPositiveButton("Oui", (dialog, which) -> {
                                    // Delete the student
                                    es.delete(e);
                                    id.setText(""); // Clear the ID field
                                    res.setText(""); // Clear the result field
                                    Toast.makeText(MainActivity.this, "Étudiant supprimé!", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Non", null)
                                .show();
                    } else {
                        Toast.makeText(MainActivity.this, "Aucun étudiant trouvé avec cet ID", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException ex) {
                    Toast.makeText(MainActivity.this, "ID doit être un nombre", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Etudiant e = es.findById(Integer.parseInt(id.getText().toString()));
                if (e != null) {
                    res.setText(e.getNom() + " " + e.getPrenom());
                } else {
                    res.setText("Étudiant introuvable");
                }
            }
        });


        findAll.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        });


    }
    private void dispatchTakePictureIntent() {
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
        pickPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            currentImagePath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    setImagePreview(currentImagePath);
                    break;
                case REQUEST_IMAGE_PICK:
                    if (data != null && data.getData() != null) {
                        Uri selectedImage = data.getData();
                        try {
                            // Use ContentResolver to get a file path
                            InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                                // Save to internal storage and get new path
                                currentImagePath = saveImageToInternalStorage(bitmap);
                                imagePreview.setImageBitmap(bitmap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
        }
    }
    private void setImagePreview(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imagePreview.setImageBitmap(bitmap);
            }
        }
    }
    private void showImageSelectionDialog() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_PERMISSION_CODE
            );
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
            switch (which) {
                case 0: dispatchTakePictureIntent(); break;
                case 1: dispatchPickPictureIntent(); break;
            }
        });
        builder.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                showImageSelectionDialog(); // Retry after getting permissions
            } else {
                Toast.makeText(this,
                        "Permissions are required to use this feature",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }}