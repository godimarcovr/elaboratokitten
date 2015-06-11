package types;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.MethodGen;

import bytecode.CONST;
import bytecode.RETURN;
import javaBytecodeGenerator.TestClassGenerator;
import translation.Block;
import absyn.CodeDeclaration;

public class TestSignature extends CodeSignature {

	public TestSignature(ClassType clazz, String name,
			CodeDeclaration abstractSyntax) {
		super(clazz, VoidType.INSTANCE, TypeList.EMPTY.push(clazz), name, abstractSyntax);
	}

	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}

	public void createTest(TestClassGenerator classGen) {
		// i test della classe C diventano metodi privati statici in CTest
		// con un parametro di tipo C
		//this.getCode().
		
		MethodGen methodGen = new MethodGen(Constants.ACC_PRIVATE
				| Constants.ACC_STATIC, // public
				ClassType.mk("String").toBCEL(), // return type
				this.getParameters().toBCEL(), // parameters types, if any
				null, // parameters names: we do not care
				getName(), // method's name
				classGen.getClassName(), // defining class
				classGen.generateJavaBytecode(getCode()), // bytecode of the
															// method
				classGen.getConstantPool()); // constant pool
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// we add a method to the class that we are generating
		classGen.addMethod(methodGen.getMethod());
	}

}
