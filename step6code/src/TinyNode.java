
import java.util.*;

public class TinyNode {

    String OpCode;
    String Operand;
    String Result;

    static HashMap<String, String> registerMap = new HashMap<String, String>();

    private static int tinyReg = 0;

    // Storing the Variables eg: var a, var b, var c
    public TinyNode(String opCode, String varName) {
        this.OpCode = opCode;
        this.Operand = null;
        this.Result = varName;
    }

    // For Storing all the Remaining Instructions
    public TinyNode(String opCode, String varName, String Result) {
        this.OpCode = opCode;
        this.Operand = varName;
        this.Result = Result;
    }

    public static String getTinyRegister() {
        String registerNo = new String("r" + tinyReg);
        tinyReg += 1;
        return registerNo;
    }

    public static String tinyOpCodeCheck(String Operation) {

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

    public static void printTinyList(List<TinyNode> tinyNodeList) {

        System.out.println(";tiny code");
        
        //System.out.println(tinyNodeList.size());
        
        for(TinyNode tinyNode: tinyNodeList) {
            if(tinyNode.Operand == null) {
                System.out.println(tinyNode.OpCode + " " + tinyNode.Result);
            }
            else {
                System.out.println(tinyNode.OpCode + " " + tinyNode.Operand + " " + tinyNode.Result);
            }
        }

        System.out.println("sys halt");
    }

    public static List<TinyNode> convertIRtoTiny(List<IRNode> irNodeList, SymbolsTree symbolsTree, ArrayList<String> variablesList, List<TinyNode> tinyNodeArrayList) {

        //System.out.println(irNodeList.size());

        //System.out.println("Priting Variables");

		/*for(int index = 0; index < variablesList.size(); index++) {
			System.out.println(variablesList.get(index));
		} */

        for (IRNode irNode : irNodeList) {

            if (irNode.OpCode.equals("ADDI") || irNode.OpCode.equals("SUBI") || irNode.OpCode.equals("MULTI") || irNode.OpCode.equals("DIVI") || irNode.OpCode.equals("ADDF") || irNode.OpCode.equals("SUBF") || irNode.OpCode.equals("MULTF") || irNode.OpCode.equals("DIVF")) {

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

            else if (irNode.OpCode.equals("READF") || irNode.OpCode.equals("READI")) {
                if (irNode.OpCode.equals("READF")) {
                    tinyNodeArrayList.add(new TinyNode("sys readr", irNode.Result));
                } else {
                    tinyNodeArrayList.add(new TinyNode("sys readi", irNode.Result));
                }
            }
            else if(irNode.OpCode.equals("STOREI") || irNode.OpCode.equals("STOREF")) {
                //System.out.println("Inside STORE");
                //System.out.println("Entered Condition: " + Integer.toString(entered_Cond));
                //System.out.println("Entered Expr: " + Integer.toString(entered_Expr));

                //				if(entered_Expr == 1) {

                //System.out.println(irNode.toString());

                if(irNode.Fst_Op.contains("$")) {
                    //System.out.println("Inside $ Variable");
                    tinyNodeArrayList.add(new TinyNode("move", registerMap.get(irNode.Fst_Op), irNode.Result));
                    //registerMap.put(irNode.Result, getTinyRegister());
                }

                else{

                    if(variablesList.contains(irNode.Fst_Op)){
                        //System.out.println("Inside 2 variables");
                        String tinyRegister = "r" + Integer.toString(tinyReg);
                        tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, tinyRegister));
                        tinyReg += 1;
                        tinyNodeArrayList.add(new TinyNode("move", tinyRegister, irNode.Result));
                        //registerMap.put(irNode.Result, getTinyRegister());
                    }	//tinyReg += 1;

                    else {
                        //System.out.println("Inside not 2 variables");
                        //System.out.println(irNode.);
                        tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, "r" + Integer.toString(tinyReg)));
                        registerMap.put(irNode.Result, getTinyRegister());
                    }
                }
            }

            else if(irNode.OpCode.equals("JUMP")) {
                tinyNodeArrayList.add(new TinyNode("jmp",irNode.Result));
            }

            else if(irNode.OpCode.equals("LABEL")) {
                tinyNodeArrayList.add(new TinyNode("label",irNode.Result));
            }

            else if(irNode.OpCode.equals("GE")) {
                if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {

                    if(variablesList.contains(irNode.Sec_Op)) {
                        String TinyRegister = getTinyRegister();
                        tinyNodeArrayList.add(new TinyNode("move", irNode.Sec_Op,TinyRegister));
                        tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,TinyRegister));
                    }
                    else {
                        tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                    }
                }

                else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {

                    if(variablesList.contains(irNode.Sec_Op)) {
                        String TinyRegister = getTinyRegister();
                        tinyNodeArrayList.add(new TinyNode("move", irNode.Sec_Op,TinyRegister));
                        tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,TinyRegister));
                    }
                    else {
                        tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                    }
                }

                tinyNodeArrayList.add(new TinyNode("jge",irNode.Result));
            }

            else if(irNode.OpCode.equals("GT")) {
                if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                tinyNodeArrayList.add(new TinyNode("jgt",irNode.Result));
            }

            else if(irNode.OpCode.equals("LE")) {
                if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                tinyNodeArrayList.add(new TinyNode("jle",irNode.Result));
            }

            else if(irNode.OpCode.equals("LT")) {
                if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                tinyNodeArrayList.add(new TinyNode("jlt",irNode.Result));
            }

            else if(irNode.OpCode.equals("EQ")) {

                if(irNode.Fst_Op.contains("$")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",registerMap.get(irNode.Fst_Op),registerMap.get(irNode.Sec_Op)));
                }

                else {
                    if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                        tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                    }

                    else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                        tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                    }

                    tinyNodeArrayList.add(new TinyNode("jeq",irNode.Result));
                }
            }

            else if(irNode.OpCode.equals("NE")) {
                //System.out.println("Inside NE");

                // Check if the two operands are registers

                if(irNode.Fst_Op.contains("$")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",registerMap.get(irNode.Fst_Op),registerMap.get(irNode.Sec_Op)));
                }

                else {
                    if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                        tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                    }

                    else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                        tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                    }

                }
                tinyNodeArrayList.add(new TinyNode("jne",irNode.Result));
                //System.out.println("Exit NE");
            }
        }

        return tinyNodeArrayList;
    }
}