package it.alessandro.latteria.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.alessandro.latteria.Object.Utente;

public class ParseUserJSON {

    Utente utente;

    private String json;

    public ParseUserJSON(String json) {

        this.json = json;
    }

    public void getUserFromDB() {

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

    public Utente getUtente() {
        return utente;
    }

}
