package absyn;

import java.io.FileWriter;

import types.CodeSignature;
import semantical.TypeChecker;
import translate.CodeBlock;

/**
 * A node of abstract syntax representing a sequence of two commands.
 * Local declarations are not visible after the sequence.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public class CommandSeq extends Command {

    /**
     * The first command in the sequence.
     */

    private Command first;

    /**
     * The second command in the sequence.
     */

    private Command second;

    /**
     * Constructs the abstract syntax of a sequence of two commands.
     *
     * @param pos the position in the source file where it starts
     *            the concrete syntax represented by this abstract syntax
     * @param first the <i>first</i> component of the sequence
     * @param second the <i>second</i> component of the sequence
     */

    public CommandSeq(int pos, Command first, Command second) {
	super(pos);

	this.first = first;
	this.second = second;
    }

    /**
     * Adds abstract syntax class-specific information in the dot file
     * representing the abstract syntax of a sequence of two commands.
     * This amounts to adding arcs from the node for the sequence
     * command to the abstract syntax for its <tt>first</tt> and
     * <tt>second</tt> components.
     *
     * @param where the file where the dot representation must be written
     */

    protected void toDot$0(FileWriter where) throws java.io.IOException {
	linkToNode("first",first.toDot(where),where);
	linkToNode("second",second.toDot(where),where);
    }

    /**
     * Performs the type-checking of the sequence of two commands
     * by using a given type-checker. It type-checks the second command in
     * the type-checker resulting from the type-checking of the first command.
     * It returns the type-checker resulting from the type-checking of the
     * second command.
     *
     * @param checker the type-checker to be used for type-checking
     * @return the type-checker resulting from the type-checking of the
     *         second command
     */

    protected TypeChecker typeCheck$0(TypeChecker checker) {
	return second.typeCheck(first.typeCheck(checker));
    }

    /**
     * Checks that this conditional does not contain <i>dead-code</i>, that is,
     * commands which can never be executed. It checks that there is no
     * dead-code in any of the two commands. If the first command ends with
     * a <tt>return</tt>, it signals an error.
     *
     * @return true if and only if every execution path in both branches of the
     *         conditional ends with a <tt>return</tt> command
     */

    public boolean checkForDeadcode() {
	if (first.checkForDeadcode()) error("dead-code after this statement");

	return second.checkForDeadcode();
    }

    /**
     * Translates this command into intermediate
     * Kitten bytecode. Namely, it translate the first command by using
     * as continuation the translation of the second command using
     * the given <tt>continuation</tt>.
     *
     * @param where the method or constructor where this expression occurs
     * @param continuation the continuation to be executed after this sequence
     *        of commands
     * @return the code executing this sequence of commands and then
     *         the <tt>continuation</tt>
     */

    public CodeBlock translate(CodeSignature where, CodeBlock continuation) {
	return first.translate(where,second.translate(where,continuation));
    }
}