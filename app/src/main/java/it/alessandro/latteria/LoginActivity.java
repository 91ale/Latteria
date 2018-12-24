package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String SELECT_UTENTE_DA_UID = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_user_from_UID.php?IDUtente=";

    FirebaseUser loggeduser;

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

                SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = myPrefs.edit();
                prefsEditor.putString("FirebaseUser", loggeduser.getUid());
                prefsEditor.apply();

                //verifico se l'utente che si è loggato è gia presente nel dblatteria, nel caso non lo dovesse essere lo faccio registrare
                getUtente(SELECT_UTENTE_DA_UID + loggeduser.getUid());
            } else {
                createSignInIntent();
            }
        }

    }

    private void getUtente(final String urlWebService) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                //getting user object from json array
                                try {
                                    utenteJ = jsonArray.getJSONObject(i);

                                    Utente utente = new Utente();

                                    utente.setUID(utenteJ.getString("IDUtente"));
                                    utente.setnome(utenteJ.getString("Nome"));
                                    utente.setcognome(utenteJ.getString("Cognome"));
                                    utente.setindirizzo(utenteJ.getString("Indirizzo"));
                                    utente.settipo(utenteJ.getString("Tipo"));

                                    if (utente.getUID() == null) {
                                        Intent intentsignup = new Intent(getApplicationContext(), SignUpActivity.class);
                                        intentsignup.putExtra("UID", loggeduser.getUid());
                                        startActivity(intentsignup);
                                    } else {
                                        switch (utente.gettipo()) {

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

                                    final SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    String jsonValue = new Gson().toJson(utente);
                                    editor.putString("CurrentUser", jsonValue);
                                    editor.apply();

                                    Log.d("Utente", utente.getnome());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

}