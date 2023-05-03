// Copyright 2012- Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package jminusminus;

import java.util.ArrayList;

import static jminusminus.CLConstants.*;

/**
 * The AST node for a try-catch-finally statement.
 */
class JTryStatement extends JStatement {
    // The try block.
    private JBlock tryBlock;

    // The catch parameters.
    private ArrayList<JFormalParameter> parameters;

    // The catch blocks.
    private ArrayList<JBlock> catchBlocks;

    // The finally block.
    private JBlock finallyBlock;

    /**
     * Constructs an AST node for a try-statement.
     *
     * @param line         line in which the while-statement occurs in the source file.
     * @param tryBlock     the try block.
     * @param parameters   the catch parameters.
     * @param catchBlocks  the catch blocks.
     * @param finallyBlock the finally block.
     */
    public JTryStatement(int line, JBlock tryBlock, ArrayList<JFormalParameter> parameters,
                         ArrayList<JBlock> catchBlocks, JBlock finallyBlock) {
        super(line);
        this.tryBlock = tryBlock;
        this.parameters = parameters;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;
    }

    /**
     * {@inheritDoc}
     */
    public JTryStatement analyze(Context context) {
        tryBlock.analyze(context);
        for (int i = 0; i < parameters.size(); i++) {
            LocalContext localContext = new LocalContext(context);
            JFormalParameter param = parameters.get(i);
            LocalVariableDefn defn = new LocalVariableDefn(param.type(), localContext.nextOffset());
            defn.initialize();
            defn.type().setClassRep(Exception.class);
            localContext.addEntry(param.line(), param.name(), defn);
            catchBlocks.get(i).analyze(localContext);
        }
        if (finallyBlock != null) {
            LocalContext localContext = new LocalContext(context);
            finallyBlock.analyze(localContext);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void codegen(CLEmitter output) {
        /*
        String tryLabel = output.createLabel();
        output.addLabel(tryLabel);
        tryBlock.codegen(output);
        if (finallyBlock != null) {
            finallyBlock.codegen(output);
            String endFinally = output.createLabel();
            output.addBranchInstruction(GOTO, endFinally);
        }
        String endTry = output.createLabel();
        output.addLabel(endTry);
        for (int i = 0; i < parameters.size(); i++) {
            String startCatch = output.createLabel();
            output.addLabel(startCatch);
            parameters.get(i).codegen(output);
            catchBlocks.get(i).codegen(output);
            String endCatch = output.createLabel();
            output.addLabel(endCatch);
            output.addExceptionHandler(startCatch, endCatch, endTry, parameters.get(i).name());
            if (finallyBlock != null) {
                finallyBlock.codegen(output);
                String endFinally = output.createLabel();
                output.addBranchInstruction(GOTO, endFinally);
            }
        }
        if (finallyBlock != null) {
            String startFinally = output.createLabel();
            output.addOneArgInstruction(ASTORE, ...);
            finallyBlock.codegen(output);
            String endFinally = output.createLabel();
            output.addBranchInstruction(GOTO, endFinally);
        }
        */
    }

    /**
     * {@inheritDoc}
     */
    public void toJSON(JSONElement json) {
        JSONElement e = new JSONElement();
        json.addChild("JTryStatement:" + line, e);
        JSONElement e1 = new JSONElement();
        e.addChild("TryBlock", e1);
        tryBlock.toJSON(e1);
        if (catchBlocks != null) {
            for (int i = 0; i < catchBlocks.size(); i++) {
                JFormalParameter param = parameters.get(i);
                JBlock catchBlock = catchBlocks.get(i);
                JSONElement e2 = new JSONElement();
                e.addChild("CatchBlock", e2);
                String s = String.format("[\"%s\", \"%s\"]", param.name(), param.type() == null ?
                        "" : param.type().toString());
                e2.addAttribute("parameter", s);
                catchBlock.toJSON(e2);
            }
        }
        if (finallyBlock != null) {
            JSONElement e2 = new JSONElement();
            e.addChild("FinallyBlock", e2);
            finallyBlock.toJSON(e2);
        }
    }
}
