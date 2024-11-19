package com.example.testmapboxkotlin.viewModel;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
import java.util.List;

import com.google.firebase.firestore.EventListener;



public class ReporteViewModel extends ViewModel {
    private static ArrayList<Reportes> listaReportes = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference ReportCollection = firestore.collection("report-collection");
    private final MutableLiveData<List<Reportes>> listaReportesLiveData = new MutableLiveData<>();



// ...

    public LiveData<List<Reportes>> getListaReportes() {
        return listaReportesLiveData;
    }
    public void addReport(String Tipo, String fecha, String lat, String log) {

        // Crea un nuevo objeto Modelo y lo agrega a Firebase
        Reportes report = new Reportes(""+Math.random()*10,Tipo, DateTime.getDefaultInstance().toString(),"autor",lat,log);
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
                        String fecha = document.getString("fecha");
                        String tipo = document.getString("tipo");
                        String lat = document.getString("lat");
                        String log = document.getString("log");
                        Reportes report = new Reportes(id,tipo,fecha,autor,lat,log);
                        if (report != null) {
                            listaReportes.add(report);
                            Log.d("TAG", "No haydfsfsdfsdfonibles");
                        }
                        else{
                            Log.d("asdf","sad");
                        }
                    }
                    listaReportesLiveData.postValue(listaReportes);
                    Log.d("TAG", "Documentos obtenidos: " + listaReportes.toString());
                } else {
                    Log.d("TAG", "No hay datos disponibles");
                }
            }
        });
    }


}
