import java.util.*;

class Function {

    private String functionName;

    private ArrayList<IRNode> IRList;
    private SymbolsTable funcSymbols;
    private ArrayList<String> localVariables;
    private ArrayList<String> Parameters;
    private HashMap<String, String> registerMap; // Used to store the mapping from a register to a variable/parameter

    // Register No. for Parameters
    private int paramReg = 0;

    // Regiser No. for Local Variables
    private int localReg = 0;

    // This constructor is called whenver a new function is created
    public Function(String functionName) {
        this.functionName = functionName;
        this.IRList = new ArrayList<IRNode>();
        this.funcSymbols = new SymbolsTable(functionName);
        this.localVariables = new ArrayList<String>();
        this.Parameters = new ArrayList<String>();
        this.registerMap = new HashMap<String, String>();
    }

    private String getParamRegister() {
        paramReg += 1;
        return new String("$P" + paramReg);
    }

    private String getLocalRegister() {
        localReg += 1;
        return new String("$L" + localReg);
    }

    // Get Parameters List
    public ArrayList<String> getParameters() {
        return Parameters;
    }

    // Add a new parameter to a list
    public void addParameter(String parameter) {
        Parameters.add(parameter);
        registerMap.put(parameter, getParamRegister());
    }

    // Get List of Local Variables
    public ArrayList<String> getLocalVariables() {
        return localVariables;
    }

    // Add a new local variable
    public void addLocalVariable(String localVariable) {
        localVariables.add(localVariable);
        registerMap.put(localVariable, getLocalRegister());
    }

    public void setSymbolsTable(SymbolsTable symbolsTable) {
        funcSymbols = symbolsTable;
    }

    public SymbolsTable getFuncSymbols() {
        return funcSymbols;
    }

    public void addIRNode(IRNode irNode) {
        //System.out.println(irNode);
        IRList.add(irNode);
    }

    public HashMap<String, String> getRegisterMap() {
        return registerMap;
    }

    public void printIRList(ArrayList<IRNode> IRList) {
        for (int index = 0; index < IRList.size(); index++) {
            IRNode irNode = IRList.get(index);
            System.out.println(irNode.toString());
        }
    }

    // For Debugging Purposes only

    // Print the registerMap, SymbolsTable and IRList for Function.
    public void printFunction() {
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("FUNCTION: " + getFunctionName());
        System.out.println("");
        System.out.println(new String("registerMap: " + Arrays.asList(registerMap)));

        funcSymbols.printSymTable();

        //System.out.println(IRList.size());


        System.out.println(";IR code");
        printIRList(IRList);
        System.out.println("---------------------------------------------------------------------------");
    }

    // Get the Name of the Function
    public String getFunctionName() {
        return this.functionName;
    }


}