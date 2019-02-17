package it.alessandro.latteria;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.alessandro.latteria.Adapter.ProductCommessoAdapter;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.Object.Utente;
import it.alessandro.latteria.Parser.ParseProductJSON;
import it.alessandro.latteria.Parser.ParseUserJSON;
import it.alessandro.latteria.Parser.ParserCategoryJSON;

import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestioneProdottiActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String SELECT_PRODOTTO_DA_NOME = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_name.php?Nome=";
    private static final String SELECT_PRODOTTO_DA_CATEGORIA = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_category.php?Categoria=";
    private static final String SELECT_UTENTE_DA_IDUTENTE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_user_from_UID.php";
    private static final String SELECT_CATEGORIE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_category.php";

    private static final int QR = 14;

    private static final int RC_SCANNED_QR = 105;
    private static final int IN_NEGOZIO = 1;
    private static final String BACK = "back";

    SearchView searchView;
    private SimpleCursorAdapter cursorAdapter;
    private String[] strArrData = {"Inserisci il nome del prodotto"};
    private DrawerLayout drawerLayout;
    private List<Prodotto> productList = new ArrayList<>();
    private RecyclerView recyclerView;
    ProductCommessoAdapter mAdapter;
    private Utente utente = new Utente();
    String loggeduser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestione_prodotti);

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

        //inizializzaione della toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Prodotti");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        handleIntent(getIntent());

        //imposta il navigation drawer e l'icona relativa visualizzata nella toolbar
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.baseline_menu_24));

        recyclerView = findViewById(R.id.recycler_view);
        //assegna l'adapter alla recyclerview
        mAdapter = new ProductCommessoAdapter(this, productList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

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
            getProduct(SELECT_PRODOTTO_DA_NOME, query);
        }
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        productList.clear();
        visualizzaAiuto();
        mAdapter.notifyDataSetChanged();
    }*/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cerca) {
            return true;
        } else if (id == R.id.action_catalogo) {
            Intent intentcatalogo = new Intent(getApplicationContext(), AggiungiProdottiAlCatalogoActivity.class);
            startActivity(intentcatalogo);
            return true;
        } else if (id == R.id.action_magazzino) {
            Intent intentmagazzino = new Intent(getApplicationContext(), AggiungiProdottiMagazzinoActivity.class);
            startActivity(intentmagazzino);
            return true;
        }else if (id == R.id.action_negozio) {
            Intent intentnegozio = new Intent(getApplicationContext(), AggiungiProdottiNegozioActivity.class);
            startActivity(intentnegozio);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_prodotti, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_cerca)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        final String[] from = new String[]{"NomeProdotto"};
        final int[] to = new int[]{android.R.id.text1};
        cursorAdapter = new SimpleCursorAdapter(GestioneProdottiActivity.this, android.R.layout.simple_spinner_dropdown_item, null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
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
                getProduct(SELECT_PRODOTTO_DA_NOME, cursor.getString(cursor.getColumnIndex("NomeProdotto")));
                getProduct(SELECT_PRODOTTO_DA_CATEGORIA, cursor.getString(cursor.getColumnIndex("NomeProdotto")));
                return true;
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
                if (s.length() == 0) {
                    searchView.setSuggestionsAdapter(cursorAdapter);
                    getCategorySearch();
                } else {
                    searchView.setSuggestionsAdapter(cursorAdapter);
                    getProductSearch(SELECT_PRODOTTO_DA_NOME, s);
                }
                return false;
            }
        });

        return true;
    }

    private void getProduct(final String urlWebService, String nomeprodotto) {
        //VolleyLog.DEBUG = true;
        productList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + nomeprodotto,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        visualizzaAiuto();
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new ProductCommessoAdapter(GestioneProdottiActivity.this, productList);
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

    private void getInformazioniUtente(final String idutente, final VolleyCallBack callback) {

        StringRequest stringRequestAdd = new StringRequest(Request.Method.POST, SELECT_UTENTE_DA_IDUTENTE,
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

    private void getProductSearch (final String urlWebService, final String nome) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + nome,
                new Response.Listener<String>() {
                    ArrayList<String> dataList = new ArrayList<String>();
                    @Override
                    public void onResponse(String response) {
                        ParseProductJSON pj = new ParseProductJSON(response);
                        pj.getProductFromDB();
                        List<Prodotto> productList = pj.getProduct();
                        for(int i=0; i < productList.size(); i++){
                            dataList.add(productList.get(i).getNome());
                        }
                        strArrData = dataList.toArray(new String[dataList.size()]);
                        // Filter data
                        final MatrixCursor mc = new MatrixCursor(new String[]{ BaseColumns._ID, "NomeProdotto" });
                        for (int i=0; i<strArrData.length; i++) {
                            if (strArrData[i].toLowerCase().startsWith(nome.toLowerCase()))
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

    private void getCategorySearch() {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, SELECT_CATEGORIE,
                new Response.Listener<String>() {
                    ArrayList<String> dataList = new ArrayList<String>();
                    @Override
                    public void onResponse(String response) {
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

    private void visualizzaAiuto() {
        TextView txtAiuto = findViewById(R.id.txtAiuto);
        if (productList.size() == 0) {
            txtAiuto.setVisibility(View.VISIBLE);
        } else {
            txtAiuto.setVisibility(View.GONE);
        }
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

        if (requestCode == RC_SCANNED_QR) {
            if (resultCode == Activity.RESULT_OK) {
                String scannedqr = data.getStringExtra("SCANNED_CODE");
                if (!scannedqr.equals(BACK)) {
                    int idordine = Integer.valueOf(scannedqr);
                    Log.d("SCANNED_CODE", scannedqr);

                    Intent intentspesacommesso = new Intent(this, SpesaCommessoActivity.class);
                    intentspesacommesso.putExtra("ID_ORDINE", idordine);
                    intentspesacommesso.putExtra("TIPO_SPESA", IN_NEGOZIO);
                    startActivity(intentspesacommesso);
                }
            }
        }
    }
}
