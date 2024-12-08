package com.example.testmapboxkotlin.viewModel;
import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.testmapboxkotlin.model.Reportes;
import com.example.testmapboxkotlin.view.MainActivity;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.firestore.EventListener;



public class ReporteViewModel extends ViewModel {

    private static final ArrayList<Reportes> listaReportes = new ArrayList<>();

    private static final ArrayList<Reportes> listaFavoritos = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference ReportCollection = firestore.collection("report-collection");

    private final MutableLiveData<List<Reportes>> listaReportesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Reportes>> listaFavoritosLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteReportSuccess = new MutableLiveData<>();
    StorageReference audioRef;
    private final CollectionReference UserCollection = firestore.collection("roles");


    public LiveData<Boolean> getDeleteReportSuccess() {
        return deleteReportSuccess;
    }
    private final FirebaseStorage storage = FirebaseStorage.getInstance(); // Instancia de Firebase Storage

    public LiveData<List<Reportes>> getListaFavoritos() {return listaFavoritosLiveData;
    }

    public LiveData<List<Reportes>> getListaReportes() {return listaReportesLiveData;
    }
    private MutableLiveData<String> rolUsuario = new MutableLiveData<>();

    public void GetUserRol(String userUid) {
        DocumentReference docRef = UserCollection.document(userUid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String rol = document.getString("rol");
                    rolUsuario.setValue(rol);
                }
            }
        });
    }

    public LiveData<String> getRolUsuario() {
        return rolUsuario;
    }
    public void EditReport(String id,String Tipo,String desc) {
        for (Reportes r : listaReportes) {
            if (r.getId().equals(id)) {  // Usar equals() para comparar cadenas de texto
                // Usar update() para actualizar solo los campos que cambian
                Map<String, Object> updates = new HashMap<>();
                updates.put("tipo", Tipo);
                updates.put("desc", desc);// Agregar los campos que deseas actualizar

                // Actualizar el documento solo con los campos que cambian
                ReportCollection.document(id)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            // Este bloque se ejecuta si la operación es exitosa
                            System.out.println("Reporte actualizado exitosamente!");
                        })
                        .addOnFailureListener(e -> {
                            // Este bloque se ejecuta si ocurre un error
                            System.err.println("Error al actualizar el reporte: " + e.getMessage());
                        });
            }
        }
    }

    public void deleteReport(String reportId) {
        List<Reportes> currentList = listaReportesLiveData.getValue();
        if (currentList != null) {
            currentList.removeIf(report -> report.getId().equals(reportId));
            listaReportesLiveData.setValue(currentList); // Notificar a la UI con la nueva lista
        }
        ReportCollection.document(reportId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Reporte eliminado con éxito");
                    deleteReportSuccess.setValue(true);


                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al eliminar el reporte", e);
                });
    }
    public void addReport(String Tipo, Date fecha, String autor, String lat, String log, Boolean denunciado, Uri imagen_uri, Long horasVida, String desc, Uri audio_uri) {
        String reportId = "" + Math.random() * 10;
        String imageName = "imagenes/" + reportId + ".jpg";
        String audioName = "audios/" + reportId;

        StorageReference audioRef = storage.getReference().child(audioName);
        StorageReference imageRef = storage.getReference().child(imageName);

        // Subir imagen si existe
        if (imagen_uri != null) {
            imageRef.putFile(imagen_uri).addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(url -> {
                    handleAudioUpload(audio_uri, url.toString(), reportId, Tipo, fecha, autor, lat, log, denunciado, horasVida, desc);
                }).addOnFailureListener(e -> {
                    handleAudioUpload(audio_uri, null, reportId, Tipo, fecha, autor, lat, log, denunciado, horasVida, desc);
                });
            }).addOnFailureListener(e -> {
                handleAudioUpload(audio_uri, null, reportId, Tipo, fecha, autor, lat, log, denunciado, horasVida, desc);
            });
        } else {
            // Si no hay imagen, procedemos directamente con el audio
            String placeholderImageUrl = "https://developers.elementor.com/docs/assets/img/elementor-placeholder-image.png";
            handleAudioUpload(audio_uri, placeholderImageUrl, reportId, Tipo, fecha, autor, lat, log, denunciado, horasVida, desc);
        }
    }

    private void handleAudioUpload(Uri audio_uri, String imageUrl, String reportId, String Tipo, Date fecha, String autor, String lat, String log, Boolean denunciado, Long horasVida, String desc) {
        StorageReference audioRef = storage.getReference().child("audios/" + reportId);

        if (audio_uri != null) {
            audioRef.putFile(audio_uri).addOnSuccessListener(taskSnapshot1 -> {
                audioRef.getDownloadUrl().addOnSuccessListener(urlaudio -> {
                    String audioUrl = urlaudio.toString();
                    createReport(reportId, Tipo, fecha, autor, lat, log, denunciado, imageUrl, horasVida.intValue(), desc, audioUrl);
                }).addOnFailureListener(e -> {
                    createReport(reportId, Tipo, fecha, autor, lat, log, denunciado, imageUrl, horasVida.intValue(), desc, null);
                });
            }).addOnFailureListener(e -> {
                createReport(reportId, Tipo, fecha, autor, lat, log, denunciado, imageUrl, horasVida.intValue(), desc, null);
            });
        } else {
            // Si no hay audio, asignar el mensaje por defecto
            createReport(reportId, Tipo, fecha, autor, lat, log, denunciado, imageUrl, horasVida.intValue(), desc, null);
        }
    }

    private void createReport(String reportId, String Tipo, Date fecha, String autor, String lat, String log, Boolean denunciado, String imageUrl, int horasVida, String desc, String audioUrl) {
        Reportes report = new Reportes(reportId, Tipo, fecha, autor, lat, log, denunciado, imageUrl, horasVida, desc, audioUrl);
        ReportCollection.document(report.getId())
                .set(report)
                .addOnSuccessListener(task -> {
                    Log.d("Report", "Report successfully created!");
                }).addOnFailureListener(e -> {
                    Log.e("Report", "Failed to save report", e);
                });
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
                        String desc = document.getString("desc");
                        String audio_url=document.getString("audioUrl");
                        Reportes report = new Reportes(id, tipo, fecha, autor, lat, log, denunciado,image_url,tiempoDeVida,desc,audio_url);
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

    public void getAllReportFavorites(String users) {
        CollectionReference ReportFavorites = firestore.collection("users")
                .document(users)  // Usamos el correo del usuario como identificador
                .collection("favorites");
        ReportFavorites.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                if (snapshots != null && !snapshots.isEmpty()) {
                    listaFavoritos.clear();
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
                        String desc = document.getString("desc");
                        String audio_url=document.getString("audioUrl");
                        Reportes report = new Reportes(id, tipo, fecha, autor, lat, log, denunciado,image_url,tiempoDeVida,desc,audio_url);
                        if (report != null) {
                            listaFavoritos.add(report);
                            listaFavoritosLiveData.setValue(listaFavoritos);
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
