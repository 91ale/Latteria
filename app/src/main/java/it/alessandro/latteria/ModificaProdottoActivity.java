package it.alessandro.latteria;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class ModificaProdottoActivity extends AppCompatActivity {

    private static final String UPDATE_PRODOTTO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_product_from_PID.php?";
    private static final String SELECT_PRODOTTO = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_PID.php?IDProdotto=";

    EditText edtNome;
    EditText edtMarca;
    EditText edtCategoria;
    EditText edtPrezzoVendita;
    EditText edtQuantitaNegozio;
    EditText edtQuantitaMagazzino;
    EditText edtDescrizione;

    private List<Prodotto> productList = new ArrayList<>();

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
                            edtPrezzoVendita.setText(String.valueOf(productList.get(0).getPrezzovenditaAttuale()));
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

        String queryurl = "";

        edtNome = findViewById(R.id.edtNome);
        edtMarca = findViewById(R.id.edtMarca);
        edtCategoria = findViewById(R.id.edtCategoria);
        edtDescrizione = findViewById(R.id.edtDescrizione);
        edtPrezzoVendita = findViewById(R.id.edtPrezzoVendita);
        edtQuantitaNegozio = findViewById(R.id.edtQuantitaNegozio);
        edtQuantitaMagazzino = findViewById(R.id.edtQuantitaMagazzino);

        queryurl = UPDATE_PRODOTTO + "IDProdotto=" + productList.get(0).getIDprodotto() + "&" +
                "Nome=" + edtNome.getText() + "&" +
                "Marca=" + edtMarca.getText() + "&" +
                "Categoria=" + edtCategoria.getText() + "&" +
                "Descrizione=" + edtDescrizione.getText() + "&" +
                "PrezzoVenditaAttuale=" + edtPrezzoVendita.getText() + "&" +
                "QuantitaNegozio=" + edtQuantitaNegozio.getText() + "&" +
                "QuantitaMagazzino=" + edtQuantitaMagazzino.getText();

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
