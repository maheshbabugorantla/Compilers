
public class IRNode {

    String OpCode; // OpCode
    String Fst_Op; // First Operand
    String Sec_Op; // Second Operand
    String Result; // Result


    public IRNode(String opcode, String fst_op, String sec_op, String result) {

        this.OpCode = opcode;
        this.Fst_Op = fst_op;
        this.Sec_Op = sec_op;
        this.Result = result;
    }

    // When there are no operands For example for JUMP, LABEL, READI, READF, WRITEI, WRITEF, JSR
    public IRNode(String opcode, String result){

        this.OpCode = opcode;
        this.Fst_Op = null;
        this.Sec_Op = null;
        this.Result = result;
    }

    // For STOREI and STOREF
    public IRNode(String opcode, String fst_op, String result) {

        this.OpCode = opcode;
        this.Fst_Op = fst_op;
        this.Sec_Op = null;
        this.Result = result;
    }

    // for LINK, RET
    public IRNode(String opCode) {
        this.OpCode = opCode;
        this.Fst_Op = null;
        this.Sec_Op = null;
        this.Result = null;
    }

    @Override
    public String toString() {

        String semi_colon = ";";

        if(Result == null) {
           return (semi_colon + OpCode);
        }

        // JUMP, LABEL, READI, READF, WRITEI, WRITEF
        else if(Sec_Op == null && Fst_Op == null) {
            return (semi_colon + OpCode + " " + Result);
        }
        else if(Sec_Op == null) { // For STOREI and STOREF
            return (semi_colon + OpCode + " " + Fst_Op + " " + Result);
        }

        else {
            return (semi_colon + OpCode + " " + Fst_Op + " " + Sec_Op + " " + Result);
        }
    }
}