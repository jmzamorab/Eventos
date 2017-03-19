package es.upv.master.eventos;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by padres on 28/02/2017.
 */

//TODO
// Si hay tiempo revisar
// ejemplo en
// https://poliformat.upv.es/portal/site/CFP_505_15903/page/72b57980-a497-48d4-b5e8-8dad243c5f9b
public class EventosAplicacion extends Application {
    static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String ITEMS_CHILD_NAME = "eventos";
    private static DatabaseReference eventosReference;
    private static Context context;
    static final String URL_SERVIDOR = "http://cursoandroid.hol.es/notificaciones/";
    static final String URL_SERVIDOR_MIO = "http://toletor.hol.es/notificaciones/";
    static String ID_PROYECTO = "eventos-d4f6a";

    String idRegistro = "";
    private FirebaseStorage storage;
    private static StorageReference storageRef;
    private final String urlStorageFirebase = "gs://eventos-d4f6a.appspot.com";

    static FirebaseRemoteConfig mFirebaseRemoteConfig;
    static String colorFondo;
    static Boolean acercaDe;

    static String API_KEY = "AAAAaxywe7A:APA91bG54kXOUmDD2obse6rn-2IHaMnt5XUxBAeBCuUY12XO0qrCOxmj-4T_fxWY8SA5VHOx1RqKiavniaC5k8zMDADhOQ3x8sPtp6CgZ_VyZy74Ui_llJqlw32dLHuM3adujiAdq7D8";


    @Override
    public void onCreate() {
        super.onCreate();
        EventosAplicacion.context = getApplicationContext();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        eventosReference = database.getReference(ITEMS_CHILD_NAME);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl(urlStorageFirebase);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        boolean kk = configSettings.isDeveloperModeEnabled();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_default);
        long cacheExpiration = 60;


//expire the cache immediately for development mode.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFirebaseRemoteConfig.activateFetched();
                getColorFondo();
                getAcercaDe();
                Log.d("** EVENTOSAPLICACION", "OnCreate -> dime AcercaDe FB => " + acercaDe);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
                acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
            }
        });
    }

    public static StorageReference getStorageReference() {
        return storageRef;
    }

    public static Context getAppContext() {
        return EventosAplicacion.context;
    }

    public static DatabaseReference getItemsReference() {
        return eventosReference;
    }

    static void mostrarDialogo(final Context context, final String mensaje, final String idEvento) {
        Log.d("*** EventosAplicacion", "estoy en mostrarDialogo");
        Intent intent = new Intent(context, Dialogo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("evento", idEvento);
        context.startActivity(intent);
    }

    public static class registrarDispositivoEnServidorWebTask extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea = "";

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Log.d("*** Registrar dispositivo", "Dentro de Registrar dispositivo");
                Uri.Builder constructorParametros = new Uri.Builder().appendQueryParameter("iddevice", idRegistroTarea).appendQueryParameter("idapp", ID_PROYECTO);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "registrar.php";
                Log.d("*** Registrar dispositivo", "url " + url);
                Log.d("*** Registrar dispositivo", "parámetros (1)... iddevice " + idRegistroTarea);
                Log.d("*** Registrar dispositivo", "parámetros (2) ... idapp " + ID_PROYECTO);
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                    Log.d("*** Registrar dispositivo", "respuesta correcta  " + respuesta);
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }

        public void onPostExecute(String res) {
            if (res == "ok") {
                Log.d("*** Registrar dispositivo PostExecute ", "voy a guardar Id en  registroPreferencias " + idRegistroTarea);
                guardarIdRegistroPreferencias(contexto, idRegistroTarea);
            }
        }
    }

    public static void guardarIdRegistroPreferencias(Context context, String idRegistro) {
        final SharedPreferences prefs = context.getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("idRegistro", idRegistro);
        editor.commit();
    }

    /*public static void guardarUriPreferencias(Context context, Uri uri) {
        final SharedPreferences prefs = context.getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("uri", String.valueOf(uri));
        editor.commit();
    }

    public static String leerUriPreferencias(Context context)
    {
        final SharedPreferences prefs = context.getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        prefs.get
    }*/

    public static String dameIdRegistroPreferencias(Context context) {
        final SharedPreferences preferencias = context.getSharedPreferences("Eventos", Context.MODE_PRIVATE);
        String idRegistro = preferencias.getString("idRegistro", "");
        return idRegistro;
    }

    public static void guardarIdRegistro(Context context, String idRegistro) {
        registrarDispositivoEnServidorWebTask tarea = new registrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = idRegistro;
        tarea.execute();
    }

    public static void eliminarIdRegistro(Context context) {
        desregistrarDispositivoEnServidorWebTask tarea = new desregistrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = dameIdRegistroPreferencias(context);
        tarea.execute();
    }

    public static class desregistrarDispositivoEnServidorWebTask extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea = dameIdRegistroPreferencias(context);

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder().appendQueryParameter("iddevice", idRegistroTarea).appendQueryParameter("idapp", ID_PROYECTO);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "desregistrar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }

        public void onPostExecute(String res) {
            if (res == "ok") {
                guardarIdRegistroPreferencias(contexto, "");
            }
        }
    }

    private void getColorFondo() {
        Log.d("** EVENTOSAPLICACION", "valor de firebaseRemoteconfig ...." + mFirebaseRemoteConfig.getBoolean("color_fondo"));
        colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
    }

    private void getAcercaDe() {
        Log.d("** EVENTOSAPLICACION", "valor de firebaseRemoteconfig ...." + mFirebaseRemoteConfig.getBoolean("acera_de"));
        acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
    }
}