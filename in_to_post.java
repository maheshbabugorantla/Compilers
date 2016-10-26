import java.util.*;
import java.lang.*;

public class in_to_post {

    public static void main(String[] args) {
        String infix_Str = "c + a*b + (a*b+c)/a + 20";

        String intStr = "A10";

        /*try {
            System.out.println(Integer.parseInt(infix_Str));
        }
        catch (NumberFormatException e) {
            System.out.println("Error");
        } */

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
        //InOrderTraversal(parsePostfixExpr(postFixStr.trim()));

//        Integer.valueOf("Mahesh");


        return;
    }

    public static String infix_to_Postfix(String infix_Str) {

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

                //System.out.println("Left: " + Node2.data);
                //System.out.println("Right: " + Node1.data);

                BSTNode opNode = new BSTNode(elements[index]); // Creating the Operator Node.

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

    public static void InOrderTraversal(BSTNode bstNodeTree) {

        if (bstNodeTree != null) {
            InOrderTraversal(bstNodeTree.left);
            System.out.println(bstNodeTree.data);
            InOrderTraversal(bstNodeTree.right);

        }

        return;
    }


    private static class BSTNode {

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