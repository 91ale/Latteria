package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MioProfiloActivity extends AppCompatActivity {

    private static final String SELECT_UTENTE_DA_UID = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_user_from_UID.php?IDUtente=";

    String loggeduser = "";

    TextView txtNomeCognome;
    TextView txtIndirizzo;
    Button btnModificaProfilo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mio_profilo);

        //ricava l'ID dell'utente loggato (loggeduser) ed un eventuale ordine da caricare (idordine) dalle SharedPreferences
        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggeduser = myPrefs.getString("logged_user", "0");

        //getUtente(SELECT_UTENTE_DA_UID + loggeduser);
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);

        final SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String value = prefs.getString("Utente", "");
        Utente utente = new Gson().fromJson(value, Utente.class);

        Log.d("Utente", utente.getnome());

        txtNomeCognome = findViewById(R.id.txtNomeCognome);
        txtIndirizzo = findViewById(R.id.txtIndirizzo);
        btnModificaProfilo = findViewById(R.id.btnModificaProfilo);

        txtNomeCognome.setText(utente.getnome() + " " + utente.getcognome());
        txtIndirizzo.setText(utente.getindirizzo());

        btnModificaProfilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
