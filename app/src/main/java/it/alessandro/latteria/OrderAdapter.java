package it.alessandro.latteria;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final int COMPLETATO = 1;

    private static final int IN_NEGOZIO = 1;
    private static final int ONLINE = 2;

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
                        Intent intentcompletato = new Intent(mCtx, SpesaActivity.class);
                        intentcompletato.putExtra("STATO_ORDINE", COMPLETATO);
                        intentcompletato.putExtra("ID_ORDINE", orderList.get(position).getIDordine());
                        mCtx.startActivity(intentcompletato);
                        break;
                    case "In attesa di pagamento":
                        Intent intentapproviazionespesa = new Intent(mCtx, ApprovazioneSpesaActivity.class);
                        intentapproviazionespesa.putExtra("ID_ORDINE", String.valueOf(orderList.get(position).getIDordine()));
                        if (orderList.get(position).getTipo().equals("In negozio")) intentapproviazionespesa.putExtra("TIPO_SPESA", IN_NEGOZIO);
                        if (orderList.get(position).getTipo().equals("Online")) intentapproviazionespesa.putExtra("TIPO_SPESA", ONLINE);
                        mCtx.startActivity(intentapproviazionespesa);
                        break;
                    case "In corso":
                        Intent intentspesa = new Intent(mCtx, SpesaActivity.class);
                        intentspesa.putExtra("ID_ORDINE", orderList.get(position).getIDordine());
                        if (orderList.get(position).getTipo().equals("In negozio")) intentspesa.putExtra("TIPO_SPESA", IN_NEGOZIO);
                        if (orderList.get(position).getTipo().equals("Online")) intentspesa.putExtra("TIPO_SPESA", ONLINE);
                        mCtx.startActivity(intentspesa);
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

    public void setListItems(List<Ordine> productList) { this.orderList = orderList; }

}
