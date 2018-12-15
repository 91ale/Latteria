package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MioProfiloActivity extends AppCompatActivity {

    private static final String INSERT_USER = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_user.php?";

    EditText edtNome;
    EditText edtCognome;
    EditText edtIndirizzo;
    EditText edtPassword;
    Button btnModificaProfilo;
    Button btnSalva;
    Button btnCambiaPassword;
    Button btnSalvaPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mio_profilo);

        Utente utente = getUtenteLoggato();

        Log.d("Utente", utente.getnome());

        edtNome = findViewById(R.id.edtNome);
        edtCognome = findViewById(R.id.edtCognome);
        edtIndirizzo = findViewById(R.id.edtIndirizzo);
        edtPassword = findViewById(R.id.edtPassword);
        btnModificaProfilo = findViewById(R.id.btnModificaProfilo);
        btnSalva = findViewById(R.id.btnSalva);
        btnSalva.setVisibility(View.INVISIBLE);
        btnCambiaPassword = findViewById(R.id.btnCambiaPassword);
        btnSalvaPassword = findViewById(R.id.btnSalvaPassword);
        btnSalvaPassword.setVisibility(View.INVISIBLE);

        edtNome.setText(utente.getnome());
        edtCognome.setText(utente.getcognome());
        edtIndirizzo.setText(utente.getindirizzo());

        edtNome.setEnabled(false);
        edtCognome.setEnabled(false);
        edtIndirizzo.setEnabled(false);

        btnModificaProfilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtNome.setEnabled(true);
                edtCognome.setEnabled(true);
                edtIndirizzo.setEnabled(true);
                btnSalva.setVisibility(View.VISIBLE);
                btnModificaProfilo.setVisibility(View.INVISIBLE);
                btnCambiaPassword.setVisibility(View.INVISIBLE);
            }
        });

        btnSalva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtNome.setEnabled(false);
                edtCognome.setEnabled(false);
                edtIndirizzo.setEnabled(false);
                Utente utente = getUtenteLoggato();
                utente.setnome(edtNome.getText().toString());
                utente.setcognome(edtCognome.getText().toString());
                utente.setindirizzo(edtIndirizzo.getText().toString());
                insertUser(INSERT_USER+"IDUtente="+utente.getUID()+"&Nome="+utente.getnome()+"&Cognome="+utente.getcognome()+"&Indirizzo="+utente.getindirizzo()+"&Tipo="+utente.gettipo());
                setUtenteLoggato(utente);
                btnSalva.setVisibility(View.INVISIBLE);
                btnModificaProfilo.setVisibility(View.VISIBLE);
                btnCambiaPassword.setVisibility(View.VISIBLE);
            }
        });

        btnCambiaPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSalvaPassword.setVisibility(View.VISIBLE);
                btnCambiaPassword.setVisibility(View.INVISIBLE);
            }
        });

        btnSalvaPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFirebasePassword (edtPassword.getText().toString());
                btnSalvaPassword.setVisibility(View.INVISIBLE);
                btnCambiaPassword.setVisibility(View.VISIBLE);
            }
        });

    }

    private Utente getUtenteLoggato() {

        final SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String value = prefs.getString("CurrentUser", "");
        Utente utente = new Gson().fromJson(value, Utente.class);

        return utente;

    }

    private void setUtenteLoggato(Utente utente) {

        final SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String jsonValue = new Gson().toJson(utente);
        editor.putString("CurrentUser", jsonValue);
        editor.apply();
    }

    private void insertUser(final String urlWebService) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void setFirebasePassword (String password) {
        FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseuser.updatePassword(password);
    }

   /* private void insertUser(final Utente Utente) {

        class insertUser extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

                Intent intenttipospesa = new Intent(getApplicationContext(), TipoSpesaActivity.class);
                startActivity(intenttipospesa);

            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(INSERT_USER+"IDUtente="+Utente.getUID()+"&Nome="+Utente.getnome()+"&Cognome="+Utente.getcognome()+"&Indirizzo="+Utente.getindirizzo()+"&Tipo="+Utente.gettipo());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String result;
                    while ((result = bufferedReader.readLine()) != null) {
                        sb.append(result + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }

        insertUser insertUser = new insertUser();
        insertUser.execute();

    }*/

}
