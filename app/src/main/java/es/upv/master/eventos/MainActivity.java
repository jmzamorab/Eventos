package es.upv.master.eventos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.database.DatabaseReference;

import org.example.eventos.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static es.upv.master.eventos.EventosAplicacion.PLAY_SERVICES_RESOLUTION_REQUEST;

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
}