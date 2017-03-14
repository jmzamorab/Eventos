package es.upv.master.eventos;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by padres on 14/03/2017.
 */
public class FotografiasDrive extends AppCompatActivity {
    public TextView mDisplay;
    String evento;
    static Drive servicio = null;
    static GoogleAccountCredential credencial = null;
    static String nombreCuenta = null;
    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final String DISPLAY_MESSAGE_ACTION = "org.example.eventos.DISPLAY_MESSAGE";
    private static Handler manejador = new Handler();
    private static Handler carga = new Handler();
    private static ProgressDialog dialogo;
    private Boolean noAutoriza = false;
    static final int SOLICITUD_SELECCION_CUENTA = 1;
    static final int SOLICITUD_AUTORIZACION = 2;
    static final int SOLICITUD_SELECCIONAR_FOTOGRAFIA = 3;
    static final int SOLICITUD_HACER_FOTOGRAFIA = 4;
    private static Uri uriFichero;
    private String idCarpeta = "";
    private String idCarpetaEvento = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fotografias_drive);
        Bundle extras = getIntent().getExtras();
        evento = extras.getString("evento");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        credencial = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        nombreCuenta = prefs.getString("nombreCuenta", null);
        noAutoriza = prefs.getBoolean("noAutoriza", false);
        idCarpeta = prefs.getString("idCarpeta", null);
        idCarpetaEvento = prefs.getString("idCarpeta_" + evento, null);
        if (!noAutoriza) {
            if (nombreCuenta == null) {
                PedirCredenciales();
            } else {
                credencial.setSelectedAccountName(nombreCuenta);
                servicio = obtenerServicioDrive(credencial);
                if (idCarpetaEvento == null) {
                    crearCarpetaEnDrive(evento, idCarpeta);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View vista = (View) findViewById(android.R.id.content);
        int id = item.getItemId();
        switch (id) {
            case R.id.action_camara:
                break;
            case R.id.action_galeria:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    static void mostrarMensaje(final Context context, final String mensaje) {
        manejador.post(new Runnable() {
            public void run() {
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static void mostrarCarga(final Context context, final String mensaje) {
        carga.post(new Runnable() {
            public void run() {
                dialogo = new ProgressDialog(context);
                dialogo.setMessage(mensaje);
                dialogo.show();
            }
        });
    }

    static void ocultarCarga(final Context context) {
        carga.post(new Runnable() {
            public void run() {
                dialogo.dismiss();
            }
        });
    }

    private void PedirCredenciales() {
        if (nombreCuenta == null) {
            startActivityForResult(credencial.newChooseAccountIntent(),
                    SOLICITUD_SELECCION_CUENTA);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case SOLICITUD_SELECCION_CUENTA:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    nombreCuenta = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (nombreCuenta != null) {
                        credencial.setSelectedAccountName(nombreCuenta);
                        servicio = obtenerServicioDrive(credencial);
                        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("nombreCuenta", nombreCuenta);
                        editor.commit();
                        crearCarpetaEnDrive(evento, idCarpeta);
                    }
                }
                break;
            case SOLICITUD_HACER_FOTOGRAFIA:
                break;
            case SOLICITUD_SELECCIONAR_FOTOGRAFIA:
                break;
            case SOLICITUD_AUTORIZACION:
                if (resultCode == Activity.RESULT_OK) {
                    crearCarpetaEnDrive(evento, idCarpeta);
                } else {
                    noAutoriza = true;
                    SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("noAutoriza", true);
                    editor.commit();
                    mostrarMensaje(this, "El usuario no autoriza usar Google Drive");
                }
                break;
        }
    }

    private Drive obtenerServicioDrive(GoogleAccountCredential credencial) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credencial).build();
    }

    private void crearCarpetaEnDrive(final String nombreCarpeta, final String carpetaPadre) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String idCarpetaPadre = carpetaPadre;
                    mostrarCarga(FotografiasDrive.this, "Creando carpeta...");
                    //Crear carpeta EventosDrive
                    if (idCarpeta == null) {
                        File metadataFichero = new File();
                        metadataFichero.setName("EventosDrive");
                        metadataFichero.setMimeType("application/vnd.google-apps.folder");
                        File fichero = servicio.files().create(metadataFichero).setFields("id").execute();
                        if (fichero.getId() != null) {
                            SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("idCarpeta", fichero.getId());
                            editor.commit();
                            idCarpetaPadre = fichero.getId();
                        }
                    }
                    File metadataFichero = new File();
                    metadataFichero.setName(nombreCarpeta);
                    metadataFichero.setMimeType("application/vnd.google-apps.folder");
                    if (!idCarpetaPadre.equals("")) {
                        metadataFichero.setParents(Collections.singletonList(idCarpetaPadre));
                    }
                    File fichero = servicio.files().create(metadataFichero).setFields("id").execute();
                    if (fichero.getId() != null) {
                        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("idCarpeta_" + evento, fichero.getId());
                        editor.commit();
                        idCarpetaEvento = fichero.getId();
                        mostrarMensaje(FotografiasDrive.this, "¡Carpeta creada!");
                    }
                    ocultarCarga(FotografiasDrive.this);
                } catch (UserRecoverableAuthIOException e) {
                    ocultarCarga(FotografiasDrive.this);
                    startActivityForResult(e.getIntent(), SOLICITUD_AUTORIZACION);

                } catch (IOException e) {
                    mostrarMensaje(FotografiasDrive.this, "Error;" + e.getMessage());
                    ocultarCarga(FotografiasDrive.this);
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}
