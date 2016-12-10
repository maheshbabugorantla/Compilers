import java.util.*;
import java.util.regex.*;

class TestInfix {

    // This Fucntion is good enough for Step 0 - Step 5
    public static String infix_to_Postfix(String infix_Str) {

        infix_Str = infix_Str.split(";")[0];
        infix_Str = infix_Str.replaceAll("[\\s]", ""); // Removing all the spaces in the expression
        String[] elements = infix_Str.split("(?<=[-+*/()])|(?=[-+*/()])"); // Splitting the Infix String into individual Tokens

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

    public static void main(String[] args) {

        HashSet<String> funcNames = new HashSet<String>();

        funcNames.add("func");
        funcNames.add("func1");
        funcNames.add("sin");
        funcNames.add("multiply");
        funcNames.add("add");
        funcNames.add("mul");

        //String str = "(a + b) + func(b-c) + func1(a+b)"; // (PASS)
        //String str = "sin(x+y) * z"; // PASS
        //String str = "multiply(a,b);"; // PASS
        //String str = "add(multiplyresult,c);"; // PASS
        //String str = "0 - a + b";
        //String str = "(a+b)*(b+d)/mul(a,b)+play";
        //String str = "c + a*b + (a*b+c)/a + 20";
        String str = "a + 27 + b*(c/d + 4+c);";

        infixtoPostfix(str, funcNames);
        //System.out.println(infix_to_Postfix(str));
    }

    public static void infixtoPostfix(String infixStr, HashSet<String> funcNames) {

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

        Stack<String> OpStack = new Stack<String>(); // Stack used to store the Operators

        HashMap<String, Integer> opPrecedence = new HashMap<String, Integer>(); // This HashMap Stores the Operator Precedence

        opPrecedence.put("+", 1);
        opPrecedence.put("-", 1);
        opPrecedence.put("*", 2);
        opPrecedence.put("/", 2);

        OpStack.push("(");


        for(String token: tokens) {

/*            if(token.endsWith("(")) {
                System.out.println(token.substring(0,(token.length() - 1)));
            } */

            System.out.println("Token: " + token);
            if(!token.equals("")) {
            if(token.equals("(")) {
                OpStack.push(token);
            }

            // Pushing the Function Label
            else if(token.endsWith("(") && funcNames.contains(token.substring(0,(token.length()-1)))) {
                OpStack.push(token.substring(0,token.length()-1)); // Pushing the Function name onto the Stack
                //System.out.println("Inside function");
            }

            // Dealing with Operators ( + , - , * , / )
            else if(token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {

                if(OpStack.isEmpty() || OpStack.peek().equals("(") || funcNames.contains(OpStack.peek())) {
                    OpStack.push(token);
                }

                else if(opPrecedence.get(token) != null && (opPrecedence.get(token) <= opPrecedence.get(OpStack.peek()))) {
                    postFix.append(OpStack.pop() + " ");
                    OpStack.push(token);
                }

                else {
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
                //System.out.println("Adding the Operand");
                postFix.append(token + " ");
            }

        } }

        System.out.println(postFix.toString());
    }

}
