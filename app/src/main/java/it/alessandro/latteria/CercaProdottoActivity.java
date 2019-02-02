package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.alessandro.latteria.Adapter.ProductAdapter;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.Parser.ParseProductJSON;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class CercaProdottoActivity extends AppCompatActivity {

    private static final String SELECT_PRODOTTO_DA_NOME = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_name.php?Nome=";
    private static final String SELECT_PRODOTTO_DA_CATEGORIA = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_category.php?Categoria=";

    private static final int QUANTITA_SELEZIONATA = 102;
    private static final int RICERCA = 5;
    ProductAdapter mAdapter;
    private List<Prodotto> productList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerca_prodotto);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String nomemarca = getIntent().getStringExtra("NOME_MARCA_PRODOTTO");
        int tipospesa = getIntent().getIntExtra("TIPO_SPESA", -1);

        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new ProductAdapter(CercaProdottoActivity.this, productList, tipospesa);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        getProduct(SELECT_PRODOTTO_DA_NOME, nomemarca, tipospesa);
        getProduct(SELECT_PRODOTTO_DA_CATEGORIA, nomemarca, tipospesa);
        //getProduct(SELECT_PRODOTTO_DA_MARCA, nomemarca);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == QUANTITA_SELEZIONATA) {
            if (resultCode == Activity.RESULT_OK) {
                int quantita = data.getIntExtra("QUANTITA_SELEZIONATA", -1);
                int position = data.getIntExtra("POSITION", -1);
                productList.get(position).setQuantitàOrdinata(quantita);
                Intent intent = new Intent();
                intent.putExtra("PRODOTTO_SELEZIONATO", productList.get(position));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }

    private void getProduct(final String urlWebService, String nome, final int tipospesa) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + nome,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new ProductAdapter(CercaProdottoActivity.this, productList, tipospesa, RICERCA);
                        recyclerView.setAdapter(mAdapter);
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

}
