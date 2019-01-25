package it.alessandro.latteria;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import it.alessandro.latteria.Object.Utente;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private static final String INSERT_USER = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_user.php?";

    it.alessandro.latteria.Object.Utente Utente = new Utente();

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

    private void insertUser(final Utente Utente) {

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

    }
}
