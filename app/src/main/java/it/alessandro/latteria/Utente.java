package it.alessandro.latteria;

public class Utente {
    private String UID;
    private String nome;
    private String cognome;
    private String indirizzo;
    private String tipo;

    public Utente () { };

    public Utente(String UID, String nome, String cognome, String indirizzo, String tipo) {
        this.UID = UID;
        this.nome = nome;
        this.cognome = cognome;
        this.indirizzo = indirizzo;
        this.tipo = tipo;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getnome() {
        return nome;
    }

    public String getcognome() {
        return cognome;
    }

    public String getindirizzo() {
        return indirizzo;
    }

    public String gettipo() {
        return tipo;
    }

    public void setnome(String nome) {
        this.nome = nome;
    }

    public void setcognome(String cognome) {
        this.cognome = cognome;
    }

    public void setindirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public void settipo(String tipo) {
        this.tipo = tipo;
    }

}
