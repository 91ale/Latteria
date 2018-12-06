package it.alessandro.latteria;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String SELECT_FROM_UID = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_user_from_UID.php?IDUtente=";

    FirebaseUser loggeduser;
    Utente Utente = new Utente();

    JSONObject utenteJ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createSignInIntent();

    }

    //Login utente
    public void createSignInIntent() {
        // lista dei providers supportati
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        //crea e lancia l'intent sign-in
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false) //per test, disabilita smart lock
                        .setAvailableProviders(providers)
                        //.setLogo(R.drawable.my_great_logo)      // Set logo drawable
                        //.setTheme(R.style.MySuperAppTheme)      // Set theme
                        .build(),
                RC_SIGN_IN);
    }

    //RC_SIGN_IN risultato login utente
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                loggeduser = FirebaseAuth.getInstance().getCurrentUser();
                Log.d("NOME", loggeduser.getDisplayName());
                Log.d("EMAIL", loggeduser.getEmail());
                Log.d("UID", loggeduser.getUid());
                //verifico se l'utente che si è loggato è gia presente nel dblatteria, nel caso non lo dovesse essere lo faccio registrare
                getUtente(SELECT_FROM_UID + loggeduser.getUid());
            } else {
                createSignInIntent();
            }
        }

    }

    private void getUtente(final String urlWebService) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected void onPostExecute(String utente) {
                super.onPostExecute(utente);
                Toast.makeText(getApplicationContext(), utente, Toast.LENGTH_SHORT).show();
                try {
                    JSONArray jsonArray = new JSONArray(utente);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        //getting user object from json array
                        try {
                            utenteJ = jsonArray.getJSONObject(i);

                            Utente.setUID(utenteJ.getString("IDUtente"));
                            Utente.setnome(utenteJ.getString("Nome"));
                            Utente.setcognome(utenteJ.getString("Cognome"));
                            Utente.setindirizzo(utenteJ.getString("Indirizzo"));
                            Utente.settipo(utenteJ.getString("Tipo"));
                            Log.d("Utente", Utente.getnome());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (Utente.getUID() == null) {
                    Intent intentsignup = new Intent(getApplicationContext(), SignUpActivity.class);
                    intentsignup.putExtra("UID",loggeduser.getUid());
                    startActivity(intentsignup);
                } else {
                    switch (Utente.gettipo()) {

                        case "cliente":
                            Intent intenttipospesa = new Intent(getApplicationContext(), TipoSpesaActivity.class);
                            intenttipospesa.putExtra("LOGGED_UID", loggeduser.getUid());
                            startActivity(intenttipospesa);
                            break;

                        case "commesso":
                            Intent intentcommesso = new Intent(getApplicationContext(), CommessoActivity.class);
                            startActivity(intentcommesso);
                            break;
                    }

                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }

        GetJSON getJSON = new GetJSON();
        getJSON.execute();

    }

}