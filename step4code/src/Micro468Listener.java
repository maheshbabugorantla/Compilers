import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro468Listener extends MicroBaseListener {


	public SymbolsTree symbolsTree;

	private int blockNum;
	private int regNum;
	private int tinyReg;

	NodesPrinter nodesPrinter; // Used to print all the IRNodes

	List<TinyNode> tinyNodeArrayList = new ArrayList<TinyNode>();

	HashMap<String, String> registerMap;

	int entered_Expr = 0;
	int exit_init = 0;

	public Micro468Listener() {
		this.symbolsTree = new SymbolsTree();
		this.blockNum = 1;
		this.nodesPrinter = new NodesPrinter();
		this.regNum = 0;
		this.tinyReg = 0;
		this.registerMap = new HashMap<String, String>();
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

	public String getTinyRegister() {
		String registerNo = new String("r" + tinyReg);
		tinyReg += 1;
		return registerNo;
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

	public String tinyOpCodeCheck(String Operation) {

		switch (Operation) {

			case "ADDI":
				return "addi";

			case "ADDF":
				return "addr";

			case "SUBI":
				return "subi";

			case "SUBF":
				return "subr";

			case "MULTI":
				return "muli";

			case "MULTF":
				return "mulr";

			case "DIVI":
				return "divi";

			case "DIVF":
				return "divr";

			default:
				return "ERROR";
		}
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
		convertIRtoTiny(nodesPrinter.getIRNodeList());
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
		if (ctx.getChild(0) == null)
			return;

		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);

		if (ctx.getChild(1) == null || ctx.getChild(1).getText() == "")
			return;

		String[] global_vars = ctx.getChild(1).getText().split(";");

		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, table);
		}
	}

	@Override
	public void exitElse_part(MicroParser.Else_partContext ctx) {

		//System.out.println("Exit Else_part");

		if (ctx.getChild(0) == null)
			return;
	}

	// Might Have to Modify it
	@Override
	public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {

		//System.out.println("Enter Do_while");

		if (ctx.getChild(0) == null)
			return;

		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);

		if (ctx.getChild(1) == null || ctx.getChild(1).getText() == "")
			return;

		String[] global_vars = ctx.getChild(1).getText().split(";");

		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, table);
		}
	}

	@Override
	public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {

		//System.out.println("Enter Write_stmt");

		if (ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")) {
			return;
		}

		exit_init = 1;

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
			String tinyReg = getTinyRegister();

			nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), expr, regStore));
			nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), regStore, store));

			tinyNodeArrayList.add(new TinyNode("move", expr, tinyReg));
			tinyNodeArrayList.add(new TinyNode("move", tinyReg, store));

		} catch (NumberFormatException e) {

		}

		try {
			if (flag_int == 0) {

				Float.parseFloat(expr);
				String regStore = getRegister();

				nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), expr, regStore));
				nodesPrinter.addIRNode(new IRNode(checkStore(symbolsTree.getParentScope().checkDataType(store)), regStore, store));

				if(exit_init == 0)
				{
					String tinyReg = getTinyRegister();
					tinyNodeArrayList.add(new TinyNode("move", expr, tinyReg));
					tinyNodeArrayList.add(new TinyNode("move", tinyReg, store));
				}
			}
		} catch (NumberFormatException e) {
			exit_init = 1;
			String postFixExpr = infix_to_Postfix(expr);

			parsePostfixExpr(postFixExpr, type, result);
		}
	}

	public void parsePostfixExpr(String postFix_Expr, String type, String outputVariable) {

		Stack<String> RegisterStack = new Stack<String>();
		//Stack<String> tinyRegisterStack = new Stack<String>();

		String[] elements = postFix_Expr.split(" ");

		for (int index = 0; index < elements.length; index++) {

			if (elements[index].equals("+") || elements[index].equals("-") || elements[index].equals("*") || elements[index].equals("/")) {

				// Popping the Two Nodes
				String Node1 = RegisterStack.pop();
				String Node2 = RegisterStack.pop();

				//String tinyNode1 = tinyRegisterStack.pop();
				//String tinyNode2 = tinyRegisterStack.pop();

				String newRegister = getRegister();
				//String tinyRegister = getTinyRegister();

				if (elements[index].equals("+")) {

					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("+", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

					/*if (symbolsTree.getParentScope().checkDataType(tinyNode2) != null) {
						tinyNodeArrayList.add(new TinyNode("move", tinyNode2, tinyRegister));
					}
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck("+", type), tinyNode1, tinyRegister));
					tinyRegisterStack.push(tinyRegister); */
				} else if (elements[index].equals("-")) {
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("-", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

					/*if (symbolsTree.getParentScope().checkDataType(tinyNode2) != null) {
						tinyNodeArrayList.add(new TinyNode("move", tinyNode2, tinyRegister));
					}
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck("-", type), tinyNode1, tinyRegister));
					tinyRegisterStack.push(tinyRegister); */
				} else if (elements[index].equals("*")) {
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("*", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

				/*	if (symbolsTree.getParentScope().checkDataType(tinyNode2) != null) {
						tinyNodeArrayList.add(new TinyNode("move", tinyNode2, tinyRegister));
					}
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck("*", type), tinyNode1, tinyRegister));
					tinyRegisterStack.push(tinyRegister); */
				} else if (elements[index].equals("/")) {
					nodesPrinter.addIRNode(new IRNode(OpCodeCheck("/", type), Node2, Node1, newRegister));
					RegisterStack.push(newRegister);

					/*if (symbolsTree.getParentScope().checkDataType(tinyNode2) != null) {
						tinyNodeArrayList.add(new TinyNode("move", tinyNode2, tinyRegister));
					}
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck("/", type), tinyNode1, tinyRegister));
					tinyRegisterStack.push(tinyRegister); */
				}

			} else {

				int flag_integer = 0;
				try {
					Integer.parseInt(elements[index]);
					flag_integer = 1;
					String regStore = getRegister();
//					String tinyRegStore = getTinyRegister();

					nodesPrinter.addIRNode(new IRNode(checkStore(type), elements[index], regStore));
					RegisterStack.push(regStore);

					//tinyNodeArrayList.add(new TinyNode("move", elements[index], tinyRegStore));
					//tinyRegisterStack.push(tinyRegStore);
				} catch (NumberFormatException e) {

				}

				try {
					if (flag_integer == 0) {
						Float.parseFloat(elements[index]);

						String regStore = getRegister();
//						String tinyRegStore = getTinyRegister();

						nodesPrinter.addIRNode(new IRNode(checkStore(type), elements[index], regStore));
						RegisterStack.push(regStore);

//						tinyNodeArrayList.add(new TinyNode("move", elements[index], tinyRegStore));
//						tinyRegisterStack.push(tinyRegStore);
					}
				} catch (NumberFormatException e) {
					RegisterStack.push(elements[index]); // Pushing the Variable Names to the Stack
					//tinyRegisterStack.push(elements[index]);
				}
			}
		}

		// Storing the Final Value of the Expression
		String FinalRegister = RegisterStack.pop();
		nodesPrinter.addIRNode(new IRNode(checkStore(type), FinalRegister, outputVariable));

		//String FinalTinyRegister = tinyRegisterStack.pop();
		//tinyNodeArrayList.add(new TinyNode("move", FinalTinyRegister, outputVariable));

		return;
	}

	@Override
	public void enterVar_decl(MicroParser.Var_declContext ctx) {
		String[] variables = ctx.getChild(1).getText().split(",");

		for (String variable : variables) {
			tinyNodeArrayList.add(new TinyNode("var", variable));
		}
	}

	public List<TinyNode> convertIRtoTiny(List<IRNode> irNodeList) {

		for (IRNode irNode : irNodeList) {

			if (irNode.OpCode.equals("ADDI") || irNode.OpCode.equals("SUBI") || irNode.OpCode.equals("MULTI") || irNode.OpCode.equals("DIVI") || irNode.OpCode.equals("ADDF") || irNode.OpCode.equals("SUBF") || irNode.OpCode.equals("MULTF") || irNode.OpCode.equals("DIVF")) {

				entered_Expr = 1;
				if (irNode.Fst_Op.contains("$") && irNode.Sec_Op.contains("$")) {
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), registerMap.get(irNode.Sec_Op), registerMap.get(irNode.Fst_Op)));
					registerMap.put(irNode.Result, registerMap.get(irNode.Fst_Op));
				} else if (irNode.Fst_Op.contains("$")) {
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), irNode.Sec_Op, registerMap.get(irNode.Fst_Op)));
					registerMap.put(irNode.Result, registerMap.get(irNode.Fst_Op));
				} else if (irNode.Sec_Op.contains("$")) {

					tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, "r" + Integer.toString(tinyReg)));
					registerMap.put(irNode.Fst_Op, "r" + Integer.toString(tinyReg));
					tinyReg += 1;
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), registerMap.get(irNode.Sec_Op), registerMap.get(irNode.Fst_Op)));
					registerMap.put(irNode.Result, registerMap.get(irNode.Fst_Op));
				} else {
					tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, "r" + Integer.toString(tinyReg)));
					registerMap.put(irNode.Result, "r" + Integer.toString(tinyReg));
					tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), irNode.Sec_Op, "r" + Integer.toString(tinyReg)));
					tinyReg += 1;
				}
			} else if (irNode.OpCode.equals("WRITEF") || irNode.OpCode.equals("WRITEI")) {
				if (irNode.OpCode.equals("WRITEF")) {
					tinyNodeArrayList.add(new TinyNode("sys writer", irNode.Result));
				} else {
					tinyNodeArrayList.add(new TinyNode("sys writei", irNode.Result));
				}
			}

			else if((irNode.OpCode.equals("STOREI") || irNode.OpCode.equals("STOREF")) && entered_Expr == 1) {
				//System.out.println("Inside STORE");

				if(irNode.Fst_Op.contains("$")){
					tinyNodeArrayList.add(new TinyNode("move", registerMap.get(irNode.Fst_Op), irNode.Result));
					//registerMap.put(irNode.Result, getTinyRegister());
				}

				else{
					tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, "r" + Integer.toString(tinyReg)));
					registerMap.put(irNode.Result, getTinyRegister());
				}	//tinyReg += 1;
			}
		}
		return tinyNodeArrayList;
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

	public void printIRNodes()
	{
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

// For Testing purposes.
class in_to_post {

    /*public static void main(String[] args) {
        String infix_Str = "c + a*b + (a*b+c)/a + 20;";
        //   String infix_Str = "(b*a)/a";
        String postFixStr = infix_to_Postfix(infix_Str);

        int count = 0;

        for(int index = 0; index < postFixStr.length(); index++) {
            if(postFixStr.charAt(index) == ' ') {
                count += 1;
            }
        }

        System.out.println(count);

        System.out.println(postFixStr);
        InOrderTraversal(parsePostfixExpr(postFixStr.trim()));
        return;
    } */

	public String infix_to_Postfix(String infix_Str) {

		infix_Str = infix_Str.split(";")[0];
		infix_Str = infix_Str.replaceAll("[\\s]", "");
		String[] elements = infix_Str.split("(?<=[-+*/()])|(?=[-+*/()])");

/*        for(int index = 0; index < elements.length; index++) {
            System.out.println(elements[index]);
        } */

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
				//System.out.println("Inside Main If");
				if (elements[index].equals(")")) {

					//                System.out.println("Found )");

					while (opStack.peek() != "(") {
						String popped_op = opStack.pop();
//                        System.out.println("Popping " + popped_op);
						output_Str.append(popped_op + " ");
					}

					opStack.pop();
				} else if (elements[index].equals("(")) {
					//                System.out.println("Pushing ( on to Stack");
					opStack.push("(");
				} else {

					int currentPrecedence = 3;

					if (opPrecedence.containsKey(elements[index])) {
						currentPrecedence = opPrecedence.get(elements[index]);
					}

					while (!opStack.empty() && opPrecedence.get(opStack.peek()) != null && currentPrecedence <= opPrecedence.get(opStack.peek())) {
						String popped_op = opStack.pop();
						//                      System.out.println("Popped " + " " + popped_op);
						output_Str.append(popped_op + " ");
					}

//                    System.out.println("Pushing " + elements[index] + " onto stack");
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

	public BSTNode parsePostfixExpr(String postFix_Expr) {

		Stack<BSTNode> bstNodeStack = new Stack<BSTNode>();

//        String[] elements = postFix_Expr.split("(?<=[-+*/])|(?=[-+*/])");

		String[] elements = postFix_Expr.split(" ");

		for (int index = 0; index < elements.length; index++) {

			if (elements[index].equals("+") || elements[index].equals("-") || elements[index].equals("*") || elements[index].equals("/")) {

				// Popping the Two Nodes
				BSTNode Node1 = bstNodeStack.pop();
				BSTNode Node2 = bstNodeStack.pop();

				//System.out.println("Left: " + Node2);
				//System.out.println("Right: " + Node1);
				BSTNode opNode = new BSTNode(elements[index]);
				opNode.left = Node2; // Push the last Popped Node onto the Left Side of the Expression Tree
				opNode.right = Node1; // Push the first popped Node onto the Right Side of the Expression Tree

				bstNodeStack.push(opNode); // Pushing new Expression Tree onto the Stack
			} else {
				//System.out.println("Pushing " + elements[index]);
				bstNodeStack.push(new BSTNode(elements[index])); // Pushing the Variable Names or Numbers to the Stack
			}
		}

		return bstNodeStack.pop();
	}

	public void InOrderTraversal(BSTNode bstNodeTree) {

		if (bstNodeTree != null) {
			InOrderTraversal(bstNodeTree.left);
			System.out.println(bstNodeTree.data);
			InOrderTraversal(bstNodeTree.right);

		}

		return;
	}


	private class BSTNode {

		String data; // Used to Store the data or variable names
		BSTNode left; // Left Pointer
		BSTNode right; // Right Pointer

		public BSTNode(String data) {
			this.data = data;
			this.left = null;
			this.right = null;
		}
	}
}