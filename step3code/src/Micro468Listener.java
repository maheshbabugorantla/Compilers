import java.util.*;
import org.antlr.v4.runtime.*;

public class Micro468Listener extends MicroBaseListener{
	

	public SymbolsTree symbolsTree;
	private int blockNum;
	public Micro468Listener() {
		this.symbolsTree = new SymbolsTree();
		this.blockNum = 1;
	}

	public void pushSymbol(String currentValue, SymbolsTable table) {

		if (currentValue.startsWith("INT", 0)) {
			String[] intVal = currentValue.substring(3).split(",");
			for (String str : intVal) {
				Symbols symbol = new Symbols(str, "INT");
				table.addSymbol(symbol);
			}
		}
		else if (currentValue.startsWith("FLOAT", 0)) {
			String[] float_val = currentValue.substring(5).split(",");
			for (String str : float_val) {
				Symbols symbol = new Symbols(str, "FLOAT");
				table.addSymbol(symbol);
			}
		}
		else if (currentValue.startsWith("STRING", 0)) {
			String[] str_val = currentValue.substring(6).split(":=");
			Symbols symbol = new Symbols(str_val[0], "STRING",str_val[1]);
			table.addSymbol(symbol);
		}
	}

	public void functionParameters(String currentValue, SymbolsTable table) {
		String[] intVal = currentValue.split(",");
		for (int index = 0; index < intVal.length; index++) {
		if (currentValue.startsWith("INT", 0)) {
				String str = intVal[index].subString(3);
				Symbols symbol = new Symbols(str, "INT");
				table.addSymbol(symbol);
		}
		else if (currentValue.startsWith("FLOAT", 0)) {
				String str = intVal[index].subString(5);
				Symbols symbol = new Symbols(str, "FLOAT");
				table.addSymbol(symbol);
			}
		}
	}

	public String getBlockName() {
		return "BLOCK " + blockNum++;
	}

	@Override public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
		if (ctx.getChild(0) == null || ctx.getChild(0).getText() == "") return;
		String[] global_vars = ctx.getChild(0).getText().split(";");
		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, symbolsTree.getParentScope());
		}
	}

	// Here we print the whole symbolsTree when we exit the PROGRAM
	@Override public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
		symbolsTree.printWholeTree();
	}

	@Override public void enterFunc_decl(MicroParser.Func_declContext ctx) {
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

	@Override public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);
		if(ctx.getChild(4)==null || ctx.getChild(4).getText() == "")
			return;
		String[] global_vars = ctx.getChild(4).getText().split(";");
		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, table);
		}
	}

	@Override public void enterElse_part(MicroParser.Else_partContext ctx) {

		if (ctx.getChild(0) == null)
			return;

 		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);

		if(ctx.getChild(1) == null || ctx.getChild(1).getText() == "")
			return;

		String[] global_vars = ctx.getChild(1).getText().split(";");

		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, table);
		}
	}
	
	@Override public void exitElse_part(MicroParser.Else_partContext ctx) {
		if(ctx.getChild(0) == null)
			return;
	}

	// Have to Modify it
	@Override public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {

		if (ctx.getChild(0) == null)
			return;

		SymbolsTable table = new SymbolsTable(getBlockName());
		symbolsTree.getCurrentScope().addChild(table);

		if(ctx.getChild(1) == null || ctx.getChild(1).getText() == "")
			return;

		String[] global_vars = ctx.getChild(1).getText().split(";");

		for (int index = 0; index < global_vars.length; index++) {
			String currentValue = global_vars[index];
			pushSymbol(currentValue, table);
		}
	}
}
