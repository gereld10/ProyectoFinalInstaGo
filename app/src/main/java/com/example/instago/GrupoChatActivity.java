package com.example.instago;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
//https://boxicons.com/
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.SimpleFormatter;

public class GrupoChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView EnviarMensajeBoton;
    private EditText MensajeUsuario;
    private ScrollView scrollView;
    private TextView verMensajes;
    private String CurrentGrupoNombre, CurrentUserId, CurrentUserName, Fecha, Hora;

    private FirebaseAuth auth;
    private DatabaseReference UserRef, GrupoRef, GrupoMensajeKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo_chat);

        CurrentGrupoNombre= getIntent().getExtras().get("nombregrupo").toString();


        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Usuarios");

        GrupoRef = FirebaseDatabase.getInstance().getReference().child("Grupos").child(CurrentGrupoNombre);


        IniciarObjetos();

        InformacionUsuario();

        EnviarMensajeBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuardarMnesajeDb();
                MensajeUsuario.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();

        GrupoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    MostrarMensajes(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){
                    MostrarMensajes(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void IniciarObjetos() {
        toolbar=(Toolbar) findViewById(R.id.grupo_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(CurrentGrupoNombre);
        EnviarMensajeBoton=(ImageView)findViewById(R.id.enviar_mensaje_grupo);
        MensajeUsuario=(EditText)findViewById(R.id.texto_grupo_chat);
        scrollView=(ScrollView)findViewById(R.id.mi_scroll_view);
        verMensajes=(TextView)findViewById(R.id.grupo_chat_texto);
    }


    private void InformacionUsuario() {
        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    CurrentUserName = snapshot.child("nombre").getValue().toString();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }});
    }
    private void GuardarMnesajeDb() {

        String mensaje = MensajeUsuario.getText().toString();
        String mensajekey = GrupoRef.push().getKey();

        if (TextUtils.isEmpty(mensaje)){
            Toast.makeText(this, "Por favor ingrese su mensaje", Toast.LENGTH_SHORT).show();
        }else{

            Calendar fechacalendar = Calendar.getInstance();
            SimpleDateFormat currentFecha = new SimpleDateFormat("MMM dd, yyyy");
            Fecha= currentFecha.format(fechacalendar.getTime());

            Calendar horacalendar = Calendar.getInstance();
            SimpleDateFormat currentHora = new SimpleDateFormat("hh:mm a");
            Hora = currentHora.format(horacalendar.getTime());

            HashMap<String, Object> mensajegrupo = new HashMap<>();
            GrupoRef.updateChildren(mensajegrupo);

            GrupoMensajeKeyRef = GrupoRef.child(mensajekey);

            HashMap<String, Object> mensajeinformacion = new HashMap<>();
            mensajeinformacion.put("nombre", CurrentUserName);
            mensajeinformacion.put("mensaje", mensaje);
            mensajeinformacion.put("fecha", Fecha);
            mensajeinformacion.put("hora", Hora);
            GrupoMensajeKeyRef.updateChildren(mensajeinformacion);
        }

    }



    private void MostrarMensajes(DataSnapshot snapshot) {

        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String fecha = (String) ((DataSnapshot)iterator.next()).getValue();
            String hora = (String) ((DataSnapshot)iterator.next()).getValue();
            String mensaje = (String) ((DataSnapshot)iterator.next()).getValue();
            String nombre = (String) ((DataSnapshot)iterator.next()).getValue();
            verMensajes.append(nombre+"\n"+mensaje+"\n"+fecha+"  "+hora+"\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }


    }


}