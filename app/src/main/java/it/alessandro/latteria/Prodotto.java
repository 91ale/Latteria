package it.alessandro.latteria;

import java.io.Serializable;

public class Prodotto implements Serializable {
    private int idprodotto, quantitamagazzino, quantitanegozio, quantitaordinata = 0, idprodottovenduto = 0;
    private String barcode, nome, marca, categoria, descrizione;
    private double prezzovenditaattuale;
    private String immagine;

    public Prodotto(int idprodotto, String barcode, String nome, String marca, String categoria, int quantitamagazzino, int quantitanegozio, String descrizione, double prezzovenditaattuale, int idprodottovenduto, int quantitaordinata, String immagine) {
        this.idprodotto = idprodotto;
        this.nome = nome;
        this.barcode = barcode;
        this.marca = marca;
        this.categoria = categoria;
        this.quantitamagazzino = quantitamagazzino;
        this.quantitanegozio = quantitanegozio;
        this.descrizione = descrizione;
        this.prezzovenditaattuale = prezzovenditaattuale;
        this.idprodottovenduto = idprodottovenduto;
        this.quantitaordinata = quantitaordinata;
        this.immagine = immagine;
    }

    public int getIDprodotto() {
        return idprodotto;
    }

    public void setIDprodotto(int idprodotto) {
        this.idprodotto = idprodotto;
    }

    public String getBarCode() {
        return barcode;
    }

    public void setBarCode(String barcode) {
        this.barcode = barcode;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String nome) {
        this.categoria = categoria;
    }

    public int getQuantitamagazzino() {
        return quantitamagazzino;
    }

    public void setQuantitamagazzino(int quantitamagazzino) {
        this.quantitamagazzino = quantitamagazzino;
    }

    public int getQuantitanegozio() {
        return quantitanegozio;
    }

    public void setQuantitanegozio(int quantitanegozio) {
        this.quantitanegozio = quantitanegozio;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public double getPrezzovenditaAttuale() {
        return prezzovenditaattuale;
    }

    public void setPrezzovenditaAttuale(double prezzovenditaattuale) {
        this.prezzovenditaattuale = prezzovenditaattuale;
    }

    public String getImmagine() {
        return immagine;
    }

    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }

    public int getQuantitàOrdinata() {
        return quantitaordinata;
    }

    public void setQuantitàOrdinata(int quantitaordinata) {
        this.quantitaordinata = quantitaordinata;
    }

    public int getIdprodottovenduto() {
        return idprodottovenduto;
    }

    public void setIdprodottovenduto(int idprodottovenduto) {
        this.idprodottovenduto = idprodottovenduto;
    }

}