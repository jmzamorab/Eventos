package es.upv.master.eventos;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static es.upv.master.eventos.EventosAplicacion.mostrarDialogo;
import static es.upv.master.eventos.R.layout.evento;

/**
 * Created by padres on 02/03/2017.
 */

public class EventosFCMService extends FirebaseMessagingService {
private Boolean lAdd = false;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // if (remoteMessage.getNotification() != null) {
        //     mostrarDialogo(getApplicationContext(), remoteMessage.getNotification().getBody());
        // }
        if (remoteMessage.getData().size() > 0) {

            Log.d("*** EvtoFCM.onMessageReceive", "Dentro de if, me llegan datos ... ");
            if ((remoteMessage.getNotification() != null) && (remoteMessage.getNotification().getClickAction() != null))
            {
                Log.d("*** EvtoFCM.onMessageReceive", "Además de datos, notificaciones ... ");
                lAdd = true;
            }
            String evento = "";
            evento = "Evento: " + remoteMessage.getData().get("evento") + "\n";
            /*if (!lAdd){
            evento = evento + "Día: " + remoteMessage.getData().get("dia") + "\n";
            evento = evento + "Ciudad: " + remoteMessage.getData().get("ciudad") + "\n";
            evento = evento + "Comentario: " + remoteMessage.getData().get("comentario");}
            mostrarDialogo(getApplicationContext(), evento);*/
            if (lAdd){
                Log.d("*** EvtoFCM.onMessageReceive", "me han llegado las dos cosas, muestro eventoDetalle");
            Intent intent = new Intent(getApplicationContext(), EventoDetalles.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("evento",  remoteMessage.getData().get("evento"));
            getApplicationContext().startActivity(intent);}
            else{
                evento = evento + "Día: " + remoteMessage.getData().get("dia") + "\n";
                evento = evento + "Ciudad: " + remoteMessage.getData().get("ciudad") + "\n";
                evento = evento + "Comentario: " + remoteMessage.getData().get("comentario");
            }
            Log.d("*** EvtoFCM.onMessageReceive", " y después muestro diálogo");
            mostrarDialogo(getApplicationContext(), evento);

        } else {
            if (remoteMessage.getNotification() != null) {
                mostrarDialogo(getApplicationContext(), remoteMessage.getNotification().getBody());
            }
        }
    }
}
