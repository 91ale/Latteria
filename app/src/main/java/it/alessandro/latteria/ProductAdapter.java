package it.alessandro.latteria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context mCtx;
    private List<Prodotto> productList;
    private int tipospesa;
    private int statoordine;

    private static final int IN_NEGOZIO = 1;
    private static final int QUANTITA_SELEZIONATA = 102;
    private static final int COMPLETATO = 1;

    //dichiaro l'interfaccia
    private OrderAdapter.OnItemClicked onClick;

    public interface OnItemClicked {
        void onItemClick(int position);
    }

    public ProductAdapter(Context mCtx, List<Prodotto> productList, int tipospesa, int statoordine) {
        this.mCtx = mCtx;
        this.productList = productList;
        this.tipospesa = tipospesa;
        this.statoordine = statoordine;
    }

    public ProductAdapter(Context mCtx, List<Prodotto> productList, int tipospesa) {
        this.mCtx = mCtx;
        this.productList = productList;
        this.tipospesa = tipospesa;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        public ImageView immagine;
        public TextView nome, prezzo, marca;
        public Spinner quantita;
        public ConstraintLayout viewBackground;
        public ConstraintLayout viewForeground;

        public ProductViewHolder(View view) {
            super(view);

            immagine = view.findViewById(R.id.imgProdotto);
            nome = view.findViewById(R.id.txtNome);
            prezzo = view.findViewById(R.id.txtPrezzo);
            marca = view.findViewById(R.id.txtMarca);
            quantita = view.findViewById(R.id.spnQuantità);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);

        }
    }

    //impostazione layout della recycleview
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.product_list_row, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, final int position) {

        String[] arrayquantità;

        Prodotto prodotto = productList.get(position);

        DecimalFormat prezzovdec = new DecimalFormat("€ 0.00");
        holder.prezzo.setText(prezzovdec.format(prodotto.getPrezzovenditaAttuale()));

        new DownloadImageTask(holder.immagine).execute(prodotto.getImmagine());
        holder.nome.setText(prodotto.getNome());
        holder.prezzo.setText(prezzovdec.format(prodotto.getPrezzovenditaAttuale()));
        holder.marca.setText(prodotto.getMarca());
        if (tipospesa == IN_NEGOZIO) {
            arrayquantità = arrayQuantità(prodotto.getQuantitanegozio());
        } else {
            arrayquantità = arrayQuantità(prodotto.getQuantitamagazzino());
        }
        // crea un ArrayAdapter usando l'array delle quantità e il layout passato
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<String>(mCtx, android.R.layout.simple_list_item_1, arrayquantità);
        // specifica il layout della lista scelte
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // applica l'adapter allo spinner
        holder.quantita.setAdapter(spinnerAdapter);
        holder.quantita.setSelection(productList.get(position).getQuantitàOrdinata()-1);

        holder.quantita.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int positionInSpinner, long id) {
                String quantita = parent.getItemAtPosition(positionInSpinner).toString();
                productList.get(position).setQuantitàOrdinata(Integer.parseInt(quantita));
                Intent intent = new Intent("quantita_modificata");
                LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int quantitadisponibile = 0;

                if (tipospesa == IN_NEGOZIO) {
                    quantitadisponibile = productList.get(position).getQuantitanegozio();
                } else {
                    quantitadisponibile = productList.get(position).getQuantitamagazzino();
                }

                Intent intentinformazioniprodotto = new Intent(mCtx, InformazioniProdottoActivity.class);
                intentinformazioniprodotto.putExtra("STATO", statoordine);
                intentinformazioniprodotto.putExtra("POSITION", position);
                intentinformazioniprodotto.putExtra("NOME", productList.get(position).getNome());
                intentinformazioniprodotto.putExtra("MARCA", productList.get(position).getMarca());
                intentinformazioniprodotto.putExtra("PREZZO", productList.get(position).getPrezzovenditaAttuale());
                intentinformazioniprodotto.putExtra("IMMAGINE", productList.get(position).getImmagine());
                intentinformazioniprodotto.putExtra("QUANTITA_SELEZIONATA", productList.get(position).getQuantitàOrdinata());
                intentinformazioniprodotto.putExtra("QUANTITA_DISPONIBILE", quantitadisponibile);
                intentinformazioniprodotto.putExtra("DESCRIZIONE", productList.get(position).getDescrizione());

                ((Activity) mCtx).startActivityForResult(intentinformazioniprodotto,QUANTITA_SELEZIONATA);

            }
        });
    }

    //restituisce la lunghezza della lista prodotti
    @Override
    public int getItemCount() {
        return productList.size();
    }

    //rimuove l'oggetto nella posizione passate
    public void removeItem(int position) {
        productList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    //ripristina l'oggetto nella posizione passata
    public void restoreItem(Prodotto item, int position) {
        productList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    //rimuove tutti gli oggetti dalla lista
    public void removeAllItem() {
        productList.clear();
    }

    //restituisce la somma dei prezzi dei prodotti in lista
    public double sumAllItem() {
        int i;
        double sum = 0;
        for(i = 0; i < productList.size(); i++)
            sum += productList.get(i).getPrezzovenditaAttuale()*productList.get(i).getQuantitàOrdinata();
        return sum;
    }

    private String[] arrayQuantità (int quantità) {
        String[] arrayquantità = new String[quantità];
        for ( int i = 0; i < quantità; i++) {
            arrayquantità[i] = String.valueOf(i+1);
        }
        return arrayquantità;
    }

    public List<Prodotto> getListItems() {
        return productList;
    }

    public void setListItems(List<Prodotto> productList) { this.productList = productList; }

}
