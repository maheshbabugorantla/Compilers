import java.util.ArrayList;
import java.util.HashSet;


class SymbolsTable {

	// All the below variables will be retrieved using get and set methods
	private String scopeName; // This stored the name of the scope such as "Global" or "Local Scope No."
	private SymbolsTable parent_global; // This the "Global" Scope or Parent
	private ArrayList<SymbolsTable> child; // List of local scopes
	private ArrayList<Symbols> table; // This will store all the symbols
	private HashSet<String> hashSet; // This is used to search for existing symbols in O(1) time

	// This is called only when a 'GLOBAL' scope is created
	public SymbolsTable(String scopeName) {
		this.scopeName = scopeName;
		parent_global = null;
		child = new ArrayList<SymbolsTable>();
		table = new ArrayList<Symbols>();
		hashSet = new HashSet<String>();
	}

	// This constructor is called when a child scope (Local Scope) is created
	public SymbolsTable(String scopeName, SymbolsTable parent_scope) {
		this.scopeName = scopeName;
		this.parent_global = parent_scope; // Assigning the parent to its child
	}

	// Add Symbol to the table
	public void addSymbol(Symbols symbol) throws IllegalArgumentException {

		String varName = symbol.getVarName();

		// Checking the symbol is already present in the current scope
		if (hashSet.contains(varName)) {
			throw new IllegalArgumentException("DECLARATION ERROR " + varName + "\n");
		} else {
			checkShadowVariable(varName);
			table.add(symbol);
			hashSet.add(symbol.getVarName());
		}
	}

	// This is used to check if there is any variable with the similar names both the currentScope and its parentScope 
	private void checkShadowVariable(String name) {

		SymbolsTable symbolsParent = this.parent_global; // Parent of the Current Scope

		// Checking for a variable name match from top-bottom until we reach the global scope
		while (symbolsParent != null) {

			// Found a match
			if (this.hashSet.contains(name)) {
				System.out.println("SHADOW VARIABLE WARNING: " + name);
				// This works similar to the -Wshadow flag for GCC Compiler to compile C Code.
			}
			symbolsParent = symbolsParent.getParentScope();
		}
		return;
	}

	// This sets the parent for the currentScope in case the scope is not a Global Scope
	public void setParentScope(SymbolsTable parent_scope) {
		this.parent_global = parent_scope;
	}

	// Get Method the fetch parent of the current Scope
	public SymbolsTable getParentScope() {
		return this.parent_global;
	}

	// Adding children scopes to the parent
	public void addChild(SymbolsTable child_table) {
		child.add(child_table);
	}

	// Fetching all the siblings of the current scope
	public ArrayList<SymbolsTable> getChildren() {
		return child;
	}

	public ArrayList<Symbols> getSymbols() {
		return table;
	}

	// This prints the SymbolsTable for the currentScope and all its children
	public void printSymTable() {

		System.out.println("Symbol table " + scopeName);

		for (Symbols symbol : table) {
			System.out.println(symbol.toString());
		}

		System.out.println();

		for (SymbolsTable symbolsTable : child) {
			symbolsTable.printSymTable();
		}
	}

	public String checkDataType(String symbolName) {
		if (hashSet.contains(symbolName)) {
			for(Symbols symbol: table) {
				if(symbol.getVarName().equals(symbolName)){
					return symbol.getVarType();
				}
			}
		}
		return null;
	}
}