import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;

class CFGHandler {

        ArrayList<LinkedList<CFGNode>> cfgList;
        SymbolsTree symbolsTree;

        int nodeCount = 1;

        HashMap<IRNode,Integer> IRNodeMap = new HashMap<IRNode, Integer>();
        HashMap<Integer, CFGNode> CFGNodeMap = new HashMap<Integer, CFGNode>();

       class CFGNode {

           String[] code;

           IRNode irNode;

           int NodeNo;

           ArrayList<CFGNode> predecessors;
           ArrayList<CFGNode> successors;

           HashSet<String> GEN; // Variables Created
           HashSet<String> KILL; // Variables Used

           HashSet<String> IN;
           HashSet<String> OUT;

           // Constructor
           public CFGNode(String[] code, int NodeNo, IRNode irNode) {

               this.code = code;

               this.predecessors = new ArrayList<CFGNode>();
               this.successors = new ArrayList<CFGNode>();

               this.GEN = new HashSet<String>();
               this.KILL = new HashSet<String>();

               this.IN = new HashSet<String>();
               this.OUT = new HashSet<String>();

               // Initializations
               this.NodeNo = NodeNo; // Acts as a unique Identifier for the IRNode in the IRNodeArrayList and also helps in Constructing Basic Block
               this.irNode = irNode;

               IRNodeMap.put(irNode, NodeNo); // This makes it faster to search for Each Node associated with a CFGNode
           }

           public String getOpCode() {
               return this.code[0];
           }

           // get Labels for Conditional or Unconditional Jumps
           public String getLabel() {

               // Labels in the IR Code
               if (this.irNode.getOpCode().equals("LABEL") || this.irNode.getOpCode().equals("JUMP")) {
                   return this.irNode.getResult();
               }

               // Conditionals: Example EQ i $T1 label1
               else if (this.code.length == 4 && this.irNode.getResult().startsWith("label")) {
                   return this.irNode.getResult();
               } else {
                   return "";
               }
           }

           // Example EQ i $T1 label1 (length = 4)
           public boolean isConditional() {
               return ((this.irNode.getOpCode() != null) && (this.irNode.getFirstOperand() != null) && (this.irNode.getResult().startsWith("label")) && (this.irNode.getSecondOperand() != null));
           }

           public void addSuccessor(CFGNode cfgNode) {
               successors.add(cfgNode);
           }

           public void addPredecessor(CFGNode cfgNode) {
               predecessors.add(cfgNode);
           }

           // All the temporaries and variables that are used in an instruction
           public void generates(String string) {
               GEN.add(string);
           }

           // All the temporaries and variables that are used in an instruction
           public void kills(String string) {
               KILL.add(string);
           }

           public boolean isLeader() { // This needs to be changed
               return (!this.successors.isEmpty() || !this.predecessors.isEmpty());
           }

           @Override
           public String toString() {
               StringBuilder str = new StringBuilder();

               for(int index = 0; index < this.code.length; index++) {
                   str.append(code[index] + " ");
               }

               return str.toString();
           }  // Same as IRNode.toString() in mine.

       }
           public CFGHandler(ArrayList<IRNode> IRNodeList, SymbolsTree symbolsTree, HashMap<String, Function> functionsMap) {

               this.symbolsTree = symbolsTree;

               SymbolsTable symbolsTable = symbolsTree.getParentScope();
               ArrayList<Symbols> symbolsArrayList = symbolsTable.getSymbols();

               ArrayList<String> globalVariables =  new ArrayList<String>(); // Used for "JST" Generate Set

               for(Symbols symbol: symbolsArrayList) {
                   globalVariables.add(symbol.getVarName());
               }


               // REMEMBER to Use functionsMap here (DON'T FORGET)
               // Parse the linkedList into a list of CFNode Linked Lists
               // this breaks the IR into individual Functions so we can
               // Further Analysis
               //System.out.println("Inside CFG Handler");
               this.cfgList = new ArrayList<LinkedList<CFGNode>>();

               LinkedList<CFGNode> functionList = null;

               for(IRNode irNode: IRNodeList) {
                   String[] code = irNode.toString().split(";");

                   code = code[1].split(" ");
                   //System.out.println(code[0]);
                   // Check if the code is representing a Function Call
                   if(irNode.getOpCode().equals("LABEL") && !irNode.getResult().startsWith("label")) {
                       //System.out.println("Checking for Function Call");

                       if (functionList != null && !functionList.isEmpty()) {
                           cfgList.add(functionList);
                       }

                       // Creating a new List for every new Function
                       functionList = new LinkedList<CFGNode>();
                   }

                   CFGNode cfgNode = new CFGNode(code, nodeCount, irNode);
                   functionList.add(cfgNode);

                   CFGNodeMap.put(nodeCount, cfgNode);

                   nodeCount += 1;
               }

               if(functionList != null) {
                   // Adding the Last Function (DOUBTFUL)
                   cfgList.add(functionList);
               }

               //System.out.println("Done with breaking IRNodes into Individual Functions");

               // Now we have Individual Linked Lists for each Function
               for(LinkedList<CFGNode> linkedList: cfgList) {

                   for (int index = 0; index < linkedList.size(); index++) {

                       // Adding Predecessor to all the IRNodes other than First Node
                       if (index > 0) {

                           if (!linkedList.get(index - 1).irNode.getOpCode().equals("JUMP") && !(linkedList.get(index - 1).irNode.getOpCode().equals("RET"))) { // If Previous Nodes are "JUMP" and "RET" Do not add them as Predecessors
                               linkedList.get(index).addPredecessor(linkedList.get(index - 1));
                           }
                       }
                       // Adding Successor to all the IRNodes other than Last Node
                       if (index < linkedList.size() - 1) {

                           if (!linkedList.get(index).irNode.getOpCode().equals("RET") && !linkedList.get(index).irNode.getOpCode().equals("JUMP")) { // If the Current Nodes are "RET" and "JUMP", they will not have Successor Nodes
                               linkedList.get(index).addSuccessor(linkedList.get(index + 1));
                           }
                       }
                   }
               }

               // Creating the "Control Flow Graphs"
               // For each Function populating the successor and predecessor nodes which will transform
               // individual Function Linked Lists to Control Flow Graphs
               for(LinkedList<CFGNode> linkedList: cfgList) {

                   for(CFGNode node: linkedList) {

                       if( node.irNode.getOpCode().equals("JUMP") || node.isConditional()) {

                           String label = node.getLabel(); // This will decide which label to return if it is an unconditional or Conditional

                           for(CFGNode targetNode: linkedList) {

                               if(targetNode.irNode.getOpCode().equals("LABEL") && targetNode.getLabel().equals(label)) {
                                   node.addSuccessor(targetNode); // In case of a Conditional you will see 2 Successors
                                   targetNode.addPredecessor(node);
                               }
                           }
                       }
                   }
               }

               // Creates the Gen Kill List
               createGenKillSet(cfgList, globalVariables);

               // This Creates the In and Out Sets
               livenessAnalysisSet(IRNodeList, globalVariables);

               printCFGLinkedList(cfgList);
           }

