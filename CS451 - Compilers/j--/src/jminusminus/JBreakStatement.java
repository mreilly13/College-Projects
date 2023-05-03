// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * An AST node for a break-statement.
 */
public class JBreakStatement extends JStatement {

    // the enclosing statement for this break statement
    JStatement enclosingStatement;

    /**
     * Constructs an AST node for a break-statement.
     *
     * @param line line in which the break-statement occurs in the source file.
     */
    public JBreakStatement(int line) {
        super(line);
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        enclosingStatement = JMember.enclosingStatement.peek();
        if (enclosingStatement instanceof JDoStatement) {
            ((JDoStatement) enclosingStatement).hasBreak();
        } else if (enclosingStatement instanceof JWhileStatement) {
            ((JWhileStatement) enclosingStatement).hasBreak();
        } else if (enclosingStatement instanceof JForStatement) {
            ((JForStatement) enclosingStatement).hasBreak();
        } else if (enclosingStatement instanceof JSwitchStatement) {
            ((JSwitchStatement) enclosingStatement).hasBreak();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (enclosingStatement instanceof JDoStatement) {
            output.addBranchInstruction(GOTO, ((JDoStatement) enclosingStatement).breakLabel());
        } else if (enclosingStatement instanceof JWhileStatement) {
            output.addBranchInstruction(GOTO, ((JWhileStatement) enclosingStatement).breakLabel());
        } else if (enclosingStatement instanceof JForStatement) {
            output.addBranchInstruction(GOTO, ((JForStatement) enclosingStatement).breakLabel());
        } else if (enclosingStatement instanceof JSwitchStatement) {
            output.addBranchInstruction(GOTO, ((JSwitchStatement) enclosingStatement).breakLabel());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JBreakStatement:" + line, e);
    }
}
