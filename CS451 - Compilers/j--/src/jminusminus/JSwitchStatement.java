// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;
import java.util.TreeMap;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a switch-statement.
 */
public class JSwitchStatement extends JStatement {
    // Test expression.
    private JExpression condition;

    // List of switch-statement groups.
    private ArrayList<SwitchStatementGroup> stmtGroup;

    // whether this structure contains a break
    private boolean hasBreak;

    // the label for a potential break statement
    private String breakLabel;

    /**
     * Constructs an AST node for a switch-statement.
     *
     * @param line      line in which the switch-statement occurs in the source file.
     * @param condition test expression.
     * @param stmtGroup list of statement groups.
     */
    public JSwitchStatement(int line, JExpression condition,
                            ArrayList<SwitchStatementGroup> stmtGroup) {
        super(line);
        this.condition = condition;
        this.stmtGroup = stmtGroup;
        this.hasBreak = false;
    }

    /**
     * {@inheritDoc}
     */
    public JStatement analyze(Context context) {
        JMember.enclosingStatement.push(this);
        condition = condition.analyze(context);
        condition.type().mustMatchExpected(line, Type.INT);
        for (SwitchStatementGroup stmts : stmtGroup) {
            for (JExpression label : stmts.switchLabels()) {
                if (label != null) {
                    label.analyze(context);
                    if (!(label instanceof JLiteralInt)) {
                        JAST.compilationUnit.reportSemanticError(line, "Cases must be int literals");
                    }
                }
            }
        }
        LocalContext localContext = new LocalContext(context);
        for (SwitchStatementGroup stmts : stmtGroup) {
            for (JStatement stmt : stmts.block()) {
                stmt.analyze(localContext);
            }
        }
        JMember.enclosingStatement.pop();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        if (hasBreak) {
            breakLabel = output.createLabel();
        }
        int nLabels = 0;
        int lo = Integer.MAX_VALUE;
        int hi = Integer.MIN_VALUE;
        int val;
        for (SwitchStatementGroup stmts : stmtGroup) {
            for (JExpression label : stmts.switchLabels()) {
                if (label != null) {
                    nLabels++;
                    val = ((JLiteralInt) label).toInt();
                    if (val < lo) {
                        lo = val;
                    }
                    if (val > hi) {
                        hi = val;
                    }
                }
            }
        }
        long tableSpaceCost = 5 + hi - lo;
        long tableTimeCost = 3;
        long lookupSpaceCost = 3 + 2 * nLabels;
        long lookupTimeCost = nLabels;
        int opcode = nLabels > 0 && 
                (tableSpaceCost + 3 * tableTimeCost <= lookupSpaceCost + 3 * lookupTimeCost) ?
                TABLESWITCH : LOOKUPSWITCH;
        condition.codegen(output);
        if (opcode == TABLESWITCH) {
            ArrayList<String> labels = new ArrayList<String>();
            for (int i = lo; i <= hi; i++) {
                labels.add(output.createLabel());
            }
            String defaultLabel = output.createLabel();
            output.addTABLESWITCHInstruction(defaultLabel, lo, hi, labels);
            boolean hasDefault = false;
            int index = 0;
            for (SwitchStatementGroup stmts : stmtGroup) {
                JStatement lastStmt = stmts.switchLabels().get(stmts.switchLabels().size() - 1);
                boolean hasGap = false;
                String lastLabel = null;
                if (lastStmt != null) {
                    lastLabel = labels.get(((JLiteralInt) lastStmt).toInt() - lo);
                }
                for (JStatement label : stmts.switchLabels()) {
                    if (label != null) {
                        int caseVal = ((JLiteralInt) label).toInt();
                        if (hasGap) {
                            output.addBranchInstruction(GOTO, lastLabel);
                        }
                        while (index < caseVal - lo) {
                            hasGap = true;
                            output.addLabel(labels.get(index++));
                            output.addBranchInstruction(GOTO, defaultLabel);
                        }
                        output.addLabel(labels.get(index++));
                    } else {
                        output.addLabel(defaultLabel);
                        hasDefault = true;
                    }
                }
                for (JStatement stmt : stmts.block()) {
                    stmt.codegen(output);
                }
            }
            if (!hasDefault) {
                output.addLabel(defaultLabel);
            }
        } else {
            TreeMap<Integer, String> matchLabelPairs = new TreeMap<Integer, String>();
            for (SwitchStatementGroup stmts : stmtGroup) {
                for (JStatement label : stmts.switchLabels()) {
                    if (label != null) {
                        int caseVal = ((JLiteralInt) label).toInt();
                        matchLabelPairs.put(caseVal, output.createLabel());
                    }
                }
            }
            String defaultLabel = output.createLabel();
            output.addLOOKUPSWITCHInstruction(defaultLabel, matchLabelPairs.size(), 
                    matchLabelPairs);
            for (SwitchStatementGroup stmts : stmtGroup) {
                for (JStatement label : stmts.switchLabels()) {
                    if (label != null) {
                        int caseVal = ((JLiteralInt) label).toInt();
                        output.addLabel(matchLabelPairs.get(caseVal));
                    } else {
                        output.addLabel(defaultLabel);
                    }
                }
                for (JStatement stmt : stmts.block()) {
                    stmt.codegen(output);
                }
            }
        }
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
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JSwitchStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("Condition", e1);
        condition.toJSON(e1);
        for (SwitchStatementGroup group : stmtGroup) {
            group.toJSON(e);
        }
    }
}

/**
 * A switch statement group consists of case labels and a block of statements.
 */
class SwitchStatementGroup {
    // Case labels.
    private ArrayList<JExpression> switchLabels;

    // Block of statements.
    private ArrayList<JStatement> block;

    /**
     * Constructs a switch-statement group.
     *
     * @param switchLabels case labels.
     * @param block        block of statements.
     */
    public SwitchStatementGroup(ArrayList<JExpression> switchLabels, ArrayList<JStatement> block) {
        this.switchLabels = switchLabels;
        this.block = block;
    }

    /**
     * Returns the switchLabels from this SwitchStatementGroup.
     *
     * @return the switchLabels from this SwitchStatementGroup.
     */
    public ArrayList<JExpression> switchLabels() {
        return switchLabels;
    }

    /**
     * Returns the block of this SwitchStatementGroup.
     *
     * @return the block of this SwitchStatementGroup.
     */
    public ArrayList<JStatement> block() {
        return block;
    }

    /**
     * Stores information about this switch statement group in JSON format.
     *
     * @param json the JSON emitter.
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("SwitchStatementGroup", e);
        for (JExpression label : switchLabels) {
            JSONElement e1 = new JSONElement();
            if (label != null) {
                e.addChild("Case", e1);
                label.toJSON(e1);
            } else {
                e.addChild("Default", e1);
            }
        }
        if (block != null) {
            for (JStatement stmt : block) {
                stmt.toJSON(e);
            }
        }
    }
}
