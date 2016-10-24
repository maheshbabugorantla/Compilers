import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro468Listener extends MicroBaseListener {


	public SymbolsTree symbolsTree;

	ParseTreeProperty<NodeProperty> parseTreeProperty;

	private int blockNum;
	private int regNum;

	NodesPrinter nodesPrinter;

	public Micro468Listener() {
		this.symbolsTree = new SymbolsTree();
		this.blockNum = 1;
//		this.TreeResolveData = new TreeResolve<dataNode>();
		this.parseTreeProperty = new ParseTreeProperty<NodeProperty>(); // Try Removing "NodeProperty"
		this.nodesPrinter = new NodesPrinter();
		this.regNum = 0;
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

	/********************/
	public NodeProperty getValue(ParseTree ctx) {
		System.out.println("Inside getValue");
		System.out.println(ctx.getText());
		if (ctx.getText() == "" || ctx == null) {
			System.out.println("Inside If ERROR");
			return null;
		}
		System.out.println(parseTreeProperty.get(ctx));
		return parseTreeProperty.get(ctx);
	}

	public void setValue(ParseTree ctx, NodeProperty value) {
		parseTreeProperty.put(ctx, value);
	}

	public String getRegister() {
		regNum += 1;
		String registerNo = new String("$T" + regNum);
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
		System.out.println("Enter Pgm_body");
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
		System.out.println("Exit Pgm_body");
		//	symbolsTree.printWholeTree(); // Print the whole SymbolTree Blocks
		nodesPrinter.addSymbols(symbolsTree.getAllSymbols(symbolsTree.getParentScope()));
		nodesPrinter.printIRNodes();
	}

	@Override
	public void enterFunc_decl(MicroParser.Func_declContext ctx) {
		System.out.println("Enter Func_decl");
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
		System.out.println("Enter If_stmt");
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
		System.out.println("Enter Else_part");
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

		System.out.println("Exit Else_part");

		if (ctx.getChild(0) == null)
			return;
	}

	// Have to Modify it
	@Override
	public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {

		System.out.println("Enter Do_while");

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

	@Override public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {

		System.out.println("Enter Write_stmt");

		if(ctx.getChild(2) == null || ctx.getChild(2).getText().equals("")) {
			return;
		}

		String[] ids = ctx.getChild(2).getText().split(",");

		for(String id: ids) {
			String type = symbolsTree.checkDataType(id);

			if(type == null) {
				System.out.println("ERROR ID");
				return;
			}

			if(type.equals("INT")) {
				IRNode irNodeI = new IRNode("WRITEI",id);
				nodesPrinter.addIRNode(irNodeI);
			}
			else if(type.equals("FLOAT")) {
				IRNode irNodeF = new IRNode("WRITEF",id);
				nodesPrinter.addIRNode(irNodeF);
			}
		}
	}

	@Override public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {

		System.out.println("Enter Assign_expr");

//		System.out.println(ctx.getChild(0).getText());
//		System.out.println(ctx.getChild(1).getText());

		String result = ctx.getChild(0).getText();

		String type = symbolsTree.checkDataType(result);

//		System.out.println("Type: " + type);

//		System.out.println(ctx.getChild(2).getText());

		System.out.println(ctx.getChild(2).getClass().getName());
		NodeProperty exprNode = getValue(ctx.getChild(2)); // Here is the Error

		String exprText = exprNode.getTemp();

		String opName = "";

		if(type == null) {
			System.out.println("ERROR ID");
			return;
		}

		else if(type.equals("INT")) {
			System.out.println("Int Detected");
			opName = "STOREI";
		}

		else if(type.equals("FLOAT")) {
			opName = "STOREF";
		}
		else {
			System.out.println("ERROR NAME");
			return;
		}

		nodesPrinter.addIRNode(new IRNode(opName, exprText,result));
	}

	@Override public void enterExpr(MicroParser.ExprContext ctx) {

		System.out.println("Enter Expr");

		if(ctx.getText().equals("")) {
			return;
		}

		NodeProperty expr_prefix = getValue(ctx.getChild(0)); // From Micro.g4, expr Grammar Rule
		NodeProperty factor = getValue(ctx.getChild(1)); // From Micro.g4, expr Grammar Rule
		String factorType = factor.getType();
		String factorText = factor.getTemp();
		String addOp = ctx.getChild(2).getText();

		if(expr_prefix == null) {
			NodeProperty expr_prefix_new = new NodeProperty(addOp,factorText,factorType);
			setValue(ctx,expr_prefix_new);
		}

		else {
			String registerName = getRegister();
			String temp = expr_prefix.getTemp();
			String mathOp = expr_prefix.getOpCode();
			String opCode = OpCodeCheck(mathOp,factorType);
			IRNode node = new IRNode(opCode,temp,factorText,registerName);
			nodesPrinter.addIRNode(node);
			NodeProperty expr_prefix_new = new NodeProperty(addOp,registerName,factorType);
			setValue(ctx,expr_prefix_new);
		}
	}

	@Override public void enterFactor(MicroParser.FactorContext ctx) {

		System.out.println("Enter Factor");

		NodeProperty factor_prefix = getValue(ctx.getChild(0));
		NodeProperty postfix_expr = getValue(ctx.getChild(1));

		String postfix_type = postfix_expr.getType();
		String postfix_text = postfix_expr.getTemp();

		if(factor_prefix == null) {
			NodeProperty factor =  new NodeProperty(null, postfix_text, postfix_type);
			setValue(ctx,factor);
		}

		else {
			String registerName = getRegister();
			String temp = factor_prefix.getTemp();
			String mathOp = factor_prefix.getOpCode();
			String opCode = OpCodeCheck(mathOp,postfix_type);
			IRNode node = new IRNode(opCode,temp,postfix_text,registerName);
			nodesPrinter.addIRNode(node);
			NodeProperty factor = new NodeProperty(null,registerName,postfix_type);
			setValue(ctx,factor);
		}
	}

	@Override public void enterFactor_prefix(MicroParser.Factor_prefixContext ctx) {

		System.out.println("Enter Factor_prefix");

		if(ctx.getText().equals("")) {
			return;
		}

		NodeProperty factor_prefix = getValue(ctx.getChild(0));
		NodeProperty postfix_expr = getValue(ctx.getChild(1));
		String postfix_type = postfix_expr.getType();
		String postfix_text = postfix_expr.getTemp();
		String mulOp = ctx.getChild(2).getText();

		if(factor_prefix == null) {
			NodeProperty factor_prefix_new =  new NodeProperty(mulOp, postfix_text, postfix_type);
			setValue(ctx,factor_prefix_new);
		}

		else {
			String registerName = getRegister();
			String temp = factor_prefix.getTemp();
			String mathOp = factor_prefix.getOpCode();
			String opCode = OpCodeCheck(mathOp,postfix_type);
			IRNode node = new IRNode(opCode,temp,postfix_text,registerName);
			nodesPrinter.addIRNode(node);
			NodeProperty factor_prefix_new = new NodeProperty(mulOp,registerName,postfix_type);
			setValue(ctx,factor_prefix_new);
		}
	}

	@Override public void enterPostfix_expr(MicroParser.Postfix_exprContext ctx) {

		System.out.println("Enter Postfix_expr");

		NodeProperty postfix_expr = getValue(ctx.getChild(0));
		setValue(ctx,postfix_expr);
	}

	@Override public void enterPrimary(MicroParser.PrimaryContext ctx) {

		System.out.println("Enter Primary");

		NodeProperty expr = getValue(ctx.getChild(1));

		if(expr != null) {
			setValue(ctx,expr);
		}

		else {
			String primary = ctx.getChild(0).getText();
			String type = symbolsTree.checkDataType(primary);

			if(!primary.matches("[A-Za-z]+")) {
				String registerName = getRegister();
				String opCode = checkStore(type);
				IRNode irNode = new IRNode(opCode,primary,registerName);
				nodesPrinter.addIRNode(irNode);
				NodeProperty value = new NodeProperty(null,registerName,type);
				setValue(ctx,value);
			}

			else {
				NodeProperty value = getValue(ctx.getChild(0));
				setValue(ctx,value);
			}
		}
	}

	@Override public void enterId(MicroParser.IdContext ctx) {
		System.out.println("Enter Id");
		System.out.println(ctx.getText());
		String type = symbolsTree.checkDataType(ctx.getText());
		NodeProperty id = new NodeProperty(null, ctx.getText(), type);
		setValue(ctx,id);
	}
}

class NodesPrinter {

	private ArrayList<IRNode> IRNodeList;
	private ArrayList<Symbols> symbolsArrayList;
//		ArrayList<TinyNode> tinyNodeArrayList;
//		private int tinycount;
// 		private HashMap<String, String> regMap;

	public NodesPrinter(){
		IRNodeList = new ArrayList<>();
		symbolsArrayList = new ArrayList<>();

		//this.tinyNodeArrayList = new ArrayList<>();
		//tinycount = 0;
		//regMap = new HashMap<>();
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
		String semi_colon = ";";
		for(int index = 0; index < IRNodeList.size(); index++) {
			IRNode irNode = IRNodeList.get(index);
			System.out.println(semi_colon + irNode.toString());
		}
	}
}

class NodeProperty {

	private String opCode;
	private String temp;
	private String type;

	public NodeProperty(String OpCode, String temp, String Type) {

		this.opCode = OpCode;
		this.temp = temp;
		this.type = Type;
	}

	public String getOpCode() {
		return opCode;
	}

	public String getTemp() {
		return temp;
	}

	public String getType() {
		return type;
	}
}