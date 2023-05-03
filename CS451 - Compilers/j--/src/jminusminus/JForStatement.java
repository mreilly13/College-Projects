// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a for-statement.
 */
class JForStatement extends JStatement {
    // Initialization.
    private ArrayList<JStatement> init;

    // Test expression
    private JExpression condition;

    // Update.
    private ArrayList<JStatement> update;

    // The body.
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
     * Constructs an AST node for a for-statement.
     *
     * @param line      line in which the for-statement occurs in the source file.
     * @param init      the initialization.
     * @param condition the test expression.
     * @param update    the update.
     * @param body      the body.
     */
    public JForStatement(int line, ArrayList<JStatement> init, JExpression condition,
                         ArrayList<JStatement> update, JStatement body) {
        super(line);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
        this.hasBreak = false;
    }

    /**
     * {@inheritDoc}
     */
    public JForStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
        LocalContext localContext = new LocalContext(context);
        if (init != null) {
            for (JStatement stmt : init) {
                stmt.analyze(localContext);
            }
        }
        if (condition != null) {
            condition.analyze(localContext);
            condition.type().mustMatchExpected(line, Type.BOOLEAN);
        }
        if (update != null) {
            for (JStatement stmt : update) {
                stmt.analyze(localContext);
            }
        }
        body.analyze(localContext);
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
            continueLabel = output.createLabel();
        }
        if (init != null) {
            for (JStatement stmt : init) {
                stmt.codegen(output);
            }
        }
        output.addLabel(test);
        if (condition != null) {
            condition.codegen(output, out, false);
        }
        body.codegen(output);
        if (hasContinue) {
            output.addLabel(continueLabel);
        }
        if (update != null) {
            for (JStatement stmt : update) {
                stmt.codegen(output);
            }
        }
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
        json.addChild("JForStatement:" + line, e);
        if (init != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Init", e1);
            for (JStatement stmt : init) {
                stmt.toJSON(e1);
            }
        }
        if (condition != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Condition", e1);
            condition.toJSON(e1);
        }
        if (update != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Update", e1);
            for (JStatement stmt : update) {
                stmt.toJSON(e1);
            }
        }
        if (body != null) {
            JSONElement e1 = new JSONElement();
            e.addChild("Body", e1);
            body.toJSON(e1);
        }
    }
}
