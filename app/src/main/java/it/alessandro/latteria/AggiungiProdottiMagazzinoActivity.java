package it.alessandro.latteria;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AggiungiProdottiMagazzinoActivity extends AppCompatActivity {

    private static final String SELECT_PRODOTTO_IN_CATALOGO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php";
    private static final String INSERT_PRODOTTO_IN_MAGAZZINO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_product_in_warehouse.php";

    private static final int EAN_13 = 13;

    private static final int RC_SCANNED_BC = 100;
    private static final String BACK = "back";

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
        String messaggio = "Inquadra il codice a barre del prodotto che vuoi aggiungere al magazzino";
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
                //se il prodotto scansionato esiste già in catalogo ne estraggo le info altrimenti propongo l'aggiunta
                getProduct(SELECT_PRODOTTO_IN_CATALOGO, scannedbc);
            }
        }
    }

    private void getProduct(final String urlWebService, final String scannedbc) {
        VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlWebService,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        //verifica se il prodotto è presente a catalogo, se non lo è chiede all'utente se vuole aggiungerlo
                        if (productList.size() > 0) {
                            txtNomeProdotto = findViewById(R.id.txtNomeProdotto);
                            txtMarcaProdotto = findViewById(R.id.txtMarcaProdotto);
                            txtCategoriaProdotto = findViewById(R.id.txtCategoriaProdotto);

                            txtNomeProdotto.setText(productList.get(0).getNome());
                            txtMarcaProdotto.setText(productList.get(0).getMarca());
                            txtCategoriaProdotto.setText(productList.get(0).getCategoria());
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(AggiungiProdottiMagazzinoActivity.this).create();
                            alertDialog.setTitle("Prodotto non presente in catalogo");
                            alertDialog.setMessage("Il prodotto scansionato non è presente in catalogo, si desidera aggiungerlo ora?");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SI",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intentaggiungicatalogo = new Intent(getBaseContext(), AggiungiProdottiAlCatalogoActivity.class);
                                            intentaggiungicatalogo.putExtra("BAR_CODE", scannedbc);
                                            startActivity(intentaggiungicatalogo);
                                            finish();
                                        }
                                    });
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                            alertDialog.show();
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

    private void aggiungiProdottoMagazzino() {

        edtPrezzoAcquisto = findViewById(R.id.edtPrezzoAcquisto);
        edtPrezzoVendita = findViewById(R.id.edtPrezzoVendita);
        edtQuantita = findViewById(R.id.edtQuantita);

        NumberFormat format = NumberFormat.getInstance(Locale.ITALY);
        Number pacquisto = null;
        Number pvendita = null;
        try {
            pacquisto = format.parse(edtPrezzoAcquisto.getText().toString());
            pvendita = format.parse(edtPrezzoVendita.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final double dpacquisto = pacquisto.doubleValue();
        final double dpvendita = pvendita.doubleValue();

        StringRequest stringRequestAdd = new StringRequest(Request.Method.POST, INSERT_PRODOTTO_IN_MAGAZZINO,
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
                params.put("PrezzoAcquisto",String.valueOf(dpacquisto));
                params.put("PrezzoVenditaAttuale",String.valueOf(dpvendita));
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
