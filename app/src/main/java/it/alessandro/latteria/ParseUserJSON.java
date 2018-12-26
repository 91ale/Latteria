package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParseUserJSON {

    Utente utente;

    private String json;

    public ParseUserJSON(String json) {

        this.json = json;
    }

    protected void getUserFromDB() {

        try {
            //converte la stringa in array JSON
            JSONArray array = new JSONArray(json);

            //passa da tutti gli oggetti
            for (int i = 0; i < array.length(); i++) {


                //prende il prodotto dall'array JSON
                JSONObject utenteJ = array.getJSONObject(i);

                //aggiunge il prodotto alla lista
                utente = new Utente(
                        utenteJ.getString("IDUtente"),
                        utenteJ.getString("Nome"),
                        utenteJ.getString("Cognome"),
                        utenteJ.getString("Indirizzo"),
                        utenteJ.getString("Tipo")
                );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Utente getUtente() {
        return utente;
    }

}
