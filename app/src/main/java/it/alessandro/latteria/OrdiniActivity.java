package it.alessandro.latteria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class OrdiniActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String SELECT_ORDINI = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_orders_from_UID.php?IDUtente=";

    String loggeduser = "";

    private MaterialSearchBar searchBar;
    private DrawerLayout drawerLayout;

    private List<Ordine> orderList = new ArrayList<>();
    private RecyclerView recyclerView;
    OrderAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordini);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_menu_24);

        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        loggeduser = myPrefs.getString("logged_user", "0");

        //imposta il navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new OrderAdapter(OrdiniActivity.this, orderList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        getOrdini(SELECT_ORDINI);

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
            Intent intentspesa = new Intent(getApplicationContext(), TipoSpesaActivity.class);
            startActivity(intentspesa);
        } else if (id == R.id.nav_profilo) {
            Intent intentprofilo = new Intent(getApplicationContext(), MioProfiloActivity.class);
            startActivity(intentprofilo);
        } else if (id == R.id.nav_ordini) {
            Intent intentordini = new Intent(getApplicationContext(), OrdiniActivity.class);
            startActivity(intentordini);
        } else if (id == R.id.nav_aiuto) {
            Intent intentaiuto = new Intent(getApplicationContext(), AiutoActivity.class);
            startActivity(intentaiuto);
        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getOrdini(final String urlWebService) {
        //VolleyLog.DEBUG = true;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlWebService + loggeduser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseOrderJSON pj = new ParseOrderJSON(response);
                        pj.getOrderFromDB();
                        orderList.addAll(pj.getOrder());
                        //crea l'adapter e lo assegna alla recycleview
                        mAdapter = new OrderAdapter(OrdiniActivity.this, orderList);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
