/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/* Generated By:JJTree: Do not edit this line. ASTLetExpr.java */
/* JJT: 0.3pre1 */

package Mini;

import org.apache.bcel.Const;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

/**
 */
public class ASTLetExpr extends ASTExpr {
    public static Node jjtCreate(final MiniParser p, final int id) {
        return new ASTLetExpr(p, id);
    }

    private ASTIdent[] idents;
    private ASTExpr[] exprs;

    private ASTExpr body;

    // Generated methods
    ASTLetExpr(final int id) {
        super(id);
    }

    ASTLetExpr(final MiniParser p, final int id) {
        super(p, id);
    }

    /**
     * Fifth pass, produce Java byte code.
     */
    @Override
    public void byte_code(final InstructionList il, final MethodGen method, final ConstantPoolGen cp) {
        final int size = idents.length;
        final LocalVariableGen[] l = new LocalVariableGen[size];

        for (int i = 0; i < size; i++) {
            final String ident = idents[i].getName();
            final Variable entry = (Variable) env.get(ident);
            final Type t = BasicType.getType((byte) idents[i].getType());
            final LocalVariableGen lg = method.addLocalVariable(ident, t, null, null);
            final int slot = lg.getIndex();

            entry.setLocalVariable(lg);
            InstructionHandle start = il.getEnd();
            exprs[i].byte_code(il, method, cp);
            start = start == null ? il.getStart() : start.getNext();
            lg.setStart(start);
            il.append(new ISTORE(slot));
            ASTFunDecl.pop();
            l[i] = lg;
        }

        body.byte_code(il, method, cp);
        final InstructionHandle end = il.getEnd();
        for (int i = 0; i < size; i++) {
            l[i].setEnd(end);
        }
    }

    /**
     * Overrides ASTExpr.closeNode() Cast children nodes to appropriate types.
     */
    @Override
    public void closeNode() {
        int i; /*
                * length must be a multiple of two (ident = expr) + 1 (body expr)
                */
        final int len_2 = children.length / 2;
        idents = new ASTIdent[len_2];
        exprs = new ASTExpr[len_2];

        // At least one assignment is enforced by the grammar
        for (i = 0; i < len_2; i++) {
            idents[i] = (ASTIdent) children[i * 2];
            exprs[i] = (ASTExpr) children[i * 2 + 1];
        }

        body = (ASTExpr) children[children.length - 1]; // Last expr is the body
        children = null; // Throw away old reference
    }

    /**
     * Fifth pass, produce Java code.
     */
    @Override
    public void code(final StringBuffer buf) {
        for (int i = 0; i < idents.length; i++) {
            final String ident = idents[i].getName();
            final int t = idents[i].getType(); // can only be int

            /*
             * Idents have to be declared at start of function for later use. Each name is unique, so there shouldn't be a problem
             * in application.
             */
            exprs[i].code(buf);

            buf.append("    " + Const.getTypeName(t) + " " + ident + " = " + ASTFunDecl.pop() + ";\n");
        }

        body.code(buf);
    }

    @Override
    public void dump(final String prefix) {
        System.out.println(toString(prefix));

        for (int i = 0; i < idents.length; i++) {
            idents[i].dump(prefix + " ");
            exprs[i].dump(prefix + " ");
        }

        body.dump(prefix + " ");
    }

    /**
     * Second pass Overrides AstExpr.eval()
     *
     * @return type of expression
     * @param expected type
     */
    @Override
    public int eval(final int expected) {
        // is_simple = true;

        for (int i = 0; i < idents.length; i++) {
            final int t = exprs[i].eval(Const.T_UNKNOWN);

            idents[i].setType(t);
            // is_simple = is_simple && exprs[i].isSimple();
        }

        return type = body.eval(expected);
    }

    /**
     * Overrides ASTExpr.traverse()
     */
    @Override
    public ASTExpr traverse(final Environment env) {
        this.env = env;

        // Traverse RHS exprs first, so no references to LHS vars are allowed
        for (int i = 0; i < exprs.length; i++) {
            exprs[i] = exprs[i].traverse((Environment) env.clone());
        }

        // Put argument names into hash table aka. environment
        for (final ASTIdent id : idents) {
            final String name = id.getName();
            final EnvEntry entry = env.get(name);

            if (entry != null) {
                MiniC.addError(id.getLine(), id.getColumn(), "Redeclaration of " + entry + ".");
            } else {
                env.put(new Variable(id));
            }
        }

        body = body.traverse(env);

        return this;
    }

}
