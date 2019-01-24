package com.GTI.Grupo1.IoT;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.GTI.Grupo1.IoT.InicioFragment.view;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class PerfilFragment extends Fragment {

    View vistaPerfil;

    private FirebaseUser user;

    private static final String TAG = "PerfilFragment";
    private EditText mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private Button btnGuardar;

    private String date;
    private SeekBar seekBar;
    private String nivelEjer;
    private TextView textViewEjer;

    public TextView fecha;
    public TextView sexoo;
    public TextView telefono;

    private Usuario usuario = MainActivity.usuario;

    private long progreso;

    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vistaPerfil = inflater.inflate(R.layout.perfil, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        TextView nombre = vistaPerfil.findViewById(R.id.nombreUsuario);
        TextView correo = vistaPerfil.findViewById(R.id.correoUsuario);
        final ImageView foto = vistaPerfil.findViewById(R.id.fotoUsuario);


        nombre.setText(user.getDisplayName());
        correo.setText(user.getEmail());
        final String proveedor = user.getProviders().get(0);


        /*if (proveedor.equals("google.com")) {
            String uri = user.getPhotoUrl().toString();
            Log.d("ygh", uri);
            uri = uri.replace("s96-c", "s300-c");
            Picasso.with(getActivity().getBaseContext()).load(uri).into(foto);
            System.out.println("dentro de getPhoto");
        }else {*/
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        storageReference.child("imagenes/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getActivity().getBaseContext()).load(uri.toString())
                        .resize(168, 168).centerCrop()
                        .into(foto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (proveedor.equals("google.com")) {
                    String uri = user.getPhotoUrl().toString();
                    uri = uri.replace("s96-c", "s300-c");
                    Picasso.with(getActivity().getBaseContext()).load(uri).into(foto);
                }
            }
        });
        //}

        //Sexo
        sexoo = vistaPerfil.findViewById(R.id.sexo);
        if(!usuario.getSexo().equals(null)) {
            sexoo.setText(usuario.getSexo());
        }
        //Fecha
        fecha = vistaPerfil.findViewById(R.id.editfecha);
        if(!fecha.getText().equals(null)) {
            fecha.setText(usuario.getFechaNacimiento());
        }

        //Telefono
        telefono = vistaPerfil.findViewById(R.id.textoTel);
        if(!usuario.getTelefono().equals(null)) {
            telefono.setText(usuario.getTelefono());
        }



//---------------------------  CALENDARIO  ------------------------------------------------------------------------------------
        mDisplayDate = vistaPerfil.findViewById(R.id.editfecha);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                        mDateSetListener,
                        year, month, day);
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

                    if (pref.getString("fecha", "0").equals("1")) {
                        Log.d(TAG, "onDateSet: dd/mm/yyyy: " + month + "/" + day + "/" + year);
                        date = month + "/" + day + "/" + year;
                        mDisplayDate.setText(date);
                    } else if (pref.getString("fecha", "0").equals("2")) {
                        Log.d(TAG, "onDateSet: yyyy/mm/dd: " + year + "/" + month + "/" + day);
                        date = year + "/" + month + "/" + day;
                        mDisplayDate.setText(date);
                    } else {
                        Log.d(TAG, "onDateSet: mm/dd/yyyy: " + day + "/" + month + "/" + year);
                        date = day + "/" + month + "/" + year;
                        mDisplayDate.setText(date);
                    }

            }
        };

//------------------------------  SEEKBAR  ---------------------------------------------------------------------------------
        seekBar = vistaPerfil.findViewById(R.id.seekBar);
        textViewEjer = vistaPerfil.findViewById(R.id.TextoEjer);

        seekBar.setProgress((int) usuario.getNivelEjercicio());

        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    //Toast.makeText(getActivity(), "Nivel de ejercicio " + seekBar.getProgress(), Toast.LENGTH_SHORT).show();

                    if (seekBar.getProgress() == 0){
                        nivelEjer = "Nada de ejercicio ";
                    }else if (seekBar.getProgress()== 1){
                        nivelEjer = "Poco ejercicio";
                    }else if (seekBar.getProgress()== 2){
                        nivelEjer = "Ejercicio una vez por semana";
                    }else if (seekBar.getProgress()== 3){
                        nivelEjer = "Bastante ejercicio";
                    }else if (seekBar.getProgress()== 4){
                        nivelEjer = "Mucho ejercicio";
                    }

                    textViewEjer.setText(nivelEjer);
                }
            });
        }


//-------------------------------------------------------------------
        //Guardar datos del usuario
       addListenerOnButton(vistaPerfil);
        return vistaPerfil;
    }//onCreate()

//-------------------  GUARDAR DATOS DEL USUARIO  -----------------------------------------------------------------------
    public void addListenerOnButton(View view) {

        progreso = seekBar.getProgress();
        //seekBar.setProgress((int) usuario.getNivelEjercicio());
        btnGuardar = view.findViewById(R.id.guardar);
        final ImageView imageView = view.findViewById(R.id.fotoUsuario);



        btnGuardar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast toast3 = Toast.makeText(getContext(), "Se han guardado con éxito tus datos", Toast.LENGTH_SHORT);
                toast3.show();
//-------------------------GUARDAR LOS DATOS DEL USUARIO EN LA BASE DE DATOS---------------------------------------

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    Map<String, Object> perfilUser = new HashMap<>();
                    if(date!=null) {
                        perfilUser.put("FechaNacimiento", date);
                        usuario.setFechaNacimiento(date);
                    }
                    if(perfilUser!=null) {
                        perfilUser.put("NivelEjer", seekBar.getProgress());
                    }
                    usuario.setNivelEjercicio(seekBar.getProgress());

                    db.collection("USUARIOS").document(user.getUid()).update(perfilUser);
            }

        });

    }//Addlistener()

}//()


