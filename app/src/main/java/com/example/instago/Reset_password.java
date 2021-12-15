package com.example.instago;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Reset_password extends AppCompatActivity {

    private EditText txtemail;
    private Button btnreset;

    private String email = "";
    private FirebaseAuth mAuth;

    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);


        txtemail = findViewById(R.id.txtemail);
        btnreset = findViewById(R.id.btnreset);

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);


        btnreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = txtemail.getText().toString();

                if (!email.isEmpty()){
                    mDialog.setMessage("Espere un momento....");
                    //esto es para que el usuario no pueda quitarlo la funcion de restablecer password
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    resetPassword();
                }
                else
                {
                    Toast.makeText(Reset_password.this, "Debe ingresar el email", Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void resetPassword() {
        //que lengueaje quiere (es) español
        mAuth.setLanguageCode("es");

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(Reset_password.this, "Se ha enviado un correo para reestablecer contraseña", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(Reset_password.this, "No se puedo enviar el correo de reestablecer contraseña", Toast.LENGTH_SHORT).show();
                }

                // le decimos que se oculte
                mDialog.dismiss();


            }
        });

    }

}