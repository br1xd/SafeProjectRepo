package com.example.testmapboxkotlin.viewModel;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.EventListener;



public class ReporteViewModel extends ViewModel {

    private static final ArrayList<Reportes> listaReportes = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference ReportCollection = firestore.collection("report-collection");
    private final MutableLiveData<List<Reportes>> listaReportesLiveData = new MutableLiveData<>();


// ...

    public LiveData<List<Reportes>> getListaReportes() {
        return listaReportesLiveData;
    }
    public void addReport(String Tipo, String fecha, String lat, String log,Boolean denunciado) {

        // Crea un nuevo objeto Modelo y lo agrega a Firebase
        Reportes report = new Reportes(""+Math.random()*10,Tipo, DateTime.getDefaultInstance().toString(),"autor",lat,log,denunciado);
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
                        Boolean denunciado = document.getBoolean("denunciado");
                        Reportes report = new Reportes(id,tipo,fecha,autor,lat,log,denunciado);
                        if (report != null) {
                            listaReportes.add(report);
                            listaReportesLiveData.setValue(listaReportes);
                            Log.d("TAG", "No haydfsfsdfsdfonibles");
                        }
                        else{
                            Log.d("asdf","sad");
                        }


                    }

                } else {
                    Log.d("TAG", "No hay datos disponibles");
                }
            }
        });
    }


}
