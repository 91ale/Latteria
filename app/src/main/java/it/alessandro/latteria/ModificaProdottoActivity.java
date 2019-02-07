package it.alessandro.latteria;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.Parser.ParseProductJSON;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModificaProdottoActivity extends AppCompatActivity {

    private static final String UPDATE_PRODOTTO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_product_from_PID_manual.php";
    private static final String SELECT_PRODOTTO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_PID.php?IDProdotto=";

    EditText edtNome;
    EditText edtMarca;
    EditText edtCategoria;
    EditText edtPrezzoVendita;
    EditText edtQuantitaNegozio;
    EditText edtQuantitaMagazzino;
    EditText edtDescrizione;

    private List<Prodotto> productList = new ArrayList<>();

    DecimalFormat pdec = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifica_prodotto);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        int PID = intent.getIntExtra("PID", -1);

        getProduct(SELECT_PRODOTTO, PID);

        Button btnModificaProdotto = findViewById(R.id.btnModificaProdotto);

        btnModificaProdotto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificaProdotto();
                finish();
            }
        });
    }

    private void getProduct(final String urlWebService, int PID) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + PID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        if (productList.size() > 0) {
                            edtNome = findViewById(R.id.edtNome);
                            edtMarca = findViewById(R.id.edtMarca);
                            edtCategoria = findViewById(R.id.edtCategoria);
                            edtDescrizione = findViewById(R.id.edtDescrizione);
                            edtPrezzoVendita = findViewById(R.id.edtPrezzoVendita);
                            edtQuantitaNegozio = findViewById(R.id.edtQuantitaNegozio);
                            edtQuantitaMagazzino = findViewById(R.id.edtQuantitaMagazzino);

                            edtNome.setText(productList.get(0).getNome());
                            edtMarca.setText(productList.get(0).getMarca());
                            edtCategoria.setText(productList.get(0).getCategoria());
                            edtPrezzoVendita.setText(pdec.format(productList.get(0).getPrezzovenditaAttuale()));
                            edtQuantitaNegozio.setText(String.valueOf(productList.get(0).getQuantitanegozio()));
                            edtQuantitaMagazzino.setText(String.valueOf(productList.get(0).getQuantitamagazzino()));
                            edtDescrizione.setText(productList.get(0).getDescrizione());

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

    private void modificaProdotto() {

        //String queryurl = "";

        edtNome = findViewById(R.id.edtNome);
        edtMarca = findViewById(R.id.edtMarca);
        edtCategoria = findViewById(R.id.edtCategoria);
        edtDescrizione = findViewById(R.id.edtDescrizione);
        edtPrezzoVendita = findViewById(R.id.edtPrezzoVendita);
        edtQuantitaNegozio = findViewById(R.id.edtQuantitaNegozio);
        edtQuantitaMagazzino = findViewById(R.id.edtQuantitaMagazzino);

        NumberFormat format = NumberFormat.getInstance(Locale.ITALY);
        Number pvendita = null;
        try {
            pvendita = format.parse(edtPrezzoVendita.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final double dpvendita = pvendita.doubleValue();

        /*queryurl = UPDATE_PRODOTTO + "IDProdotto=" + productList.get(0).getIDprodotto() + "&" +
                "Nome=" + edtNome.getText() + "&" +
                "Marca=" + edtMarca.getText() + "&" +
                "Categoria=" + edtCategoria.getText() + "&" +
                "Descrizione=" + edtDescrizione.getText() + "&" +
                "PrezzoVenditaAttuale=" + dpvendita + "&" +
                "QuantitaNegozio=" + edtQuantitaNegozio.getText() + "&" +
                "QuantitaMagazzino=" + edtQuantitaMagazzino.getText();*/

        StringRequest stringRequestAdd = new StringRequest(Request.Method.POST, UPDATE_PRODOTTO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

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
                params.put("IDProdotto", String.valueOf(productList.get(0).getIDprodotto()));
                params.put("Nome",edtNome.getText().toString());
                params.put("Marca",edtMarca.getText().toString());
                params.put("Categoria",edtCategoria.getText().toString());
                params.put("Descrizione",edtDescrizione.getText().toString());
                params.put("PrezzoVenditaAttuale",String.valueOf(dpvendita));
                params.put("QuantitaNegozio",edtQuantitaNegozio.getText().toString());
                params.put("QuantitaMagazzino",edtQuantitaMagazzino.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequestAdd);

    }
}
