package com.example.testmapboxkotlin.view;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.testmapboxkotlin.R;
import com.example.testmapboxkotlin.viewModel.ReporteViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddReportActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    private Uri cameraImageUri;
    private ImageView imageView;

    private FusedLocationProviderClient fusedLocationClient;

    ReporteViewModel ViewModelRep = new ReporteViewModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        setContentView(R.layout.activity_add_record);
        Button submit_btn = findViewById(R.id.submit_btn);
        Button back_btn = findViewById(R.id.back_btn);
        TextView tipoTv = findViewById(R.id.TvTipo);
        TextView fechaTv = findViewById(R.id.TvFecha);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);

        btnSelectImage.setOnClickListener(v -> openCamera());

        Bundle bundle = getIntent().getExtras();
        double log = bundle.getDouble("long");
        double lat = bundle.getDouble("lat");

        submit_btn.setOnClickListener(view -> {

            String tipo= tipoTv.getText().toString();
            String fecha= fechaTv.getText().toString();
            ViewModelRep.addReport(tipo,fecha,""+lat,""+log,Boolean.FALSE,cameraImageUri);

        });
        back_btn.setOnClickListener(view -> {
            Intent i= new Intent(AddReportActivity.this, MainActivity.class);
            startActivity(i);
        });
        ViewModelRep.getAllReport();


    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Crear un archivo temporal para almacenar la imagen
        File photoFile = createImageFile();
        if (photoFile != null) {
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    "com.example.testmapboxkotlin.fileprovider", // Cambia esto a tu paquete
                    photoFile
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    // Crear archivo temporal para la imagen capturada
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // Usa el URI de la imagen capturada
            imageView.setImageURI(cameraImageUri);
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }





}

// sdjkfkjds hola
