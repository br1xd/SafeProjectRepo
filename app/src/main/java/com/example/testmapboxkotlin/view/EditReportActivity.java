package com.example.testmapboxkotlin.view;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.testmapboxkotlin.R;
import com.example.testmapboxkotlin.viewModel.ReporteViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.List;

public class EditReportActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    private Uri cameraImageUri;
    private ImageView imageView;
    private String tipoSeleccionado;

    private FusedLocationProviderClient fusedLocationClient;

    ReporteViewModel ViewModelRep = new ReporteViewModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_record);

        Spinner spinnerCategorias = findViewById(R.id.spinnerCategoriasEdit);

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


        Bundle bundle = getIntent().getExtras();
        String reportId = bundle.getString("reporteId");

        EditText tvDesc = findViewById(R.id.edtv_desc);



        submit_btn.setOnClickListener(view -> {
            String desc = tvDesc.getText().toString();
            Log.d("desc",desc);
            ViewModelRep.EditReport(reportId,tipoSeleccionado,desc);
            Toast.makeText(this,"Modificando reporte, espere un momento...",Toast.LENGTH_SHORT);

        });
        back_btn.setOnClickListener(view -> {
            Intent i= new Intent(EditReportActivity.this, MainActivity.class);
            startActivity(i);
        });
        ViewModelRep.getAllReport();


    }







}