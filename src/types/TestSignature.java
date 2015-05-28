package types;

import javaBytecodeGenerator.TestClassGenerator;
import translation.Block;
import absyn.CodeDeclaration;

public class TestSignature extends CodeSignature {

	public TestSignature(ClassType clazz,String name, CodeDeclaration abstractSyntax) {
		super(clazz, VoidType.INSTANCE,TypeList.EMPTY, name, abstractSyntax);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}
	
	public void createTest(TestClassGenerator classGen){
		
	}

}
