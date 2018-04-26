package be.unamur.info.b314.compiler.semantics;

import be.unamur.info.b314.compiler.B314BaseListener;
import be.unamur.info.b314.compiler.B314Parser.ArenaContext;
import be.unamur.info.b314.compiler.B314Parser.ArrayContext;
import be.unamur.info.b314.compiler.B314Parser.BoardContext;
import be.unamur.info.b314.compiler.B314Parser.BoolValContext;
import be.unamur.info.b314.compiler.B314Parser.CaseContext;
import be.unamur.info.b314.compiler.B314Parser.ClauseDefaultContext;
import be.unamur.info.b314.compiler.B314Parser.ClauseWhenContext;
import be.unamur.info.b314.compiler.B314Parser.ComputeContext;
import be.unamur.info.b314.compiler.B314Parser.EnvCaseContext;
import be.unamur.info.b314.compiler.B314Parser.ExprBoolContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDFctContext;
import be.unamur.info.b314.compiler.B314Parser.ExprIntContext;
import be.unamur.info.b314.compiler.B314Parser.FctDeclContext;
import be.unamur.info.b314.compiler.B314Parser.FileDeclContext;
import be.unamur.info.b314.compiler.B314Parser.IfThenContext;
import be.unamur.info.b314.compiler.B314Parser.IfThenElseContext;
import be.unamur.info.b314.compiler.B314Parser.ImpDeclContext;
import be.unamur.info.b314.compiler.B314Parser.IntValContext;
import be.unamur.info.b314.compiler.B314Parser.LocalVarDeclContext;
import be.unamur.info.b314.compiler.B314Parser.MoveContext;
import be.unamur.info.b314.compiler.B314Parser.NearbyContext;
import be.unamur.info.b314.compiler.B314Parser.NextContext;
import be.unamur.info.b314.compiler.B314Parser.NothingContext;
import be.unamur.info.b314.compiler.B314Parser.OpBoolCompareContext;
import be.unamur.info.b314.compiler.B314Parser.OpBoolContext;
import be.unamur.info.b314.compiler.B314Parser.OpIntContext;
import be.unamur.info.b314.compiler.B314Parser.ProgramContext;
import be.unamur.info.b314.compiler.B314Parser.ProgramMondeContext;
import be.unamur.info.b314.compiler.B314Parser.ProgramMondeGlobalDeclContext;
import be.unamur.info.b314.compiler.B314Parser.ProgramStratContext;
import be.unamur.info.b314.compiler.B314Parser.ProgramStratGlobalDeclContext;
import be.unamur.info.b314.compiler.B314Parser.RootContext;
import be.unamur.info.b314.compiler.B314Parser.ScalarContext;
import be.unamur.info.b314.compiler.B314Parser.SetToContext;
import be.unamur.info.b314.compiler.B314Parser.ShootContext;
import be.unamur.info.b314.compiler.B314Parser.SkipContext;
import be.unamur.info.b314.compiler.B314Parser.TypeContext;
import be.unamur.info.b314.compiler.B314Parser.UseContext;
import be.unamur.info.b314.compiler.B314Parser.VarContext;
import be.unamur.info.b314.compiler.B314Parser.VarDeclContext;
import be.unamur.info.b314.compiler.B314Parser.WhileContext;
import be.unamur.info.b314.compiler.semantics.exception.AlreadyGloballyDeclared;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.antlr.symtab.ArrayType;
import org.antlr.symtab.GlobalScope;
import org.antlr.symtab.Scope;
import org.antlr.symtab.Symbol;
import org.antlr.symtab.SymbolTable;
import org.antlr.symtab.Type;
import org.antlr.symtab.VariableSymbol;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @overview SymTableFiller has to fills a symbol table using ANTLR listener for B314 langage.
 * SymTableFiller is mutable.
 * @specfield symbolTable : Holds all scopes and symbols of the parsed .B314
 * @specfield currentScope : Represents the last Scope entered
 * @inv symbolTable must contains at least the global scope and the predefined types such as
 * Boolean, Integer, Square.
 */
