package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.Parser.ParseProductJSON;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggiungiProdottiNegozioActivity extends AppCompatActivity {

    private static final String SELECT_PRODOTTO_IN_CATALOGO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php";
    private static final String INSERT_PRODOTTO_IN_NEGOZIO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_product_in_shop.php";

    private static final int EAN_13 = 13;

    private static final int RC_SCANNED_BC = 100;
    private static final String BACK = "back";

    String scannedbc;

    TextView txtNomeProdotto;
    TextView txtMarcaProdotto;
    TextView txtCategoriaProdotto;

    EditText edtQuantita;

    private List<Prodotto> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_prodotti_negozio);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        scanBarCode();

        Button btnAggiungiProdotto = findViewById(R.id.btnAggiungiProdotto);

        btnAggiungiProdotto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aggiungiProdottoNegozio();
                finish();
            }
        });

    }

    private void scanBarCode() {

        Intent intentscanbarcode = new Intent(this, ScanBarcodeActivity.class);
        String messaggio = "Inquadra il codice a barre del prodotto che vuoi aggiungere al catalogo";
        intentscanbarcode.putExtra("TIPO_CODICE", EAN_13);
        intentscanbarcode.putExtra("MESSAGGIO", messaggio);
        startActivityForResult(intentscanbarcode, RC_SCANNED_BC);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SCANNED_BC) {
            if (resultCode == Activity.RESULT_OK) {
                scannedbc = data.getStringExtra("SCANNED_CODE");
                if (scannedbc.equals(BACK)) finish();
                Log.d("SCANNED_CODE", scannedbc);
                //se il prodotto scansionato esiste già in catalogo ne estraggo le info
                getProduct(SELECT_PRODOTTO_IN_CATALOGO, scannedbc);
            }
        }
    }

    private void getProduct(final String urlWebService, final String scannedbc) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlWebService,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        if (productList.size() > 0) {
                            txtNomeProdotto = findViewById(R.id.txtNomeProdotto);
                            txtMarcaProdotto = findViewById(R.id.txtMarcaProdotto);
                            txtCategoriaProdotto = findViewById(R.id.txtCategoriaProdotto);

                            txtNomeProdotto.setText(productList.get(0).getNome());
                            txtMarcaProdotto.setText(productList.get(0).getMarca());
                            txtCategoriaProdotto.setText(productList.get(0).getCategoria());
                        }
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
                params.put("BarCode",scannedbc);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void aggiungiProdottoNegozio() {

        //String queryurl = "";

        edtQuantita = findViewById(R.id.edtQuantita);

        /*queryurl = INSERT_PRODOTTO_IN_NEGOZIO + "IDProdotto=" + productList.get(0).getIDprodotto() + "&" +
                "Quantita=" + edtQuantita.getText();*/

        StringRequest stringRequestAdd = new StringRequest(Request.Method.POST, INSERT_PRODOTTO_IN_NEGOZIO,
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
                params.put("IDProdotto",String.valueOf(productList.get(0).getIDprodotto()));
                params.put("Quantita",edtQuantita.getText().toString());
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
