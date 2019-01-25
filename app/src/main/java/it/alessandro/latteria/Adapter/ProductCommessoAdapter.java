package it.alessandro.latteria.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import it.alessandro.latteria.Utility.DownloadImageTask;
import it.alessandro.latteria.ModificaProdottoActivity;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class ProductCommessoAdapter extends RecyclerView.Adapter<ProductCommessoAdapter.ProductViewHolder>{

    private Context mCtx;
    private List<Prodotto> productList;

    //dichiaro l'interfaccia
    private OrderAdapter.OnItemClicked onClick;

    public ProductCommessoAdapter(Context mCtx, List<Prodotto> productList) {
        this.mCtx = mCtx;
        this.productList = productList;
    }

    //impostazione layout della recycleview
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.product_list_quantity_row, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductCommessoAdapter.ProductViewHolder holder, final int position) {

        Prodotto prodotto = productList.get(position);

        DecimalFormat prezzovdec = new DecimalFormat("€ 0.00");
        holder.prezzo.setText(prezzovdec.format(prodotto.getPrezzovenditaAttuale()));

        new DownloadImageTask(holder.immagine).execute(prodotto.getImmagine());
        holder.nome.setText(prodotto.getNome());
        holder.prezzo.setText(prezzovdec.format(prodotto.getPrezzovenditaAttuale()));
        holder.marca.setText(prodotto.getMarca());
        holder.quantitamagazzino.setText(String.valueOf(prodotto.getQuantitamagazzino()));
        holder.quantitanegozio.setText(String.valueOf(prodotto.getQuantitanegozio()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentmodificaprodotto = new Intent(mCtx, ModificaProdottoActivity.class);
                intentmodificaprodotto.putExtra("PID", productList.get(position).getIDprodotto());
                ((Activity) mCtx).startActivity(intentmodificaprodotto);
            }
        });
    }

    //restituisce la lunghezza della lista prodotti
    @Override
    public int getItemCount() {
        return productList.size();
    }

    public List<Prodotto> getListItems() {
        return productList;
    }

    public void setListItems(List<Prodotto> productList) {
        this.productList = productList;
    }

    public interface OnItemClicked {
        void onItemClick(int position);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        public ImageView immagine;
        public TextView nome, prezzo, marca, quantitamagazzino, quantitanegozio;

        public ProductViewHolder(View view) {
            super(view);

            immagine = view.findViewById(R.id.imgProdotto);
            nome = view.findViewById(R.id.txtNome);
            prezzo = view.findViewById(R.id.txtPrezzo);
            marca = view.findViewById(R.id.txtMarca);
            quantitamagazzino = view.findViewById(R.id.txtQuantitaProdottoMagazzino);
            quantitanegozio = view.findViewById(R.id.txtQuantitaProdottoNegozio);

        }
    }

}
