package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class AggiungiProdottiMagazzinoActivity extends AppCompatActivity {

    private static final String SELECT_PRODOTTO_IN_MAGAZZINO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php?BarCode=";
    private static final String INSERT_PRODOTTO_IN_MAGAZZINO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_product_in_warehouse.php?";

    private static final int EAN_13 = 13;

    private static final int RC_SCANNED_BC = 100;

    String scannedbc;

    TextView txtNomeProdotto;
    TextView txtMarcaProdotto;
    TextView txtCategoriaProdotto;

    EditText edtPrezzoAcquisto;
    EditText edtPrezzoVendita;
    EditText edtQuantita;

    private List<Prodotto> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_prodotti_magazzino);

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
                aggiungiProdottoMagazzino();
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
                Log.d("SCANNED_CODE", scannedbc);
                //se il prodotto scansionato esiste già in catalogo ne estraggo le info
                getProduct(SELECT_PRODOTTO_IN_MAGAZZINO, scannedbc);
            }
        }
    }

    private void getProduct(final String urlWebService, String scannedbc) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + scannedbc,
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
                });

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void aggiungiProdottoMagazzino() {

        String queryurl = "";

        edtPrezzoAcquisto = findViewById(R.id.edtPrezzoAcquisto);
        edtPrezzoVendita = findViewById(R.id.edtPrezzoVendita);
        edtQuantita = findViewById(R.id.edtQuantita);

        queryurl = INSERT_PRODOTTO_IN_MAGAZZINO + "IDProdotto=" + productList.get(0).getIDprodotto() + "&" +
                "PrezzoAcquisto=" + edtPrezzoAcquisto.getText() + "&" +
                "PrezzoVenditaAttuale=" + edtPrezzoVendita.getText() + "&" +
                "Quantita=" + edtQuantita.getText() + "&";

        StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequestAdd);

    }

}
