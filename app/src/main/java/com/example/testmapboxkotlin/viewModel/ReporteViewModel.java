package com.example.testmapboxkotlin.viewModel;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.testmapboxkotlin.model.Reportes;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;

import org.w3c.dom.Comment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import com.google.firebase.firestore.EventListener;



public class ReporteViewModel extends ViewModel {
    private static ArrayList<Reportes> listaReportes = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference ReportCollection = firestore.collection("report-collection");
    public static ArrayList<Reportes> findAll(){
        return listaReportes;
    }
// ...

    public void addReport(String Tipo, String fecha) {
        // Crea un nuevo objeto Modelo y lo agrega a Firebase
        Reportes report = new Reportes(""+Math.random()*10,Tipo, DateTime.getDefaultInstance().toString(),"autor");
        ReportCollection.
                document(report.getId()).
                set(report);
        Log.d("PEPEXD",""+listaReportes.size());
    }

    public void getAllReport() {
        ReportCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                if (snapshots != null && !snapshots.isEmpty()) {
                    listaReportes.clear();
                    Log.d("TAG", "No324234");

                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        String id = document.getString("id");
                        String autor = document.getString("autor");
                        String fecha = document.getString("id");
                        String tipo = document.getString("id");
                        Reportes report = new Reportes(id,tipo,fecha,autor);
                        if (report != null) {
                            listaReportes.add(report);
                            Log.d("TAG", "No haydfsfsdfsdfonibles");
                        }
                    }

                    Log.d("TAG", "Documentos obtenidos: " + listaReportes.toString());
                } else {
                    Log.d("TAG", "No hay datos disponibles");
                }
            }
        });
    }


}
