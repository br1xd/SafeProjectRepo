package com.example.testmapboxkotlin.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.testmapboxkotlin.model.Comunas;
import com.example.testmapboxkotlin.model.Reportes;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComunaViewModel extends ViewModel {
    private static final ArrayList<Comunas> listaComunas = new ArrayList<>();

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference ComunaCollection = firestore.collection("comunas");

    private final MutableLiveData<List<Comunas>> listaComunasLiveData = new MutableLiveData<>();


    public LiveData<List<Comunas>> getListaComunas() {return listaComunasLiveData;
    }
    public void getAllReport() {
        ComunaCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                if (snapshots != null && !snapshots.isEmpty()) {
                    listaComunas.clear();

                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        //Comunases report = document.toObject(Reportes.class); probar luego  mapeo automatico
                        String id = document.getId();
                        Integer año = document.getLong("Año").intValue();
                        Integer tasa = document.getLong("Tasa crimenes").intValue();
                        Comunas comuna = new Comunas(id,año,tasa);
                        Log.d("comuna",comuna.getIdComuna());
                        Log.d("comuna",""+comuna.getTasaCrimen());
                        if (comuna!= null) {
                            listaComunas.add(comuna);
                            listaComunasLiveData.setValue(listaComunas);
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
