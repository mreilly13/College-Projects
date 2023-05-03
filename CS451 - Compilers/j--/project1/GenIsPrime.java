import java.util.ArrayList;

import jminusminus.CLEmitter;

import static jminusminus.CLConstants.*;

/**
 * This class programmatically generates the class file for the following Java application:
 * 
 * <pre>
 * public class IsPrime {
 *     // Entry point.
 *     public static void main(String[] args) {
 *         int n = Integer.parseInt(args[0]);
 *         boolean result = isPrime(n);
 *         if (result) {
 *             System.out.println(n + " is a prime number");
 *         } else {
 *             System.out.println(n + " is not a prime number");
 *         }
 *     }
 *
 *     // Returns true if n is prime, and false otherwise.
 *     private static boolean isPrime(int n) {
 *         if (n < 2) {
 *             return false;
 *         }
 *         for (int i = 2; i <= n / i; i++) {
 *             if (n % i == 0) {
 *                 return false;
 *             }
 *         }
 *         return true;
 *     }
 * }
 * </pre>
 */
public class GenIsPrime {
    public static void main(String[] args) {
        CLEmitter e = new CLEmitter(true);
        
        // Create an ArrayList instance to store modifiers
        ArrayList<String> modifiers = new ArrayList<String>();

        // public class IsPrime {
        modifiers.add("public");
        e.addClass(modifiers, "IsPrime", "java/lang/Object", null, true);

        // public static void main(String[] args) {
        modifiers.clear();
        modifiers.add("public");
        modifiers.add("static");
        e.addMethod(modifiers, "main", "([Ljava/lang/String;)V", null, true);

        // int n = Integer.parseInt(args[0]);
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt",
                "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_1);

        // Get System.out on stack
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

        // Create an intance (say sb) of StringBuffer on stack for string concatenations
        //    sb = new StringBuffer();
        e.addReferenceInstruction(NEW, "java/lang/StringBuffer");
        e.addNoArgInstruction(DUP);
        e.addMemberAccessInstruction(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");

        // sb.append(n);
        e.addNoArgInstruction(ILOAD_1);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(I)Ljava/lang/StringBuffer;");
        
        // Perform isPrime calculation
        e.addNoArgInstruction(ILOAD_1);
        e.addMemberAccessInstruction(INVOKESTATIC, "IsPrime", "isPrime", "(I)I");
        e.addBranchInstruction(IFEQ, "NotPrime");
        
        // True branch
        e.addLDCInstruction(" is a prime number");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        e.addBranchInstruction(GOTO, "Finish");

        // False branch        
        e.addLabel("NotPrime");
        e.addLDCInstruction(" is not a prime number");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        
        // System.out.println(sb.toString());
        e.addLabel("Finish");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/lang/StringBuffer",
                "toString", "()Ljava/lang/String;");
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V");
        
        // return;
        e.addNoArgInstruction(RETURN);

        // private static boolean isPrime(int n) {
        modifiers.clear();
        modifiers.add("private");
        modifiers.add("static");
        e.addMethod(modifiers, "isPrime", "(I)I", null, true);
        
        // if (n < 2) return false
        e.addNoArgInstruction(ILOAD_0);
        e.addNoArgInstruction(ICONST_2);
        e.addBranchInstruction(IF_ICMPGE, "CheckFactors");
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(IRETURN);

        // Set up check for primality
        e.addLabel("CheckFactors");
        e.addNoArgInstruction(ICONST_2);
        e.addNoArgInstruction(ISTORE_1);

        // Perform check for primality
        e.addLabel("CheckLoop");
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ILOAD_0);
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(IDIV);
        e.addBranchInstruction(IF_ICMPGT, "Prime");
        e.addNoArgInstruction(ILOAD_0);
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(IREM);
        e.addBranchInstruction(IFNE, "Iterate");
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(IRETURN);

        e.addLabel("Iterate");
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ICONST_1);
        e.addNoArgInstruction(IADD);
        e.addNoArgInstruction(ISTORE_1);
        e.addBranchInstruction(GOTO, "CheckLoop");

        // return true
        e.addLabel("Prime");
        e.addNoArgInstruction(ICONST_1);
        e.addNoArgInstruction(IRETURN);

        // Write IsPrime.class to file system
        e.write();
    }
}
