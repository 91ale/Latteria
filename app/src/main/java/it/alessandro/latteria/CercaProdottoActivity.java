package it.alessandro.latteria;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CercaProdottoActivity extends AppCompatActivity {

    private static final String SELECT_PRODOTTO_DA_NOME = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_name.php?Nome=";
    private static final String SELECT_PRODOTTO_DA_MARCA = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_product_from_barcode.php?=";
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerca_prodotto);


    }
}
