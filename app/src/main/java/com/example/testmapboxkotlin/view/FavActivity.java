package com.example.testmapboxkotlin.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testmapboxkotlin.R;
import com.example.testmapboxkotlin.model.Reportes;
import com.example.testmapboxkotlin.viewModel.ReporteViewModel;

import java.lang.reflect.Array;
import java.util.List;



public class FavActivity extends AppCompatActivity {


    private RecyclerView recyclerViewFavoritos;
    private ReportAdapter reportAdapter;

    private ReporteViewModel ReporteVM = new ReporteViewModel();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);

        // Obtener el correo del usuario desde el Intent
        Bundle bundle = getIntent().getExtras();
        String userEmail = bundle.getString("userEmail");

        recyclerViewFavoritos = findViewById(R.id.recyclerViewFavoritos);
        recyclerViewFavoritos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));




        // Usar el correo recibido para hacer lo que necesites, por ejemplo, buscar los reportes favoritos
        if (userEmail != null) {
            Log.d("Funciona",userEmail);
            ReporteVM.getAllReportFavorites(userEmail);

            // Aquí puedes hacer la consulta a Firebase para obtener los reportes favoritos del usuario
            // por ejemplo, con userEmail puedes hacer la consulta a la colección de favoritos del usuario
        }

        ReporteVM.getListaFavoritos().observe(this, new Observer<List<Reportes>>() {
            @Override
            public void onChanged(List<Reportes> reportes) {
                if (reportes != null && !reportes.isEmpty()) {
                    // Configuramos el Adapter con la lista de reportes
                    reportAdapter = new ReportAdapter(reportes);
                    recyclerViewFavoritos.setAdapter(reportAdapter);
                }
            }
        });



    }
}