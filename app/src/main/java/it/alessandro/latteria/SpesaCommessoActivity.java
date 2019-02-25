package it.alessandro.latteria;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.alessandro.latteria.Adapter.ProductAdapter;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.Object.Utente;
import it.alessandro.latteria.Parser.ParseProductJSON;
import it.alessandro.latteria.Parser.ParseUserJSON;
import it.alessandro.latteria.Parser.ParserCategoryJSON;
import it.alessandro.latteria.Utility.RecyclerItemTouchHelper;

public class SpesaCommessoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String SELECT_PRODOTTO_DA_BARCODE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php";
    private static final String INSERT_PRODOTTI_VENDUTI = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/insert_ordered_products.php?";
    private static final String SELECT_PRODOTTI_DA_ORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_products_from_orderid.php?IDOrdine=";
    private static final String DELETE_PRODOTTI_DA_ORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/delete_product_from_order.php?IDProdottoVenduto=";
    private static final String UPDATE_ORDINE_IMPORTO_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_order_total_from_orderid.php?";
    private static final String SELECT_UTENTE_DA_IDUTENTE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_user_from_UID.php";
    private static final String SELECT_PRODOTTO_DA_NOME = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_name.php?Nome=";
    private static final String SELECT_CATEGORIE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_category.php";
    private static final String SELECT_INDIRIZZO_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_address_from_orderid.php?";
    private static final String UPDATE_STATO_ORDINE_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_order_status_from_orderid.php?";
    private static final String UPDATE_QUANTITA_DA_IDORDINE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/update_quantity_from_orderid.php?";


    private static final int RC_SCANNED_BC = 100;
    private static final int QUANTITA_SELEZIONATA = 102;
    private static final int PRODOTTI_SELEZIONATI = 103;
    private static final int QR = 14;
    private static final int RC_SCANNED_QR = 105;
    private static final int AGGIUNGI = 1;
    private static final int SOSTITUISCI = 2;
    private static final int IN_NEGOZIO = 1;
    private static final int ONLINE = 2;
    private static final int COMPLETATO = 1;
    private static final int EVASO = 2;
    private static final int EAN_13 = 13;
    private static final String BACK = "back";
    private static final int NO_DESC = 10;
    private boolean CATEGORIA = false;


    String loggeduser = "";
    ProductAdapter mAdapter;
    TextView txtPrezzoTotale;
    SearchView searchView;
    private SimpleCursorAdapter cursorAdapter;
    private String[] strArrData = {"Inserisci il nome del prodotto"};
    DecimalFormat pdec = new DecimalFormat("€ 0.00");
    //aggiorna il prezzo totale quando viene modificata la quantità di un prodotto
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            txtPrezzoTotale.setText(String.valueOf(pdec.format(mAdapter.sumAllItem())));
        }
    };
    int tipospesa = 0;
    int statoordine = 0;
    int idordine = 0;

    private DrawerLayout drawerLayout;
    private List<Prodotto> productList = new ArrayList<>();
    private List<Prodotto> rproductList = new ArrayList<>();
    private RecyclerView recyclerView;
    private Utente utente = new Utente();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spesa_commesso);

        //inizializzaione della toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Spesa");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        handleIntent(getIntent());

        //valorizza la variabile in base alla selezione effettuata dall'utente nell'activity TipoSpesaActivity (IN_NEGOZIO | ONLINE)
        tipospesa = getIntent().getIntExtra("TIPO_SPESA", -1);
        //valorizza la variabile in base allo stato dell'ordine (COMPLETATO | IN_CORSO)
        statoordine = getIntent().getIntExtra("STATO_ORDINE", -1);
        //valorizza la variabile con l'ID Ordine passato
        idordine = getIntent().getIntExtra("ID_ORDINE", -1);
        //ricava l'ID dell'utente loggato (loggeduser) ed un eventuale ordine da caricare (idordine) dalle SharedPreferences
        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggeduser = myPrefs.getString("FirebaseUser", "0");

        getInformazioniUtente(loggeduser, new VolleyCallBack() {
            @Override
            public void onSuccess(String response) {
                TextView txtNomeCognome = findViewById(R.id.txtNomeCognome);
                txtNomeCognome.setText("Benvenuto " + utente.getnome() + " " + utente.getcognome());
            }
        });

        if (statoordine != COMPLETATO && statoordine != EVASO) {
            //imposta il navigation drawer e l'icona relativa visualizzata nella toolbar
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.baseline_menu_24));
            FloatingActionButton fabScanCode = findViewById(R.id.fabScanCode);
            fabScanCode.show();
            fabScanCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ScanBarcode();
                }
            });
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_ios_24px));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        recyclerView = findViewById(R.id.recycler_view);

        //assegna l'adapter alla recyclerview
        mAdapter = new ProductAdapter(this, productList, tipospesa, statoordine, NO_DESC);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //collega l'ItemTouchHelper alla recyclerview (necessario per rilevare lo swipe di eliminazione prodotto)
        if (statoordine != EVASO && statoordine != COMPLETATO) {
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        }

        caricaOrdine(idordine);

        //riceve gli intent inviati dalla classe ProductAdapter quando viene modificata la quantità dallo spinner
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("quantita_modificata"));

    }

    //quando viene rilevato un intent richiama handleIntent
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    //codice eseguito nel momento in cui viene confermata la ricerca del prodotto
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchView.setIconified(true);
            searchView.setIconified(true);
            Intent intentcercaprodotto = new Intent(this, CercaProdottoActivity.class);
            intentcercaprodotto.putExtra("NOME_CATEGORIA_PRODOTTO", query);
            intentcercaprodotto.putExtra("TIPO_SPESA", tipospesa);
            startActivityForResult(intentcercaprodotto, PRODOTTI_SELEZIONATI);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        final MenuItem CompletaSpesa = menu.findItem(R.id.action_importo);
        txtPrezzoTotale = CompletaSpesa.getActionView().findViewById(R.id.txtImportoTotale);
        //imposta il formato del prezzo totale nel modo seguente € 0,00
        txtPrezzoTotale.setText(pdec.format(0.00));

        if (statoordine == COMPLETATO && tipospesa == IN_NEGOZIO) {
            menu.findItem(R.id.action_cerca).setVisible(false);
            menu.findItem(R.id.action_importo).getActionView().findViewById(R.id.imgCart).setVisibility(View.GONE);
        } else if (statoordine == EVASO) {
            menu.findItem(R.id.action_cerca).setVisible(false);
            menu.findItem(R.id.action_importo).getActionView().findViewById(R.id.imgCart).setVisibility(View.GONE);
        } else {
            if  (statoordine == COMPLETATO && tipospesa == ONLINE) {
                menu.findItem(R.id.action_cerca).setVisible(false);
            }
            //imposta un click listener sul tasto completa spesa
            final Menu finalMenu = menu;
            CompletaSpesa.getActionView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalMenu.performIdentifierAction(CompletaSpesa.getItemId(), 0);
                }
            });
            //inizializza la funzione di ricerca nel menu della toolbar
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) menu.findItem(R.id.action_cerca)
                    .getActionView();
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            final String[] from = new String[] {"NomeProdotto"};
            final int[] to = new int[] {android.R.id.text1};
            cursorAdapter = new SimpleCursorAdapter(SpesaCommessoActivity.this, android.R.layout.simple_spinner_dropdown_item, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            getCategorySearch();
            searchView.setSuggestionsAdapter(cursorAdapter);
            // ottiene il suggerimento cliccato e lo assegna alla casella di ricerca
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionClick(int position) {

                    // Add clicked text to search box
                    CursorAdapter ca = searchView.getSuggestionsAdapter();
                    Cursor cursor = ca.getCursor();
                    cursor.moveToPosition(position);
                    searchView.setIconified(true);
                    searchView.setIconified(true);
                    if (CATEGORIA) {
                        Intent intentcercaprodotto = new Intent(SpesaCommessoActivity.this, CercaProdottoActivity.class);
                        intentcercaprodotto.putExtra("NOME_CATEGORIA_PRODOTTO", cursor.getString(cursor.getColumnIndex("NomeProdotto")));
                        intentcercaprodotto.putExtra("TIPO_SPESA", tipospesa);
                        intentcercaprodotto.putExtra("NOME_CATEGORIA", CATEGORIA);
                        startActivityForResult(intentcercaprodotto, PRODOTTI_SELEZIONATI);
                        return true;
                    } else {
                        //se il prodotto è stato aggiunto precedentemente alla lista lo incrementa di 1
                        int exist = checkExistInList(cursor.getString(cursor.getColumnIndex("Barcode")), 1, AGGIUNGI);
                        //altrimenti acquisico le info dal DB e lo aggiungo alla lista
                        if (exist == 0) getProductFromName(SELECT_PRODOTTO_DA_NOME, cursor.getString(cursor.getColumnIndex("NomeProdotto")), tipospesa);
                        return true;
                    }
                }

                @Override
                public boolean onSuggestionSelect(int position) {
                    return true;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    //carica i suggerimenti soltanto se è stato inserito almeno un carattere
                    if (s.length() == 0){
                        searchView.setSuggestionsAdapter(cursorAdapter);
                        getCategorySearch();
                    } else {
                        searchView.setSuggestionsAdapter(cursorAdapter);
                        getProductSearch(SELECT_PRODOTTO_DA_NOME, s);
                    }
                    return false;
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_importo) {
            evadiOrdine();
            /*if (tipospesa == ONLINE) {
                evadiOrdine();
            } else {
                aggiungiOrdine("In corso", idordine, new SpesaCommessoActivity.VolleyCallBack() {
                    @Override
                    public void onSuccess(String response) { }
                });
                Intent intentordinecompletato = new Intent(getApplicationContext(), OrdineCompletatoActivity.class);
                intentordinecompletato.putExtra("ID_ORDINE", idordine);
                intentordinecompletato.putExtra("TOTALE_ORDINE", mAdapter.sumAllItem());
                intentordinecompletato.putExtra("TIPO_SPESA", tipospesa);
                startActivity(intentordinecompletato);
            }*/
            return true;
        } else if (id == R.id.action_cerca) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            startActivityForResult(intentscanqrcode, RC_SCANNED_QR);
        } else if (id == R.id.nav_prodotti) {
            Intent intentgestioneprodotti = new Intent(getApplicationContext(), GestioneProdottiActivity.class);
            startActivity(intentgestioneprodotti);
        } else if (id == R.id.nav_ordini_online) {
            Intent intentordinionline = new Intent(getApplicationContext(), OrdiniOnlineActivity.class);
            startActivity(intentordinionline);
        } else if (id == R.id.nav_ordini_conclusi) {
            Intent intentordiniconclusi = new Intent(getApplicationContext(), OrdiniConclusiActivity.class);
            startActivity(intentordiniconclusi);
        } else if (id == R.id.nav_statistiche_vendita) {
            Intent intentstatistiche = new Intent(getApplicationContext(), StatisticheActivity.class);
            startActivity(intentstatistiche);
        } else if (id == R.id.nav_logout) {
            SignOut();
            Intent intentlogin = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intentlogin);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Logout utente
    private void SignOut() {
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

        if (requestCode == RC_SCANNED_BC) {
            if (resultCode == Activity.RESULT_OK) {
                String scannedbc = data.getStringExtra("SCANNED_CODE");
                Log.d("SCANNED_CODE", scannedbc);
                if (!scannedbc.equals(BACK)) {
                    //se il prodotto è stato aggiunto precedentemente alla lista lo incrementa di 1
                    int exist = checkExistInList(scannedbc, 1, AGGIUNGI);
                    //altrimenti acquisico le info dal DB e lo aggiungo alla lista
                    if (exist == 0) getProduct(SELECT_PRODOTTO_DA_BARCODE, scannedbc);
                }
            }
        } else if (requestCode == QUANTITA_SELEZIONATA) {
            if (resultCode == Activity.RESULT_OK) {
                int quantita = data.getIntExtra("QUANTITA_SELEZIONATA", -1);
                int position = data.getIntExtra("POSITION", -1);
                productList.get(position).setQuantitaOrdinata(quantita);
                //crea l'adapter e lo assegna alla recycleview
                mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine, NO_DESC);
                double totalespesa = mAdapter.sumAllItem();
                txtPrezzoTotale.setText(pdec.format(totalespesa));
                recyclerView.setAdapter(mAdapter);
            }
        } else if (requestCode == PRODOTTI_SELEZIONATI) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<Prodotto> sproductList = (ArrayList<Prodotto>) data.getSerializableExtra("PRODOTTI_SELEZIONATI");
                for (Prodotto prodotto : sproductList){
                    if (checkExistInList(prodotto.getBarCode(), prodotto.getQuantitaOrdinata(), SOSTITUISCI) != 1) {
                        productList.add(prodotto);
                        mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine);
                        double totalespesa = mAdapter.sumAllItem();
                        txtPrezzoTotale.setText(pdec.format(totalespesa));
                        recyclerView.setAdapter(mAdapter);
                    }
                }
            }
        } else if (requestCode == RC_SCANNED_QR) {
            if (resultCode == Activity.RESULT_OK) {
                String scannedqr = data.getStringExtra("SCANNED_CODE");
                if (!scannedqr.equals(BACK)) {
                    int idordine = Integer.valueOf(scannedqr);
                    Log.d("SCANNED_CODE", scannedqr);
                    finish();
                    Intent intentspesacommesso = new Intent(this, SpesaCommessoActivity.class);
                    intentspesacommesso.putExtra("ID_ORDINE", idordine);
                    intentspesacommesso.putExtra("TIPO_SPESA", IN_NEGOZIO);
                    startActivity(intentspesacommesso);
                }
            }
        }
    }

    private void getProduct(final String urlWebService, final String scannedbc) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlWebService,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() == 2) {
                            Toast.makeText(SpesaCommessoActivity.this, "Codice a barre non riconosciuto prego riprovare la scansione, oppure utilizzare la ricerca tramite nome prodotto", Toast.LENGTH_LONG).show();
                        } else {
                            ParseProductJSON pj = new ParseProductJSON(response);
                            pj.getProductFromDB();
                            productList.addAll(pj.getProduct());
                            //crea l'adapter e lo assegna alla recycleview
                            mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine, NO_DESC);
                            double totalespesa = mAdapter.sumAllItem();
                            txtPrezzoTotale.setText(pdec.format(totalespesa));
                            recyclerView.setAdapter(mAdapter);
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

    private void getProductFromName(final String urlWebService, String nome, final int tipospesa) {
        //VolleyLog.DEBUG = true;
        nome = nome.replace("'", "''");
        nome = nome.replace("&", "''&");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + nome,
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

    private void getProductSearch (final String urlWebService, final String nome) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + nome,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        CATEGORIA = false;
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        List<Prodotto> productList = pj.getProduct();
                        final MatrixCursor mc = new MatrixCursor(new String[]{ BaseColumns._ID, "NomeProdotto", "Barcode" });
                        for (Prodotto prodotto : productList) {
                            if (prodotto.getNome().toLowerCase().startsWith(nome.toLowerCase()))
                                mc.newRow()
                                        .add("NomeProdotto", prodotto.getNome())
                                        .add("Barcode", prodotto.getBarCode());
                        }
                        cursorAdapter.changeCursor(mc);
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

    private void getCategorySearch() {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, SELECT_CATEGORIE,
                new Response.Listener<String>() {
                    ArrayList<String> dataList = new ArrayList<String>();
                    @Override
                    public void onResponse(String response) {
                        CATEGORIA = true;
                        ParserCategoryJSON cj = new ParserCategoryJSON(response);
                        cj.getCategoriaFromDB();
                        dataList.addAll(cj.getCategorie());
                        strArrData = dataList.toArray(new String[dataList.size()]);
                        // Filter data
                        final MatrixCursor mc = new MatrixCursor(new String[]{ BaseColumns._ID, "NomeProdotto" });
                        for (int i=0; i<strArrData.length; i++) {
                            mc.addRow(new Object[] {i, strArrData[i]});
                        }
                        cursorAdapter.changeCursor(mc);
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

                    // ANNULLA selezionato, ripristino il prodotto e lo elimino dalla lista dei rimossi
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                    rproductList.remove(rproductList.size()-1);
                    double totalespesa = mAdapter.sumAllItem();
                    txtPrezzoTotale.setText(pdec.format(totalespesa));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private int checkExistInList(String scannedbc, int quantita, int operazione) {

        int f = 0;

        for (int i = 0; i <= productList.size() - 1; i++) {
            if (productList.get(i).getBarCode().equals(scannedbc)) {
                if (operazione == AGGIUNGI) {
                    if (tipospesa == IN_NEGOZIO && productList.get(i).getQuantitanegozio() > productList.get(i).getQuantitaOrdinata() + quantita) {
                        productList.get(i).setQuantitaOrdinata(productList.get(i).getQuantitaOrdinata() + quantita);
                    } else if (tipospesa != IN_NEGOZIO && productList.get(i).getQuantitamagazzino() > productList.get(i).getQuantitaOrdinata() + quantita) {
                        productList.get(i).setQuantitaOrdinata(quantita);
                    }
                } else if (operazione == SOSTITUISCI) {
                    productList.get(i).setQuantitaOrdinata(quantita);
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

    private void aggiungiOrdine(String stato, final int idordine, final SpesaCommessoActivity.VolleyCallBack callback) {

        String queryurl = "";

        //se sono stati rimossi prodotti dalla recycleview li elimino anche dal DB
        if (rproductList.size() > 0) {
            for (int i = 0; i < rproductList.size(); i++) {
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

    private void aggiungiProdottiOrdinati(String IDOrdine) {

        String queryurl = "";

        for (int i = 0; i < productList.size(); i++) {
            //verifico se il prodotto è già stato aggiunto al DB (fa parte di un ordine già esistente)
            if (productList.get(i).getIdprodottovenduto() == 0) {
                queryurl = INSERT_PRODOTTI_VENDUTI + "IDProdottoVenduto=" + "&" +
                        "Quantita=" + productList.get(i).getQuantitaOrdinata() + "&" +
                        "PrezzoVendita=" + productList.get(i).getPrezzovenditaAttuale() + "&" +
                        "Ordini_IDOrdine=" + IDOrdine + "&" +
                        "Prodotti_In_Catalogo_IDProdotto=" + productList.get(i).getIDprodotto();
            } else {
                queryurl = INSERT_PRODOTTI_VENDUTI + "IDProdottoVenduto=" + productList.get(i).getIdprodottovenduto() + "&" +
                        "Quantita=" + productList.get(i).getQuantitaOrdinata() + "&" +
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

    private void caricaOrdine(int idordine) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, SELECT_PRODOTTI_DA_ORDINE + idordine,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new ProductAdapter(SpesaCommessoActivity.this, productList, tipospesa, statoordine, NO_DESC);
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

    public interface VolleyCallBack {
        void onSuccess(String response);
    }

    private void getInformazioniUtente(final String idutente, final SpesaCommessoActivity.VolleyCallBack callback) {

        String queryurl = SELECT_UTENTE_DA_IDUTENTE;

        StringRequest stringRequestAdd = new StringRequest(Request.Method.POST, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseUserJSON pj = new ParseUserJSON(response);
                        pj.getUserFromDB();
                        utente = pj.getUtente();
                        callback.onSuccess(response);
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
                params.put("IDUtente",idutente);
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

    private void ScanBarcode () {
        Intent intentscanbarcode = new Intent(this, ScanBarcodeActivity.class);
        String messaggio = "Inquadra il codice a barre del prodotto che vuoi acquistare";
        intentscanbarcode.putExtra("TIPO_CODICE", EAN_13);
        intentscanbarcode.putExtra("MESSAGGIO", messaggio);
        startActivityForResult(intentscanbarcode, RC_SCANNED_BC);
    }

    private void dialogEvasioneOrdine (Utente cliente) {
        AlertDialog alertDialog = new AlertDialog.Builder(SpesaCommessoActivity.this).create();
        if (tipospesa == ONLINE) {
            alertDialog.setTitle("Confermare l'evasione dell'ordine?");
            String s1 = "Dettagli di consegna:";
            String s2 = "Destinatario: " + "<b>" + cliente.getnome() + " " + cliente.getcognome() + "</b>";
            String s3 = "Indirizzo: " + "<b>" + cliente.getindirizzo() + "</b>";
            String s4 = "Importo totale: " + "<b>" + txtPrezzoTotale.getText() + "</b>";
            Spanned strMessage = Html.fromHtml("<br>" + s1 + "<br><br>" + s2 + "<br>" + s3 + "<br>" + s4);
            alertDialog.setMessage(strMessage);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SI",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setOrdineCompletato(idordine, "Evaso");
                            Intent intentordinionline = new Intent(getApplicationContext(), OrdiniOnlineActivity.class);
                            startActivity(intentordinionline);
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else {
            alertDialog.setTitle("Confermare il pagamento dell'ordine?");
            String s1 = "Importo totale dovuto dal cliente: " + "<b>" + txtPrezzoTotale.getText() + "</b>";
            Spanned strMessage = Html.fromHtml("<br>" + s1);
            alertDialog.setMessage(strMessage);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SI",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setOrdineCompletato(idordine, "Completato");
                            Intent intentordinionline = new Intent(getApplicationContext(), OrdiniOnlineActivity.class);
                            startActivity(intentordinionline);
                            dialog.dismiss();
                            finish();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }

        alertDialog.show();
    }

    private void evadiOrdine () {

        String queryurl = SELECT_INDIRIZZO_DA_IDORDINE + "IDOrdine=" + idordine;

        StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseUserJSON pj = new ParseUserJSON(response);
                        pj.getUserFromDB();
                        Utente cliente = pj.getUtente();
                        dialogEvasioneOrdine(cliente);
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

    private void setOrdineCompletato(int idordine, String stato) {

        //modifico lo stato dell'ordine
        String queryurl = UPDATE_STATO_ORDINE_DA_IDORDINE + "IDOrdine=" + idordine + "&" +
                "Stato=" + stato;

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

        //aggiorno le quantità in giacenza in base a quelle ordinate
        queryurl = UPDATE_QUANTITA_DA_IDORDINE + "IDOrdine=" + idordine;

        stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
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
