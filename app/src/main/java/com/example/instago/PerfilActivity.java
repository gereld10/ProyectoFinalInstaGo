package com.example.instago;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilActivity extends AppCompatActivity {

    private String usuario_revid, usuario_enviarid, CurrenEstado;
    private TextView usuarionom, usuariociu, usuarioest;
    private CircleImageView usuarioima;
    private Button enviarmensaje, cancelarmensaje;
    private DatabaseReference UserRef, SolicitudRef, ContactosRef, NotificacionesRef;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        usuario_revid = getIntent().getExtras().get("usuario_id").toString();
        auth = FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ContactosRef= FirebaseDatabase.getInstance().getReference().child("Contactos");
        SolicitudRef=FirebaseDatabase.getInstance().getReference().child("Solicitudes");
        NotificacionesRef=FirebaseDatabase.getInstance().getReference().child("Notificaciones");
        usuario_enviarid = auth.getCurrentUser().getUid();
        usuarionom = (TextView)findViewById(R.id.usuario_vic_nombre);
        usuariociu = (TextView)findViewById(R.id.usuario_vic_ciudad);
        usuarioest = (TextView)findViewById(R.id.usuario_vic_estado);
        usuarioima = (CircleImageView) findViewById(R.id.usuario_vic_imagen);
        enviarmensaje = (Button) findViewById(R.id.enviar_mensaje_usuario_vic);
        cancelarmensaje=(Button) findViewById(R.id.cancelar_mensaje_usuario_vic);
        CurrenEstado = "nueva";
        ObtenerInfromacionDB();
    }

    private void ObtenerInfromacionDB() {
        UserRef.child(usuario_revid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("imagen"))){
                    String nombreUser = snapshot.child("nombre").getValue().toString();
                    String ciudadUser = snapshot.child("ciudad").getValue().toString();
                    String estadoUser = snapshot.child("estado").getValue().toString();
                    String imagenUser = snapshot.child("imagen").getValue().toString();
                    Picasso.get().load(imagenUser).placeholder(R.drawable.error).into(usuarioima);
                    usuarionom.setText(nombreUser);
                    usuariociu.setText(ciudadUser);
                    usuarioest.setText(estadoUser);
                    MotorEnviarReuqeriemiento();
                }else{
                    String nombreUser = snapshot.child("nombre").getValue().toString();
                    String ciudadUser = snapshot.child("ciudad").getValue().toString();
                    String estadoUser = snapshot.child("estado").getValue().toString();
                    usuarionom.setText(nombreUser);
                    usuariociu.setText(ciudadUser);
                    usuarioest.setText(estadoUser);
                    MotorEnviarReuqeriemiento();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }});
    }

    private void MotorEnviarReuqeriemiento() {
        SolicitudRef.child(usuario_enviarid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(usuario_revid)){
                    String requerimiento = snapshot.child(usuario_revid).child("tipo").getValue().toString();
                    if (requerimiento.equals("enviado")){
                        CurrenEstado = "enviada";
                        enviarmensaje.setText("CANCELAR SOLICITUD");
                    } else  if (requerimiento.equals("recibido")){
                        CurrenEstado = "recibida";
                        enviarmensaje.setText("ACEPTAR SOLICITUD");
                        cancelarmensaje.setVisibility(View.VISIBLE);
                        cancelarmensaje.setEnabled(true);
                        cancelarmensaje.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelarSolicitudMensaje();
                            }
                        });
                    }
                }else {
                    ContactosRef.child(usuario_enviarid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(usuario_revid)){
                                CurrenEstado="amigos";
                                enviarmensaje.setText("ELIMINAR CONTACTO");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!usuario_enviarid.equals(usuario_revid)){
            enviarmensaje.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enviarmensaje.setEnabled(false);
                    if (CurrenEstado.equals("nueva")){
                        EnviarSolicitudMensaje();
                    }
                    if (CurrenEstado.equals("enviada")){
                        CancelarSolicitudMensaje();
                    }
                    if (CurrenEstado.equals("recibida")){
                        AceptadaSolicitudMensaje();
                    }
                    if (CurrenEstado.equals("amigos")){
                        EliminarContacto();
                    }
                }
            });
        }else {
            enviarmensaje.setVisibility(View.GONE);
        }

    }

    private void EliminarContacto() {
        ContactosRef.child(usuario_enviarid).child(usuario_revid)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    ContactosRef.child(usuario_revid).child(usuario_enviarid)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                enviarmensaje.setEnabled(true);
                                CurrenEstado="nueva";
                                enviarmensaje.setText("ENVIAR MENSAJE");
                                cancelarmensaje.setVisibility(View.GONE);
                                cancelarmensaje.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }


    private void AceptadaSolicitudMensaje() {
        ContactosRef.child(usuario_enviarid).child(usuario_revid).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    ContactosRef.child(usuario_revid).child(usuario_enviarid).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                SolicitudRef.child(usuario_enviarid).child(usuario_revid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            SolicitudRef.child(usuario_revid).child(usuario_enviarid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    enviarmensaje.setEnabled(true);
                                                    CurrenEstado="amigos";
                                                    enviarmensaje.setText("ELIMINAR CONTACTO");

                                                    cancelarmensaje.setVisibility(View.GONE);
                                                    cancelarmensaje.setEnabled(false);

                                                }
                                            });
                                        }

                                    }
                                });
                            }

                        }
                    });
                }
            }
        });

    }

    private void CancelarSolicitudMensaje() {

        SolicitudRef.child(usuario_enviarid).child(usuario_revid)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    SolicitudRef.child(usuario_revid).child(usuario_enviarid)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                enviarmensaje.setEnabled(true);
                                CurrenEstado="nueva";
                                enviarmensaje.setText("ENVIAR MENSAJE");

                                cancelarmensaje.setVisibility(View.GONE);
                                cancelarmensaje.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void EnviarSolicitudMensaje() {
        SolicitudRef.child(usuario_enviarid).child(usuario_revid)
                .child("tipo").setValue("enviado")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()){
                           SolicitudRef.child(usuario_revid).child(usuario_enviarid)
                                   .child("tipo").setValue("recibido")
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){

                                               HashMap<String, String> chatNoficicacion = new HashMap<>();
                                               chatNoficicacion.put("de", usuario_revid);
                                               chatNoficicacion.put("tipo","requerimiento");

                                               NotificacionesRef.child(usuario_enviarid).push().setValue(chatNoficicacion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                       if (task.isSuccessful()){

                                                           enviarmensaje.setEnabled(true);
                                                           CurrenEstado="vieja";
                                                           enviarmensaje.setText("Cancelar Requerimiento");

                                                       }
                                                   }
                                               });



                                           }
                                       }
                                   });
                       }
                    }
                });
    }
}