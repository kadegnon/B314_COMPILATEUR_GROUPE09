package be.unamur.info.b314.compiler.main.exception;

import org.mozilla.javascript.ast.Symbol;

public class NonValideLocalVarDeclException extends RuntimeException {

    private Symbol symbol;
    private Symbol symbol1;

    public NonValideLocalVarDeclException (Symbol symbol, Symbol symbol1) {
        this.symbol = symbol;
        this.symbol1 = symbol1;
    }
}
