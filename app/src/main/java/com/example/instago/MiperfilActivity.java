package com.example.instago;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MiperfilActivity extends AppCompatActivity {

    private EditText nombre, ciudad, genero, edad, estado,Email;
    private Button botonmiperfil;
    private CircleImageView imagenmiperfil;
    private Toolbar toolbar;
    private String CurrentuserID;
    private FirebaseAuth auth;
    private DatabaseReference RootRef;
    final  static  int Gallery_PICK =1;
    private ProgressDialog dialog;
    private StorageReference UserProfileImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miperfil);

        Componentes();
        dialog = new ProgressDialog(this);
        botonmiperfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActualizarInformacion();
            }
        });
        UserProfileImagen= FirebaseStorage.getInstance().getReference().child("ImagesPerfil");
        RootRef= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        CurrentuserID=auth.getCurrentUser().getUid();

        RootRef.child("Usuarios").child(CurrentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.hasChild("imagen")) {
                    String nom1 = snapshot.child("nombre").getValue().toString();
                    String ciu1 = snapshot.child("ciudad").getValue().toString();
                    String eda1 = snapshot.child("edad").getValue().toString();
                    String gen1 = snapshot.child("genero").getValue().toString();
                    String est1 = snapshot.child("estado").getValue().toString();
                    String imagen1 = snapshot.child("imagen").getValue().toString();

                    nombre.setText(nom1);
                    ciudad.setText(ciu1);
                    edad.setText(eda1);
                    genero.setText(gen1);
                    estado.setText(est1);

                    Picasso.get()
                            .load(imagen1)
                            .placeholder(R.drawable.welcome)
                            .error(R.drawable.welcome)
                            .into(imagenmiperfil);


                }else if (snapshot.exists()){
                    String nom1 = snapshot.child("nombre").getValue().toString();
                    String ciu1 = snapshot.child("ciudad").getValue().toString();
                    String eda1 = snapshot.child("edad").getValue().toString();
                    String gen1 = snapshot.child("genero").getValue().toString();
                    String est1 = snapshot.child("estado").getValue().toString();
                    nombre.setText(nom1);
                    ciudad.setText(ciu1);
                    edad.setText(eda1);
                    genero.setText(gen1);
                    estado.setText(est1);
                }
            }@Override public void onCancelled(@NonNull DatabaseError error) { }});

        imagenmiperfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);

            }
        });

    }

    private void ActualizarInformacion() {

        String nom = nombre.getText().toString();
        String ciu = ciudad.getText().toString();
        String eda = edad.getText().toString();
        String gen = genero.getText().toString();
        String est = estado.getText().toString();

        if (TextUtils.isEmpty(nom)){
            Toast.makeText(this, "Ingrese su nombre", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(ciu)){
            Toast.makeText(this, "Ingrese su ciudad", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(eda)){
            Toast.makeText(this, "Ingrese su edad", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(gen)){
            Toast.makeText(this, "Ingrese su genero", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(est)){
            Toast.makeText(this, "Ingrese su estado", Toast.LENGTH_SHORT).show();
        }else{
            HashMap profile = new HashMap();
            profile.put("uid",CurrentuserID);
            profile.put("nombre",nom);
            profile.put("ciudad",ciu);
            profile.put("edad",eda);
            profile.put("genero",gen);
            profile.put("estado",est);
            RootRef.child("Usuarios").child(CurrentuserID).updateChildren(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                EnviaralInicio();
                                Toast.makeText(MiperfilActivity.this, "Guardado exitosamente.......", Toast.LENGTH_SHORT).show();
                            }else{
                                String err = task.getException().getMessage().toString();
                                Toast.makeText(MiperfilActivity.this, "Error: "+err, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode ==RESULT_OK){
                dialog.setTitle("Imagen de perfil");
                dialog.setMessage("Estamos guardando su foto de perfil..");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                final Uri resultUri = result.getUri();
                StorageReference filePath = UserProfileImagen.child(CurrentuserID+".jpg");
                final File url = new File(resultUri.getPath());
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MiperfilActivity.this, "Imagen guardada", Toast.LENGTH_SHORT).show();
                            UserProfileImagen.child(CurrentuserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    RootRef.child("Usuarios").child(CurrentuserID).child("imagen").setValue(downloadUri)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Picasso.get()
                                                                .load(downloadUri)
                                                                .error(R.drawable.welcome)
                                                                .into(imagenmiperfil);
                                                        Toast.makeText(MiperfilActivity.this, "Imagen se guardo en la Base de datos", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }else{
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(MiperfilActivity.this, "Imagen no gauardada code:"+error, Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }else{
                Toast.makeText(this, "Imagen no es soportada Intentelo de nuevo", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }
    }


    private void EnviaralInicio() {
        Intent intent = new Intent(MiperfilActivity.this, InicioActivity.class);
        startActivity(intent);
    }

    private void Componentes() {
        nombre=(EditText)findViewById(R.id.nombre_miperfil);
        ciudad=(EditText)findViewById(R.id.ciudad_miperfil);
        genero=(EditText)findViewById(R.id.genero_miperfil);
        edad=(EditText)findViewById(R.id.edad_miperfil);
        estado=(EditText)findViewById(R.id.estado_miperfil);

        botonmiperfil=(Button) findViewById(R.id.boton_miperfil);
        imagenmiperfil=(CircleImageView) findViewById(R.id.imagen_miperfil);
        toolbar=(Toolbar) findViewById(R.id.toolbar_miperfil);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mi perfil");

    }
}