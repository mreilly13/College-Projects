// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a do-statement.
 */
public class JDoStatement extends JStatement {
    // Body.
    private JStatement body;

    // Test expression.
    private JExpression condition;

    // whether this structure contains a break
    private boolean hasBreak;

    // the label for a potential break statement
    private String breakLabel;

    // whether this structure contains a continue
    private boolean hasContinue;

    // the label for a potential continue statement
    private String continueLabel;

    /**
     * Constructs an AST node for a do-statement.
     *
     * @param line      line in which the do-statement occurs in the source file.
     * @param body      the body.
     * @param condition test expression.
     */
    public JDoStatement(int line, JStatement body, JExpression condition) {
        super(line);
        this.body = body;
        this.condition = condition;
        this.hasBreak = false;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line, Type.BOOLEAN);
        body = (JStatement) body.analyze(context);
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        String topLoop = output.createLabel();
        if (hasBreak) {
            breakLabel = output.createLabel();
        }
        if (hasContinue) {
            continueLabel = output.createLabel();
        }
        output.addLabel(topLoop);
        body.codegen(output);
        if (hasContinue) {
            output.addLabel(continueLabel);
        }
        condition.codegen(output, topLoop, true);
        if (hasBreak) {
            output.addLabel(breakLabel);
        }
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
        json.addChild("JDoStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Body", e1);
        body.toJSON(e1);
        JSONElement e2 = new JSONElement();
        e.addChild("Condition", e2);
        condition.toJSON(e2);
    }
}
