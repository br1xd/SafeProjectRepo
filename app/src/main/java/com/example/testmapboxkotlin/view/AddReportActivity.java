package com.example.testmapboxkotlin.view;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testmapboxkotlin.R;
import com.example.testmapboxkotlin.viewModel.ReporteViewModel;
import com.example.testmapboxkotlin.viewModel.ReportesRepositorio;

public class AddReportActivity extends AppCompatActivity {
    ReporteViewModel ViewModelRep = new ReporteViewModel();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        Button submit_btn = findViewById(R.id.submit_btn);
        Button back_btn = findViewById(R.id.back_btn);
        TextView tipoTv = findViewById(R.id.TvTipo);
        TextView fechaTv = findViewById(R.id.TvFecha);
        submit_btn.setOnClickListener(view -> {
            String tipo= tipoTv.getText().toString();
            String fecha= fechaTv.getText().toString();
            ViewModelRep.addReport(tipo,fecha);
        });
        back_btn.setOnClickListener(view -> {
            Intent i= new Intent(AddReportActivity.this, MainActivity.class);
            startActivity(i);
        });
        ViewModelRep.getAllReport();




    }





}
