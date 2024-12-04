package com.example.testmapboxkotlin.view;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
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
        TextView nameTv = findViewById(R.id.tw_user);
        TextView correoTv = findViewById(R.id.tw_correo);
        Button volver_btn = findViewById(R.id.btn_volver);
        Button signout_btn = findViewById(R.id.btn_signout);

        signout_btn.setOnClickListener(view->{
            // Desloguearse de Google
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Después de que la sesión de Google esté cerrada
                Log.d("SignOut", "User is signed out of Google.");
            });
            mAuth.signOut();
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
}
