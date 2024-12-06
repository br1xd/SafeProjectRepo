package com.example.testmapboxkotlin.view;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.testmapboxkotlin.R;
import com.example.testmapboxkotlin.viewModel.ReporteViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddReportActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    private Uri cameraImageUri;
    private ImageView imageView;
    private String tipoSeleccionado;
    private Integer minutoSeleccionado;

    private FusedLocationProviderClient fusedLocationClient;

    ReporteViewModel ViewModelRep = new ReporteViewModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        setContentView(R.layout.activity_add_record);

        List<Integer> minutos = new ArrayList<>();
        minutos.add(15);
        minutos.add(30);
        minutos.add(45);
        minutos.add(60);

        Spinner spinnerMinutos = findViewById(R.id.spinnerTiempoVida);
        ArrayAdapter<Integer> adapterMinutos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, minutos);
        adapterMinutos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  // Estilo de la lista desplegable

        // Asignar el adaptador al Spinner
        spinnerMinutos.setAdapter(adapterMinutos);

        // Establecer un listener para cuando el usuario seleccione una categoría
        spinnerMinutos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener la categoría seleccionada
                minutoSeleccionado = minutos.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
        Spinner spinnerCategorias = findViewById(R.id.spinnerCategorias2);


        List<String> categorias = new ArrayList<>();
        categorias.add("Asalto");
        categorias.add("Carterista");
        categorias.add("Asesinato");
        categorias.add("Sospechoso");

        // Crear un adaptador para el Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  // Estilo de la lista desplegable

        // Asignar el adaptador al Spinner
        spinnerCategorias.setAdapter(adapter);

        // Establecer un listener para cuando el usuario seleccione una categoría
        spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener la categoría seleccionada
                tipoSeleccionado = categorias.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        Button submit_btn = findViewById(R.id.edit_btn);
        Button back_btn = findViewById(R.id.back_btn);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        EditText descTv = findViewById(R.id.Tv_desc);
        imageView = findViewById(R.id.imageView);

        btnSelectImage.setOnClickListener(v -> openCamera());

        Bundle bundle = getIntent().getExtras();
        double log = bundle.getDouble("long");
        double lat = bundle.getDouble("lat");
        String autor = bundle.getString("userEmail");
        Log.d("autor",autor);

        submit_btn.setOnClickListener(view -> {

            Date fecha = new Date();
            String horasVidaString = minutoSeleccionado.toString();
            String descString = descTv.getText().toString();
            Long horasVida = 0L;
            if (!horasVidaString.isEmpty()) {
                horasVida = Long.parseLong(horasVidaString);
            }
            ViewModelRep.addReport(tipoSeleccionado,fecha,autor,""+lat,""+log,Boolean.FALSE,cameraImageUri,horasVida,descString);
            Toast.makeText(this, "Agregando reporte, espere un momento...", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                Intent i = new Intent(AddReportActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }, 3000);


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