           // From Step7 Document
           public void computeGenKill(CFGNode node, ArrayList<String> globalVariables) {

               String opcode = node.irNode.getOpCode();

               if (opcode == null || opcode.equals("RET")) {
                   return;
               }

               if (opcode.equals("ADD") || opcode.equals("SUB") // arithmetics
                       || opcode.equals("DIV") || opcode.equals("MUL")) {
                   node.generates(node.code[1]);
                   node.generates(node.code[2]);
                   node.kills(node.code[3]);

               } else if (opcode.equals("STORE")) { // store
                   node.generates(node.code[1]);
                   node.kills(node.code[2]);
               } else if (opcode.equals("STORE")) { // load
                   node.kills(node.code[1]);
                   node.generates(node.code[2]);

               } else if (opcode.equals("GE") || opcode.equals("LE")  // comparators
                       || opcode.equals("NE") || opcode.equals("EQ")) {
                   node.generates(node.code[1]);
                   node.generates(node.code[2]);
                   // code[3] is the jump label

               } else if (opcode.equals("WRITE")) { // write & read
                   node.generates(node.code[1]);
               } else if (opcode.equals("READ")) {
                   node.kills(node.code[1]);

               } else if (opcode.equals("PUSH")) { // push & pop
                   if (node.code.length > 1) {
                       node.generates(node.code[1]);
                   }
               } else if (opcode.equals("POP")) {
                   if (node.code.length > 1) {
                       node.kills(node.code[1]);
                   }
               }

               // Adding all global Variables to the "JST" SubRoutine
               else if (opcode.equals("JSR")) {
                   for (String globalVariable : globalVariables) {
                       node.generates(globalVariable);
                   }
               }
           }

           // This Creates the InSet and OutSet for each CFG Node (Formula: From Lecture Notes)
           public void livenessAnalysisSet(ArrayList<IRNode> irNodeArrayList, ArrayList<String> globalVariables) {

                for(int index = irNodeArrayList.size() - 1; index >= 0; index--) {

                    // Creating Inset and Outset for CFGList
                    HashSet<String> InSet = new HashSet<String>();
                    HashSet<String> OutSet = new HashSet<String>();

                    IRNode irNode = irNodeArrayList.get(index);

                    //System.out.println(irNode);

                    CFGNode cfgNode = CFGNodeMap.get(IRNodeMap.get(irNode));

                    // Fetch the Successor's List
                    ArrayList<CFGNode> SuccessorList = CFGNodeMap.get(index + 1).successors;

                    // Adding all the InSet of each Node in Successor List to the OutSet
                    for(CFGNode successorNode: SuccessorList) {

                        // Inset for each SuccessorNode
                        HashSet<String> inSet = successorNode.IN;
                        OutSet.addAll(inSet);
                    }

                    InSet.addAll(OutSet);
                    InSet.removeAll(cfgNode.KILL);
                    InSet.addAll(cfgNode.GEN);

                    if(irNode.getOpCode().equals("RET")) {
                        OutSet.addAll(globalVariables);
                    }

                    cfgNode.IN = InSet;
                    cfgNode.OUT = OutSet;
                }
           }

           // This creates the Gen and Kill Set
           public void createGenKillSet(ArrayList<LinkedList<CFGNode>> cfgList, ArrayList<String> globalVariables) {
               for (LinkedList<CFGNode> cfgNodeLinkedList : cfgList) {
                   for (CFGNode node : cfgNodeLinkedList) {
                       computeGenKill(node, globalVariables);
                   }
               }
           }

           // PRINT FUNCTION : This prints Successor, Predecessor,
           // Helps in DEBUGGING
           public void printCFGLinkedList(ArrayList<LinkedList<CFGNode>> cfgList) {

               for (LinkedList<CFGNode> cfgNodeLinkedList : cfgList) {
                   for (CFGNode node : cfgNodeLinkedList) {
                       //computeGenKill(node, globalVariables);
                       System.out.println("Node: " + node +
                               " => { Successors: " + node.successors + ", Predecessors: " + node.predecessors
                               + ", Gen Set: " + node.GEN + ", Kill Set: " + node.KILL + ", IN Set" + node.IN + ", OUT Set" + node.OUT + " }");
                   }
                   System.out.println();
               }
           }
}