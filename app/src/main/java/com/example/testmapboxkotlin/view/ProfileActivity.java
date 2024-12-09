package com.example.testmapboxkotlin.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.testmapboxkotlin.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference TokenCollection = firestore.collection("Tokens");
    private final CollectionReference UserCollection = firestore.collection("roles");
    private final CollectionReference VentasCollection = firestore.collection("registro-ventas");
    private final CollectionReference TotalVentasCollection = firestore.collection("ventas-totales");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        // Desloguearse (cerrar sesión)
        setContentView(R.layout.activity_profile);
        Bundle bundle = getIntent().getExtras();
        String userName = bundle.getString("USER_NAME");
        String userEmail = bundle.getString("USER_EMAIL");
        String userUid = bundle.getString("USER_UID");
        TextView nameTv = findViewById(R.id.tw_user);
        TextView correoTv = findViewById(R.id.tw_correo);
        Button volver_btn = findViewById(R.id.btn_volver);
        Button signout_btn = findViewById(R.id.btn_signout);
        Button basico_btn = findViewById(R.id.btn_buyBasic);
        Button premium_btn = findViewById(R.id.btn_buyPremium);
        Timestamp fecha = new Timestamp(Instant.now());
        Timestamp fecha_exp = new Timestamp(Instant.now().plus(30, ChronoUnit.DAYS));

        //Roles hechos, falta implementar un registro de la compra con fecha y una función TTL
         UserCollection.document(userUid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String rol_user = documentSnapshot.getString("rol");
                            Log.d("rol",rol_user);
                            if ( rol_user.equals("Premium")){
                                premium_btn.setText("Comprado");
                                premium_btn.setAlpha(0.98f);
                                premium_btn.setEnabled(false);

                            }
                            else if (rol_user.equals("Basico")){
                                basico_btn.setText("Comprado");
                                basico_btn.setAlpha(0.98f);
                                basico_btn.setEnabled(false);
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("UserRole", "Error al obtener el documento: " + e.getMessage());
                    }
                });


        premium_btn.setOnClickListener(view->{
            VentanaCompraExitosa();
            Map<String, Object> userRole = new HashMap<>();
            userRole.put("rol","Premium");
            userRole.put("fecha_exp", fecha_exp);

            Map<String, Object> VentaReg = new HashMap<>();
            VentaReg.put("comprador",userUid);
            VentaReg.put("fecha_compra", fecha);
            VentaReg.put("fecha_exp", fecha_exp);

            VentaReg.put("precio",7990);
            Double precio = Double.parseDouble(VentaReg.get("precio").toString());
            VentasCollection.document().set(VentaReg);
            TotalVentasCollection.document("Ingresos").update("ingresos-premium",FieldValue.increment(precio));
            UserCollection.document(userUid).set(userRole);
            premium_btn.setText("Comprado");
            premium_btn.setAlpha(0.98f);
            premium_btn.setEnabled(false);
        });

        basico_btn.setOnClickListener(view->{
            VentanaCompraExitosa();
            Map<String, Object> userRole = new HashMap<>();
            userRole.put("rol","Basico");
            userRole.put("fecha_exp", fecha_exp);

            Map<String, Object> VentaReg = new HashMap<>();
            VentaReg.put("comprador",userUid);
            VentaReg.put("fecha_compra", fecha);
            VentaReg.put("fecha_exp", fecha_exp);
            VentaReg.put("precio",4990);

            Double precio = Double.parseDouble(VentaReg.get("precio").toString());
            TotalVentasCollection.document("Ingresos").update("ingresos-basicos",FieldValue.increment(precio));

            VentasCollection.document().set(VentaReg);
            UserCollection.document(userUid).set(userRole);
            basico_btn.setText("Comprado");
            basico_btn.setAlpha(0.98f);
            basico_btn.setEnabled(false);
        });

        signout_btn.setOnClickListener(view->{
            // Desloguearse de Google
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Después de que la sesión de Google esté cerrada
                Log.d("SignOut", "User is signed out of Google.");
            });
            mAuth.signOut();
            TokenCollection.document(userUid).delete();
            Intent i= new Intent(ProfileActivity.this, AuthActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_reverse, R.anim.slide_out_reverse);
        });
        volver_btn.setOnClickListener(view->{
            Intent i= new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_reverse, R.anim.slide_out_reverse);
        });
        //Se actualiza la vista con datos del usuario
        correoTv.setText(userEmail);
        nameTv.setText(userName);
        String imageUriString = getIntent().getStringExtra("USER_IMAGE_URI");

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            // Usar la URI en un ImageView
            ImageView imageView = findViewById(R.id.imgvw_user);
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.avatar2) // Placeholder
                    .error(R.drawable.avatar2) // Imagen de error
                    .into(imageView);
        } else {
            // Imagen por defecto si no se pasó ninguna URI
            ImageView imageView = findViewById(R.id.imgvw_user);
            imageView.setImageResource(R.drawable.avatar2);
        }

    }
    private void VentanaCompraExitosa(){
        new AlertDialog.Builder(this)
                .setTitle("Compra exitosa")
                .setMessage("¡Tu compra ha sido exitosa!")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
