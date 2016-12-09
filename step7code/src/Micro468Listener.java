
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro468Listener extends MicroBaseListener {


	public SymbolsTree symbolsTree;

	private int blockNum;
	private int regNum;
	private int labelNo;

	private ArrayList<String> variablesList;

	HashMap<String, SymbolsTable> symbolsTableMap;

	String currentFuncName;

	int flag_returnStmt = 0;

	NodesPrinter nodesPrinter; // Used to print all the IRNodes

	List<TinyNode> tinyNodeArrayList = new ArrayList<TinyNode>();

	Stack<String> labelStack1;
	Stack<String> labelStack2;

	SymbolsTable globalSymbolTable;

	HashMap<String, Function> functionsMap; // HashMap of all Functions
	HashSet<String> funcNames; // Names of all the Function in the Program

	//String returnRegister;

	public Micro468Listener() {
		this.symbolsTree = new SymbolsTree();
		this.blockNum = 1;
		this.nodesPrinter = new NodesPrinter();
		this.regNum = 0;
		this.labelNo = 1;
		this.labelStack1 = new Stack<String>();
		this.labelStack2 = new Stack<String>();
		this.variablesList = new ArrayList<String>();

		this.functionsMap = new HashMap<String, Function>();

		this.funcNames = new HashSet<String>();

		this.globalSymbolTable = new SymbolsTable("GLOBAL");

		this.symbolsTableMap = new HashMap<String, SymbolsTable>(); // Used to find appropriate SymbolTable for each Function.

		//this.returnRegister = null; // Used to store the register in which the return value of a function is stored in
	}

	public boolean isInteger(String string) {

		if(string == null) {
			return false;
		}

		try {
			Integer.parseInt(string);
		}
		catch (NumberFormatException e) {
			// TODO: handle exception
			return false;
		}

		return true;
	}

	public boolean isFloat(String string){

		if(string == null) {
			return false;
		}

		try {
			Float.parseFloat(string);
		}
		catch (NumberFormatException e) {
			// TODO: handle exception
			return false;
		}

		return true;
	}

	public String inFunction(String id, String FunctionName) {

		//System.out.println("Check for Variable inside Local Scope or Global Scope");

		Function function = functionsMap.get(FunctionName);

//		try {
			String reg = function.getRegisterMap().get(id);
			//System.out.println("Done Checking");

			// Not a local Variable
			if (reg != null) {
				return reg;
			}
//		}
//		catch (Exception exception) {
			// Checking if the variable is in the Global Scope
			if (symbolsTree.getParentScope().checkDataType(id) != null) {
				return id;
			}

			else {
				System.out.println("ERROR ID: " + id);
				System.out.println("Symbol not found");
				//System.exit(1);
			}
//		}

		return null;
	}

	public void pushSymbol(String currentValue, SymbolsTable table) {

		if (currentValue.startsWith("INT", 0)) {
			String[] intVal = currentValue.substring(3).split(",");
			for (String str : intVal) {
				Symbols symbol = new Symbols(str, "INT");
				table.addSymbol(symbol);
			}
		} else if (currentValue.startsWith("FLOAT", 0)) {
			String[] float_val = currentValue.substring(5).split(",");
			//System.out.println(float_val.length);
			for (String str : float_val) {
				Symbols symbol = new Symbols(str, "FLOAT");
				table.addSymbol(symbol);
			}
		} else if (currentValue.startsWith("STRING", 0)) {
			String[] str_val = currentValue.substring(6).split(":=");
			Symbols symbol = new Symbols(str_val[0], "STRING", str_val[1]);
			table.addSymbol(symbol);
		}
	}

	public void addLocalVariables(String localVariables, Function function) {

		if (localVariables.startsWith("INT", 0)) {
			String[] intVal = localVariables.substring(3).split(",");
			for (String str : intVal) {
				//System.out.println(str);
				function.addLocalVariable(str);
			}
		} else if (localVariables.startsWith("FLOAT", 0)) {
			String[] float_val = localVariables.substring(5).split(",");
			for (String str : float_val) {
				//System.out.println(str);
				function.addLocalVariable(str);
			}
		} else if (localVariables.startsWith("STRING", 0)) {
			String[] str_val = localVariables.substring(6).split(":=");
			function.addLocalVariable(str_val[0]);
		}
	}

	public void functionParameters(String currentValue, SymbolsTable table, Function function) {
        //System.out.println("Inside functionParameters");
        String[] intVal = currentValue.split(",");
		for (int index = 0; index < intVal.length; index++) {
			//System.out.println("Inside For Loop");
			if (currentValue.startsWith("INT", 0)) {
				String str = intVal[index];
				str = str.substring(3); // Parameter Name
				Symbols symbol = new Symbols(str, "INT");
				table.addSymbol(symbol);
				function.addParameter(str);
			} else if (currentValue.startsWith("FLOAT", 0)) {
				String str = intVal[index];
				str = str.substring(5); // Parameter Name
				Symbols symbol = new Symbols(str, "FLOAT");
				table.addSymbol(symbol);
				function.addParameter(str);
			}
		}
	}

	public String getBlockName() {
		return "BLOCK " + blockNum++;
	}


	public String infixtoPostfix(String infixStr, HashSet<String> funcNames) {

		StringBuilder postFix = new StringBuilder();

		int flag_func = 0;

		infixStr = infixStr.split(";")[0];

		infixStr = infixStr.replaceAll("[\\s]", ""); // Removing all the White Spaces in the Expression

		infixStr = new String(infixStr + ")");

		String[] tokens = infixStr.split("(?<=[-+*/(),])|(?=[-+*/(),])");

		for(int index = 0; index < tokens.length - 1; index++) {

			if(tokens[index] != null)
			{
				if(funcNames.contains(tokens[index])) {
					tokens[index] = new String(tokens[index] + tokens[index+1]);
					tokens[index + 1] = null;
					flag_func = 1;
				}

				else if(tokens[index].equals(")") && flag_func == 1) {
					tokens[index] = "]";
					flag_func = 0;
				}
			}
		}

		final List<String> list = new ArrayList<String>();
		Collections.addAll(list,tokens);
		list.removeAll(Collections.singleton(null)); // Removing Null from String array
		tokens = list.toArray(new String[list.size()]);

		/*for(String token: tokens) {
			System.out.println(token);
		}*/

		Stack<String> OpStack = new Stack<String>(); // Stack used to store the Operators

		HashMap<String, Integer> opPrecedence = new HashMap<String, Integer>(); // This HashMap Stores the Operator Precedence

		opPrecedence.put("+", 1);
		opPrecedence.put("-", 1);
		opPrecedence.put("*", 2);
		opPrecedence.put("/", 2);

		OpStack.push("(");


		for(String token: tokens) {

			if(token.endsWith("(")) {
				//System.out.println(token.substring(0,(token.length() - 1)));
			}

			//System.out.println("Token: " + token);
			if(!token.equals("")) {
				if(token.equals("(")) {
					OpStack.push(token);
				}

				// Pushing the Function Label : if token is "["
				else if(token.endsWith("(") && funcNames.contains(token.substring(0,(token.length()-1)))) {
					OpStack.push(token.substring(0,token.length()-1));
					//System.out.println("Inside function");
				}

				// Dealing with Operators ( + , - , * , / )
				else if(token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {

					//int currentPrecedence = 3;

					if(OpStack.isEmpty() || OpStack.peek().equals("(") || funcNames.contains(OpStack.peek())) {
						OpStack.push(token);
					}

					else if(opPrecedence.get(token) != null && (opPrecedence.get(token) >= opPrecedence.get(OpStack.peek()))) {
						OpStack.push(token);
					}

					else {
						postFix.append(OpStack.pop() + " ");
						OpStack.push(token);
					}
				}

				else if(token.equals(")")) {

					String topToken = OpStack.pop();

					while(!topToken.equals("(")) {
						postFix.append(topToken + " ");
						topToken = OpStack.pop();
					}
				}

				else if(token.equals("]")) {

					while(true) {
						String topToken = OpStack.pop();
						postFix.append(topToken + " ");

						// topToken == "["
						if(funcNames.contains(topToken)) {
							break;
						}
					}
				}

				else if(token.equals(",")) {

					String topToken = OpStack.peek();

					//System.out.println(topToken);

					// topToken != "["
					while(!funcNames.contains(topToken)) {
						postFix.append(topToken + " ");
						OpStack.pop();
						topToken = OpStack.peek();
					}
					OpStack.push(token);
				}

				// Appending Operand to the PostFix Expression
				else {
					postFix.append(token + " ");
				}

			} }

		//System.out.println(postFix.toString());
		return postFix.toString();
	}

	public String infix_to_Postfix(String infix_Str) {

		infix_Str = infix_Str.split(";")[0];
		infix_Str = infix_Str.replaceAll("[\\s]", "");
		String[] elements = infix_Str.split("(?<=[-+*/()])|(?=[-+*/()])");

		StringBuilder output_Str = new StringBuilder(); // This Stores the PostFix Conversion String

		Stack<String> opStack = new Stack<String>(); // This Stack is used to store the Operators

		HashMap<String, Integer> opPrecedence = new HashMap<String, Integer>(); // This HashMap Stores the Operator Precedence

		opPrecedence.put("+", 1);
		opPrecedence.put("-", 1);
		opPrecedence.put("*", 2);
		opPrecedence.put("/", 2);

		// Traversing through all the symbols
		for (int index = 0; index < elements.length; index++) {

			//    System.out.println(elements[index]);

			if (elements[index].equals("+") || elements[index].equals("-") || elements[index].equals("*") || elements[index].equals("/") || elements[index].equals("(") || elements[index].equals(")")) {

				if (elements[index].equals(")")) {

					while (opStack.peek() != "(") {
						String popped_op = opStack.pop();
						output_Str.append(popped_op + " ");
					}

					opStack.pop();
				} else if (elements[index].equals("(")) {
					opStack.push("(");
				} else {

					int currentPrecedence = 3;

					if (opPrecedence.containsKey(elements[index])) {
						currentPrecedence = opPrecedence.get(elements[index]);
					}

					while (!opStack.empty() && opPrecedence.get(opStack.peek()) != null && currentPrecedence <= opPrecedence.get(opStack.peek())) {
						String popped_op = opStack.pop();
						output_Str.append(popped_op + " ");
					}

					opStack.push(elements[index].toString());
				}
			} else {
				output_Str.append(elements[index] + " ");
			}
		}

		// Emptying the Operator Stack when we run out of Operands
		while (!opStack.empty()) {
			output_Str.append(opStack.pop() + " ");
		}

		return output_Str.toString();
	}

	public String getRegister() {
		regNum += 1;
		String registerNo = new String("$T" + regNum);
		return registerNo;
	}

	public String getLabel() {
		String LabelNo = new String("label" + labelNo);
		labelNo += 1;
		return LabelNo;
	}

	public String OpCodeCheck(String Operation, String Type) {
		if (Operation.equals("+")) {
			if (Type.equals("INT")) {
				return "ADDI"; //Adding Integer
			} else if (Type.equals("FLOAT")) {
				return "ADDF";
			}
		}
		if (Operation.equals("-")) {
			if (Type.equals("INT")) {
				return "SUBI"; //subtraction Integer
			} else if (Type.equals("FLOAT")) {
				return "SUBF";
			}
		}
		if (Operation.equals("*")) {
			if (Type.equals("INT")) {
				return "MULTI"; //multiplication Integer
			} else if (Type.equals("FLOAT")) {
				return "MULTF";
			}
		}
		if (Operation.equals("/")) {
			if (Type.equals("INT")) {
				return "DIVI"; //Division integer
			} else if (Type.equals("FLOAT")) {
				return "DIVF";
			}
		}
		return "ERROR"; // invalid operator
	}

	public String checkStore(String Type) {
		if (Type.equals("INT")) {
			return "STOREI";
		} else if (Type.equals("FLOAT")) {
			return "STOREF";
		} else {
			return "ERROR";
		}
	}

	public void checkCompOp(String compOp, String operand1, String operand2) {

		// Checking if the operand1 is a parameter/ local or Global Variable
		if(!operand1.startsWith("$")) {
			if(inFunction(operand1, currentFuncName) != null) { // Checking in the Local Scope
				operand1 = inFunction(operand1, currentFuncName);
			}
			else if(symbolsTree.getParentScope().checkDataType(operand1) == null) { // Checking if not in Global Scope
				System.out.println("ERROR ID");
				System.out.println(operand1);
				//System.exit(1);
				return;
			}
		}

		// Checking if the 'operand2' is a parameter/ local or Global Variable
		if(!operand2.startsWith("$")) {
			if(inFunction(operand2, currentFuncName) != null) { // Checking in the Local Scope
				operand2 = inFunction(operand2, currentFuncName);
			}
			else if(symbolsTree.getParentScope().checkDataType(operand2) == null) { // Checking if not in Global Scope
				System.out.println("ERROR ID");
				System.out.println(operand2);
				// System.exit(1);
				return;
			}
		}

		switch(compOp) {

		case "<":
			nodesPrinter.addIRNode(new IRNode("GE", operand1, operand2, labelStack1.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("GE", operand1, operand2, labelStack1.peek()));
			break;

		case ">":
			nodesPrinter.addIRNode(new IRNode("LE", operand1, operand2, labelStack1.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("LE", operand1, operand2, labelStack1.peek()));
			break;

		case "<=":
			nodesPrinter.addIRNode(new IRNode("GT", operand1, operand2, labelStack1.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("GT", operand1, operand2, labelStack1.peek()));
			break;

		case ">=":
			nodesPrinter.addIRNode(new IRNode("LT", operand1, operand2, labelStack1.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("LE", operand1, operand2, labelStack1.peek()));
			break;

		case "=":
			nodesPrinter.addIRNode(new IRNode("NE", operand1, operand2, labelStack1.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("NE", operand1, operand2, labelStack1.peek()));
			break;

		case "!=":	
			nodesPrinter.addIRNode(new IRNode("EQ", operand1, operand2, labelStack1.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("EQ", operand1, operand2, labelStack1.peek()));
			break;
		}
	}

	////////////////////*/
	@Override
	public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
		//System.out.println("Enter Pgm_body");
		if (ctx.getChild(0) == null || ctx.getChild(0).getText() == "") return;
		String[] global_vars = ctx.getChild(0).getText().split(";"); // Fetching all the Global Variables

		// Adding Variables to the Symbol Table
		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, symbolsTree.getParentScope());
		}

		//symbolsTree.getParentScope().printSymTable();
	}

	// Here we print the whole symbolsTree when we exit the PROGRAM
	@Override
	public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        //System.out.println("Exit Pgm Body");
        //symbolsTree.printWholeTree();
		nodesPrinter.printIRNodes();
		//CFGHandler cfgHandler = new CFGHandler(nodesPrinter.getIRNodeList(), symbolsTree, functionsMap);
		TinyNode tinyNode = new TinyNode(functionsMap, symbolsTree.getParentScope()); // Initializing the functionsMap
		tinyNode.convertIRtoTiny(nodesPrinter.getIRNodeList(),symbolsTree,variablesList,tinyNodeArrayList);
		tinyNode.printTinyList(tinyNodeArrayList);
	}

	@Override
	public void enterFunc_decl(MicroParser.Func_declContext ctx) {
		//System.out.println("Enter Func_decl");

		regNum = 0; // Resetting the Register Value for each Function Scope.

		String retType = ctx.getChild(1).getText();
		String FuncID = ctx.getChild(2).getText();

		//System.out.println(FuncID);

		currentFuncName = FuncID;

		// Creating a new Symbol Table for each function
		SymbolsTable table = new SymbolsTable(FuncID);

		funcNames.add(FuncID); // Adding the Function Name to the 'funcNames' to the Set

		symbolsTableMap.put(FuncID,table); // Adding the SymbolTable to the hashMap.

		Function newFunction = new Function(FuncID);

		// Generating New IRNode List for each Function

		newFunction.addIRNode(new IRNode("LABEL", FuncID));
		newFunction.addIRNode(new IRNode("LINK"));
		nodesPrinter.addIRNode(new IRNode("LABEL", FuncID));
		nodesPrinter.addIRNode(new IRNode("LINK"));
		symbolsTree.getCurrentScope().addChild(table);

		functionsMap.put(newFunction.getFunctionName(), newFunction);

		// Fetching the Parameters of the Function
		if (ctx.getChild(4) != null) {
            //System.out.println("Inside If 4");
			//System.out.println(ctx.getChild(4).getText());
            functionParameters(ctx.getChild(4).getText(), table, newFunction);
		}

		// Fetching the Local Variables of the Function
		if (ctx.getChild(7) != null && ctx.getChild(7).getChild(0) != null) {

//			System.out.println("Inside If 7");
			String str = ctx.getChild(7).getChild(0).getText();

			// This below line is more useful to parse variables when it is like
			// FLOAT a,b,c;
			// FLOAT addresult,multiplyresult;
			String[] VarLines = str.split(";");

			for(String line: VarLines) {

				//System.out.println(line);
				if (line.length() == 0) return;

				// Adding localVariables to the functions SymbolTable
				pushSymbol(line, table);
				addLocalVariables(line, newFunction);
			}
		}

		//System.out.println("Done Fetching the Local Variables");
		//System.out.println(table);
		newFunction.setSymbolsTable(table); // Setting an Individual Symbol Table for each Function

//		functionsMap.put(newFunction.getFunctionName(), newFunction);
		//System.out.println("End of Function Declaration");
		//table.printSymTable();
	}

	@Override
	public void exitFunc_decl(MicroParser.Func_declContext ctx) {
		//functionsMap.get(currentFuncName).printFunction();
		//if (flag_returnStmt == 0) {
			//Function newFunction = functionsMap.get(currentFuncName);
			//newFunction.addIRNode(new IRNode("RET"));
			//nodesPrinter.addIRNode(new IRNode("RET"));
		//}
	}

	@Override
	public void exitFunc_body(MicroParser.Func_bodyContext var1) {

		//System.out.println(var1);

		Function newFunction = functionsMap.get(currentFuncName);
		newFunction.addIRNode(new IRNode("RET"));
		nodesPrinter.addIRNode(new IRNode("RET"));
	}

	@Override public void enterReturn_stmt(MicroParser.Return_stmtContext ctx) {

		//System.out.println("Enter Return Statement");

		int integer_flag = 0;
		flag_returnStmt = 1;

		String returnVariable = ctx.getChild(1).getText();

		String[] elements = returnVariable.split("(?<=[-+*/()])|(?=[-+*/()])");

		String returnType = functionsMap.get(currentFuncName).getFuncSymbols().checkDataType(elements[0]);

		Function newFunction = functionsMap.get(currentFuncName);

		try {
			Integer.parseInt(returnVariable);

			String register = getRegister();

			newFunction.addIRNode(new IRNode(checkStore("INT"), returnVariable, register));
			newFunction.addIRNode(new IRNode(checkStore("INT"), register, "$R"));

			nodesPrinter.addIRNode(new IRNode(checkStore("INT"), returnVariable, register));
			nodesPrinter.addIRNode(new IRNode(checkStore("INT"), register, "$R"));

			integer_flag = 1;
		} catch (NumberFormatException e) {
		}

		try {

			if (integer_flag == 0) {
				Float.parseFloat(returnVariable);

				String register = getRegister();

				newFunction.addIRNode(new IRNode(checkStore("INT"), returnVariable, register));
				newFunction.addIRNode(new IRNode(checkStore("INT"), register, "$R"));
				nodesPrinter.addIRNode(new IRNode(checkStore("INT"), returnVariable, register));
				nodesPrinter.addIRNode(new IRNode(checkStore("INT"), register, "$R"));
			}
		} catch (NumberFormatException e) {

			SymbolsTable symbolsTable = newFunction.getFuncSymbols();

			String postFix = infix_to_Postfix(returnVariable);
			String finalReg = parsePostfixExpr(postFix, returnType);

			if (!finalReg.startsWith("$")) {
				finalReg = inFunction(returnVariable, currentFuncName);
			}
			newFunction.addIRNode(new IRNode(checkStore(returnType), finalReg, "$R"));
			nodesPrinter.addIRNode(new IRNode(checkStore(returnType), finalReg, "$R"));
		}

		newFunction.addIRNode(new IRNode("RET"));
		nodesPrinter.addIRNode(new IRNode("RET"));
	}

	@Override
	public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
		//System.out.println("Enter If_stmt");
		//System.out.println(ctx.getText());
		//System.out.println("Child 1 " + ctx.getChild(1).getText());

		String compOp = "";
		String LeftOp = "";
		String RightOp = "";

		int childrenCount = ctx.getChild(2).getChildCount();

		//System.out.println("Children Count " + Integer.toString(childrenCount));		

		// For IF(TRUE) or IF(FALSE) or ELSIF(TRUE) or ELSIF(FALSE)
		if(childrenCount == 1) {
			//System.out.println("Binary");
			compOp = ctx.getChild(2).getText();
			//System.out.println(compOp);

			labelStack1.push(getLabel());
			labelStack2.push(getLabel());
		}

		else if(childrenCount == 3) {
			LeftOp = ctx.getChild(2).getChild(0).getText();
			compOp = ctx.getChild(2).getChild(1).getText();
			RightOp = ctx.getChild(2).getChild(2).getText();

			//		int rightChild = ctx.getChild(2).getChild(2).getChildCount();

			//System.out.println("Right Op: " + RightOp);
			//System.out.println("Blah Blah Blah Blah" + Integer.toString(rightChild));

			//System.out.println("Comp Operator " + compOp);

			if(RightOp.contains("(")) {
				String postExpr = infixtoPostfix(RightOp, funcNames);
				RightOp = parsePostfixExpr(postExpr, symbolsTree.getParentScope().checkDataType(LeftOp));
			}

			if(isInteger(RightOp) || isFloat(RightOp)) {
				String newRegister = getRegister();

				// Checking to see if the Right Hand Side of the If Statement Expression is an integer or Float
				if(isInteger(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
				}

				else if(isFloat(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("FLOAT"), RightOp, newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
				}

				RightOp = newRegister;
			}

			//System.out.println("Comp Operator " + compOp);
			labelStack1.push(getLabel());
			labelStack2.push(getLabel());

			checkCompOp(compOp, LeftOp, RightOp);
		}

		else {
			System.out.println("ERROR: IF condition ");
			return; // Error Condition
		}

		//System.out.println("Child 3 " + ctx.getChild(3).getText());
		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);

		if (ctx.getChild(4) == null || ctx.getChild(4).getText() == "")
			return;

		String[] global_vars = ctx.getChild(4).getText().split(";");

		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, table);
		}
	}

	@Override
	public void enterElse_part(MicroParser.Else_partContext ctx) {
		//System.out.println("Enter Else_part");

		String compOp = "";
		String LeftOp = "";
		String RightOp = "";

		if (ctx.getChild(0) == null)
			return;

		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);

		if (ctx.getChild(1) == null || ctx.getChild(1).getText() == "")
			return;

		int childrenCount = ctx.getChild(2).getChildCount(); 

		//System.out.println(ctx.getText());
		//System.out.println("Child 1 " + ctx.getChild(1).getText());
		//System.out.println("Child 2 " + ctx.getChild(2).getText());
		//System.out.println("Child 3 " + ctx.getChild(3).getText());

		//System.out.println("Children Count " + Integer.toString(childrenCount));


		String[] global_vars = ctx.getChild(1).getText().split(";");

		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, table);
		}

		// Checking if condition itself is a binary Value
		if(childrenCount == 1) {
			//System.out.println("Binary");
			compOp = ctx.getChild(2).getText();
			//System.out.println(compOp);
			String labelSt1 = labelStack1.pop();
			nodesPrinter.addIRNode(new IRNode("JUMP",labelStack2.peek()));
			nodesPrinter.addIRNode(new IRNode("LABEL",labelSt1));

			functionsMap.get(currentFuncName).addIRNode(new IRNode("JUMP",labelStack2.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("LABEL",labelSt1));

			String Label = getLabel();
			labelStack1.push(Label);

			String reg1 = getRegister();
			String reg2 = getRegister();
			nodesPrinter.addIRNode(new IRNode(checkStore("INT"),"1",reg1));
			nodesPrinter.addIRNode(new IRNode(checkStore("INT"),"1",reg2));
			functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("INT"),"1",reg1));
			functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("INT"),"1",reg2));

			if(compOp.equals("TRUE")) {
				checkCompOp("=", reg1, reg2);
			}
			else {
				checkCompOp("!=",reg1, reg2);
			}
		}
		else if(childrenCount == 3) {
			LeftOp = ctx.getChild(2).getChild(0).getText();
			compOp = ctx.getChild(2).getChild(1).getText();			
			RightOp = ctx.getChild(2).getChild(2).getText();

			int rightChild = ctx.getChild(2).getChild(2).getChildCount();
			//System.out.println("Blah Blah Blah Blah" + Integer.toString(rightChild));
			//System.out.println("Comp Operator " + compOp);

			if(rightChild == 3) {
				System.out.println(ctx.getChild(2).getChild(2).getChild(1).getText());
			}

			String labelSt1 = labelStack1.pop();

			nodesPrinter.addIRNode(new IRNode("JUMP",labelStack2.peek()));
			nodesPrinter.addIRNode(new IRNode("LABEL", labelSt1));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("JUMP",labelStack2.peek()));
			functionsMap.get(currentFuncName).addIRNode(new IRNode("LABEL",labelSt1));

			labelStack1.push(getLabel());


			if(isInteger(RightOp) || isFloat(RightOp)) {

				String newRegister = getRegister();

				// Checking to see if the Right Hand Side of the If Statement Expression is an integer or Float
				if(isInteger(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
				}

				else if(isFloat(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("FLOAT"), RightOp, newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("FLOAT"),RightOp,newRegister));
				}

				RightOp = newRegister;
			}

			checkCompOp(compOp, LeftOp, RightOp);
		}

		else {
			System.out.println("ERROR: ELSIF condition ");
			return; // Error Condition
		}
	}

	@Override
	public void exitElse_part(MicroParser.Else_partContext ctx) {

		//System.out.println("Exit Else_part");

		if (ctx.getChild(0) == null)
			return;
	}

	@Override public void exitIf_stmt(MicroParser.If_stmtContext ctx) {

		nodesPrinter.addIRNode(new IRNode("JUMP", labelStack2.peek()));
		functionsMap.get(currentFuncName).addIRNode(new IRNode("JUMP", labelStack2.peek()));

		String labelSt1 = labelStack1.pop();
		String labelSt2 = labelStack2.pop();

		nodesPrinter.addIRNode(new IRNode("LABEL",labelSt1));
		nodesPrinter.addIRNode(new IRNode("LABEL",labelSt2));
		functionsMap.get(currentFuncName).addIRNode(new IRNode("LABEL",labelSt1));
		functionsMap.get(currentFuncName).addIRNode(new IRNode("LABEL",labelSt2));
	}

	// Might Have to Modify it
	@Override
	public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {

		//System.out.println("Enter Do_while");

		if (ctx.getChild(0) == null)
			return;

		//int childrenCount = ctx.getChild(5).getChildCount();

		//System.out.println(ctx.getChild(5).getChild(0).getText());
		//System.out.println(ctx.getChild(5).getChild(1).getText());
		//System.out.println(ctx.getChild(5).getChild(2).getText());

		String newLabel = getLabel();
		labelStack2.push(newLabel);
		nodesPrinter.addIRNode(new IRNode("LABEL",newLabel));
		functionsMap.get(currentFuncName).addIRNode(new IRNode("LABEL",newLabel));
		labelStack1.push(getLabel());

		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);

		//System.out.println("After symbtree getCurrentScope");

		//System.out.println("Child 1 " + ctx.getChild(1).getText());

		//System.out.println(ctx.getChild(1).getChildCount());

		if (ctx.getChild(1) != null || ctx.getChild(1).getText() != "") {

			//System.out.println("After global_vars split(;)");

			String[] global_vars = ctx.getChild(1).getText().split(";");

			//System.out.println("After global_vars split(;)");

			for (int index = 0; index < global_vars.length; index++) {
				String currentValue = global_vars[index];
				pushSymbol(currentValue, table);
			}
		}

		//System.out.println("After for Loop");

		// String LeftOp = "";
		// String CompOp = "";
		// String RightOp = "";
	}

	@Override public void exitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {

		int childrenCount = ctx.getChild(5).getChildCount();

		if (childrenCount == 3) {

			String LeftOp = ctx.getChild(5).getChild(0).getText();
			String CompOp = ctx.getChild(5).getChild(1).getText();
			String RightOp = ctx.getChild(5).getChild(2).getText();

			if(isInteger(RightOp) || isFloat(RightOp)) {

				String newRegister = getRegister();


				// Checking to see if the Right Hand Side of the If Statement Expression is an integer or Float
				if(isInteger(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
				}

				else if(isFloat(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("FLOAT"), RightOp, newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore("FLOAT"), RightOp, newRegister));
				}

				RightOp = newRegister;
			}

			checkCompOp(CompOp, LeftOp, RightOp);

			//System.out.println("Inside the Child Count 3");
		}

		//System.out.println(labelStack1.size());
		//System.out.println(labelStack2.size());
		//System.out.println("Exit Do While " + ctx.getChild(5).getText());

		String labelSt1 = labelStack1.pop();
		String labelSt2 = labelStack2.pop();

		nodesPrinter.addIRNode(new IRNode("JUMP",labelSt2));
		nodesPrinter.addIRNode(new IRNode("LABEL",labelSt1));
		functionsMap.get(currentFuncName).addIRNode(new IRNode("JUMP",labelSt2));
		functionsMap.get(currentFuncName).addIRNode(new IRNode("LABEL",labelSt1));
		//System.out.println("Exit Do While");
	}

	@Override
	public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {

		//System.out.println("Enter WRITE");

		if (ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")) {
			return;
		}

		String[] ids = ctx.getChild(2).getText().split(",");

		Function newFunction = functionsMap.get(currentFuncName);

		for (String id : ids) {

			// First Check if it is available in the Local Scope
			// Else check in the Global Scope.
			String type = symbolsTableMap.get(currentFuncName).checkDataType(id);

			if(type == null) {
				type = symbolsTree.getParentScope().checkDataType(id);
			}

//			System.out.println(id);

			// The Input Symbol is not present in the SymbolTable
			if (type == null) {
				System.out.println(id);
				System.out.println("ERROR ID");
				// System.exit(0);
				return;
			}


			// Fetch the Registers
			if(inFunction(id, currentFuncName) != null) {
				id = inFunction(id, currentFuncName);
			}

			if (type.equals("INT")) {
				IRNode irNodeI = new IRNode("WRITEI", id);
				nodesPrinter.addIRNode(irNodeI);
				newFunction.addIRNode(irNodeI);
			} else if (type.equals("FLOAT")) {
				IRNode irNodeF = new IRNode("WRITEF", id);
				nodesPrinter.addIRNode(irNodeF);
				newFunction.addIRNode(irNodeF);
			}

			else if(type.equals("STRING")) {
				IRNode irNodeS = new IRNode("WRITES", id);
				nodesPrinter.addIRNode(irNodeS);
				newFunction.addIRNode(irNodeS);
			}
		}
	}

	@Override public void enterRead_stmt(MicroParser.Read_stmtContext ctx){

		//System.out.println("Enter READ");

		if (ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")) {
			return;
		}

		String[] ids = ctx.getChild(2).getText().split(",");

		for (String id : ids) {

			//String type = symbolsTree.checkDataType(id);

			// First Check if it is available in the Local Scope
			// Else check in the Global Scope.
			String type = symbolsTableMap.get(currentFuncName).checkDataType(id);

			if(type == null) {
				type = symbolsTree.getParentScope().checkDataType(id);
			}

			// The Input Symbol is not present in the SymbolTable
			if (type == null) {
				System.out.println(id);
				System.out.println("ERROR ID");
				//System.exit(1);
				return;
			}

			// Fetch the Registers
			if(inFunction(id, currentFuncName) != null) {
				id = inFunction(id, currentFuncName);
			}

			if (type.equals("INT")) {
				IRNode irNodeI = new IRNode("READI", id);
				nodesPrinter.addIRNode(irNodeI);
				functionsMap.get(currentFuncName).addIRNode(irNodeI);
			} else if (type.equals("FLOAT")) {
				IRNode irNodeF = new IRNode("READF", id);
				nodesPrinter.addIRNode(irNodeF);
				functionsMap.get(currentFuncName).addIRNode(irNodeF);
			}
		}
	}

	@Override
	public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {

		//System.out.println("Enter Assign_expr");

		String result = ctx.getChild(0).getText();

		// Checking if the Variable is already present in the localScope of the Function
		String type = null;
		String expr = ctx.getText().split(":=")[1].trim();
		String store = ctx.getText().split(":=")[0].trim();

		SymbolsTable symbolsTable = symbolsTableMap.get(currentFuncName);
		type = symbolsTable.checkDataType(store);

		//System.out.println("After SymbolsTable");

		// Check for its existence in the Global Scope if not in the Local Scope
		if(type == null) {
			type =  symbolsTree.getParentScope().checkDataType(store);
		}

		//System.out.println("Store "+ store);
		//System.out.println("Expr " + expr);

		//System.out.println("After init");

		int flag_int = 0;

		try {

			Integer.parseInt(expr); // Checking if the RHS is an integer e.g: a = 2;

			flag_int = 1;

			String regStore = getRegister();
			//System.out.println("getRegister Int Parse Assign");

			// if the Variable is a part of the function this returns the Local Register
			// Or returns the variable name if it is a global Variable
			//store = inFunction(store, currentFuncName);

			/*System.out.println("Store: " + store);
			System.out.println("Type: " + type);
			System.out.println("Expr: " + expr);
			System.out.println("RegStore: " + regStore);
			System.out.println("Current Function Name: " + currentFuncName);
			System.out.println(functionsMap.get(currentFuncName));
			System.out.println("Done Getting Details"); */

			// Checking if the 'store' variable is in the Local Scope of the Function
			if (functionsMap.get(currentFuncName).getRegisterMap().get(store) != null) {
					//System.out.println("Checkin in Local Scope");
					store = functionsMap.get(currentFuncName).getRegisterMap().get(store);
			}

			// Else check if the Variable is in the Global Scope if not it is an Error.
			else if (symbolsTree.getParentScope().checkDataType(store) == null) {
					System.out.println("Variable: " + store);
					System.out.println("ERROR: Symbol not present");
					return;
				}

			nodesPrinter.addIRNode(new IRNode(checkStore(type), expr, regStore));
			nodesPrinter.addIRNode(new IRNode(checkStore(type), regStore, store));
			//System.out.println("Done 1");
			functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), expr, regStore));
			functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), regStore, store));
			//System.out.println("Done 2");
			//System.out.println("Done Integer");

		} catch (NumberFormatException e) {

		}

		try {
			if (flag_int == 0) {

				Float.parseFloat(expr); // Checking if the RHS is a float e.g: a = 1.0;
				String regStore = getRegister();
				//System.out.println("getRegister Float Parse Assign");

				// Checking if the 'store' variable is in the Local Scope of the Function
				if(functionsMap.get(currentFuncName).getRegisterMap().get(store) != null) {
					store = functionsMap.get(currentFuncName).getRegisterMap().get(store);
				}

				// Else check if the Variable is in the Global Scope if not it is an Error.
				else if(symbolsTree.getParentScope().checkDataType(store) == null) {
					System.out.println("Variable: " + store);
					System.out.println("ERROR: Symbol not present");
					return;
				}

				nodesPrinter.addIRNode(new IRNode(checkStore(type), expr, regStore));
				nodesPrinter.addIRNode(new IRNode(checkStore(type), regStore, store));
				functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), expr, regStore));
				functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), regStore, store));
			}
		} catch (NumberFormatException e) {

			String postFixExpr = infixtoPostfix(expr,funcNames);
			//System.out.println(postFixExpr);
			//System.out.println("After infix_to_Postfix");
			String FinalRegister = parsePostfixExpr(postFixExpr, type);
			//System.out.println("After ParsePostFix");

			// Checking Local Scope
			if(inFunction(result,currentFuncName) != null) {
				result = inFunction(result, currentFuncName);
				nodesPrinter.addIRNode(new IRNode(checkStore(type), FinalRegister, result));
				functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), FinalRegister, result)); // Adding IRNode to the respective Function
				return;
			}

			// Checking in Global Scope
			if(symbolsTree.getParentScope().checkDataType(result) != null) {
				nodesPrinter.addIRNode(new IRNode(checkStore(type), FinalRegister, result));
				functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), FinalRegister, result));
				return;
			}
