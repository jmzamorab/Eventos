package es.upv.master.eventos;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static es.upv.master.eventos.EventosAplicacion.mostrarDialogo;

/**
 * Created by padres on 02/03/2017.
 */

public class EventosFCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            mostrarDialogo(getApplicationContext(), remoteMessage.getNotification().getBody());
        }
    }
}
