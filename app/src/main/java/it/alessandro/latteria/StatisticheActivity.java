package it.alessandro.latteria;

import android.app.DatePickerDialog;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class StatisticheActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

    private static final String SELECT_COSTI_DA_DATE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_cost_from_date.php?";
    private static final String SELECT_RICAVI_DA_DATE = "http://ec2-18-185-88-246.eu-central-1.compute.amazonaws.com/select_proceeds_from_date.php?";

    static String datainizio;
    static String datafine;

    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiche);

        final DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, StatisticheActivity.this, 2018, 12, 1);

        graph = findViewById(R.id.graph);

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        // imposta l'etichetta delle ascisse
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getBaseContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space


        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        //graph.getGridLabelRenderer().setHumanRounding(false);

    }

    private void getCosti(String datainizio, String datafine) {

        VolleyLog.DEBUG = true;

        String queryurl = SELECT_COSTI_DA_DATE + "DataInizio=" + datainizio + "&DataFine=" + datafine;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            LineGraphSeries<DataPoint> seriescosti = new LineGraphSeries<>();

                            for (int i = 0; i < jsonArray.length(); i++) {

                                //getting user object from json array
                                try {

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);

                                    JSONObject costiJ = jsonArray.getJSONObject(i);

                                    seriescosti.appendData(new DataPoint(sdf.parse(costiJ.getString("DataOra")), costiJ.getDouble("Costo")),true,365);

                                } catch (ParseException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            graph.addSeries(seriescosti);
                            seriescosti.setColor(Color.RED);

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

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void getRicavi(String datainizio, String datafine) {

        String queryurl = SELECT_RICAVI_DA_DATE + "DataInizio=" + datainizio + "&DataFine=" + datafine;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            LineGraphSeries<DataPoint> seriesricavi = new LineGraphSeries<>();

                            for (int i = 0; i < jsonArray.length(); i++) {

                                //getting user object from json array
                                try {

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                                    JSONObject costiJ = jsonArray.getJSONObject(i);

                                    seriesricavi.appendData(new DataPoint(sdf.parse(costiJ.getString("DataOra")), costiJ.getDouble("Ricavo")),true,365);


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            graph.addSeries(seriesricavi);
                            seriesricavi.setColor(Color.GREEN);

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

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sbi = new StringBuilder();
        StringBuilder sbf = new StringBuilder();
        sbi.append(year);
        sbi.append("-");
        sbi.append(month+1);
        sbi.append("-");
        sbi.append(dayOfMonth);
        if (month + 7 > 12) {
            sbf.append(year+1);
            sbf.append("-");
            sbf.append((month + 7) % 12);
        } else {
            sbf.append(year);
            sbf.append("-");
            sbf.append(month + 7);
        }
        sbf.append("-");
        sbf.append(dayOfMonth);
        datainizio = sbi.toString();
        datafine = sbf.toString();
        getCosti(datainizio, datafine);
        getRicavi(datainizio, datafine);
        // imposta gli step delle ascisse
        try {
            graph.getViewport().setMinX(sdf.parse(datainizio).getTime());
            graph.getViewport().setMaxX(sdf.parse(datafine).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        graph.getViewport().setXAxisBoundsManual(true);
    }
}