//			nodesPrinter.addIRNode(new IRNode(checkStore(type), FinalRegister, result));
		}
	}

	public String parsePostfixExpr(String postFix_Expr, String type) {

		//System.out.println("DataType: " + type);

		Stack<String> RegisterStack = new Stack<String>();

		String[] elements = postFix_Expr.split(" ");

		for (int index = 0; index < elements.length; index++) {

			//System.out.println(elements[index]);

			Function function = functionsMap.get(currentFuncName); // Used to fetch the Corresponding Local Registers for each parameters passed to called functions

			if (elements[index].equals("+") || elements[index].equals("-") || elements[index].equals("*") || elements[index].equals("/") || elements[index].equals(",")) {

				int flag_Int = 0;

				// Popping the Two Nodes
				String Node1 = RegisterStack.pop();
				String Node2 = RegisterStack.pop();

				// Checking if in Parameter or in Local Scope. Else in Global Scope
				if(!Node1.startsWith("$")) {
					if(inFunction(Node1, currentFuncName) != null) {
						Node1 = inFunction(Node1, currentFuncName);
					}

					else if(symbolsTree.getParentScope().checkDataType(Node1) == null) {
						System.out.println("ID: " + Node1);
						System.out.println("ERROR, " + Node1 + " Not Available");
					}
				}

				// Checking if in Parameter or in Local Scope. Else in Global Scope
				if(!Node2.startsWith("$")) {
					if(inFunction(Node2, currentFuncName) != null) {
						Node2 = inFunction(Node2, currentFuncName);
					}

					else if(symbolsTree.getParentScope().checkDataType(Node2) == null) {
						System.out.println("ID: " + Node2);
						System.out.println("ERROR, " + Node2 + " Not Available");
					}
				}

//				Node1 = inFunction(Node1, currentFuncName);
//				Node2 = inFunction(Node2, currentFuncName);

				if (elements[index].equals("+")) {
					String newRegister = getRegister();
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("+", type), Node2, Node1, newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(OpCodeCheck("+", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

				} else if (elements[index].equals("-")) {
					String newRegister = getRegister();
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("-", type), Node2, Node1, newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(OpCodeCheck("-", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

				} else if (elements[index].equals("*")) {
					String newRegister = getRegister();
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("*", type), Node2, Node1, newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(OpCodeCheck("*", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

				} else if (elements[index].equals("/")) {
					String newRegister = getRegister();
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("/", type), Node2, Node1, newRegister));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(OpCodeCheck("/", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);
				}

				else if(elements[index].equals(",")) {
					//System.out.println("Inside \",\"");
					RegisterStack.push(new String(Node2 + "," + Node1)); // Parameters of the Function need to be pushed in the same order of their appearance
				}

			} else {

				int flag_integer = 0;
				try {
					Integer.parseInt(elements[index]);
					flag_integer = 1;
					String regStore = getRegister();
					//System.out.println(regStore);
					//System.out.println("getRegister Int Parse Assign");
					nodesPrinter.addIRNode(new IRNode(checkStore(type), elements[index], regStore));
					functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), elements[index], regStore));
					RegisterStack.push(regStore);
				} catch (NumberFormatException e) {

				}

				try {
					if (flag_integer == 0) {
						Float.parseFloat(elements[index]);

						String regStore = getRegister();
						//System.out.println(regStore);
						//System.out.println("getRegister Float Parse Assign");
						nodesPrinter.addIRNode(new IRNode(checkStore(type), elements[index], regStore));
						functionsMap.get(currentFuncName).addIRNode(new IRNode(checkStore(type), elements[index], regStore));
						RegisterStack.push(regStore);
					}
				} catch (NumberFormatException e) {
					//System.out.println("Variables");

					// Checking of the Operand is a Function Label
						if(funcNames.contains(elements[index])) {
							String Parameters = RegisterStack.pop(); // For example, the Value of Parameters = "a,b,c"
							//System.out.println("Parameters: " + Parameters);
							String[] LocalVariables = Parameters.split(",");
							nodesPrinter.addIRNode(new IRNode("PUSH"));
							functionsMap.get(currentFuncName).addIRNode(new IRNode("PUSH"));
							for(String variable: LocalVariables) {
								if (variable.startsWith("$")) {
									nodesPrinter.addIRNode(new IRNode("PUSH", variable));
									functionsMap.get(currentFuncName).addIRNode(new IRNode("PUSH", variable));
								}
								else {
									nodesPrinter.addIRNode(new IRNode("PUSH", function.getRegisterMap().get(variable)));
									functionsMap.get(currentFuncName).addIRNode(new IRNode("PUSH", function.getRegisterMap().get(variable)));
								}
							}

							//System.out.println("after adding PUSH");

							nodesPrinter.addIRNode(new IRNode("JSR", elements[index]));
							functionsMap.get(currentFuncName).addIRNode(new IRNode("JSR", elements[index]));
							for(int index1 = 0; index1 < LocalVariables.length; index1++) {
								nodesPrinter.addIRNode(new IRNode("POP"));
								functionsMap.get(currentFuncName).addIRNode(new IRNode("POP"));
							}
							String returnRegister = getRegister();
							nodesPrinter.addIRNode(new IRNode("POP", returnRegister));
							functionsMap.get(currentFuncName).addIRNode(new IRNode("POP", returnRegister));
							//System.out.println("After all POP");

							RegisterStack.push(returnRegister);
						}
						else {
						RegisterStack.push(elements[index]); // Pushing the Variable Names to the Stack
					}
				}
			}
		}

		// Storing the Final Value of the Expression
		//String FinalRegister = RegisterStack.pop();
		//nodesPrinter.addIRNode(new IRNode(checkStore(type), FinalRegister, outputVariable));

		return RegisterStack.pop();
	}

	@Override
	public void enterVar_decl(MicroParser.Var_declContext ctx) {

		//System.out.println("Enter Variable Declaration");

		String[] variables = ctx.getChild(1).getText().split(",");

		for (String variable : variables) {

			//System.out.println(variable);
			// Here I need to add the variable to the SymbolsTree
			//tinyNodeArrayList.add(new TinyNode("var", variable));
			variablesList.add(variable);
		}
	}

	// Used to add IRNodes to IRNodeList and print the IR Instructions
	class NodesPrinter {

		private ArrayList<IRNode> IRNodeList;
		private ArrayList<Symbols> symbolsArrayList;

		public NodesPrinter(){
			IRNodeList = new ArrayList<>();
			symbolsArrayList = new ArrayList<>();
		}

		public void addSymbols(ArrayList<Symbols> arrayList) {
			symbolsArrayList.addAll(arrayList);
		}

		public void addIRNode(IRNode irNode) {
			IRNodeList.add(irNode);
		}

		public void printIRNodes() {
			System.out.println(";IR code");
			for(int index = 0; index < IRNodeList.size(); index++) {
				IRNode irNode = IRNodeList.get(index);
				System.out.println(irNode.toString());
			}
		}

		public ArrayList<IRNode> getIRNodeList() {
			return IRNodeList;
		}
	}
	}