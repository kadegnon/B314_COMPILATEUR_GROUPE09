package be.unamur.info.b314.compiler.main;

import be.unamur.info.b314.compiler.main.exception.NonValideLocalVarDeclException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */

public class Function extends Symbol implements Scope {

    private String name;
    private Scope enclosingScope;
    private Symbol[] paramters;
    private Map<String, org.mozilla.javascript.ast.Symbol> sym = new HashMap<String, org.mozilla.javascript.ast.Symbol>();

    /**
     * Construction for initialisation
     * @param name of the symbol
     * @param type type of the symbol
     * @param n scope name
     * @param scope enclosing scope
     * @param p parameter of the function
     */

    public Function(String name, Type type, String n, Scope scope, Symbol[] p) {
        super(name, type);
        this.name = n;
        this.paramters = p;
        enclosingScope = scope;
    }

    /**
     * @return scope name
     */

    @Override
    public String getScopeName() {
        return this.name;
    }

    /**
     * @return enclosing scope
     */

    @Override
    public Scope getEnclosingScope() {
        return this.enclosingScope;
    }

    /**
     * Defines a new symbol
     * @param symbol to be definied
     * @throws NonValideLocalVarDeclException throw if the symbol already exists
     */

    @Override
    public void define(org.mozilla.javascript.ast.Symbol symbol) throws NonValideLocalVarDeclException {
        org.mozilla.javascript.ast.Symbol syml = sym.get(symbol.getName());
        if (syml != null) {
            throw new NonValideLocalVarDeclException(syml, symbol);
        } else {
            if (symbol.getName().equals(this.getName())) {
                throw new NonValideLocalVarDeclException(syml, symbol) ;
            }
            sym.put(symbol.getName(), symbol);
        }
    }

    /**
     * Symbol resolver
     * @param name of symbol to resolve
     * @return the symbol corresponding to th name; either from enclosign scope
     */

    @Override
    public org.mozilla.javascript.ast.Symbol relsolve(String name) {
        if (sym.get(name) == null) {
            return getEnclosingScope().relsolve(name);
        } else {
            return sym.get(name);
        }
    }

    /**
     * @return symbols size
     */

    @Override
    public int getVarCount() {
        return sym.size();
    }
}
