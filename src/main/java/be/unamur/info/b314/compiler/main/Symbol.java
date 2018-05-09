package be.unamur.info.b314.compiler.main;

public class Symbol {

    private String name;
    private Type type;

    /**
     * 1- constructor without type
     * @param name fo the symbol
     */

    public Symbol (String name) {
        this.name = name;
    }

    /**
     * constructor with type
     * @param name of the symbol
     * @param type type of the symbol
     */

    public Symbol (String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return the name of the symbol
     */

    public String getName() {
        return this.name;
    }

    /**
     * @return the type of the symbol
     */

    public Type getType() {
        return this.type;
    }
}
