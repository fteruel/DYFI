package com.example.dyfi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-05-02&minfelt=50&minmagnitude=5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TerremotoAsynTask task = new TerremotoAsynTask();
        task.execute(USGS_REQUEST_URL);

    }

    public void updateUi(Evento earthquake) {
        TextView tituloTV = findViewById(R.id.tituloTV);
        tituloTV.setText(earthquake.titulo);

        TextView nroPersonasTV = (TextView) findViewById(R.id.nroPersonasTV);
        nroPersonasTV.setText(getString(R.string.gente_que_lo_sintio, earthquake.nroPersonas));

        TextView magnitudPercibidaTV = (TextView) findViewById(R.id.magnitudPercibidaTV);
        magnitudPercibidaTV.setText(earthquake.fuerzaPercibida);
    }

    class TerremotoAsynTask extends AsyncTask<String,Void, Evento> {


        @Override
        protected Evento doInBackground(String... urls) {
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            Evento result = Utils.traerDataDeTerremoto(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Evento evento) {
            if (evento == null) {
                return;
            }

            updateUi(evento);
        }


    }


}
