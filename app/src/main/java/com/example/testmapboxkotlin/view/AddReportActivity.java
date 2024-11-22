package com.example.testmapboxkotlin.view;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.testmapboxkotlin.R;
import com.example.testmapboxkotlin.viewModel.ReporteViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class AddReportActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;

    ReporteViewModel ViewModelRep = new ReporteViewModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_record);
        Button submit_btn = findViewById(R.id.submit_btn);
        Button back_btn = findViewById(R.id.back_btn);
        TextView tipoTv = findViewById(R.id.TvTipo);
        TextView fechaTv = findViewById(R.id.TvFecha);

        Bundle bundle = getIntent().getExtras();
        double log = bundle.getDouble("long");
        double lat = bundle.getDouble("lat");

        submit_btn.setOnClickListener(view -> {

            String tipo= tipoTv.getText().toString();
            String fecha= fechaTv.getText().toString();
            ViewModelRep.addReport(tipo,fecha,""+lat,""+log);

        });
        back_btn.setOnClickListener(view -> {
            Intent i= new Intent(AddReportActivity.this, MainActivity.class);
            startActivity(i);
        });
        ViewModelRep.getAllReport();


    }

}

// sdjkfkjds hola
