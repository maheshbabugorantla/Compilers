
public class Symbols {

	 // This class is used to create the symbols for each Symbol in .micro file
	
	private String variable_name; // Used the store the name of the Variable
	private String var_type; // Stores the DataType of the Variable
	private String var_value; // Value stored in the variable 
	
	// Constructor for each Symbol entry in the Program with no defined value
	public Symbols(String variable_name, String var_type) {
		
		this.variable_name = variable_name;
		this.var_type = var_type;		
		this.var_value = null; // Default value if no value is specified for the variable
	}
		
	// Constructor for each Symbol entry in the Program with defined value
	public Symbols(String variable_name, String var_type, String var_value) {
		
		this.variable_name = variable_name;
		this.var_type = var_type;		
		this.var_value = var_value;
	}
	
	
	// Get method to fetch the name of the variable
	public String getVarName() {
		return variable_name;
	}

	// Get method to fetch the datatype of the variable
	public String getVarType() {
		return var_type;		
	}
	
	// Get method to fetch the value stored in the variable	
	public String getVarValue() {
		return var_value;
	}
	
	// Overriding the default toString() method to print the name and type of the variable

	@Override
	public String toString() {
		
		if(var_type.equals("STRING")) {
			return("name " + variable_name + " type " + var_type + " value " + var_value);
		}
		
		else {
			return("name " + variable_name + " type " + var_type);
		}
	}
}
