package it.alessandro.latteria;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.alessandro.latteria.Adapter.ProductAdapter;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.Parser.ParseProductJSON;

import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class CercaProdottoActivity extends AppCompatActivity {

    private static final String SELECT_PRODOTTO_DA_NOME = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_name.php?Nome=";
    private static final String SELECT_PRODOTTO_DA_CATEGORIA = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_category.php?Categoria=";

    private static final int QUANTITA_SELEZIONATA = 102;
    private static final int RICERCA = 5;
    ProductAdapter mAdapter;
    private List<Prodotto> productList = new ArrayList<>();
    private ArrayList<Prodotto> sproductList = new ArrayList<>();
    private RecyclerView recyclerView;
    private int selectedproduct = 0;
    View viewForegroundOld = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerca_prodotto);

        String nome_categoria_prodotto = getIntent().getStringExtra("NOME_CATEGORIA_PRODOTTO");
        boolean nome_categoria = getIntent().getBooleanExtra("NOME_CATEGORIA", false);
        int tipospesa = getIntent().getIntExtra("TIPO_SPESA", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (nome_categoria) {
            getSupportActionBar().setTitle(nome_categoria_prodotto);
        } else {
            getSupportActionBar().setTitle("Risultati per: " + nome_categoria_prodotto);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ColorDrawable colorDrawable = new ColorDrawable(Color.GREEN);
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_ios_24px));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("PRODOTTI_SELEZIONATI", sproductList);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new ProductAdapter(CercaProdottoActivity.this, productList, tipospesa, RICERCA);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        if (nome_categoria) {
            getProduct(SELECT_PRODOTTO_DA_CATEGORIA, nome_categoria_prodotto, tipospesa);
        } else {
            getProduct(SELECT_PRODOTTO_DA_NOME, nome_categoria_prodotto, tipospesa);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == QUANTITA_SELEZIONATA) {
            if (resultCode == Activity.RESULT_OK) {
                int quantita = data.getIntExtra("QUANTITA_SELEZIONATA", -1);
                int position = data.getIntExtra("POSITION", -1);
                productList.get(position).setQuantitaOrdinata(quantita);
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
                        mAdapter.setClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                final Prodotto tempprodotto = productList.get(recyclerView.getChildAdapterPosition(v));
                                FloatingActionButton fab = findViewById(R.id.fabAggiungiProdotto);
                                if (recyclerView.getChildAdapterPosition(v) == selectedproduct && fab.getVisibility() == View.VISIBLE) {
                                    final View viewForeground = ((ProductAdapter.ProductViewHolder) recyclerView.getChildViewHolder(v)).viewForeground;
                                    viewForeground.setBackgroundColor(Color.WHITE);
                                    fab.hide();
                                } else {
                                    if (viewForegroundOld != null) viewForegroundOld.setBackgroundColor(Color.WHITE);
                                    final View viewForeground = ((ProductAdapter.ProductViewHolder) recyclerView.getChildViewHolder(v)).viewForeground;
                                    viewForeground.setBackgroundColor(Color.parseColor("#D3D3D3"));
                                    fab.show();
                                }
                                selectedproduct = recyclerView.getChildAdapterPosition(v);
                                viewForegroundOld = ((ProductAdapter.ProductViewHolder) recyclerView.getChildViewHolder(v)).viewForeground;
                                fab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View z) {
                                        sproductList.add(tempprodotto);
                                    }
                                });
                            }
                        });
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
