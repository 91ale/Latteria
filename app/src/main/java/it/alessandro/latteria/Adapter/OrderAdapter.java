package it.alessandro.latteria.Adapter;

import android.content.Context;
import android.content.Intent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import it.alessandro.latteria.ApprovazioneSpesaActivity;
import it.alessandro.latteria.Object.Ordine;
import it.alessandro.latteria.R;
import it.alessandro.latteria.SpesaClienteActivity;
import it.alessandro.latteria.SpesaCommessoActivity;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final int COMPLETATO = 1;
    private static final int EVASO = 2;

    private static final int IN_NEGOZIO = 1;
    private static final int ONLINE = 2;
    private static final int COMMESSO = 1;

    private Context mCtx;
    private List<Ordine> orderList;
    private int commesso = 0;


    //dichiaro l'interfaccia
    private OnItemClicked onClick;

    public OrderAdapter(Context mCtx, List<Ordine> orderList) {
        this.mCtx = mCtx;
        this.orderList = orderList;
    }

    public OrderAdapter(Context mCtx, List<Ordine> orderList, int commesso) {
        this.mCtx = mCtx;
        this.orderList = orderList;
        this.commesso = commesso;
    }

    //impostazione layout della recycleview
    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        holder.txtordine.setText(String.valueOf("#" + ordine.getIDordine()));
        holder.txtstato.setText(ordine.getStato());
        switch (orderList.get(position).getStato()) {
            case "Evaso":
                    holder.txtstato.setTextColor(Color.GREEN);
                break;
            case "Completato":
                holder.txtstato.setTextColor(Color.RED);
                break;
            case "In corso":
                holder.txtstato.setTextColor(Color.BLUE);
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (orderList.get(position).getStato()) {
                    case "Evaso":
                        Intent intentevaso = new Intent(mCtx, SpesaCommessoActivity.class);
                        intentevaso.putExtra("STATO_ORDINE", EVASO);
                        intentevaso.putExtra("ID_ORDINE", orderList.get(position).getIDordine());
                        mCtx.startActivity(intentevaso);
                        break;
                    case "Completato":
                        if (commesso == COMMESSO) {
                            Intent intentcompletatocommesso = new Intent(mCtx, SpesaCommessoActivity.class);
                            if (orderList.get(position).getTipo().equals("Online")) {
                                intentcompletatocommesso.putExtra("TIPO_SPESA", ONLINE);
                            } else {
                                intentcompletatocommesso.putExtra("TIPO_SPESA", IN_NEGOZIO);
                            }
                            intentcompletatocommesso.putExtra("STATO_ORDINE", COMPLETATO);
                            intentcompletatocommesso.putExtra("ID_ORDINE", orderList.get(position).getIDordine());
                            mCtx.startActivity(intentcompletatocommesso);
                        } else {
                            Intent intentcompletato = new Intent(mCtx, SpesaClienteActivity.class);
                            intentcompletato.putExtra("STATO_ORDINE", COMPLETATO);
                            intentcompletato.putExtra("ID_ORDINE", orderList.get(position).getIDordine());
                            mCtx.startActivity(intentcompletato);
                        }
                        break;
                    case "In attesa di pagamento":
                        Intent intentapproviazionespesa = new Intent(mCtx, ApprovazioneSpesaActivity.class);
                        intentapproviazionespesa.putExtra("ID_ORDINE", String.valueOf(orderList.get(position).getIDordine()));
                        intentapproviazionespesa.putExtra("IMPORTO", String.valueOf(orderList.get(position).getImporto()));
                        if (orderList.get(position).getTipo().equals("In negozio"))
                            intentapproviazionespesa.putExtra("TIPO_SPESA", IN_NEGOZIO);
                        if (orderList.get(position).getTipo().equals("Online"))
                            intentapproviazionespesa.putExtra("TIPO_SPESA", ONLINE);
                        mCtx.startActivity(intentapproviazionespesa);
                        break;
                    case "In corso":
                        Intent intentspesa = new Intent(mCtx, SpesaClienteActivity.class);
                        intentspesa.putExtra("ID_ORDINE", orderList.get(position).getIDordine());
                        if (orderList.get(position).getTipo().equals("In negozio"))
                            intentspesa.putExtra("TIPO_SPESA", IN_NEGOZIO);
                        if (orderList.get(position).getTipo().equals("Online"))
                            intentspesa.putExtra("TIPO_SPESA", ONLINE);
                        mCtx.startActivity(intentspesa);
                        break;
                }
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

    public void setListItems(List<Ordine> productList) {
        this.orderList = orderList;
    }

    public interface OnItemClicked {
        void onItemClick(int position);
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

}
