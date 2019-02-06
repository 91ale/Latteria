package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import it.alessandro.latteria.Object.Utente;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String INSERT_USER = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_user.php";

    Utente Utente = new Utente();

    EditText edtNome;
    EditText edtCognome;
    EditText edtIndirizzo;
    Button btnRegistrati;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtNome = findViewById(R.id.edtNome);
        edtCognome = findViewById(R.id.edtCognome);
        edtIndirizzo = findViewById(R.id.edtIndirizzo);
        btnRegistrati = findViewById(R.id.btnSignUp);

        btnRegistrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentsignup = getIntent();
                Utente.setUID(intentsignup.getExtras().getString("UID"));
                Utente.setnome(edtNome.getText().toString());
                Utente.setcognome(edtCognome.getText().toString());
                Utente.setindirizzo(edtIndirizzo.getText().toString());
                Utente.settipo("cliente");

                insertUser(Utente);
            }
        });
    }

    private void insertUser(final Utente utente) {
        VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent intenttipospesa = new Intent(getApplicationContext(), TipoSpesaActivity.class);
                        startActivity(intenttipospesa);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                Log.d("UID",utente.getUID());
                params.put("IDUtente",utente.getUID());
                params.put("Nome",utente.getnome());
                params.put("Cognome",utente.getcognome());
                params.put("Indirizzo",utente.getindirizzo());
                params.put("Tipo",utente.gettipo());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);

    }

    /*private void insertUser(final Utente Utente) {

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
                    URL url = new URL(INSERT_USER + "IDUtente=" + Utente.getUID() + "&Nome=" + Utente.getnome() + "&Cognome=" + Utente.getcognome() + "&Indirizzo=" + Utente.getindirizzo() + "&Tipo=" + Utente.gettipo());
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
