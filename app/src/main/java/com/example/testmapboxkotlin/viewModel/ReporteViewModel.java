package com.example.testmapboxkotlin.viewModel;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.firebase.firestore.EventListener;



public class ReporteViewModel extends ViewModel {

    private static final ArrayList<Reportes> listaReportes = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference ReportCollection = firestore.collection("report-collection");
    private final MutableLiveData<List<Reportes>> listaReportesLiveData = new MutableLiveData<>();
    private final FirebaseStorage storage = FirebaseStorage.getInstance(); // Instancia de Firebase Storage



// ...

    public LiveData<List<Reportes>> getListaReportes() {
        return listaReportesLiveData;
    }
    public void addReport(String Tipo, Date fecha, String lat, String log, Boolean denunciado, Uri imagen_uri,Long horasVida) {
        String reportId = ""+Math.random()*10;
        String imageName = "imagenes/" + reportId + ".jpg";
        StorageReference imageRef = storage.getReference().child(imageName);
        imageRef.putFile(imagen_uri).addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(url -> {
                String imageUrl = url.toString();
                Log.d("TAG IMAGE URL",imageUrl);
                Reportes report = new Reportes(reportId,Tipo, fecha,"autor",lat,log,denunciado,imageUrl,horasVida.intValue());
                ReportCollection.
                        document(report.getId()).
                        set(report);
            });
        });
        // Crea un nuevo objeto Modelo y lo agrega a Firebase

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
                        //Reportes report = document.toObject(Reportes.class); probar luego  mapeo automatico
                        String id = document.getString("id");
                        String autor = document.getString("autor");
                        Date fecha = document.getDate("fecha");
                        String tipo = document.getString("tipo");
                        String lat = document.getString("lat");
                        String log = document.getString("log");
                        Boolean denunciado = document.getBoolean("denunciado");
                        String image_url = document.getString("image_url"); //los nombres deben ser iguales a los del modelo
                        Integer tiempoDeVida = document.getLong("tiempoDeVida").intValue();
                        Reportes report = new Reportes(id, tipo, fecha, autor, lat, log, denunciado,image_url,tiempoDeVida);
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
