package it.alessandro.latteria.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import it.alessandro.latteria.Utility.DownloadImageTask;
import it.alessandro.latteria.InformazioniProdottoActivity;
import it.alessandro.latteria.Object.Prodotto;
import it.alessandro.latteria.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final int IN_NEGOZIO = 1;
    private static final int QUANTITA_SELEZIONATA = 102;
    private static final int COMPLETATO = 1;
    private static final int EVASO = 2;
    private static final int RICERCA = 5;
    private static final int NO_DESC = 10;
    private Context mCtx;
    private List<Prodotto> productList;
    private int tipospesa;
    private int statoordine;
    private int descrizione;
    View.OnClickListener mClickListener;
    int selectedProduct = -1;

    public ProductAdapter(Context mCtx, List<Prodotto> productList, int tipospesa, int statoordine, int descrizione) {
        this.mCtx = mCtx;
        this.productList = productList;
        this.tipospesa = tipospesa;
        this.statoordine = statoordine;
        this.descrizione = descrizione;
    }

    public ProductAdapter(Context mCtx, List<Prodotto> productList, int tipospesa) {
        this.mCtx = mCtx;
        this.productList = productList;
        this.tipospesa = tipospesa;
    }

    public ProductAdapter(Context mCtx, List<Prodotto> productList, int tipospesa, int statoordine) {
        this.mCtx = mCtx;
        this.productList = productList;
        this.tipospesa = tipospesa;
        this.statoordine = statoordine;
    }

    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

    //impostazione layout della recycleview
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.product_list_row, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        final ProductViewHolder mViewHolder = new ProductViewHolder(view);
        if (statoordine == RICERCA) {
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onClick(view);
                    selectedProduct = mViewHolder.getAdapterPosition();
                }
            });
        }
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {

        Prodotto prodotto = productList.get(position);

        DecimalFormat prezzovdec = new DecimalFormat("€ 0.00");
        holder.prezzo.setText(prezzovdec.format(prodotto.getPrezzovenditaAttuale()));

        new DownloadImageTask(holder.immagine).execute(prodotto.getImmagine());
        holder.nome.setText(prodotto.getNome());
        holder.prezzo.setText(prezzovdec.format(prodotto.getPrezzovenditaAttuale()));
        holder.marca.setText(prodotto.getMarca());

        if (tipospesa == IN_NEGOZIO) {
            holder.txtQuantitaDisponibile.setText(String.valueOf(prodotto.getQuantitanegozio()));
        } else {
            holder.txtQuantitaDisponibile.setText(String.valueOf(prodotto.getQuantitamagazzino()));
        }

        if(statoordine == COMPLETATO || statoordine == EVASO) {
            holder.txtQuantita.setVisibility(View.VISIBLE);
            holder.txtQuantita.setText("Quantità " + String.valueOf(productList.get(position).getQuantitaOrdinata()));
            holder.txtSeparator.setVisibility(View.GONE);
            holder.txtQuantitaDisponibile.setVisibility(View.GONE);
            holder.txtQuantitaOrdinata.setVisibility(View.GONE);
            holder.btnAdd.setVisibility(View.GONE);
            holder.btnMin.setVisibility(View.GONE);
        } else {
            if (tipospesa == IN_NEGOZIO && productList.get(position).getQuantitaOrdinata() > prodotto.getQuantitanegozio())
            {
                holder.txtQuantitaOrdinata.setText(String.valueOf(productList.get(position).getQuantitanegozio()));
                Intent intent = new Intent("quantita_modificata");
                LocalBroadcastManager.getInstance(holder.itemView.getContext()).sendBroadcast(intent);
            } else if (tipospesa != IN_NEGOZIO && productList.get(position).getQuantitaOrdinata() > prodotto.getQuantitamagazzino()) {
                holder.txtQuantitaOrdinata.setText(String.valueOf(productList.get(position).getQuantitamagazzino()));
                Intent intent = new Intent("quantita_modificata");
                LocalBroadcastManager.getInstance(holder.itemView.getContext()).sendBroadcast(intent);
            } else if (productList.get(position).getQuantitaOrdinata() != 0){
                //imposta il valore visualizzato nella textview della quantità ordinata sulla quantità letta dal DB
                holder.txtQuantitaOrdinata.setText(String.valueOf(productList.get(position).getQuantitaOrdinata()));
                Intent intent = new Intent("quantita_modificata");
                LocalBroadcastManager.getInstance(holder.itemView.getContext()).sendBroadcast(intent);
            } else {
                holder.txtQuantitaOrdinata.setText("1");
                productList.get(position).setQuantitaOrdinata(1);
                Intent intent = new Intent("quantita_modificata");
                LocalBroadcastManager.getInstance(holder.itemView.getContext()).sendBroadcast(intent);
            }
        }

        if (statoordine == RICERCA) {
            holder.txtSeparator.setVisibility(View.GONE);
            holder.txtQuantitaDisponibile.setVisibility(View.GONE);
            holder.txtQuantitaOrdinata.setVisibility(View.GONE);
            holder.btnAdd.setVisibility(View.GONE);
            holder.btnMin.setVisibility(View.GONE);
        }

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
        for (i = 0; i < productList.size(); i++) {
            if (tipospesa == IN_NEGOZIO && productList.get(i).getQuantitaOrdinata() > productList.get(i).getQuantitanegozio()) {
                sum += productList.get(i).getPrezzovenditaAttuale() * productList.get(i).getQuantitanegozio();
            } else if (tipospesa != IN_NEGOZIO && productList.get(i).getQuantitaOrdinata() > productList.get(i).getQuantitamagazzino()) {
                sum += productList.get(i).getPrezzovenditaAttuale() * productList.get(i).getQuantitamagazzino();
            } else {
                sum += productList.get(i).getPrezzovenditaAttuale() * productList.get(i).getQuantitaOrdinata();
            }
        }
        return sum;
    }

    public List<Prodotto> getListItems() {
        return productList;
    }

    public void setListItems(List<Prodotto> productList) {
        this.productList = productList;
    }


    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        public ImageView immagine;
        public TextView nome, prezzo, marca, txtQuantita;
        public Button btnMin;
        public Button btnAdd;
        public TextView txtQuantitaOrdinata;
        public TextView txtQuantitaDisponibile;
        public TextView txtSeparator;
        public ConstraintLayout viewBackground;
        public ConstraintLayout viewForeground;

        public ProductViewHolder(View view) {
            super(view);

            immagine = view.findViewById(R.id.imgProdotto);
            nome = view.findViewById(R.id.txtNome);
            prezzo = view.findViewById(R.id.txtPrezzo);
            marca = view.findViewById(R.id.txtMarca);
            btnMin = view.findViewById(R.id.btnMin);
            btnAdd = view.findViewById(R.id.btnAdd);
            txtQuantita = view.findViewById(R.id.txtQuantita);
            txtQuantitaOrdinata = view.findViewById(R.id.txtQuantitaOrdinata);
            txtQuantitaDisponibile = view.findViewById(R.id.txtQuantitaDisponibile);
            txtSeparator = view.findViewById(R.id.txtSeparator);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);

            btnMin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantita = productList.get(getAdapterPosition()).getQuantitaOrdinata();
                    if (quantita > 1) {
                        quantita--;
                        productList.get(getAdapterPosition()).setQuantitaOrdinata(quantita);
                        txtQuantitaOrdinata.setText(String.valueOf(quantita));
                        Intent intent = new Intent("quantita_modificata");
                        LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                    }
                }
            });

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantita = productList.get(getAdapterPosition()).getQuantitaOrdinata();
                    if (tipospesa == IN_NEGOZIO && productList.get(getAdapterPosition()).getQuantitanegozio() > quantita) {
                        quantita++;
                        productList.get(getAdapterPosition()).setQuantitaOrdinata(quantita);
                        txtQuantitaOrdinata.setText(String.valueOf(quantita));
                        Intent intent = new Intent("quantita_modificata");
                        LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                    } else if (tipospesa != IN_NEGOZIO && productList.get(getAdapterPosition()).getQuantitamagazzino() > quantita) {
                        quantita++;
                        productList.get(getAdapterPosition()).setQuantitaOrdinata(quantita);
                        txtQuantitaOrdinata.setText(String.valueOf(quantita));
                        Intent intent = new Intent("quantita_modificata");
                        LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                    }
                }
            });

            if (descrizione != NO_DESC) {
                immagine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int quantitadisponibile = 0;

                        if (tipospesa == IN_NEGOZIO) {
                            quantitadisponibile = productList.get(getAdapterPosition()).getQuantitanegozio();
                        } else {
                            quantitadisponibile = productList.get(getAdapterPosition()).getQuantitamagazzino();
                        }

                        Intent intentinformazioniprodotto = new Intent(mCtx, InformazioniProdottoActivity.class);
                        intentinformazioniprodotto.putExtra("STATO", statoordine);
                        intentinformazioniprodotto.putExtra("POSITION", getAdapterPosition());
                        intentinformazioniprodotto.putExtra("NOME", productList.get(getAdapterPosition()).getNome());
                        intentinformazioniprodotto.putExtra("MARCA", productList.get(getAdapterPosition()).getMarca());
                        intentinformazioniprodotto.putExtra("PREZZO", productList.get(getAdapterPosition()).getPrezzovenditaAttuale());
                        intentinformazioniprodotto.putExtra("IMMAGINE", productList.get(getAdapterPosition()).getImmagine());
                        intentinformazioniprodotto.putExtra("QUANTITA_SELEZIONATA", productList.get(getAdapterPosition()).getQuantitaOrdinata());
                        intentinformazioniprodotto.putExtra("QUANTITA_DISPONIBILE", quantitadisponibile);
                        intentinformazioniprodotto.putExtra("DESCRIZIONE", productList.get(getAdapterPosition()).getDescrizione());

                        ((Activity) mCtx).startActivityForResult(intentinformazioniprodotto, QUANTITA_SELEZIONATA);

                    }
                });
            }

        }
    }

}
