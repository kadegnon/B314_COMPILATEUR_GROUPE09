package be.unamur.info.b314.compiler.main.exception;

public class TwoSameGlobalVarDeclException extends RuntimeException {

    private static final String msg = "Variable already existed";

    public TwoSameGlobalVarDeclException (String name) {
        super(msg + name);
    }
}
