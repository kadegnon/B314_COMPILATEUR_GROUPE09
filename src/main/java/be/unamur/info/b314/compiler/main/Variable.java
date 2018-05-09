package be.unamur.info.b314.compiler.main;

/**
 * Represent a variable definition (name, type) in symbol table
 */
public class Variable extends Symbol {

    private int ad;
    private int context;


    public Variable(String name, Type type, int adresse, int ctx) {
        super(name, type);
        this.ad = adresse;
        this.context = ctx;
    }

    public int getAd() {
        return this.ad;
    }

    public int getContext() {
        return this.context;
    }
}
