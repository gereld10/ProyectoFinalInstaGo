package com.example.instago;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {
    private EditText txtEmail, txtPassword;
    private Button btnRegistrar;


    private ProgressDialog cargando;

    private String email = "";
    private String password = "";

    private Toolbar toolbar1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        txtEmail = findViewById(R.id.txtemail);
        txtPassword = findViewById(R.id.txtpassword);

        btnRegistrar = findViewById(R.id.btnguardar);
        toolbar1 = (Toolbar)findViewById(R.id.toolbar_setup);
        setSupportActionBar(toolbar1);
        getSupportActionBar().setTitle("Registrate");

        cargando = new ProgressDialog(this);



        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = txtEmail.getText().toString();
                password = txtPassword.getText().toString();


                    if (!email.isEmpty() && !password.isEmpty() ){

                        if (password.length() >= 6)
                        {
                            registrarUser();

                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this,"La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show();
                        }


                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,"Debe completar los campos", Toast.LENGTH_LONG).show();
                    }






            }
        });







    }//***********************ONCREATE***********



    private void registrarUser() {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                cargando.setTitle("Subiendo foto...");
                cargando.setTitle("Espere por favor...");
                cargando.show();

                if (task.isSuccessful()){



                    String email = txtEmail.getText().toString();
                    String pass = txtPassword.getText().toString();
                    String id = mAuth.getCurrentUser().getUid();



                    UsuariosLogin user = new UsuariosLogin(pass, email, id );


                    //creando nodo


                    mDatabase.child("Users").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {

                            if(task2.isSuccessful()){
                                startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this, "No se pudieron crear los datos correctamente", Toast.LENGTH_SHORT).show();
                            }



                        }
                    });

                }
                else
                {
                    Toast.makeText(LoginActivity.this, "No se pudo registrar este usuario", Toast.LENGTH_SHORT).show();
                    cargando.dismiss();
                }


            }
        });

        cargando.dismiss();

    }


}