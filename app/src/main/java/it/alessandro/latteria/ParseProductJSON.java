package it.alessandro.latteria;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ParseProductJSON {

    List<Prodotto> productList;

    private String json;

    public ParseProductJSON(String json) {

        this.json = json;
    }


    protected void getProductFromDB() {

        try {
            //converte la stringa in array JSON
            JSONArray array = new JSONArray(json);

            productList = new ArrayList<>();

            //passa da tutti gli oggetti
            for (int i = 0; i < array.length(); i++) {


                //prende il prodotto dall'array JSON
                JSONObject prodottoJ = array.getJSONObject(i);

                //aggiunge il prodotto alla lista
                productList.add(0, new Prodotto(
                        prodottoJ.getInt("IDProdotto"),
                        prodottoJ.getString("BarCode"),
                        prodottoJ.getString("Nome"),
                        prodottoJ.getString("Marca"),
                        prodottoJ.getString("Categoria"),
                        prodottoJ.getInt("QuantitaMagazzino"),
                        prodottoJ.getInt("QuantitaNegozio"),
                        prodottoJ.getString("Descrizione"),
                        prodottoJ.getDouble("PrezzoVenditaAttuale"),
                        prodottoJ.getInt("IDProdottoVenduto"),
                        prodottoJ.getInt("Quantita"),
                        prodottoJ.getString("Percorso")
                ));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    List<Prodotto> getProduct() {
        return productList;
    }

}
