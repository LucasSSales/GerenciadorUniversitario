package projectp4.studio.com.gerenciador_universitario;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import projectp4.studio.com.gerenciador_universitario.DataBase.InfosDB;

public class AddNotas extends AppCompatActivity {

    private Spinner materias;
    private Spinner avaliacoes;
    private EditText nota;
    private Button add;
    private InfosDB idb;
    private SQLiteDatabase banco;
    private String[] avs = {"AB1", "AB2", "Reavaliação", "Final"};
    private int posAv;
    private int posMat;
    private Integer id;
    private Calculos c = new Calculos(AddNotas.this);
    private NotificationCompat.Builder notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notas);

        materias = (Spinner) findViewById(R.id.spMaterias);
        avaliacoes = (Spinner) findViewById(R.id.spAvaliacao);
        nota = (EditText) findViewById(R.id.nota);
        add = (Button) findViewById(R.id.btadd);
        idb = new InfosDB(AddNotas.this);


        try {
            banco = openOrCreateDatabase("Gerenciador_universitario", MODE_PRIVATE, null);

            banco.execSQL("CREATE TABLE IF NOT EXISTS materias (id INTEGER PRIMARY KEY AUTOINCREMENT, nome VARCHAR, cargaHoraria INT(2), maxFaltas INT(2), faltas INT(2), ab1 DOUBLE, ab2 DOUBLE, reav DOUBLE, provaFinal DOUBLE, mediaFinal DOUBLE, conceito VARCHAR, nivelDeFaltas VARCHAR)");
            idb.recuperarInfo(banco);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, idb.recuperarInfo(banco));
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            materias.setAdapter(adapter);
            materias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    posMat = i;
                    id = idb.getIds().get(i);

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, avs);
            adapter2.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            avaliacoes.setAdapter(adapter2);
            avaliacoes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    posAv = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(nota.getText().toString().equals("")){
                        Toast.makeText(AddNotas.this, "Digite uma nota!", Toast.LENGTH_LONG).show();
                    }else{
                        Double n = Double.parseDouble(nota.getText().toString());

                        if(n > 10 || n < 0){
                            Toast.makeText(AddNotas.this, "Notas de 0 a 10 apenas!", Toast.LENGTH_LONG).show();
                        }else{

                            switch(posAv){
                                case 0:
                                    banco.execSQL("UPDATE materias SET ab1="+ n +" WHERE id=" + id);
                                    break;
                                case 1:
                                    banco.execSQL("UPDATE materias SET ab2="+ n +" WHERE id=" + id);
                                    break;
                                case 2:
                                    banco.execSQL("UPDATE materias SET reav="+ n +" WHERE id=" + id);
                                    break;
                                case 3:
                                    banco.execSQL("UPDATE conceito SET provaFinal="+ n +" WHERE id=" + id);
                                    break;
                            }

                            idb.recuperarInfo(banco);

                            ArrayList<Double> notas = new ArrayList<>();

                            notas.add(idb.getAb1().get(idb.getIds().indexOf(id)));
                            notas.add(idb.getAb2().get(idb.getIds().indexOf(id)));
                            notas.add(idb.getReav().get(idb.getIds().indexOf(id)));
                            notas.add(idb.getProvaFinal().get(idb.getIds().indexOf(id)));

                            String con = c.conceito(c.media(posAv, notas), posAv);

                            Toast.makeText(AddNotas.this, "Nota adicionada!", Toast.LENGTH_LONG).show();

                            banco.execSQL("UPDATE materias SET mediaFinal="+ c.media(posAv, notas) +" WHERE id=" + id);
                            banco.execSQL("UPDATE materias SET conceito='"+ con +"' WHERE id=" + id);

                            if(!idb.getConceitos().get(idb.getIds().indexOf(id)).equals(con)){
                                notificar(con, idb.getIds().indexOf(id));
                            }

                        }
                    }

                }
            });


        }catch (Exception e){}





    }

    public void notificar(String status, int position){

        notification = new NotificationCompat.Builder(AddNotas.this);
        notification.setAutoCancel(true);


        String msg = "";
        String msg2= "";

        if(status.equals("Aprovado")){
            msg = "PARABÉNS! Você está APROVADO!";
            msg2 = "Continue assim e logo se formará!";
            notification.setSmallIcon(R.drawable.aprovado);
        }else if(status.equals("Reprovado")){
            msg = "QUE PENA! Você está Reprovado";
            msg2 = "Não desanime, continue tentando!";
            notification.setSmallIcon(R.drawable.reprovado);
        }

        notification.setTicker(msg);
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle(msg);
        notification.setContentText(msg2);
        int id = (int) System.currentTimeMillis();
        notification.setVibrate(new long[] {150, 800});

        try{
            Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone toque = RingtoneManager.getRingtone(AddNotas.this, som);
            toque.play();
        }catch(Exception e){}


        Intent i = new Intent(AddNotas.this, SituGeral.class);
        i.putExtra("ID", idb.getIds().get(position));

        PendingIntent pi = PendingIntent.getActivity(AddNotas.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pi);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(id, notification.build());
    }
}
