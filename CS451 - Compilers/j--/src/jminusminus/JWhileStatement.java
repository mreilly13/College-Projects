// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a while-statement.
 */
class JWhileStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // Body.
    private JStatement body;

    // whether this structure contains a break
    private boolean hasBreak;

    // the label for a potential break statement
    private String breakLabel;

    // whether this structure contains a continue
    private boolean hasContinue;

    // the label for a potential continue statement
    private String continueLabel;

    /**
     * Constructs an AST node for a while-statement.
     *
     * @param line      line in which the while-statement occurs in the source file.
     * @param condition test expression.
     * @param body      the body.
     */
    public JWhileStatement(int line, JExpression condition, JStatement body) {
        super(line);
        this.condition = condition;
        this.body = body;
        this.hasBreak = false;
    }

    /**
     * {@inheritDoc}
     */
    public JWhileStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line(), Type.BOOLEAN);
        body = (JStatement) body.analyze(context);
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String test = output.createLabel();
        String out = output.createLabel();
        if (hasBreak) {
            breakLabel = out;
        }
        if (hasContinue) {
            continueLabel = test;
        }
        output.addLabel(test);
        condition.codegen(output, out, false);
        body.codegen(output);
        output.addBranchInstruction(GOTO, test);
        output.addLabel(out);
    }

    /**
     * Sets hasBreak to true.
     */
    public void hasBreak() {
        hasBreak = true;
    }

    /**
     * Returns the breakLabel for this statement.
     * 
     * @return the breaklabel for this statement.
     */
    public String breakLabel() {
        return breakLabel;
    }

    /**
     * Sets hasContinue to true.
     */
    public void hasContinue() {
        hasContinue = true;
    }

    /**
     * Returns the continueLabel for this statement.
     * 
     * @return the continuelabel for this statement.
     */
    public String continueLabel() {
        return continueLabel;
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JWhileStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Body", e2);
        body.toJSON(e2);
    }
}
