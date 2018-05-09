package be.unamur.info.b314.compiler.main;

import org.mozilla.javascript.ast.Symbol;
import be.unamur.info.b314.compiler.main.Exception.NonValideLocalVarDeclException;
import be.unamur.info.b314.compiler.main.Exception.TwoSameGlobalVarDeclException;

public interface Scope {

    String getScopeName();
    Scope getEnclosingScope();

    void define (Symbol symbol) throws NonValideLocalVarDeclException, TwoSameGlobalVarDeclException;

    Symbol relsolve (String name);

    int getVarCount();
}
