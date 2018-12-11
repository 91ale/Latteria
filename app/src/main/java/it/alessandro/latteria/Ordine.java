package it.alessandro.latteria;

import java.util.Date;

public class Ordine {

    private int idordine;
    private Date dataora;
    private String stato, tipo;
    private double importo;

    public Ordine ( int idordine, Date dataora, String stato, String tipo, double importo) {
        this.idordine = idordine;
        this.dataora = dataora;
        this.stato = stato;
        this.tipo = tipo;
        this.importo = importo;
    }

    public int getIDordine() {
        return idordine;
    }

    public void setIDordine(int idordine) { this.idordine = idordine; }

    public Date getDataOra() {
        return dataora;
    }

    public void setDataOra(Date dataora) { this.dataora = dataora; }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) { this.stato = stato; }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getImporto() {
        return importo;
    }

    public void setImporto(double importo) { this.importo = importo; }


}