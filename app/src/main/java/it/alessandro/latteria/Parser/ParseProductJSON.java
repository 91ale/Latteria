package it.alessandro.latteria.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.alessandro.latteria.Object.Prodotto;


public class ParseProductJSON {

    private List<Prodotto> productList;

    private String json;

    public ParseProductJSON(String json) {
        this.json = json;
    }

    public void getProductFromDB() {

        try {
            JSONArray array = new JSONArray(json);

            productList = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {

                JSONObject prodottoJ = array.getJSONObject(i);

                productList.add(0, new Prodotto(
                        prodottoJ.getInt("IDProdotto"),
                        prodottoJ.getString("BarCode"),
                        prodottoJ.getString("Nome"),
                        prodottoJ.getString("Marca"),
                        prodottoJ.getString("Categoria"),
                        prodottoJ.optInt("QuantitaMagazzino"),
                        prodottoJ.optInt("QuantitaNegozio"),
                        prodottoJ.getString("Descrizione"),
                        prodottoJ.optDouble("PrezzoVenditaAttuale"),
                        prodottoJ.optInt("IDProdottoVenduto"),
                        prodottoJ.optInt("Quantita"),
                        prodottoJ.getString("Percorso")
                ));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Prodotto> getProduct() {
        return productList;
    }

}
