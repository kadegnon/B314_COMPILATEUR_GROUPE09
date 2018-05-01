package be.unamur.info.b314.compiler.semantics;

import be.unamur.info.b314.compiler.B314BaseListener;
import be.unamur.info.b314.compiler.B314Parser.ArenaEltContext;
import be.unamur.info.b314.compiler.B314Parser.ArrayContext;
import be.unamur.info.b314.compiler.B314Parser.ArrayEltContext;
import be.unamur.info.b314.compiler.B314Parser.EnvCaseContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDBoolContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDCaseContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDFctContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDIntContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDOpBoolContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDOpIntContext;
import be.unamur.info.b314.compiler.B314Parser.ExprDParContext;
import be.unamur.info.b314.compiler.B314Parser.ExprGContext;
import be.unamur.info.b314.compiler.B314Parser.RootContext;
import be.unamur.info.b314.compiler.B314Parser.ScalarContext;
import be.unamur.info.b314.compiler.B314Parser.SetToContext;
import be.unamur.info.b314.compiler.B314Parser.TypeContext;
import be.unamur.info.b314.compiler.B314Parser.VarContext;
import be.unamur.info.b314.compiler.B314Parser.VarDeclContext;
import be.unamur.info.b314.compiler.semantics.exception.AlreadyGloballyDeclared;
import be.unamur.info.b314.compiler.semantics.exception.NotMatchingType;
import be.unamur.info.b314.compiler.semantics.exception.NotPositiveSizeForArray;
import be.unamur.info.b314.compiler.semantics.exception.UndeclaredVariable;
import be.unamur.info.b314.compiler.semantics.symtab.ArrayType;
import java.util.Collections;
import java.util.Map;
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
    if (currentScope instanceof GlobalScope) {
      if(symTable.GLOBALS.getSymbol(name) != null)
        throw new AlreadyGloballyDeclared(name);
    }
    try {
      VariableSymbol var = new VariableSymbol(name);
      currentScope.define(var);
    } catch (IllegalArgumentException e) {
      // throw IllegalArgumentException  if the symbol cannot be defined
      return;
    }
  }


  /**
   * @effects Define the type of an SymbolVariable already inserted in the symbtable.
   *
   */
  @Override
  public void enterType(TypeContext ctx) {
    ParseTree type = ctx.getChild(0);
    if (type == null) {
      return;
    }

    Type varType;
    String varName = ((VarDeclContext) ctx.getParent()).name.getText();
    VariableSymbol var =  (VariableSymbol) currentScope.getSymbol(varName);

    if (type instanceof ScalarContext) {
      varType = PredefinedType.get(type.getText()).type();
      var.setType(varType);
    } else {
      ArrayContext typeArray = (ArrayContext) type;

      // Check for the positivity of the array' size
      int sizeArray = Integer.parseInt(typeArray.one.INTEGER().getText());
      if(sizeArray <= 0){
        throw new NotPositiveSizeForArray(""+sizeArray);
      }

      int secondSizeArray = 0;
      // If defined, check for the positivity of the second array' size
      if(typeArray.second != null) {
        secondSizeArray = Integer.parseInt(typeArray.second.INTEGER().getText());
        if (secondSizeArray <= 0) {
          throw new NotPositiveSizeForArray("" + secondSizeArray);
        }
      }

      // First, get the type of this array vaWr.
      varType = PredefinedType.get(typeArray.scalar().getText()).type();

      // Init the array
      ArrayType array = this.createArrayType(varType, sizeArray, secondSizeArray);
      var.setType(array);
    }
  }

  /**
   *
   * @requires type - The scalar type of the array. Must be defined
   * @requires size - The size of the array
   * @return the {@link ArrayType} with the nested type if sizeSecondArray is defined.
   */
  private ArrayType createArrayType(Type type,int size, int sizeSecondArray) {
    if (sizeSecondArray > 0) {
      // Create an nested array if the array two-dimensional
      ArrayType nestedArray = new ArrayType(type, sizeSecondArray);
      type = nestedArray;
    }

    return new ArrayType(type, size);
  }

  /**
   * @effects Check the type of the instructions, if the types of the Expr named <i>var</i> and <i>value</i> are
   *          compatible.
   * @throws NotMatchingType if the types of both expr in instruction are not compatible.
   */
  @Override
  public void enterSetTo(SetToContext ctx) {
//    if(!TypeChecker.check(ctx.var, ctx.value)) {
    ExprGContext exprG = ctx.var;
    ExprDContext exprD = ctx.value;
    PredefinedType exprGType = getTypeOfExprG(exprG);
    PredefinedType exprDType = getTypeOfExprD(exprD);

    if(exprGType == PredefinedType.CASE) {
      if(exprDType == null || !exprDType.equals(PredefinedType.CASE_ITEM)) {
        throw new NotMatchingType(ctx.toString());
      }
    } else{
      // Check if the exprG var or array is in the symtab
      String symName;
      Symbol sym;

      if(exprG instanceof VarContext){
        symName = ((VarContext)exprG).name.getText();
      }else{
        symName = ((ArrayEltContext)exprG).name.getText();
      }
      sym = currentScope.resolve(symName);
      if(sym == null)
        throw new UndeclaredVariable(symName);

      // Check for the type' matching

    }

    super.enterSetTo(ctx);
  }


  /**
   * @return The corresponding the {@see PredefinedType} for this expression <br>
   *          otherwise <b>null</b>.
   */
  private PredefinedType getTypeOfExprG(ExprGContext expr) {
    RuleContext ctx = expr.getRuleContext();
    if(ctx instanceof ArenaEltContext)
      return PredefinedType.CASE;

    if (ctx instanceof ArrayEltContext)
      return PredefinedType.ARRAY;

    return PredefinedType.VARIABLE;
  }

  /**
   * @requires expr to be not null
   * @return The corresponding the {@see PredefinedType} for this expression <br>
   *          otherwise <b>null</b>.
   */
  private PredefinedType getTypeOfExprD(ExprDContext expr) {
    RuleContext ctx = expr.getRuleContext();

    if(ctx instanceof ExprDIntContext || ctx instanceof ExprDOpIntContext)
      return PredefinedType.INTEGER;

    if(ctx instanceof ExprDBoolContext || ctx instanceof ExprDOpBoolContext)
      return PredefinedType.BOOLEAN;

    if(ctx instanceof ExprDCaseContext){
      if(((ExprDCaseContext)ctx).children.get(0) instanceof EnvCaseContext)
        return PredefinedType.CASE_ITEM;
      else
        return null;
    }

    if(ctx instanceof ExprDCaseContext)
      return PredefinedType.VALUE;

    if(ctx instanceof ExprDFctContext)
      return PredefinedType.FUNCTION;

    if(ctx instanceof ExprDParContext)
      return this.getTypeOfExprD(((ExprDParContext)ctx).expr);

    return null;
  }



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
