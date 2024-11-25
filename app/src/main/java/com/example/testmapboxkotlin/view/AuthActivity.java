package com.example.testmapboxkotlin.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testmapboxkotlin.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuthActivity extends AppCompatActivity {

    private Button submit_btn;
    private Button changeuser_btn;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final int RC_SIGN_IN = 9001;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ...

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        String web_client_id = getString(R.string.default_web_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(web_client_id) // Asegúrate de tener este ID en strings.xml
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        submit_btn= findViewById(R.id.btn_submit);
        submit_btn.setOnClickListener(view -> {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN); // Define RC_SIGN_IN como un requestCode constante

                }

        );


    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign-in failed", e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        Log.d("GoogleSignIn", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        db.collection("roles").document(user.getUid()).get()
                                .addOnSuccessListener(document -> {
                                    if (!document.exists()) {
                                        // Si no existe, registrar el rol predeterminado
                                        Map<String, Object> userRole = new HashMap<>();
                                        userRole.put("rol", "usuario");

                                        db.collection("roles").document(user.getUid())
                                                .set(userRole)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Auth", "Nuevo usuario registrado con rol 'usuario'.");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Auth", "Error al registrar rol: " + e.getMessage());
                                                });
                                    } else {
                                        Log.d("Auth", "El usuario ya existe con rol: " + document.getString("rol"));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Auth", "Error al verificar usuario: " + e.getMessage());
                                });
                        // Aquí puedes actualizar la UI con los datos del usuario
                        updateUI(user);
                    } else {
                        Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(),"Esta cuenta esta inhabilitada",Toast.LENGTH_SHORT).show();
                        changeuser_btn = findViewById(R.id.btn_changeuser);
                        changeuser_btn.setVisibility(View.VISIBLE);
                        String web_client_id = getString(R.string.default_web_client_id);
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(web_client_id) // Asegúrate de tener este ID en strings.xml
                                .requestEmail()
                                .build();
                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

                        changeuser_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Cerrar sesión en Google
                                googleSignInClient.signOut()
                                        .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // Cerrar sesión en Firebase
                                                mAuth.signOut();
                                                Intent signInIntent = googleSignInClient.getSignInIntent();
                                                startActivityForResult(signInIntent, RC_SIGN_IN);
                                                // Ahora el usuario está desconectado, puedes redirigir a otra actividad

                                            }
                                        });
                            }
                        });
                        updateUI(null);
                    }
                });

    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            Intent i= new Intent(AuthActivity.this, MainActivity.class);
            startActivity(i);
            //String photoUrl = Objects.requireNonNull(user.getPhotoUrl()).toString();
            Log.d("GoogleSignIn", "Name: " + name + ", Email: " + email);

            // Actualiza la interfaz, por ejemplo, muestra los datos del usuario
        } else {
            Log.d("GoogleSignIn", "No user signed in.");
        }
    }





}
