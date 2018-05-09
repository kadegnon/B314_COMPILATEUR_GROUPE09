package be.unamur.info.b314.compiler.main;

import org.mozilla.javascript.ast.Symbol;
import be.unamur.info.b314.compiler.main.exception.NonValideLocalVarDeclException;

import java.util.HashMap;
import java.util.Map;


public class WhenScope implements Scope {

    private String name;
    private Scope scope;
    private Map<String, Symbol> stringSymbolMap = new HashMap<>();


    /**
     * Constructor of Scope
     * @param name for the name of scope
     * @param scope the scope
     */
    public WhenScope (String name, Scope scope) {
        this.name = name;
        this.scope = scope;
    }

    /**
     * Function that obtaining the Scope name
     * @return the name of the scope
     */

    @Override
    public String getScopeName() {
        return this.name;
    }

    /**
     * Containing the enclosing scope
     * @return the scope as scope objet
     */

    @Override
    public Scope getEnclosingScope() {
        return this.scope;
    }

    /**
     * Add a symbol to the symbol Hashmap
     * @param symbol that be added
     * @throws NonValideLocalVarDeclException if the local variable is already declared
     */

    @Override
    public void define(Symbol symbol) throws NonValideLocalVarDeclException {
        Symbol sym = stringSymbolMap.get(symbol.getName());
        if (sym != null) {
            throw new NonValideLocalVarDeclException(sym, symbol);
        } else {
            stringSymbolMap.put(symbol.getName(), symbol);
        }

    }

    /**
     * That function used for resolving a symbol from a name
     * @param name of the symbol
     * @return the symbol corresponding to the passed name
     */

    @Override
    public Symbol relsolve(String name) {
        if (stringSymbolMap.get(name) == null) {
            return this.getEnclosingScope().relsolve(name);
        } else {
            return stringSymbolMap.get(name);
        }
    }

    /**
     * Count variable
     * @return symbols size
     */

    @Override
    public int getVarCount() {
        return stringSymbolMap.size();
    }
}
