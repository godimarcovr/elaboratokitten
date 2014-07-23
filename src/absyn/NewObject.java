package absyn;

import java.io.FileWriter;
import java.util.HashSet;

import symbol.Symbol;
import types.ConstructorSignature;
import types.Type;
import types.TypeList;
import types.ClassType;
import types.KittenClassType;
import types.CodeSignature;
import semantical.TypeChecker;
import translate.CodeBlock;
import bytecode.NEW;
import bytecode.DUP;
import bytecode.CONSTRUCTORCALL;

/**
 * A node of abstract syntax representing the creation of an object.
 *
 * @author  <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public class NewObject extends Expression {

    /**
     * The name of the class which in instantiated.
     */

    private Symbol className;

    /**
     * The abstract syntax of the actual parameters passed to the constructor.
     */

    private ExpressionSeq actuals;

    /**
     * The signature of the constructor which is called upon creation.
     * This is <tt>null</tt> if type-checking has not been performed yet.
     */

    private ConstructorSignature constructor;

    /**
     * Constructs the abstract syntax of an object creation.
     *
     * @param pos the position in the source file where it starts
     *            the concrete syntax represented by this abstract syntax
     * @param className the name of the class which is instantiated
     * @param actuals the abstract syntax of the actual parameters passed
     *                to a constructor of <tt>className</tt>
     */

    public NewObject(int pos, Symbol className, ExpressionSeq actuals) {
	super(pos);

	this.className = className;
	this.actuals = actuals;
    }

    /**
     * Yields the name of the class which is instantiated.
     *
     * @return the name of the class which is instantiated
     */

    public Symbol getClassName() {
	return className;
    }

    /**
     * Yields the abstract syntax of the actual parameters passed to a
     * constructor of <tt>className</tt>.
     *
     * @return the abstract syntax of the actual parameters passed to a
     *         constructor of <tt>className</tt>.
     */

    public ExpressionSeq getActuals() {
	return actuals;
    }

    /**
     * Yields a the string labelling the class
     * of abstract syntax represented by
     * this node. We redefine this method in order to add the constructor
     * referenced by this object creation, if it has already been computed
     * by a type-checker.
     *
     * @return a string describing the kind of this node of abstract syntax,
     *         followed by the static type of the expression and by
     *         the constructor referenced by this object creation, if already
     *         computed by a type-checker
     */

    protected String label() {
	// if this expression has not been type-checked yet, we do not
	// report the constructor referenced
	if (constructor == null) return super.label();
	// otherwise we add the constructor referenced
	else return super.label() + "\\nreferences " + constructor;
    }

    /**
     * Adds abstract syntax class-specific information in the dot file
     * representing the abstract syntax of the object creation.
     * This amounts to adding arcs from the node for the object creation
     * to the abstract syntax for its
     * <tt>className</tt> and <tt>actuals</tt> components.
     *
     * @param where the file where the dot representation must be written
     */

    protected void toDot$0(FileWriter where) throws java.io.IOException {
	linkToNode("className",className.toDot(where),where);
	if (actuals != null) linkToNode("actuals",actuals.toDot(where),where);
    }

    /**
     * Performs the type-checking of an object creation expression
     * by using a given type-checker. It type-checks the
     * formal parameters of the call. Then it checks that
     * there is exactly one possible target constructor.
     * It returns the semantical class type <tt>className</tt>.
     *
     * @param checker the type-checker to be used for type-checking
     * @return the semantical class type <tt>className</tt>
     */

    protected Type typeCheck$0(TypeChecker checker) {
	ClassType target = KittenClassType.mk(className);

	// <tt>target</tt> might be an open class type if <tt>className</tt>
	// could not be found or contained a syntax error
	if (target instanceof KittenClassType)
	    ((KittenClassType)target).typeCheck();

	TypeList actualsTypes = actuals != null ?
	    actuals.typeCheck(checker) : TypeList.EMPTY;

	// we collect the set of constructors which are compatible
	// with the static types of the parameters, and have no other
	// compatible constructor which is more specific than them
	HashSet constructors = target.constructorsLookup(actualsTypes);

	if (constructors.isEmpty())
	    // there is no matching constructor!
	    error("no matching constructor for \"" + className + "\"");
	else if (constructors.size() >= 2)
	    // more than two matching constructors, and none of them is
	    // more specific of the other? Ambiguous call
	    error("call to constructor of \"" + className + "\" is ambiguous");
	else
	    // there is only one candidate, that's fine
	    constructor = (ConstructorSignature)constructors.iterator().next();

	// we return the semantical class type <tt>className</tt>
	return target;
    }

    /**
     * Translates this command into intermediate
     * Kitten bytecode. Namely, it returns a code which creates a new
     * object of class <tt>className</tt> and then calls the constructor
     * identified during the type-checking. Namely, it returns the code
     * <br>
     * <tt>new constructor.getDefiningClass()</tt><br>
     * <tt>dup</tt><br>
     * <i>translation of the parameters of the constructor</i><br>
     * <tt>constructorcall constructor</tt><br>
     * <br>
     * followed by the given <tt>continuation</tt>.
     * The original stack elements are not modified.
     *
     * @param where the method or constructor where this expression occurs
     * @param continuation the continuation to be executed after this command
     * @return the code executing this command and then
     *         the <tt>continuation</tt>
     */

    public CodeBlock translate(CodeSignature where, CodeBlock continuation) {
	continuation = new CONSTRUCTORCALL(where,constructor)
	    .followedBy(continuation);

	if (actuals != null)
	    continuation = actuals.translateAs
		(where,constructor.getParameters(),continuation);

	return new NEW(where,constructor.getDefiningClass()).followedBy
	    (new DUP(where,constructor.getDefiningClass())
	     .followedBy(continuation));
    }
}