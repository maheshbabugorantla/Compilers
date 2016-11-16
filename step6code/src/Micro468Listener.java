import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro468Listener extends MicroBaseListener {


	public SymbolsTree symbolsTree;

	private int blockNum;
	private int regNum;
	private int labelNo;

	private ArrayList<String> variablesList;

	NodesPrinter nodesPrinter; // Used to print all the IRNodes

	List<TinyNode> tinyNodeArrayList = new ArrayList<TinyNode>();

	Stack<String> labelStack1; //= new Stack<String>();
	Stack<String> labelStack2; //= new Stack<String>();

	int entered_Expr = 0; // Flag to check if the program has entered the expr
	int exit_init = 0; // Flag to check if we exited initializations

	int entered_Cond = 0; // Flag to check if we entered conditional

	public Micro468Listener() {
		this.symbolsTree = new SymbolsTree();
		this.blockNum = 1;
		this.nodesPrinter = new NodesPrinter();
		this.regNum = 0;
		this.labelNo = 1;
		this.labelStack1 = new Stack<String>();
		this.labelStack2 = new Stack<String>();
		this.variablesList = new ArrayList<String>();
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

	public void pushSymbol(String currentValue, SymbolsTable table) {

		if (currentValue.startsWith("INT", 0)) {
			String[] intVal = currentValue.substring(3).split(",");
			for (String str : intVal) {
				Symbols symbol = new Symbols(str, "INT");
				table.addSymbol(symbol);
			}
		} else if (currentValue.startsWith("FLOAT", 0)) {
			String[] float_val = currentValue.substring(5).split(",");
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

	public void functionParameters(String currentValue, SymbolsTable table) {
		String[] intVal = currentValue.split(",");
		for (int index = 0; index < intVal.length; index++) {
			if (currentValue.startsWith("INT", 0)) {
				String str = intVal[index];
				str = str.substring(3);
				Symbols symbol = new Symbols(str, "INT");
				table.addSymbol(symbol);
			} else if (currentValue.startsWith("FLOAT", 0)) {
				String str = intVal[index];
				str = str.substring(5);
				Symbols symbol = new Symbols(str, "FLOAT");
				table.addSymbol(symbol);
			}
		}
	}

	public String getBlockName() {
		return "BLOCK " + blockNum++;
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

		switch(compOp) {

		case "<":
			nodesPrinter.addIRNode(new IRNode("GE", operand1, operand2, labelStack1.peek()));
			break;

		case ">":
			nodesPrinter.addIRNode(new IRNode("LE", operand1, operand2, labelStack1.peek()));
			break;

		case "<=":
			nodesPrinter.addIRNode(new IRNode("GT", operand1, operand2, labelStack1.peek()));
			break;

		case ">=":
			nodesPrinter.addIRNode(new IRNode("LT", operand1, operand2, labelStack1.peek()));
			break;

		case "=":
			nodesPrinter.addIRNode(new IRNode("NE", operand1, operand2, labelStack1.peek()));
			break;

		case "!=":	
			nodesPrinter.addIRNode(new IRNode("EQ", operand1, operand2, labelStack1.peek()));
			break;
		}		
	}

	////////////////////*/
	@Override
	public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
		//System.out.println("Enter Pgm_body");
		if (ctx.getChild(0) == null || ctx.getChild(0).getText() == "") return;
		String[] global_vars = ctx.getChild(0).getText().split(";");
		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, symbolsTree.getParentScope());
		}
	}

	// Here we print the whole symbolsTree when we exit the PROGRAM
	@Override
	public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
		nodesPrinter.printIRNodes();
		TinyNode.convertIRtoTiny(nodesPrinter.getIRNodeList(),symbolsTree,variablesList,tinyNodeArrayList);
		TinyNode.printTinyList(tinyNodeArrayList);
	}

	@Override
	public void enterFunc_decl(MicroParser.Func_declContext ctx) {
		//System.out.println("Enter Func_decl");
		SymbolsTable table = new SymbolsTable(ctx.getChild(2).getText());

		symbolsTree.getCurrentScope().addChild(table);

		if (ctx.getChild(4) != null) {
			functionParameters(ctx.getChild(4).getText(), table);
		}

		if (ctx.getChild(7) != null && ctx.getChild(7).getChild(0) != null) {
			String str = ctx.getChild(7).getChild(0).getText();
			if (str.length() == 0) return;
			pushSymbol(str.substring(0, str.length() - 1), table);
		}
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
				String postExpr = infix_to_Postfix(RightOp);
				RightOp = parsePostfixExpr(postExpr, symbolsTree.getParentScope().checkDataType(LeftOp));
			}

			if(isInteger(RightOp) || isFloat(RightOp)) {
				entered_Cond = 1;
				String newRegister = getRegister();

				// Checking to see if the Right Hand Side of the If Statement Expression is an integer or Float
				if(isInteger(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
				}

				else if(isFloat(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("FLOAT"), RightOp, newRegister));
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

			nodesPrinter.addIRNode(new IRNode("JUMP",labelStack2.peek()));
			nodesPrinter.addIRNode(new IRNode("LABEL",labelStack1.pop()));			

			String Label = getLabel();
			labelStack1.push(Label);

			String reg1 = getRegister();
			String reg2 = getRegister();
			nodesPrinter.addIRNode(new IRNode(checkStore("INT"),"1",reg1));
			nodesPrinter.addIRNode(new IRNode(checkStore("INT"),"1",reg2));

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

			/*			if(rightChild == 1) {

			} */

			if(rightChild == 3) {
				System.out.println(ctx.getChild(2).getChild(2).getChild(1).getText());
			}

			nodesPrinter.addIRNode(new IRNode("JUMP",labelStack2.peek()));
			nodesPrinter.addIRNode(new IRNode("LABEL",labelStack1.pop()));

			labelStack1.push(getLabel());


			if(isInteger(RightOp) || isFloat(RightOp)) {

				String newRegister = getRegister();

				// Checking to see if the Right Hand Side of the If Statement Expression is an integer or Float
				if(isInteger(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("INT"),RightOp,newRegister));
				}

				else if(isFloat(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("FLOAT"), RightOp, newRegister));
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
		nodesPrinter.addIRNode(new IRNode("LABEL",labelStack1.pop()));
		nodesPrinter.addIRNode(new IRNode("LABEL",labelStack2.pop()));
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


		/*		if(childrenCount == 1) {

		} */

		/*		String LeftOp = "";
		String CompOp = "";
		String RightOp = ""; */

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
				}

				else if(isFloat(RightOp)) {
					nodesPrinter.addIRNode(new IRNode(checkStore("FLOAT"), RightOp, newRegister));
				}

				RightOp = newRegister;
			}

			checkCompOp(CompOp, LeftOp, RightOp);

			//System.out.println("Inside the Child Count 3");
		}		

		//System.out.println(labelStack1.size());
		//System.out.println(labelStack2.size());
		//System.out.println("Exit Do While " + ctx.getChild(5).getText());

		nodesPrinter.addIRNode(new IRNode("JUMP",labelStack2.pop()));
		nodesPrinter.addIRNode(new IRNode("LABEL",labelStack1.pop()));
	}

	@Override
	public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {

		//System.out.println("Enter Write_stmt");

		if (ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")) {
			return;
		}

		String[] ids = ctx.getChild(2).getText().split(",");

		for (String id : ids) {
			String type = symbolsTree.getParentScope().checkDataType(id);

			// The Input Symbol is not present in the SymbolTable
			if (type == null) {
				System.out.println("ERROR ID");
				return;
			}

			if (type.equals("INT")) {
				IRNode irNodeI = new IRNode("WRITEI", id);
				nodesPrinter.addIRNode(irNodeI);
			} else if (type.equals("FLOAT")) {
				IRNode irNodeF = new IRNode("WRITEF", id);
				nodesPrinter.addIRNode(irNodeF);
			}
		}
	}

	@Override public void enterRead_stmt(MicroParser.Read_stmtContext ctx){

		//System.out.println("Enter Write_stmt");

		if (ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")) {
			return;
		}

		String[] ids = ctx.getChild(2).getText().split(",");

		for (String id : ids) {
			String type = symbolsTree.getParentScope().checkDataType(id);

			// The Input Symbol is not present in the SymbolTable
			if (type == null) {
				System.out.println("ERROR ID");
				return;
			}

			if (type.equals("INT")) {
				IRNode irNodeI = new IRNode("READI", id);
				nodesPrinter.addIRNode(irNodeI);
			} else if (type.equals("FLOAT")) {
				IRNode irNodeF = new IRNode("READF", id);
				nodesPrinter.addIRNode(irNodeF);
			}
		}
	}

	@Override
	public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {

		//		System.out.println("Enter Assign_expr");

		String result = ctx.getChild(0).getText();

		String type = symbolsTree.getParentScope().checkDataType(result);

		String expr = ctx.getText().split(":=")[1].trim();
		String store = ctx.getText().split(":=")[0].trim();

		int flag_int = 0;

		try {
			Integer.parseInt(expr);

			flag_int = 1;

			String regStore = getRegister();

			nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), expr, regStore));
			nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), regStore, store));

		} catch (NumberFormatException e) {

		}

		try {
			if (flag_int == 0) {

				Float.parseFloat(expr);
				String regStore = getRegister();

				nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), expr, regStore));
				nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), regStore, store));
			}
		} catch (NumberFormatException e) {

			String postFixExpr = infix_to_Postfix(expr);

			String FinalRegister = parsePostfixExpr(postFixExpr, type);
			nodesPrinter.addIRNode(new IRNode(checkStore(type), FinalRegister, result));
		}
	}

	public String parsePostfixExpr(String postFix_Expr, String type) {

		Stack<String> RegisterStack = new Stack<String>();

		String[] elements = postFix_Expr.split(" ");

		for (int index = 0; index < elements.length; index++) {

			if (elements[index].equals("+") || elements[index].equals("-") || elements[index].equals("*") || elements[index].equals("/")) {

				// Popping the Two Nodes
				String Node1 = RegisterStack.pop();
				String Node2 = RegisterStack.pop();


				String newRegister = getRegister();

				if (elements[index].equals("+")) {

					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("+", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

				} else if (elements[index].equals("-")) {
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("-", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

				} else if (elements[index].equals("*")) {
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("*", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

				} else if (elements[index].equals("/")) {
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("/", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);
				}

			} else {

				int flag_integer = 0;
				try {
					Integer.parseInt(elements[index]);
					flag_integer = 1;
					String regStore = getRegister();

					nodesPrinter.addIRNode(new IRNode(checkStore(type), elements[index], regStore));
					RegisterStack.push(regStore);

				} catch (NumberFormatException e) {

				}

				try {
					if (flag_integer == 0) {
						Float.parseFloat(elements[index]);

						String regStore = getRegister();

						nodesPrinter.addIRNode(new IRNode(checkStore(type), elements[index], regStore));
						RegisterStack.push(regStore);

					}
				} catch (NumberFormatException e) {
					RegisterStack.push(elements[index]); // Pushing the Variable Names to the Stack
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
		String[] variables = ctx.getChild(1).getText().split(",");

		for (String variable : variables) {
			tinyNodeArrayList.add(new TinyNode("var", variable));
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