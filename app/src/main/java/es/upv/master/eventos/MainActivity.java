package es.upv.master.eventos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static es.upv.master.eventos.EventosAplicacion.PLAY_SERVICES_RESOLUTION_REQUEST;
import static es.upv.master.eventos.EventosAplicacion.mostrarDialogo;

/*TODO
* AVISO USUA permisos, luego borrar. No de uno en uno
* ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.GET_ACCOUNTS}, 1);
*
* */


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    StorageReference mStorageRef;
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_INVITE = 0;
    /*    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            //setContentView(R.layout.activity_main);
        }*/
    @BindView(R.id.reciclerViewEventos)
    RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            } else {
                Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!comprobarGooglePlayServices()) {
            Toast.makeText(this
                    , "Error Google Play Services: no está instalado o no es válido.", Toast.LENGTH_LONG);
            finish();
        }
        ButterKnife.bind(this);
        EventosAplicacion app = (EventosAplicacion) getApplicationContext();
        databaseReference = app.getItemsReference();
        adapter = new EventosRecyclerAdapter(R.layout.evento, databaseReference);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final SharedPreferences preferencias = getApplicationContext().getSharedPreferences("Temas", Context.MODE_PRIVATE);
        if (preferencias.getBoolean("Inicializado", false) == false) {
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences("Temas", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Inicializado", true);
            editor.commit();
            FirebaseMessaging.getInstance().subscribeToTopic("Todos");
        }

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.GET_ACCOUNTS}, 1);
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addApi(AppInvite.API).enableAutoManage(this, this).build();

        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink).setResultCallback
                (new ResultCallback<AppInviteInvitationResult>() {
            @Override
            public void onResult(AppInviteInvitationResult result) {
                if (result.getStatus().isSuccess()) {
                    Intent intent = result.getInvitationIntent();
                    String deepLink = AppInviteReferral.getDeepLink(intent);
                    String invitationId = AppInviteReferral.getInvitationId(intent);
                    android.net.Uri url = Uri.parse(deepLink);
                    String descuento = url.getQueryParameter("descuento");
                    mostrarDialogo(getApplicationContext(), "Tienes un descuento del " + descuento + "% gracias a la invitación: " + invitationId, null) ;
                }
            }
        });
    }

    private boolean comprobarGooglePlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private static MainActivity current;

    @Override
    protected void onStart() {
        super.onStart();
        current = this;
    }

    public static MainActivity getCurrentContext() {
        return current;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
      /*  if (getIntent().hasExtra("body")) {
            mostrarDialogo(this, extras.getString("body"));
            extras.remove("body");
        }*/
        Log.d("*** onResume MAIN", "LLegan datos");
        if (extras != null && extras.keySet().size() > 4) {
            String evento = "";
            evento = "Evento: " + extras.getString("evento") + "\n";
            evento = evento + "Día: " + extras.getString("dia") + "\n";
            evento = evento + "Ciudad: " + extras.getString("ciudad") + "\n";
            evento = evento + "Comentario: " + extras.getString("comentario");
            mostrarDialogo(getApplicationContext(), evento, null);

            for (String key : extras.keySet()) {
                getIntent().removeExtra(key);
            }
            extras = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_temas) {
            Intent intent = new Intent(getBaseContext(), Temas.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_send_evento) {
            Intent intent = new Intent(getBaseContext(), sendEvent.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_invitar) {
            invitar();
        }
        return super.onOptionsItemSelected(item);
    }

    private void invitar() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title)).
                setMessage(getString(R.string.invitation_message)).
                setDeepLink(Uri.parse(getString(R.string.invitation_deep_link))).
                setCustomImage(Uri.parse(getString(R.string.invitation_custom_image))).
                setCallToActionText(getString(R.string.invitation_cta)).build();
        startActivityForResult(intent, REQUEST_INVITE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "Permiso denegado para mantener escribir en el almacenamiento.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 2: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "Permiso denegado para acceder a la camara", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 3: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "Permiso denegado para acceder a las cuentas", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Si hay una subida en curso, guarda la referencia para consultarla posteriormente.
        if (mStorageRef != null) {
            outState.putString("reference", mStorageRef.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Si hay una subida en progreso, obtiene su referencia y crea una nueva StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);
        // Recupera todas las UploadTasks para la StorageReference guardada.
        List tasks = mStorageRef.getActiveUploadTasks();
        if (tasks.size() > 0) {
            // Obtiene la tarea que monitoriza la subida.
            UploadTask task = (UploadTask) tasks.get(0);
            // Añade nuevos escuchadores a la tarea usando un ámbito de la actividad.
            StorageTask storageTask = task.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot state) {
                    handleSuccess(state); //llama a una función que maneje el evento.
                }

                private void handleSuccess(UploadTask.TaskSnapshot state) {
                    Toast.makeText(getApplicationContext(), "Exito " + state.getBytesTransferred() + " bytes subidos ", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }
}
