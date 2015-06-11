package absyn;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.generic.LDC;

import bytecode.CONST;
import bytecode.NEWSTRING;
import bytecode.RETURN;
import semantical.TypeChecker;
import translation.Block;
import types.BooleanType;
import types.ClassMemberSignature;
import types.ClassType;
import types.TestSignature;
import types.VoidType;

public class TestDeclaration extends CodeDeclaration {

	private final String name;

	public TestDeclaration(int pos, String name, Command body,
			ClassMemberDeclaration next) {
		super(pos, null, body, next);
		this.name = name;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("name", toDot(name, where), where);
		linkToNode("body", getBody().toDot(where), where);
	}

	@Override
	protected void addTo(ClassType clazz) {
		TestSignature ts = new TestSignature(clazz, this.name, this);
		clazz.addTest(ts);
		this.setSignature(ts);
	}

	@Override
	protected void typeCheckAux(ClassType currentClass) {
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE,
				currentClass.getErrorMsg(), true);
		checker = checker.putVar("this", currentClass);
		getBody().typeCheck(checker);
		getBody().checkForDeadcode();
	}

	@Override
	public void translate(Set<ClassMemberSignature> done) {

		if (done.add(this.getSignature())) {
			this.process(this.getSignature().getDefiningClass(), done);
			// we translate the body of the constructor or
			// method with a block containing RETURN as continuation. This way,
			// all methods returning void and
			// with some missing return command are correctly
			// terminated anyway. If the method is not void, this
			// precaution is useless since we know that every execution path
			// ends with a return command, as guaranteed by
			// checkForDeadCode() (see typeCheck() in MethodDeclaration.java)
			
			//ritorna stringa vuota se il test e passato (arrivo in fondo superando tutti gli assert
			this.getSignature().setCode(getBody().translate(this.getSignature(),
					new NEWSTRING("").followedBy(new Block(new RETURN(ClassType.mk("String"))))));

			// we translate all methods and constructors that are referenced
			// from the code we have generated
			translateReferenced(this.getSignature().getCode(), done, new HashSet<Block>());
		}

	}

}