public class SymTableFiller extends B314BaseListener {

  private final SymbolTable symTable;

  private Scope currentScope;


  public SymTableFiller() {
    this.symTable = new SymbolTable();
  }

  /**
   * @return a read-only view of the Symbol table.
   * @throws UnsupportedOperationException if attemps to modifiy the Map in any way.
   */
  public Map<String, ? extends Symbol> getSymTable() {
    return Collections.unmodifiableMap(symTable.GLOBALS.getMembers());
  }


  /**
   * @return the number of global variables declared
   */
  public int countVariables() {
    return symTable.GLOBALS.getNumberOfSymbols();
  }

  private void pushScope(Scope scope) {
    currentScope = scope;
  }

  private void popScope() {
    currentScope = currentScope.getEnclosingScope();
  }

  @Override
  public void enterRoot(RootContext ctx) {
    pushScope(symTable.GLOBALS);
  }

  @Override
  public void exitRoot(RootContext ctx) {
    popScope();
  }

  /**
   * @effects Insert a new {@link VariableSymbol} into the current scope.
   * @throws AlreadyGloballyDeclared if the scope is global and another variable has been declared <br>
   *        with the same name.
   */
  @Override
  public void enterVarDecl(VarDeclContext ctx) {
    String name = ctx.name.getText();
//    if(currentScope instanceof  GlobalScope) {
    if (ctx.getParent() instanceof ProgramMondeGlobalDeclContext) {
      if(symTable.GLOBALS.getSymbol(name) != null)
        throw new AlreadyGloballyDeclared(name);
    }
    try {
      VariableSymbol var = new VariableSymbol(name);
      currentScope.define(var);
    } catch (IllegalArgumentException e) {
      // Will throw IllegalArgumentException  if the symbol cannot be defined
      return;
    }
  }


  /*
  @Override
  public void enterType(TypeContext ctx) {
    ParseTree type = ctx.getChild(0);
    if (type == null) {
      return;
    }

    Type predefType = null;
    String varName = ((VarDeclContext) ctx.getParent()).name.getText();
    VariableSymbol var =  (VariableSymbol) currentScope.getSymbol(varName);

    if (type instanceof ScalarContext) {
      predefType = PredefinedType.get(type.getText()).type();
      var.setType(predefType);
    } else {
      ArrayContext typeArray = (ArrayContext) type;
      // First, get the type of this array vaWr.
      predefType = PredefinedType.get(typeArray.scalar().getText()).type();

      // Init the array
      ArrayType array = this.createArrayType(typeArray.elt, predefType);
      var.setType(array);
    }
  }


  @Override
  public void enterScalar(ScalarContext ctx) {
    super.enterScalar(ctx);
  }

  @Override
  public void enterArray(ArrayContext ctx) {
    super.enterArray(ctx);
  }

  private ArrayType createArrayType(List<IntValContext> arraySizes, Type type) {
    if (arraySizes.isEmpty()) {
      return null;
    }

    if (arraySizes.size() > 1) { // ? Is multi-dimension array ?
      ArrayType nestedArray;
      for (int i = arraySizes.size() - 1, sizeNested; i > 0; --i) {
        sizeNested = Integer.parseInt(arraySizes.get(i).INTEGER().getText());
        nestedArray = new ArrayType(type, sizeNested);
        type = nestedArray;
      }
    }
    int size = Integer.parseInt(arraySizes.get(0).INTEGER().getText());
    return new ArrayType(type, size);
  }

  @Override
  public void enterBoard(BoardContext ctx) {
    super.enterBoard(ctx);
  }

  @Override
  public void exitBoard(BoardContext ctx) {
    super.exitBoard(ctx);
  }

  @Override
  public void enterVarDecl(VarDeclContext ctx) {
    String name = ctx.name.getText();
    if (symTable.GLOBALS.getSymbol(name) != null) {
      throw new AlreadyGloballyDeclared(name);
    }
    try {
      VariableSymbol var = new VariableSymbol(name);
      currentScope.define(var);
    } catch (IllegalArgumentException e) {
      return;
    }
  }
*/



  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
