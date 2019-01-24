package com.GTI.Grupo1.IoT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IntroductionActivity extends AppCompatActivity {

    TextView error;
    EditText fecha;
    ProgressBar espera;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    FirebaseFirestore db;

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String sexo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction);

        System.out.println("Dentro de oncreate, despues de mostrar el layout");

        Log.d("MisDebugs", "Dentro del OnCreate");


        Button enviar = findViewById(R.id.enviar);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leerDatos();
            }
        });


    }

    public void leerDatos(){

        radioGroup = (RadioGroup) findViewById(R.id.radioSex);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(selectedId);
        sexo = (String) radioButton.getText();


        System.out.println("MisDebugs" + "Dentro del metodo leerDAtos");

        EditText casaId = findViewById(R.id.casaId);

        System.out.println("MisDebugs"+ "Despues de leer la referencia de casaId");

        final String idCasa = casaId.getText().toString();

        System.out.println("MisDebugs"+ "Despues de asignar string a idCasa");
        if(idCasa.isEmpty()){

            System.out.println("MisDebugs"+ "Dentro de si el idCasa esta vacio");

            error.setText("Introduce un nombre de la casa correcto");


        }else {

            System.out.println("MisDebugs" +"Dentro de sino");

            db = FirebaseFirestore.getInstance();

            db.collection("CASAS").document(idCasa).addSnapshotListener(
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e("Firebase", "Error al leer", e);
                                error.setText("Error en la base de datos, vuelva a intentarlo");
                            } else if (snapshot == null || !snapshot.exists()) {
                                error.setText("Introduce un nombre de la casa existente");
                            } else {
                                System.out.println("Firestore"+ "datos:" + snapshot.getData() + snapshot.getId());
                                crearUsuario(idCasa);
                            }
                        }
                    });
        }
    }

    public void crearUsuario (String idCasa){

        //fecha = findViewById(R.id.editTextFecha);
        EditText telf = findViewById(R.id.numTelf);
        RadioGroup sexo = findViewById(R.id.radioSex);
        SeekBar ejercicio = findViewById(R.id.seekBar);

//--------------------------------------------------------------------------------------------------
//        fecha = findViewById(R.id.editTextFecha);
//        fecha.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Calendar cal = Calendar.getInstance();
//                int year = cal.get(Calendar.YEAR);
//                int month = cal.get(Calendar.MONTH);
//                int day = cal.get(Calendar.DAY_OF_MONTH);
//                DatePickerDialog dialog = new DatePickerDialog( getApplicationContext(),
//                        mDateSetListener,
//                        year, month, day);
//                dialog.show();
//            }
//        });
//        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                month = month + 1;
//                //Log.d("Pruebas Fecha", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);
//                String date = day + "/" + month + "/"+year;
//                System.out.println("String date" + date);
//                //date = day + "/" + month + "/" + year;
//                fecha.setText(date);
//                Date date1;
//                try {
//                    date1 =new SimpleDateFormat("dd/MM/yyyy").parse(date);
//                    System.out.println("Formato date" + date1);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//--------------------------------------------------------------------------------------------------


//        if(fecha.getText().toString().isEmpty() || telf.getText().toString().isEmpty()
//                || sexo.getCheckedRadioButtonId() == -1)
        if(telf.getText().toString().isEmpty() || sexo.getCheckedRadioButtonId() == -1){
            error.setText("Datos incorrectos");
        }else {

            System.out.println("Telefono" + telf.getText().getClass());
            System.out.println("Sexo" + sexo.getCheckedRadioButtonId());
            //System.out.println("fecha" + fecha.getText());
            System.out.println("ejercicio" + ejercicio.getProgress());


        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore bada = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("Correo", user.getEmail().toString());
        userData.put("Nombre", user.getDisplayName().toString());
//        userData.put("FechaNaci", );
        userData.put("NivelEjer", ejercicio.getProgress());
        userData.put("Sexo", radioButton.getText());
        userData.put("telefono", telf.getText().toString());
        userData.put("idCasa", idCasa);                     //user.getUid()


            db = FirebaseFirestore.getInstance();
        db.collection("USUARIOS").document(user.getUid()).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "DocumentSnapshot successfully written!");
                        System.out.println("Sucess");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error writing document", e);
                        System.out.println("Error");
                    }
                });

        Date date = new Date();
        Timestamp fecha = new Timestamp(date);


        Map<String, Object> datosIniciales = new HashMap<>();
        datosIniciales.put("peso", 0);
        datosIniciales.put("altura", 0);
        datosIniciales.put("fecha", fecha);

        for(int i = 0; i<4; i++) {
            db.collection("USUARIOS").document(user.getUid())
                    .collection("Bascula").document().set(datosIniciales)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Log.d(TAG, "DocumentSnapshot successfully written!");
                            System.out.println("Sucess");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log.w(TAG, "Error writing document", e);
                            System.out.println("Error");
                        }
                    });

        }

            Map<String, Object> defecto = new HashMap<>();
            defecto.put("id", user.getUid());
        db.collection("CASAS").document(idCasa)
                .collection("Usuarios").document(user.getUid()).set(defecto);




            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);


        }

    }
}
