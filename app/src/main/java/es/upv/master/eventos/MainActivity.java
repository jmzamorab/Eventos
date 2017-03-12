package es.upv.master.eventos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import android.support.v4.app.ActivityCompat;

import es.upv.master.eventos.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static es.upv.master.eventos.EventosAplicacion.PLAY_SERVICES_RESOLUTION_REQUEST;
import static es.upv.master.eventos.EventosAplicacion.mostrarDialogo;

/*TODO
* AVISO USUA permisos, luego borrar. No de uno en uno
* ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA, android.Manifest.permission.GET_ACCOUNTS}, 1);
*
* */


public class MainActivity extends AppCompatActivity {

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

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                },1);
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
        return super.onOptionsItemSelected(item);
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
        }
    }
}