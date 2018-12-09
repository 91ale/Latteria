package it.alessandro.latteria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context mCtx;
    private List<Ordine> orderList;

    //dichiaro l'interfaccia
    private OnItemClicked onClick;

    public interface OnItemClicked {
        void onItemClick(int position);
    }

    public OrderAdapter(Context mCtx, List<Ordine> orderList) {
        this.mCtx = mCtx;
        this.orderList = orderList;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        public TextView txtordine, txtstato, txtimporto;
        public ConstraintLayout viewBackground;
        public ConstraintLayout viewForeground;

        public OrderViewHolder(View view) {
            super(view);

            txtordine = view.findViewById(R.id.txtNOrdine);
            txtstato = view.findViewById(R.id.txtNStato);
            txtimporto = view.findViewById(R.id.txtNImporto);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);

        }
    }

    //impostazione layout della recycleview
    @Override
    public OrderViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.order_list_row, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, final int position) {

        Ordine ordine = orderList.get(position);

        DecimalFormat importodec = new DecimalFormat("€ 0.00");
        holder.txtimporto.setText(importodec.format(ordine.getImporto()));
        holder.txtordine.setText(String.valueOf(ordine.getIDordine()));
        holder.txtstato.setText(ordine.getStato());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch ( orderList.get(position).getStato()) {
                    case "Completato":
                        //avvia activity visualizza ordine
                        break;
                    case "In attesa di pagamento":
                        SharedPreferences myPrefs = mCtx.getSharedPreferences("myPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = myPrefs.edit();
                        prefsEditor.putInt("current_orderid", orderList.get(position).getIDordine());
                        prefsEditor.commit();
                        Intent intentapproviazionespesa = new Intent(mCtx, ApprovazioneSpesaActivity.class);
                        mCtx.startActivity(intentapproviazionespesa);
                        break;
                    case "In corso":
                        switch (orderList.get(position).getTipo()) {
                            case "In negozio":
                                myPrefs = mCtx.getSharedPreferences("myPrefs", MODE_PRIVATE);
                                prefsEditor = myPrefs.edit();
                                prefsEditor.putInt("current_orderid", orderList.get(position).getIDordine());
                                prefsEditor.commit();
                                Intent intentspesainnegozio = new Intent(mCtx, SpesaInNegozioActivity.class);
                                mCtx.startActivity(intentspesainnegozio);
                                break;
                            case "Online":
                                myPrefs = mCtx.getSharedPreferences("myPrefs", MODE_PRIVATE);
                                prefsEditor = myPrefs.edit();
                                prefsEditor.putInt("current_orderid", orderList.get(position).getIDordine());
                                prefsEditor.commit();
                                Intent intentspesaonline = new Intent(mCtx, SpesaOnlineActivity.class);
                                mCtx.startActivity(intentspesaonline);
                                break;
                        }
                }
            }
        });

        holder.txtordine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClick(position);
            }
        });

    }

    //restituisce la lunghezza della lista ordini
    @Override
    public int getItemCount() {
        return orderList.size();
    }

    //rimuove tutti gli oggetti dalla lista
    public void removeAllItem() {
        orderList.clear();
    }

    public List<Ordine> getListItems() {
        return orderList;
    }

    public void setListItems(List<Ordine> productList) { this.orderList = orderList; }

}
