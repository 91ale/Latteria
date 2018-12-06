package it.alessandro.latteria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mancj.materialsearchbar.MaterialSearchBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SpesaInNegozioActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String SELECT_PRODUCT_FROM_BARCODE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php?BarCode=";

    String loggeduser = "";

    private MaterialSearchBar searchBar;
    private DrawerLayout drawerLayout;

    private static final int RC_SCANNED_BC = 100;

    private List<Prodotto> productList = new ArrayList<>();
    ProductAdapter mAdapter;
    private RecyclerView recyclerView;
    TextView txtPrezzoTotale;

    DecimalFormat pdec = new DecimalFormat("€ 0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spesa_in_negozio);

        loggeduser = getIntent().getStringExtra("LOGGED_UID");

        //imposta il navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Barra di ricerca importata da https://github.com/mancj/MaterialSearchBar
        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
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
        txtPrezzoTotale.setText(pdec.format(0.00));

        mAdapter = new ProductAdapter(SpesaInNegozioActivity.this, productList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("quantita_modificata"));

        Button procediCassa = findViewById(R.id.btnProcediCassa);
        procediCassa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrder();
                Intent intentapproviazionespesa = new Intent(getApplicationContext(), ApprovazioneSpesaActivity.class);
                startActivity(intentapproviazionespesa);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        /*} else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
*/
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        FrameLayout searchBackground = (FrameLayout)findViewById(R.id.search_transparent_background);
        String s = enabled ? "enabled" : "disabled";
        Toast.makeText(SpesaInNegozioActivity.this, "Search " + s, Toast.LENGTH_SHORT).show();
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
        startSearch(text.toString(), true, null, true);
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                drawerLayout.openDrawer(Gravity.LEFT);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                Intent intentscanbarcode = new Intent(this, ScanBarcodeActivity.class);
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
                String scannedbc = data.getStringExtra("SCANNED_BC");
                Log.d("SCANNED_BC",scannedbc);
                int exist = checkExistInList(scannedbc);
                if (exist == 0) getProduct(SELECT_PRODUCT_FROM_BARCODE, scannedbc);
            }
        }
    }

    private void getProduct(final String urlWebService, String scannedbc) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + scannedbc,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseJSON pj = new ParseJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new ProductAdapter(SpesaInNegozioActivity.this, productList);
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

            // backup dell'oggetto rimosso per un eventuale ripristino
            final Prodotto deletedItem = productList.get(viewHolder.getAdapterPosition());
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

                    // undo is selected, restore the deleted item
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                    double totalespesa = mAdapter.sumAllItem();
                    txtPrezzoTotale.setText(pdec.format(totalespesa));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            txtPrezzoTotale.setText(String.valueOf(pdec.format(mAdapter.sumAllItem())));
        }
    };
    private int checkExistInList (String scannedbc) {

        int f = 0;

        for (int i=0; i<=productList.size()-1; i++) {
            if (productList.get(i).getBarCode().equals(scannedbc)) {
                productList.get(i).setQuantitàOrdinata(productList.get(i).getQuantitàOrdinata()+1);
                mAdapter.setListItems(productList);
                mAdapter.notifyDataSetChanged();
                txtPrezzoTotale.setText(String.valueOf(pdec.format(mAdapter.sumAllItem())));
                f = 1;
                break;
            }
        }
        return f;
    }

    private void addOrder () {

        mAdapter.getListItems();
        //aggiungo l'ordine al DB con stato in corso (attendo che il cliente paghi l'ordine con PayPal o
        //che il commesso confermi lo stato di pagato)

    }

        /*private void getProduct(final String urlWebService, final String scannedbc) {

        //VolleyLog.DEBUG = true;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService+scannedbc,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                //getting user object from json array
                                try {
                                    JSONObject prodottoJ = jsonArray.getJSONObject(i);

                                    productList.add(new Prodotto(
                                            prodottoJ.getInt("IDProdotto"),
                                            scannedbc,
                                            prodottoJ.getString("Nome"),
                                            prodottoJ.getString("Marca"),
                                            prodottoJ.getString("Categoria"),
                                            prodottoJ.getInt("QuantitaMagazzino"),
                                            prodottoJ.getInt("QuantitaNegozio"),
                                            prodottoJ.getString("Descrizione"),
                                            prodottoJ.getInt("PrezzoVenditaAttuale"),
                                            prodottoJ.getString("ImmagineProdotto")
                                    ));
                                    mAdapter = new ProductAdapter(SpesaInNegozioActivity.this, productList);
                                    recyclerView.setAdapter(mAdapter);

                                    Log.d("Nome prodotto", productList.get(productList.size() - 1).getNome());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(stringRequest);

    }*/

}
