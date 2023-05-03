// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.GOTO;

/**
 * An AST node for a continue-statement.
 */
public class JContinueStatement extends JStatement {

    // the enclosing statement for this break statement
    JStatement enclosingStatement;

    /**
     * Constructs an AST node for a continue-statement.
     *
     * @param line line in which the continue-statement occurs in the source file.
     */
    public JContinueStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        if (enclosingStatement instanceof JDoStatement) {
            ((JDoStatement) enclosingStatement).hasContinue();
        } else if (enclosingStatement instanceof JWhileStatement) {
            ((JWhileStatement) enclosingStatement).hasContinue();
        } else if (enclosingStatement instanceof JForStatement) {
            ((JForStatement) enclosingStatement).hasContinue();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (enclosingStatement instanceof JDoStatement) {
            output.addBranchInstruction(GOTO, ((JDoStatement) enclosingStatement).continueLabel());
        } else if (enclosingStatement instanceof JWhileStatement) {
            output.addBranchInstruction(GOTO, ((JWhileStatement) enclosingStatement).continueLabel());
        } else if (enclosingStatement instanceof JForStatement) {
            output.addBranchInstruction(GOTO, ((JForStatement) enclosingStatement).continueLabel());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JContinueStatement:" + line, e);
    }
}
