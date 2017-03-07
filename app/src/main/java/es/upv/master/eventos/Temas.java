package es.upv.master.eventos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import static es.upv.master.eventos.EventosAplicacion.eliminarIdRegistro;
import static es.upv.master.eventos.EventosAplicacion.guardarIdRegistro;
import static es.upv.master.eventos.EventosAplicacion.mostrarDialogo;

/**
 * Created by padres on 05/03/2017.
 */

public class Temas extends AppCompatActivity {
    CheckBox checkBoxDeportes;
    CheckBox checkBoxTeatro;
    CheckBox checkBoxCine;
    CheckBox checkBoxFiestas;
    CheckBox checkBoxNoRecibirNotificaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temas);
        checkBoxDeportes = (CheckBox) findViewById(R.id.checkBoxDeportes);
        checkBoxTeatro = (CheckBox) findViewById(R.id.checkBoxTeatro);
        checkBoxCine = (CheckBox) findViewById(R.id.checkBoxCine);
        checkBoxFiestas = (CheckBox) findViewById(R.id.checkBoxFiestas);
        checkBoxDeportes.setChecked(consultarSuscripcionATemaEnPreferencias(getApplicationContext(), "Deportes"));
        checkBoxTeatro.setChecked(consultarSuscripcionATemaEnPreferencias(getApplicationContext(), "Teatro"));
        checkBoxCine.setChecked(consultarSuscripcionATemaEnPreferencias(getApplicationContext(), "Cine"));
        checkBoxFiestas.setChecked(consultarSuscripcionATemaEnPreferencias(getApplicationContext(), "Fiestas"));
        checkBoxDeportes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mantenimientoSuscripcionesATemas("Deportes", isChecked);
            }
        });
        checkBoxTeatro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mantenimientoSuscripcionesATemas("Teatro", isChecked);
            }
        });
        checkBoxCine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mantenimientoSuscripcionesATemas("Cine", isChecked);
            }
        });
        checkBoxFiestas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mantenimientoSuscripcionesATemas("Fiestas", isChecked);
            }
        });

        checkBoxNoRecibirNotificaciones = (CheckBox) findViewById(R.id.checkBoxNoRecibirNotificaciones);
        Boolean noRecibirNotificaciones = consultarSuscripcionATemaEnPreferencias(getApplicationContext(), "Todos");
        checkBoxNoRecibirNotificaciones.setChecked(noRecibirNotificaciones);
        checkBoxDeportes.setEnabled(!noRecibirNotificaciones);
        checkBoxTeatro.setEnabled(!noRecibirNotificaciones);
        checkBoxCine.setEnabled(!noRecibirNotificaciones);
        checkBoxFiestas.setEnabled(!noRecibirNotificaciones);

        checkBoxNoRecibirNotificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mantenimientoSuscripcionesATemas("Todos", isChecked);
            }
        });

    }

    private void mantenimientoSuscripcionesATemas(String tema, Boolean suscribir) {
        if (tema.equals("Todos")) {
            if (suscribir) {
                //TODO
                // Descomentar y probar, una vez se haya probado con esto cometnado.
                // Ejercicio de pg 30 a 32, posterior al de las pg 28 a 30
                // eliminarIdRegistro(getApplicationContext());
                FirebaseMessaging.getInstance().unsubscribeFromTopic(tema);
                guardarSuscripcionATemaEnPreferencias(getApplicationContext(), tema, true);
                checkBoxDeportes.setChecked(false);
                checkBoxTeatro.setChecked(false);
                checkBoxCine.setChecked(false);
                checkBoxFiestas.setChecked(false);
            /*mostrarDialogo(getApplicationContext(), "Te has suscrito a: " + tema);
            FirebaseMessaging.getInstance().subscribeToTopic(tema);
            guardarSuscripcionATemaEnPreferencias(getApplicationContext(), tema, true);*/
            } else {
                //TODO
                // Descomentar y probar, una vez se haya probado con esto cometnado.
                // Ejercicio de pg 30 a 32, posterior al de las pg 28 a 30
                //if (tema.equals("Todos")) {
                //    guardarIdRegistro(getApplicationContext(), FirebaseInstanceId.getInstance().getToken());
                //}


                /*mostrarDialogo(getApplicationContext(), "Te has dado de baja de: " + tema);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(tema);
                guardarSuscripcionATemaEnPreferencias(getApplicationContext(), tema, false);*/
                FirebaseMessaging.getInstance().subscribeToTopic(tema);
                guardarIdRegistro(getApplicationContext(), FirebaseInstanceId.getInstance().getToken());
                guardarSuscripcionATemaEnPreferencias(getApplicationContext(), tema, false);
            }
            checkBoxDeportes.setEnabled(!suscribir);
            checkBoxTeatro.setEnabled(!suscribir);
            checkBoxCine.setEnabled(!suscribir);
            checkBoxFiestas.setEnabled(!suscribir);
        } else {
            if (suscribir) {
                FirebaseMessaging.getInstance().subscribeToTopic(tema);
                guardarSuscripcionATemaEnPreferencias(getApplicationContext(), tema, true);
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(tema);
                guardarSuscripcionATemaEnPreferencias(getApplicationContext(), tema, false);
            }
        }
    }

    public static void guardarSuscripcionATemaEnPreferencias(Context context, String tema, Boolean suscrito) {
        final SharedPreferences prefs = context.getSharedPreferences("Temas", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(tema, suscrito);
        editor.commit();
    }

    public static Boolean consultarSuscripcionATemaEnPreferencias(Context context, String tema) {
        final SharedPreferences preferencias = context.getSharedPreferences("Temas", Context.MODE_PRIVATE);
        return preferencias.getBoolean(tema, false);
    }
}