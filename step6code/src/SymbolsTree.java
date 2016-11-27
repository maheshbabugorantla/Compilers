
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

	public void printWholeTree() {
		parentScope.printSymTable();
	}

	public ArrayList<Symbols> getAllSymbols(SymbolsTable symbolsTable) {

		ArrayList<Symbols> allSymbols = new ArrayList<>();

		if(symbolsTable == null) {
			return allSymbols;
		}

		allSymbols.addAll(symbolsTable.getSymbols());

		// This will exit if symbolsTable.getChildren() is null
		for(SymbolsTable childTable: symbolsTable.getChildren()) {
			getAllSymbols(childTable);
		}

		return allSymbols;
	}

	// Try Removing this later. This function is pretty Redundant
	public String checkDataType(String dataVal) {

		try {
			Integer.parseInt(dataVal);
			return "INT";
		}

		catch (NumberFormatException e) {
		}

		try {
			Float.parseFloat(dataVal); // Checks to see if we can parse for a Float Number
			return "FLOAT";
		}

		catch (NumberFormatException e) {
		}


		SymbolsTable symbolsTableScope = parentScope;

		if(symbolsTableScope != null) {

            // This Checks only for the Global Scope
			if(symbolsTableScope.checkDataType(dataVal) != null) {
				return symbolsTableScope.checkDataType(dataVal);
			}

/*			else {

                for(SymbolsTable symbolsTable: symbolsTableScope.getChildren()) {

                    String type = symbolsTable.checkDataType(dataVal);

                    if( type != null) {
                        return type
                    }
                }

            } */
		}


		return null;
	}
}