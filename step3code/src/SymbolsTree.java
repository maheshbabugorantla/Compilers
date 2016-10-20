import java.util.ArrayList;

/* This is used to Co-Ordinate between different Scopes in the program */
 class SymbolsTree {

	 private SymbolsTable parentScope;
	 private SymbolsTable currentScope;
	 private int scopeNumber = 1; // This is used to number different Local Scopes
	 
	 public SymbolsTree() {
		parentScope = new SymbolsTable("GLOBAL");
		currentScope = parentScope; // Setting the currentScope to the Global Scope
	}

	public SymbolsTable getCurrentScope()
	{
		return currentScope;
	}

	public SymbolsTable getParentScope()
	{
		return parentScope;
	}

	public void exitCurrentScope() {
		currentScope = currentScope.getParentScope();
	}

	public void printWholeTree(){
		parentScope.printSymTable();
	}
}