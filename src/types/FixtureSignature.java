package types;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.MethodGen;

import javaBytecodeGenerator.TestClassGenerator;
import translation.Block;
import absyn.CodeDeclaration;

public class FixtureSignature extends CodeSignature {
	private static int fixcounter=0;
	public FixtureSignature(ClassType clazz, CodeDeclaration abstractSyntax) {
		super(clazz, VoidType.INSTANCE, TypeList.EMPTY, "FIXTURE"+fixcounter++, abstractSyntax);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}
	
	public void createFixture(TestClassGenerator classGen){
		MethodGen methodGen = new MethodGen
				(Constants.ACC_PRIVATE | Constants.ACC_STATIC, // public
				getReturnType().toBCEL(), // return type
				getParameters().toBCEL(), // parameters types, if any
				null, // parameters names: we do not care
				getName().toString(), // method's name
				classGen.getClassName(), // defining class
				classGen.generateJavaBytecode(getCode()), // bytecode of the method
				classGen.getConstantPool()); // constant pool
		
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// we add a method to the class that we are generating
		classGen.addMethod(methodGen.getMethod());
	}

}
