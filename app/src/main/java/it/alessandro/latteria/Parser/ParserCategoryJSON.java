package it.alessandro.latteria.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParserCategoryJSON {

    private List<String> Categorie;

    private String json;

    public ParserCategoryJSON(String json) {

        this.json = json;
    }

    public void getCategoriaFromDB() {

        try {
            //converte la stringa in array JSON
            JSONArray array = new JSONArray(json);

            Categorie = new ArrayList<>();

            //passa da tutti gli oggetti
            for (int i = 0; i < array.length(); i++) {


                //prende il prodotto dall'array JSON
                JSONObject categoriaJ = array.getJSONObject(i);

                //aggiunge il prodotto alla lista
                Categorie.add(categoriaJ.getString("Categoria"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<String> getCategorie() {
        return Categorie;
    }

}