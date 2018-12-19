package it.alessandro.latteria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SpesaCommessoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String SELECT_PRODOTTO_DA_BARCODE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php?BarCode=";
    private static final String INSERT_PRODOTTI_VENDUTI = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_ordered_products.php?";
    private static final String SELECT_PRODOTTI_DA_ORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_products_from_orderid.php?IDOrdine=";
    private static final String DELETE_PRODOTTI_DA_ORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/delete_product_from_order.php?IDProdottoVenduto=";
    private static final String UPDATE_ORDINE_IMPORTO_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_order_total_from_orderid.php?";

    String loggeduser = "";

    private MaterialSearchBar searchBar;
    private DrawerLayout drawerLayout;

    private static final int RC_SCANNED_BC = 100;
    private static final int QUANTITA_SELEZIONATA = 102;
    private static final int PRODOTTO_SELEZIONATO = 103;

    private static final int QR = 14;
    private static final int RC_SCANNED_QR = 105;

    private static final int AGGIUNGI = 1;
    private static final int SOSTITUISCI = 2;

    private static final int ONLINE = 2;

    private static final int COMPLETATO = 1;

    private static final int EAN_13 = 13;


    private List<Prodotto> productList = new ArrayList<>();
    private List<Prodotto> rproductList = new ArrayList<>();
    ProductAdapter mAdapter;
    private RecyclerView recyclerView;
    TextView txtPrezzoTotale;

    DecimalFormat pdec = new DecimalFormat("€ 0.00");

    int tipospesa = 0;
    int statoordine = 0;
    int idordine = 0;

    public interface VolleyCallBack {
        void onSuccess(String response);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spesa_commesso);

        //valorizza la variabile in base alla selezione effettuata dall'utente nell'activity TipoSpesaActivity (IN_NEGOZIO | ONLINE)
        tipospesa = getIntent().getIntExtra("TIPO_SPESA", -1);
        //valorizza la variabile in base allo stato dell'ordine (COMPLETATO | IN_CORSO)
        statoordine = getIntent().getIntExtra("STATO_ORDINE", -1);
        //valorizza la variabile con l'ID Ordine passato
        idordine = getIntent().getIntExtra("ID_ORDINE", -1);
        //ricava l'ID dell'utente loggato (loggeduser) ed un eventuale ordine da caricare (idordine) dalle SharedPreferences
        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggeduser = myPrefs.getString("FirebaseUser", "0");

        //imposta il navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Barra di ricerca importata da https://github.com/mancj/MaterialSearchBar
        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        //se l'ordine è in stato COMPLETATO nasconde la barra di ricerca
        if (statoordine == COMPLETATO) searchBar.setVisibility(View.INVISIBLE);
        searchBar.setOnSearchActionListener(this);
        //abilita l'icona per la scansione del codice a barre
        searchBar.setSpeechMode(true);
        Log.d("LOG_TAG", getClass().getSimpleName() + ": text " + searchBar.getText());

        searchBar.setCardViewElevation(10);

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchBar.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        recyclerView = findViewById(R.id.recycler_view);

        txtPrezzoTotale = findViewById(R.id.txtPrezzoTotale);
        //imposta il formato del prezzo totale nel modo seguente € 0,00
        txtPrezzoTotale.setText(pdec.format(0.00));

        //assegna l'adapter alla recyclerview
        mAdapter = new ProductAdapter(this, productList, tipospesa, statoordine);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //collega l'ItemTouchHelper alla recyclerview (necessario per rilevare lo swipe di eliminazione prodotto)
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        caricaOrdine(idordine);

        //riceve gli intent inviati in dalla classe ProductAdapter quando viene modificata la quantità dallo spinner
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("quantita_modificata"));

        Button btnCompletaOrdine = findViewById(R.id.btnCompletaOrdine);
        if (statoordine == COMPLETATO){
            btnCompletaOrdine.setVisibility(View.INVISIBLE);
        } else if (tipospesa == ONLINE) btnCompletaOrdine.setText("COMPLETA L'ORDINE");

        btnCompletaOrdine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aggiungiOrdine("In attesa di pagamento", idordine, new SpesaCommessoActivity.VolleyCallBack() {
                    @Override
                    public void onSuccess(String response) {

                    }
                });
                Intent intentordinecompletato = new Intent(getApplicationContext(), OrdineCompletatoActivity.class);
                intentordinecompletato.putExtra("ID_ORDINE", idordine);
                intentordinecompletato.putExtra("TOTALE_ORDINE", mAdapter.sumAllItem());
                startActivity(intentordinecompletato);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Rileva quale voce del menu è stata selezionata ed avvia l'activity corrispondente
        int id = item.getItemId();

        if (id == R.id.nav_spesa) {
            Intent intentscanqrcode = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
            String messaggio = "Inquadra lo schermo del dispositivo del cliente";
            intentscanqrcode.putExtra("TIPO_CODICE", QR);
            intentscanqrcode.putExtra("MESSAGGIO", messaggio);
            startActivityForResult(intentscanqrcode,RC_SCANNED_QR);
        } else if (id == R.id.nav_prodotti) {
            Intent intentprofilo = new Intent(getApplicationContext(), MioProfiloActivity.class);
            startActivity(intentprofilo);
        } else if (id == R.id.nav_ordini_online) {
            Intent intentordini = new Intent(getApplicationContext(), OrdiniActivity.class);
            startActivity(intentordini);
        } else if (id == R.id.nav_ordini_conclusi) {
            Intent intentaiuto = new Intent(getApplicationContext(), AiutoActivity.class);
            startActivity(intentaiuto);
        } else if (id == R.id.nav_logout) {
            SignOut();
            Intent intentlogin = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intentlogin);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        FrameLayout searchBackground = findViewById(R.id.search_transparent_background);
        if (enabled == true) {
            searchBackground.setVisibility(View.VISIBLE);
        }
        else
        {
            searchBackground.setVisibility(View.GONE);
        }

    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        Intent intentcercaprodotto = new Intent(this, CercaProdottoActivity.class);
        intentcercaprodotto.putExtra("NOME_MARCA_PRODOTTO", text.toString());
        intentcercaprodotto.putExtra("TIPO_SPESA", tipospesa);
        searchBar.disableSearch();
        startActivityForResult(intentcercaprodotto, PRODOTTO_SELEZIONATO);
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                drawerLayout.openDrawer(Gravity.LEFT);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                Intent intentscanbarcode = new Intent(this, ScanBarcodeActivity.class);
                String messaggio = "Inquadra il codice a barre del prodotto che vuoi acquistare";
                intentscanbarcode.putExtra("TIPO_CODICE", EAN_13);
                intentscanbarcode.putExtra("MESSAGGIO", messaggio);
                startActivityForResult(intentscanbarcode,RC_SCANNED_BC);
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.disableSearch();
                break;
        }
    }

    //Logout utente
    private void SignOut () {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==RC_SCANNED_BC) {
            if (resultCode == Activity.RESULT_OK) {
                String scannedbc = data.getStringExtra("SCANNED_CODE");
                Log.d("SCANNED_CODE",scannedbc);
                //se il prodotto è stato aggiunto precedentemente alla lista lo incrementa di 1
                int exist = checkExistInList(scannedbc, 1, AGGIUNGI);
                //altrimenti acquisico le info dal DB e lo aggiungo alla lista
                if (exist == 0) getProduct(SELECT_PRODOTTO_DA_BARCODE, scannedbc);
            }
        }else if (requestCode==QUANTITA_SELEZIONATA) {
            if (resultCode == Activity.RESULT_OK) {
                int quantita = data.getIntExtra("QUANTITA_SELEZIONATA",-1);
                int position = data.getIntExtra("POSITION",-1);
                productList.get(position).setQuantitàOrdinata(quantita);
                //crea l'adapter e lo assegna alla recycleview
                mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine);
                double totalespesa = mAdapter.sumAllItem();
                txtPrezzoTotale.setText(pdec.format(totalespesa));
                recyclerView.setAdapter(mAdapter);
            }
        }else if (requestCode==PRODOTTO_SELEZIONATO) {
            if (resultCode == Activity.RESULT_OK) {
                Prodotto Prodotto = (Prodotto)data.getSerializableExtra("PRODOTTO_SELEZIONATO");
                if (checkExistInList(Prodotto.getBarCode(),  Prodotto.getQuantitàOrdinata(), SOSTITUISCI) != 1 ) {
                    productList.add(Prodotto);
                    mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine);
                    double totalespesa = mAdapter.sumAllItem();
                    txtPrezzoTotale.setText(pdec.format(totalespesa));
                    recyclerView.setAdapter(mAdapter);
                }
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
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine);
                        double totalespesa = mAdapter.sumAllItem();
                        txtPrezzoTotale.setText(pdec.format(totalespesa));
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

    /**
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ProductAdapter.ProductViewHolder) {
            // acquisisce il nome dell'oggetto eliminato per visualizzarlo sulla snackbar
            String prodotto = productList.get(viewHolder.getAdapterPosition()).getMarca().toUpperCase() + " " + productList.get(viewHolder.getAdapterPosition()).getNome().toUpperCase();

            // backup dell'oggetto rimosso per un eventuale ripristino ed aggiunta alla lista dei prodotti rimossi
            final Prodotto deletedItem = productList.get(viewHolder.getAdapterPosition());
            rproductList.add(deletedItem);
            final int deletedIndex = viewHolder.getAdapterPosition();

            // rimuove l'oggetto dalla recycler e dalla lista prodotti
            mAdapter.removeItem(viewHolder.getAdapterPosition());
            double totalespesa = mAdapter.sumAllItem();
            txtPrezzoTotale.setText(pdec.format(totalespesa));


            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(drawerLayout, prodotto + " è stato rimosso dalla spesa", Snackbar.LENGTH_LONG);
            snackbar.setAction("ANNULLA", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // ANNULA selezionato, ripristino il prodotto e lo elimino dalla lista dei rimossi
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                    rproductList.remove(deletedIndex);
                    double totalespesa = mAdapter.sumAllItem();
                    txtPrezzoTotale.setText(pdec.format(totalespesa));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    //aggiorna il prezzo totale quando viene modificata la quantità di un prodotto
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            txtPrezzoTotale.setText(String.valueOf(pdec.format(mAdapter.sumAllItem())));
        }
    };

    private int checkExistInList (String scannedbc, int quantita, int operazione) {

        int f = 0;

        for (int i=0; i<=productList.size()-1; i++) {
            if (productList.get(i).getBarCode().equals(scannedbc)) {
                if (operazione == AGGIUNGI) {
                    productList.get(i).setQuantitàOrdinata(productList.get(i).getQuantitàOrdinata()+quantita);
                } else if (operazione == SOSTITUISCI) {
                    productList.get(i).setQuantitàOrdinata(quantita);
                }
                mAdapter.setListItems(productList);
                mAdapter.notifyDataSetChanged();
                txtPrezzoTotale.setText(String.valueOf(pdec.format(mAdapter.sumAllItem())));
                f = 1;
                break;
            }
        }
        return f;
    }

    private void aggiungiOrdine (String stato, final int idordine, final SpesaCommessoActivity.VolleyCallBack callback) {

        String queryurl = "";

        //se sono stati rimossi prodotti dalla recycleview li elimino anche dal DB
        if (rproductList.size()>0) {
            for (int i=0; i<rproductList.size(); i++) {
                queryurl = DELETE_PRODOTTI_DA_ORDINE + rproductList.get(i).getIdprodottovenduto();

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

        //aggiorno l'importo dell'ordine ed inserisco gli eventuali prodotti aggiunti dal commesso
        queryurl = UPDATE_ORDINE_IMPORTO_DA_IDORDINE + "IDOrdine=" + idordine + "&" +
                "Importo=" + mAdapter.sumAllItem();

        StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        aggiungiProdottiOrdinati(String.valueOf(idordine));
                        callback.onSuccess(response);
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

    private void aggiungiProdottiOrdinati (String IDOrdine) {

        String queryurl = "";

        for (int i=0; i<productList.size(); i++) {
            //verifico se il prodotto è già stato aggiunto al DB (fa parte di un ordine già esistente)
            if (productList.get(i).getIdprodottovenduto() == 0) {
                queryurl = INSERT_PRODOTTI_VENDUTI + "IDProdottoVenduto=" + "&" +
                        "Quantita=" + productList.get(i).getQuantitàOrdinata() + "&" +
                        "PrezzoVendita=" + productList.get(i).getPrezzovenditaAttuale() + "&" +
                        "Ordini_IDOrdine=" + IDOrdine + "&" +
                        "Prodotti_In_Catalogo_IDProdotto=" + productList.get(i).getIDprodotto();
            } else {
                queryurl = INSERT_PRODOTTI_VENDUTI + "IDProdottoVenduto=" + productList.get(i).getIdprodottovenduto() + "&" +
                        "Quantita=" + productList.get(i).getQuantitàOrdinata() + "&" +
                        "PrezzoVendita=" + productList.get(i).getPrezzovenditaAttuale() + "&" +
                        "Ordini_IDOrdine=" + IDOrdine + "&" +
                        "Prodotti_In_Catalogo_IDProdotto=" + productList.get(i).getIDprodotto();
            }


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

    private void caricaOrdine (int idordine) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, SELECT_PRODOTTI_DA_ORDINE + idordine,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine);
                        double totalespesa = mAdapter.sumAllItem();
                        txtPrezzoTotale.setText(pdec.format(totalespesa));
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
