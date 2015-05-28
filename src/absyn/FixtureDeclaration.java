package absyn;

import java.io.FileWriter;
import semantical.TypeChecker;
import types.ClassType;
import types.FixtureSignature;
import types.VoidType;

public class FixtureDeclaration extends CodeDeclaration {

	public FixtureDeclaration(int pos, Command body,ClassMemberDeclaration next) {
		super(pos, null, body, next);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("body", getBody().toDot(where), where);
	}

	@Override
	protected void addTo(ClassType clazz) {
		FixtureSignature fs = new FixtureSignature(clazz, this);
		clazz.addFixture(fs);
		this.setSignature(fs);
	}

	@Override
	protected void typeCheckAux(ClassType currentClass) {
		TypeChecker checker = new TypeChecker(VoidType.INSTANCE, currentClass.getErrorMsg());
		checker = checker.putVar("this", currentClass);
		getBody().typeCheck(checker);
		getBody().checkForDeadcode();
	}

}
