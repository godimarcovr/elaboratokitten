package absyn;

import java.io.FileWriter;
import java.io.IOException;

import semantical.TypeChecker;
import types.ClassType;
import types.TestSignature;
import types.VoidType;

public class TestDeclaration extends CodeDeclaration {

	private final String name;
	
	public TestDeclaration(int pos, String name, Command body,ClassMemberDeclaration next) {
		super(pos, null, body, next);
		this.name=name;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("name", toDot(name, where), where);
		linkToNode("body", getBody().toDot(where), where);
	}

	@Override
	protected void addTo(ClassType clazz) {
		TestSignature ts=new TestSignature(clazz, this.name, this);
		clazz.addTest(ts);
		this.setSignature(ts);
	}

	@Override
	protected void typeCheckAux(ClassType currentClass) {
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, currentClass.getErrorMsg(),true);
		checker = checker.putVar("this", currentClass);
		getBody().typeCheck(checker);
		getBody().checkForDeadcode();
	}

}
