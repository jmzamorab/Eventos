package es.upv.master.eventos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class sendEvent extends AppCompatActivity {
    @BindView(R.id.textSend)
    TextView txtSendMsg;
    @BindView(R.id.editTextSend)
    EditText edtSend;
    @BindView(R.id.buttonSend)
    Button btnSend;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_event);
        ButterKnife.bind(this);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Enviarás " + edtSend.getText(), Toast.LENGTH_LONG).show();
               // return true;
                finish();
                //EventosAplicacion.registrarDispositivoEnServidorWebPropioTask.execute();

      /*          apiKey: API de acceso
• idapp: Número de Proyecto
• mensaje: mensaje a enviar*/
            }
      });

    }
}
