package absyn;

import types.Type;
import types.NumericalType;
import types.CodeSignature;
import bytecode.BinOpBytecode;
import bytecode.DIV;

/**
 * A node of abstract syntax representing the division of two expressions.
 *
 * @author  <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public class Division extends ArithmeticBinOp {

    /**
     * Constructs the abstract syntax of the division of two expressions.
     *
     * @param pos the position in the source file where it starts
     *            the concrete syntax represented by this abstract syntax
     * @param left the abstract syntax of the left-hand side expression
     * @param right the abstract syntax of the right-hand side expression
     */

    public Division(int pos, Expression left, Expression right) {
	super(pos,left,right);
    }

    /**
     * A binary operation-specific bytecode which performs a binary
     * computation on the left and right sides of this binary operation.
     * Namely, a <tt>div</tt> bytecode.
     *
     * @param where the method or constructor where this expression occurs
     * @param type the type of the values of the left and right sides of this
     *             binary expression
     * @return a <tt>div</tt> bytecode
     */

    protected BinOpBytecode operator(CodeSignature where, Type type) {
	return new DIV(where,(NumericalType)type);
    }
}