package es.upv.master.eventos;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static es.upv.master.eventos.EventosAplicacion.guardarIdRegistro;

/**
 * Created by padres on 05/03/2017.
 */

public class EventosFCMInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String idPush;
        idPush = FirebaseInstanceId.getInstance().getToken();
        guardarIdRegistro(getApplicationContext(), idPush);
    }
}
