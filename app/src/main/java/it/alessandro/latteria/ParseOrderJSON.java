package it.alessandro.latteria;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ParseOrderJSON {

    List<Ordine> orderList;

    private String json;

    public ParseOrderJSON(String json) {

        this.json = json;
    }


    protected void getOrderFromDB() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            //converte la stringa in array JSON
            JSONArray array = new JSONArray(json);

            orderList = new ArrayList<>();

            //passa da tutti gli oggetti
            for (int i = 0; i < array.length(); i++) {


                //prende l'ordine dall'array JSON
                JSONObject ordineJ = array.getJSONObject(i);

                //aggiunge l'ordine alla lista
                orderList.add(0, new Ordine(
                        ordineJ.getInt("IDOrdine"),
                        sdf.parse(ordineJ.getString("DataOra")),
                        ordineJ.getString("Stato"),
                        ordineJ.getString("Tipo"),
                        ordineJ.getDouble("Importo")
                ));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    List<Ordine> getOrder() {
        return orderList;
    }

}
