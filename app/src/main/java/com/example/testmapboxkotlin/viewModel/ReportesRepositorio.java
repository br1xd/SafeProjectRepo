package com.example.testmapboxkotlin.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testmapboxkotlin.model.Reportes;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportesRepositorio {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference ReportCollection = firestore.collection("report-collection");
    public LiveData<List<Reportes>> getReports() {
        MutableLiveData<List<Reportes>> reportLiveData = new MutableLiveData<>();
        ReportCollection.addSnapshotListener((querySnapshot, error) -> {
            if (error != null || querySnapshot == null) {
                Log.d("ErrorLivedata","Ha ocurrido un error con Livedata");
                return; // Si ocurre un error, se sale del método.
            }
            List<Reportes> reportes = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Reportes report = doc.toObject(Reportes.class); // Convierte el documento de Firestore a un objeto de clase Report
                reportes.add(report);
            }
            reportLiveData.setValue(reportes); // Actualiza el valor de LiveData para que la vista observe los cambios.
        });
        return reportLiveData;
    }

}

