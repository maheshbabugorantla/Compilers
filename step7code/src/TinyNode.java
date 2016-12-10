
import java.util.*;

public class TinyNode {

    String OpCode;
    String Operand;
    String Result;

    static HashMap<String, String> registerMap = new HashMap<String, String>();

    // Remember to fetch all the Function Names into a hashSet of Strings containing Function Names.
    private HashMap<String, Function> functionsMap = new HashMap<String, Function>(); // Used to get the No.of Local parameters for each function
    private SymbolsTable globalVariables;

    private String currFuncName;

    private String currDataType;

    private static int tinyReg = 0;

    private int RetFlag = 0;

    private int retCount = 1;

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

    public TinyNode(String opCode) {
        this.OpCode = opCode;
    }

    private static String getTinyRegister() {
        String registerNo = new String("r" + tinyReg);
        tinyReg += 1;
        return registerNo;
    }

    // Default Constructor for the TinyNode
    public TinyNode(HashMap<String, Function> functionsMap, SymbolsTable globalVariables) {
        this.functionsMap = functionsMap;
        this.globalVariables = globalVariables;
        this.currFuncName = null;
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

    public void printTinyList(List<TinyNode> tinyNodeList) {

        System.out.println(";tiny code");

        // Print all the Global Variables first.
        printGlobalVariables();

        System.out.println("push");
        System.out.println("push r0");
        System.out.println("push r1");
        System.out.println("push r2");
        System.out.println("push r3");
        System.out.println("jsr main");
        System.out.println("sys halt");

        // Then print all the TinyNodes converted from IR to Tiny
        for(TinyNode tinyNode: tinyNodeList) {

            if(tinyNode.Operand == null && tinyNode.Result == null) {
                System.out.println(tinyNode.OpCode);
            }

            else if(tinyNode.Operand == null) {
                System.out.println(tinyNode.OpCode + " " + tinyNode.Result);
            }
            else {
                System.out.println(tinyNode.OpCode + " " + tinyNode.Operand + " " + tinyNode.Result);
            }
        }

        System.out.println("end");
    }

    public List<TinyNode> convertIRtoTiny(List<IRNode> irNodeList, SymbolsTree symbolsTree, ArrayList<String> variablesList, List<TinyNode> tinyNodeArrayList) {

        //System.out.println("Inside the IR to TinyList");

        for (IRNode irNode : irNodeList) {

            //System.out.println(irNode.OpCode);

            if (irNode.OpCode.equals("ADDI") || irNode.OpCode.equals("SUBI") || irNode.OpCode.equals("MULTI") || irNode.OpCode.equals("DIVI") || irNode.OpCode.equals("ADDF") || irNode.OpCode.equals("SUBF") || irNode.OpCode.equals("MULTF") || irNode.OpCode.equals("DIVF")) {

                retCount = 1;

                if (irNode.Fst_Op.contains("$") && irNode.Sec_Op.contains("$")) {

                    if ((irNode.Fst_Op.contains("$P") || irNode.Sec_Op.contains("$L")) && (irNode.Fst_Op.contains("$L") || irNode.Sec_Op.contains("$P"))) {
                        String firstOp = getRelRegister(irNode.Fst_Op);
                        String secondOp = getRelRegister(irNode.Sec_Op);

                        //String tinyRegister = getTinyRegister();
                        //registerMap.put(irNode.Result, tinyRegister);

                        if(registerMap.get(irNode.Result) == null) {
                            tinyReg += 1;
                            registerMap.put(irNode.Result, "r" + Integer.toString(tinyReg));
                        }

                        tinyNodeArrayList.add(new TinyNode("move", firstOp, registerMap.get(irNode.Result)));
                        tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), secondOp, registerMap.get(irNode.Result)));
                    }

                    else if(irNode.Fst_Op.contains("$P") || irNode.Fst_Op.contains("$L") || irNode.Sec_Op.contains("$P") || irNode.Sec_Op.contains("$L")) {

                        String firstOp;
                        String secondOp;

                        // For firstOp
                        if(irNode.Fst_Op.contains("$P") || irNode.Fst_Op.contains("$L")) {
                            firstOp = getRelRegister(irNode.Fst_Op);
                        }

                        else{
                            firstOp = registerMap.get(irNode.Fst_Op);
                        }

                        // For secondOp
                        if(irNode.Sec_Op.contains("$P") || irNode.Sec_Op.contains("$L")) {
                            secondOp = getRelRegister(irNode.Sec_Op);
                        }

                        else{
                            secondOp = registerMap.get(irNode.Sec_Op);
                        }

//                        String tinyRegister = getTinyRegister();
//                        registerMap.put(irNode.Result, tinyRegister);

                        if(registerMap.get(irNode.Result) == null) {
                            tinyReg += 1;
                            registerMap.put(irNode.Result, "r" + Integer.toString(tinyReg));
                        }

                        tinyNodeArrayList.add(new TinyNode("move", firstOp, registerMap.get(irNode.Result)));
                        tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), secondOp, registerMap.get(irNode.Result)));
                    }
                    else { // Eg: ADDI $T1 $T2 $T3
                        tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), registerMap.get(irNode.Sec_Op), registerMap.get(irNode.Fst_Op)));
                        registerMap.put(irNode.Result, registerMap.get(irNode.Fst_Op));
                    }
                }else if (irNode.Fst_Op.contains("$")) { // Have to handle cases with Local Variable and Parameters
                    tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), irNode.Sec_Op, registerMap.get(irNode.Fst_Op)));
                    registerMap.put(irNode.Result, registerMap.get(irNode.Fst_Op));
                } else if (irNode.Sec_Op.contains("$")) { // Have to handle cases with Local Variable and Parameters
                    tinyReg += 1;
                    tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, "r" + Integer.toString(tinyReg)));
                    registerMap.put(irNode.Fst_Op, "r" + Integer.toString(tinyReg));
                    tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), registerMap.get(irNode.Sec_Op), registerMap.get(irNode.Fst_Op)));
                    registerMap.put(irNode.Result, registerMap.get(irNode.Fst_Op));
                } else { // All Global Variables
                    tinyReg += 1;
                    tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, "r" + Integer.toString(tinyReg)));
                    registerMap.put(irNode.Result, "r" + Integer.toString(tinyReg));
                    tinyNodeArrayList.add(new TinyNode(tinyOpCodeCheck(irNode.OpCode), irNode.Sec_Op, "r" + Integer.toString(tinyReg)));
                }
            } else if (irNode.OpCode.equals("WRITEF") || irNode.OpCode.equals("WRITEI") || irNode.OpCode.equals("WRITES")) {

                retCount = 1;

                String Result = irNode.Result;

                if(Result.contains("$")) {

                    if(Result.startsWith("$P") || Result.startsWith("$L")) {
                        Result = getRelRegister(Result);
                    }

                    else {
                        Result = registerMap.get(Result);
                    }
                }

                if (irNode.OpCode.equals("WRITEF")) {
                    tinyNodeArrayList.add(new TinyNode("sys writer", Result));
                } else if(irNode.OpCode.equals("WRITEI")) {
                    tinyNodeArrayList.add(new TinyNode("sys writei", Result));
                }
                else {
                    tinyNodeArrayList.add(new TinyNode("sys writes", Result));
                }
            }


            else if (irNode.OpCode.equals("READF") || irNode.OpCode.equals("READI") || irNode.equals("READS")) {

                retCount = 1;

                String Result = irNode.Result;

                if(Result.contains("$")) {

                    if(Result.startsWith("$P") || Result.startsWith("$L")) {
                        Result = getRelRegister(Result);
                    }

                    else {
                        Result = registerMap.get(Result);
                    }
                }

                if (irNode.OpCode.equals("READF")) {
                    tinyNodeArrayList.add(new TinyNode("sys readr", Result));
                } else if(irNode.OpCode.equals("READI")){
                    tinyNodeArrayList.add(new TinyNode("sys readi", Result));
                }
                  else {
                   tinyNodeArrayList.add(new TinyNode("sys reads", Result));
                }

            }
            else if(irNode.OpCode.equals("STOREI") || irNode.OpCode.equals("STOREF")) { // Remember Sec_Op will be null in this case

                retCount = 1;

                if(irNode.OpCode.equals("STOREI")) {
                    currDataType = "INT";
                }

                else {
                    currDataType = "FLOAT";
                }

                if(irNode.Fst_Op.contains("$")) {

                    if (irNode.Fst_Op.contains("$P") || irNode.Fst_Op.contains("$L")) {

                        String firstOp = getRelRegister(irNode.Fst_Op);
                        String tinyRegister = getTinyRegister();
                        String result = getRelRegister(irNode.Result);

                        if (result.startsWith("$T")) { // LT or PT
                            result = registerMap.get(result);
                        }

                        tinyNodeArrayList.add(new TinyNode("move", firstOp, tinyRegister));
                        tinyNodeArrayList.add(new TinyNode("move", tinyRegister, result));
                    } else {

                        String Result = irNode.Result;

                        //System.out.println("First Op: " + irNode.Fst_Op);
                        //System.out.println("Result: " + Result);

                        if (irNode.Result.startsWith("$P") || irNode.Result.startsWith("$L") || irNode.Result.startsWith("$R")) {
                            Result = getRelRegister(irNode.Result);
                        }

                        tinyNodeArrayList.add(new TinyNode("move", registerMap.get(irNode.Fst_Op), Result));
                    }
                }
                else{
                    // Global Variables I Guess
                    if(variablesList.contains(irNode.Fst_Op) && variablesList.contains(irNode.Result)) {

                        //System.out.println("Storing a global variable in another global variable");
                        //System.out.println("IRNode: " + irNode.toString());
                        tinyReg += 1;
                        String tinyRegister = "r" + Integer.toString(tinyReg);
                        tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, tinyRegister));
                        tinyNodeArrayList.add(new TinyNode("move", tinyRegister, irNode.Result));
                    }

                    else { // move "constant" $T# (Need to correct this to handle "constant" $T# and two variables (STOREI num1 num2)

                        if(registerMap.get(irNode.Result) == null && !irNode.Result.equals("$T1")) {
                            System.out.println();
                            tinyReg += 1;
                        }

                        tinyNodeArrayList.add(new TinyNode("move", irNode.Fst_Op, "r" + Integer.toString(tinyReg)));
                        registerMap.put(irNode.Result, "r" + Integer.toString(tinyReg));
                    }
                }
            }

            else if(irNode.OpCode.equals("JUMP")) {
                retCount = 1;
                tinyNodeArrayList.add(new TinyNode("jmp",irNode.Result));
            }

            else if(irNode.OpCode.equals("LABEL")) {
                retCount = 1;
                tinyNodeArrayList.add(new TinyNode("label",irNode.Result));

                if(!irNode.Result.startsWith("label")) {
                    //System.out.println(irNode.toString());
                    //System.out.println("Label Function: " + irNode.Result);
                    this.currFuncName = irNode.Result;
                    tinyReg = 0;
                    registerMap = new HashMap<String, String>(); // Re-Initializing
                    //RetFlag = 0; // Resetting the Return Flag.
                }
            }

            else if(irNode.OpCode.equals("GE")) {
                //System.out.println("Inside GE");

                retCount = 1;

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
                //System.out.println("Inside GT");

                retCount = 1;

                String firstOp = irNode.Fst_Op;
                String secondOp = irNode.Sec_Op;

                if(irNode.Fst_Op.startsWith("$P") || irNode.Fst_Op.startsWith("$L") || irNode.Fst_Op.startsWith("$T")) {

                    firstOp = getRelRegister(firstOp);

                    if(firstOp.startsWith("$T")) {
                        firstOp = registerMap.get(firstOp);
                    }
                }

                if(irNode.Sec_Op.startsWith("$P") || irNode.Sec_Op.startsWith("$L") || irNode.Sec_Op.startsWith("$T")) {

                    secondOp = getRelRegister(secondOp);

                    if(secondOp.startsWith("$T")) {
                        secondOp = registerMap.get(secondOp);
                    }
                }

                //if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                if(currDataType.equals("INT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",firstOp,secondOp));
                }

                //else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                if(currDataType.equals("FLOAT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpr",firstOp,secondOp));
                }

                tinyNodeArrayList.add(new TinyNode("jgt",irNode.Result));
            }

            else if(irNode.OpCode.equals("LE")) {
                //System.out.println("Inside LE");

                retCount = 1;

                String firstOp = irNode.Fst_Op;
                String secondOp = irNode.Sec_Op;

                if(irNode.Fst_Op.startsWith("$P") || irNode.Fst_Op.startsWith("$L") || irNode.Fst_Op.startsWith("$T")) {

                    firstOp = getRelRegister(firstOp);

                    if(firstOp.startsWith("$T")) {
                        firstOp = registerMap.get(firstOp);
                    }
                }

                if(irNode.Sec_Op.startsWith("$P") || irNode.Sec_Op.startsWith("$L") || irNode.Sec_Op.startsWith("$T")) {

                    secondOp = getRelRegister(secondOp);

                    if(secondOp.startsWith("$T")) {
                        secondOp = registerMap.get(secondOp);
                    }
                }

                if(currDataType.equals("INT")) {
                    //System.out.println("Inside If LE");
                    tinyNodeArrayList.add(new TinyNode("cmpi",firstOp, secondOp));
                }

                else if(currDataType.equals("FLOAT")) {
                    //System.out.println("Inside Else If LE");
                    tinyNodeArrayList.add(new TinyNode("cmpr", firstOp, secondOp));
                }

                tinyNodeArrayList.add(new TinyNode("jle",irNode.Result));
                //System.out.println("Exit LE");
            }

            else if(irNode.OpCode.equals("LT")) {
                //System.out.println("Inside LT");
                retCount = 1;
                if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpr",irNode.Fst_Op,registerMap.get(irNode.Sec_Op)));
                }

                tinyNodeArrayList.add(new TinyNode("jlt",irNode.Result));
            }

            else if(irNode.OpCode.equals("EQ")) {

                //System.out.println("Inside EQ");

               /* if(irNode.Fst_Op.contains("$")) {
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
                } */

                retCount = 1;

                String firstOp = irNode.Fst_Op;
                String secondOp = irNode.Sec_Op;

                if((firstOp.startsWith("$P") || firstOp.startsWith("$L")) && (secondOp.startsWith("$P") || secondOp.startsWith("$L"))) {

                    //System.out.println(tinyReg);

                    tinyReg += 1;
                    String tinyRegister = "r" + Integer.toString(tinyReg);

                    firstOp = getRelRegister(firstOp);
                    secondOp = getRelRegister(secondOp);

                    tinyNodeArrayList.add(new TinyNode("move", secondOp, tinyRegister));

                    if(currDataType.equals("INT")) {
                        tinyNodeArrayList.add(new TinyNode("cmpi", firstOp, tinyRegister));
                    }

                    //else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                    if(currDataType.equals("FLOAT")) {
                        tinyNodeArrayList.add(new TinyNode("cmpr",firstOp, tinyRegister));
                    }
                }


                else {
                    if (irNode.Fst_Op.startsWith("$P") || irNode.Fst_Op.startsWith("$L") || irNode.Fst_Op.startsWith("$T")) {

                        firstOp = getRelRegister(firstOp);

                        if (firstOp.startsWith("$T")) {
                            firstOp = registerMap.get(firstOp);
                        }
                    }

                    if (irNode.Sec_Op.startsWith("$P") || irNode.Sec_Op.startsWith("$L") || irNode.Sec_Op.startsWith("$T")) {

                        secondOp = getRelRegister(secondOp);

                        if (secondOp.startsWith("$T")) {
                            secondOp = registerMap.get(secondOp);
                        }
                    }

                //if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                if(currDataType.equals("INT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",firstOp,secondOp));
                }

                //else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                if(currDataType.equals("FLOAT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpr",firstOp,secondOp));
                }
            }

                tinyNodeArrayList.add(new TinyNode("jeq",irNode.Result));
            }

            else if(irNode.OpCode.equals("NE")) {
                //System.out.println("Inside NE");

                retCount = 1;

                String firstOp = irNode.Fst_Op;
                String secondOp = irNode.Sec_Op;

                if(irNode.Fst_Op.startsWith("$P") || irNode.Fst_Op.startsWith("$L") || irNode.Fst_Op.startsWith("$T")) {

                    firstOp = getRelRegister(firstOp);

                    if(firstOp.startsWith("$T")) {
                        firstOp = registerMap.get(firstOp);
                    }
                }

                if(irNode.Sec_Op.startsWith("$P") || irNode.Sec_Op.startsWith("$L") || irNode.Sec_Op.startsWith("$T")) {

                    secondOp = getRelRegister(secondOp);

                    if(secondOp.startsWith("$T")) {
                        secondOp = registerMap.get(secondOp);
                    }
                }

                //if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("INT")) {
                if(currDataType.equals("INT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpi",firstOp,secondOp));
                }

                //else if(symbolsTree.getParentScope().checkDataType(irNode.Fst_Op).equals("FLOAT")) {
                if(currDataType.equals("FLOAT")) {
                    tinyNodeArrayList.add(new TinyNode("cmpr",firstOp,secondOp));
                }

                tinyNodeArrayList.add(new TinyNode("jne",irNode.Result));
                //System.out.println("Exit NE");
            }

            else if(irNode.OpCode.equals("JSR")) {

                //System.out.println("Inside JSR");
                retCount = 1;

                tinyNodeArrayList.add(new TinyNode("push", "r0"));
                tinyNodeArrayList.add(new TinyNode("push", "r1"));
                tinyNodeArrayList.add(new TinyNode("push", "r2"));
                tinyNodeArrayList.add(new TinyNode("push", "r3"));
                tinyNodeArrayList.add(new TinyNode("jsr", irNode.Result));
                tinyNodeArrayList.add(new TinyNode("pop", "r3"));
                tinyNodeArrayList.add(new TinyNode("pop", "r2"));
                tinyNodeArrayList.add(new TinyNode("pop", "r1"));
                tinyNodeArrayList.add(new TinyNode("pop", "r0"));
            }

            else if(irNode.OpCode.equals("PUSH")) {

                retCount = 1;

                if(irNode.Result == null) {
                    tinyNodeArrayList.add(new TinyNode("push"));
                }

                else {

                    String Result = irNode.Result;

                    if(Result.startsWith("$P") || Result.startsWith("$L")) {
                        Result = getRelRegister(Result);
                        tinyNodeArrayList.add(new TinyNode("push", Result));
                    }
                    else if(Result.startsWith("$T")) {


                        if(registerMap.get(Result) == null) {
                            tinyReg += 1;
                            String tinyRegister = new String("r" + tinyReg);
                            registerMap.put(Result, tinyRegister);
                            Result = tinyRegister;
                            tinyNodeArrayList.add(new TinyNode("push", Result));
                        }
                        else {
                            tinyNodeArrayList.add(new TinyNode("push", registerMap.get(Result)));
                        }
                    }
                }

            }

            else if(irNode.OpCode.equals("POP")) {

                retCount = 1;

                if(irNode.Result == null) {
                    tinyNodeArrayList.add(new TinyNode("pop"));
                }
                else {

                    String Result = irNode.Result;

                    if (Result.startsWith("$P") || Result.startsWith("$L")) {
                        Result = getRelRegister(Result);
                        tinyNodeArrayList.add(new TinyNode("push", Result));
                    } else if (Result.startsWith("$T")) {
                        if (registerMap.get(Result) == null) {
                            tinyReg += 1;
                            String tinyRegister = new String("r" + tinyReg);
                            registerMap.put(Result, tinyRegister);
                            Result = tinyRegister;
                            tinyNodeArrayList.add(new TinyNode("pop", Result));
                        }
                        else {
                            tinyNodeArrayList.add(new TinyNode("pop", registerMap.get(Result)));
                        }
                    }
                }
            }

            else if(irNode.OpCode.equals("RET")) {

                //System.out.println("Inside RET");

                //System.out.println("retCount: " + retCount);

                if(retCount == 1) {
                    tinyNodeArrayList.add(new TinyNode("unlnk"));
                    tinyNodeArrayList.add(new TinyNode("ret"));
                    retCount += 1;
                }
            }

            else if(irNode.OpCode.equals("LINK")) {

                //System.out.println("Inside LINK");

                //System.out.println("Current Function: " + this.currFuncName);
                retCount = 1;

                Function newFunction = this.functionsMap.get(this.currFuncName); // Getting the Function Object
                //System.out.println(newFunction);
                int no_Variables = newFunction.getLocalVariables().size();

                tinyNodeArrayList.add(new TinyNode("link", Integer.toString(no_Variables)));
            }
        }

        return tinyNodeArrayList;
    }

    private String getRelRegister(String registerName) {

        Function newFunction = functionsMap.get(currFuncName);

        int noParameters = newFunction.getParameters().size();
        //int noLocalVars = newFunction.getLocalVariables().size();

        // Converting Parameter to relative Stack Register Address
        if(registerName.startsWith("$P")) {
            int parameterNo = Integer.parseInt(registerName.substring(2));
            return ("$" + Integer.toString(6 + noParameters - parameterNo));
        }

        // Converting local variables to relative Stack Register Address
        else if(registerName.startsWith("$L")) {
            int localNo = Integer.parseInt(registerName.substring(2));
            return("$" + Integer.toString(-1 * localNo));
        }

        else if(registerName.equals("$R")) {
            return("$" + Integer.toString(6 + noParameters));
        }

        return registerName;
    }

    private void printGlobalVariables() {

        ArrayList<Symbols> symbolsList = this.globalVariables.getSymbols();

        for(Symbols symbol: symbolsList) {

            if(symbol.getVarType() != "STRING") {
                System.out.print("var " + symbol.getVarName() + " ");
            }

            else {
                System.out.print("str " + symbol.getVarName() + " ");
            }

            // Printing the Value Stored in the Global Variables
            if(symbol.getVarValue() != null) {
                System.out.print(symbol.getVarValue());
            }

            System.out.println("");
        }

    }

}