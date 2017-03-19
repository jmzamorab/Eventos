package es.upv.master.eventos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static es.upv.master.eventos.EventosAplicacion.getItemsReference;

/**
 * Created by padres on 07/03/2017.
 */

public class EventoDetalles extends AppCompatActivity {
    @BindView(R.id.txtEvento)
    TextView txtEvento;
    @BindView(R.id.txtFecha)
    TextView txtFecha;
    @BindView(R.id.txtCiudad)
    TextView txtCiudad;
    @BindView(R.id.imgImagen)
    ImageView imgImagen;
    String evento;
    Query registro;
    final int SOLICITUD_SUBIR_PUTDATA = 0;
    final int SOLICITUD_SUBIR_PUTSTREAM = 1;
    final int SOLICITUD_SUBIR_PUTFILE = 2;
    final int SOLICITUD_SELECCION_STREAM = 100;
    final int SOLICITUD_SELECCION_PUTFILE = 101;
    final int SOLICITUD_FOTOGRAFIAS_DRIVE = 102;
    private ProgressDialog progresoSubida;
    Boolean subiendoDatos = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evento_detalles);
        Log.d("*** EventoDetalles ", "posterior a setContentView");
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        evento = extras.getString("evento");
        if (evento == null) evento = "";
        Log.d("*** EventoDetalles ", "el extra que llega en evento es " + evento);
        registro = getItemsReference().child(evento);
        registro.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                EventoItem currentItem = snapshot.getValue(EventoItem.class);
                txtEvento.setText(currentItem.getEvento());
                txtCiudad.setText(currentItem.getCiudad());
                txtFecha.setText(currentItem.getFecha());
                new DownloadImageTask((ImageView) imgImagen).execute(currentItem.getImagen());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                EventosAplicacion.mostrarDialogo(EventosAplicacion.getAppContext(),
                        "Ha ocurrido un error al recuperar el registro.", null);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mImagen = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mImagen = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mImagen;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View vista = (View) findViewById(android.R.id.content);
        int id = item.getItemId();
        switch (id) {
            case R.id.action_putData:
                subirAFirebaseStorage(SOLICITUD_SUBIR_PUTDATA, null);
                break;
            case R.id.action_streamData:
                seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_STREAM);
            case R.id.action_putFile:
                seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_PUTFILE);
                break;
            case R.id.action_deleteImage:
                askBorrarImagen();
                break;
            case R.id.action_fotografiasDrive:
                Intent intent = new Intent(getBaseContext(), FotografiasDrive.class);
                intent.putExtra("evento", evento);
                startActivity(intent);
                break;
            case R.id.action_acercaDe:
                Intent intentWeb = new Intent(getBaseContext(), EventosWeb.class);
                intentWeb.putExtra("evento", evento);
                startActivity(intentWeb);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void askBorrarImagen() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Borrar Imagen");
        alertDialog.setMessage("¿Está seguro que desea borrar la imagen?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                borraImagen();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                finish();
            }
        });
        alertDialog.show();
    }

    private void borraImagen() {

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://eventos-d4f6a.appspot.com");
        StorageReference refImagen = storageRef.child(evento);
        Log.d("*** borraImagen", "Borrar Imagen de FB Storage");
        refImagen.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("*** borraImagen", "Borrado de FB Storage CORRECTO, ahora vaciamos valor en BBDD");
                DatabaseReference eventoBBDD = EventosAplicacion.getItemsReference().child(evento);
                eventoBBDD.child("imagen").setValue("");
                Log.d("*** borraImagen", "valor en BBDD limpio");
                Toast.makeText(getApplicationContext(), "Imagen borrada con éxito ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("*** borraImagen", "ERROR al borrar de FB Storage " + e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), "ERROR al borrar la imagen ", Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        Uri ficheroSeleccionado;
        Cursor cursor;
        String rutaImagen;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SOLICITUD_SELECCION_STREAM:
                    ficheroSeleccionado = data.getData();
                    String[] proyeccionStream = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(ficheroSeleccionado, proyeccionStream, null, null, null);
                    cursor.moveToFirst();
                    rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionStream[0]));
                    cursor.close();
                    subirAFirebaseStorage(SOLICITUD_SUBIR_PUTSTREAM, rutaImagen);
                    break;
                case SOLICITUD_SELECCION_PUTFILE:
                    ficheroSeleccionado = data.getData();
                    String[] proyeccionFile = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(ficheroSeleccionado, proyeccionFile, null, null, null);
                    cursor.moveToFirst();
                    rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionFile[0]));
                    cursor.close();
                    subirAFirebaseStorage(SOLICITUD_SUBIR_PUTFILE, rutaImagen);
                    break;
            }
        }
    }

    public void seleccionarFotografiaDispositivo(View v, Integer solicitud) {
        Intent seleccionFotografiaIntent = new Intent(Intent.ACTION_PICK);
        seleccionFotografiaIntent.setType("image/*");
        startActivityForResult(seleccionFotografiaIntent, solicitud);
    }

    public void subirAFirebaseStorage(Integer opcion, String ficheroDispositivo) {
        final ProgressDialog progresoSubida = ProgressDialog.show(EventoDetalles.this, "Espere ...", "Subiendo ...", true);

        UploadTask uploadTask = null;
        String fichero = evento;
        StorageReference imagenRef = EventosAplicacion.getStorageReference().child(fichero);
        try {
            switch (opcion) {
                case SOLICITUD_SUBIR_PUTDATA:
                    imgImagen.setDrawingCacheEnabled(true);
                    imgImagen.buildDrawingCache();
                    Bitmap bitmap = imgImagen.getDrawingCache();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    imagenRef = EventosAplicacion.getStorageReference().child(fichero);
                    uploadTask = imagenRef.putBytes(data);
                    break;
                case SOLICITUD_SUBIR_PUTSTREAM:
                    InputStream stream = new FileInputStream(new File(ficheroDispositivo));
                    uploadTask = imagenRef.putStream(stream);
                    break;
                case SOLICITUD_SUBIR_PUTFILE:
                    Uri file = Uri.fromFile(new File(ficheroDispositivo));
                    uploadTask = imagenRef.putFile(file);
                    //Uri sessionUri = uploadTask.getSnapshot().getUploadSessionUri();
                    //     EventosAplicacion.guardarUriPreferencias(getApplicationContext(), sessionUri.toString());
                    break;
            }
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    EventosAplicacion.mostrarDialogo(getApplicationContext(), "Ha ocurrido un error al subir la imagen.", null);

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    DatabaseReference myRef = getItemsReference().child(evento);
                    DatabaseReference imagenRef = myRef.child("imagen");

                    imagenRef.setValue(taskSnapshot.getDownloadUrl().toString());
                    new DownloadImageTask((ImageView) imgImagen).execute(taskSnapshot.getDownloadUrl().toString());
                    progresoSubida.dismiss();
                    subiendoDatos = false;
                    EventosAplicacion.mostrarDialogo(getApplicationContext(), "Imagen subida correctamente.", null);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    if (!subiendoDatos) {
                        progresoSubida.show();
                        subiendoDatos = true;
                    }
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    progresoSubida.dismiss();
                    subiendoDatos = true;
                }
            });
        } catch (IOException e) {
            EventosAplicacion.mostrarDialogo(getApplicationContext(), e.toString(), null);
        }
    }

}