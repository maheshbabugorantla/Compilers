import java.util.*;

public class TinyNode {

    String OpCode;
    String Operand;
    String Result;

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
}