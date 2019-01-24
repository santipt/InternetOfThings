package com.GTI.Grupo1.IoT;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.GTI.Grupo1.IoT.InicioFragment.peso;

/*
* TODO: Modificar la foto que se muestra de la cuenta de google en forma redonda
* TODO:  Cuando el usuario se registra por usuario y contraseña, se muestra el nombre y el correo, pero la foto se muestra la que hay por defecto
**/

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SOLICITUD_PERMISO_WRITE_CALL_LOG = 0;
    public static FirebaseUser user;

    public static Usuario usuario;

    MqttClient client;

    /*////////recycler view////////
    private RecyclerView recyclerView;
    public AdaptadorUsuarios adaptador;
    private RecyclerView.LayoutManager layoutManager;
    public static UsuarioInterface usuarios = new UsuariosVector();*/

    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    ImageView foto_gallery;

    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    SOLICITUD_PERMISO_WRITE_CALL_LOG);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();

        //Página de inicio
        InicioFragment fragment = new InicioFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


// Codigo para mostrar los datos del usuario en la parte superior del menu
        //Obtenemos las referencias de las vistas
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menuView = navigationView.getMenu();
        View headerView = navigationView.getHeaderView(0);
        TextView nombre = headerView.findViewById(R.id.nombreUsuario);
        TextView correo = headerView.findViewById(R.id.correoUsuario);
        final ImageView foto = headerView.findViewById(R.id.fotoUsuario);
        // Asignamos los valores que se desean mostrar a las vistas
        nombre.setText(user.getDisplayName());
        correo.setText(user.getEmail());
// Codigo para identificar el provedor, soluciona problema cuando se hace login por Correo-contraseña
// no hay foto y da error, de esta manera filtramos y solo ejecuta el codigo cuando se hace login por google.
        final String proveedor = user.getProviders().get(0);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();



        storageReference.child("imagenes/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getBaseContext()).load(uri.toString())
                        .resize(168, 168).centerCrop()
                        .into(foto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (proveedor.equals("google.com")) {
                    String uri = user.getPhotoUrl().toString();
                    uri = uri.replace("s96-c", "s300-c");
                    Picasso.with(getBaseContext()).load(uri).into(foto);
                    }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

//------------------------------------------------------------------------------------------------------------------

        // Se hace una busqueda a la base de datos que voy a aprovechar para descargar datos


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("USUARIOS").document(user.getUid()).get()
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){

                                    usuario = new Usuario(
                                             task.getResult().getString("Correo")
                                            ,task.getResult().getString("FechaNacimiento")
                                            ,task.getResult().getLong("NivelEjer")
                                            ,task.getResult().getString("Nombre")
                                            ,task.getResult().getString("Sexo")
                                            ,task.getResult().getString("telefono"));

                                    /*Toast toast = Toast.makeText(getApplicationContext(), usuario.getCorreoElectronico(), Toast.LENGTH_SHORT);
                                    toast.show();
                                    Toast toast1 = Toast.makeText(getApplicationContext(), usuario.getFechaNacimiento(), Toast.LENGTH_SHORT);
                                    toast1.show();
                                    Toast toast2 = Toast.makeText(getApplicationContext(), usuario.getNombre(), Toast.LENGTH_SHORT);
                                    toast2.show();
                                    Toast toast3 = Toast.makeText(getApplicationContext(), usuario.getSexo(), Toast.LENGTH_SHORT);
                                    toast3.show();*/


                                } else {
                                    Toast toast1 = Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT);
                                    toast1.show();
                                }
                            }
                        }
                );
//-------------------------------------------------------------------------------------------------------------------
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }//onCreate()

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


/*
*  Codigo para el funcionamiento del segundo menu, ubicado a la parte superior derecha del activity_main
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            lanzarPreferencias(null);
            return true;
        }
        if (id == R.id.acercaDe) {
            lanzarAcercaDe(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


/*
* Codigo para el funcionamiento del menu principal estilo google
*/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;

        int id = item.getItemId();

        if (id == R.id.nav_inicio) {

            fragment = new InicioFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, "fragmentInicio");//.addToBackStack(null); //para hacer que al pulsar atras no salga de la aplicación
            fragmentTransaction.commit();


        } else if (id == R.id.nav_bascula) {

            fragment = new BasculaFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);//.addToBackStack(null); //para hacer que al pulsar atras no salga de la aplicación
            fragmentTransaction.commit();

        } else if (id == R.id.nav_casa) {

            fragment = new SensoresFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment, "fragmentCasa");//.addToBackStack(null); //para hacer que al pulsar atras no salga de la aplicación
            fragmentTransaction.commit();

        } else if (id == R.id.nav_preferencias) {
            lanzarPreferencias(null);
            return true;

        }  else if (id == R.id.nav_compartir) {
            DecimalFormat formato = new DecimalFormat("#.##");
            String ultimoPeso = formato.format(Float.parseFloat(peso));

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,
                    "¡Mira mi último peso! Poco a poco estoy consiguiendo mi objetivo, ahora peso : " + ultimoPeso + " Kg");
            startActivity(intent);
        return true;


        }  else if (id == R.id.nav_perfil) {

            fragment = new PerfilFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);//.addToBackStack(null); //para hacer que al pulsar atras no salga de la aplicación
            fragmentTransaction.commit();

        } else if (id == R.id.nav_cerrar_sesion) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK|
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    MainActivity.this.finish();
                }
            });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void lanzarAcercaDe(View view){
        Intent i = new Intent(this, AcercaDeActivity.class);
        startActivity(i);
    }

    public void lanzarPreferencias(View view){
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivity(i);
    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            foto_gallery = findViewById(R.id.fotoUsuario);
            imageUri = data.getData();
            foto_gallery.setImageURI(imageUri);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Uri uri = data.getData();
            StorageReference dataRef = storageReference.child("imagenes/" + uid);
            dataRef.putFile(uri);
            Toast.makeText(this, "Foto guardada con éxito", Toast.LENGTH_SHORT).show();

        }
    }
    public void editarFoto (View view){
        openGallery();
    }
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
    public void abrirTemperatura (View view) {
        Intent intent = new Intent(this, TemperaturaActivity.class);
        startActivity(intent);
    }
}
