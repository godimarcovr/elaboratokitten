package javaBytecodeGenerator;

import java.util.Set;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;
import types.ClassMemberSignature;
import types.ClassType;
import types.FixtureSignature;
import types.TestSignature;

/**
 * A Java bytecode generator. It transforms the Kitten intermediate language
 * into Java bytecode that can be dumped to Java class files and run.
 * It uses the BCEL library to represent Java classes and dump them on the file-system.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

@SuppressWarnings("serial")
public class TestClassGenerator extends JavaClassGenerator {

	/**
	 * Builds a class generator for the given class type.
	 *
	 * @param clazz the class type
	 * @param sigs a set of class member signatures. These are those that must be
	 *             translated. If this is {@code null}, all class members are translated
	 */

	public TestClassGenerator(ClassType clazz, Set<ClassMemberSignature> sigs) {
		super(clazz.getName()+"Test", // name of the class
			// the superclass of the Kitten Object class is set to be the Java java.lang.Object class
			"java.lang.Object",
			clazz.getName() + ".kit", // source file
			Constants.ACC_PUBLIC, // Java attributes: public!
			noInterfaces, // no interfaces
			new ConstantPoolGen()); // empty constant pool, at the beginning

		
		for (FixtureSignature fs:clazz.fixturesLookup()){
			if(sigs == null || sigs.contains(fs)){
				fs.createFixture(this);
			}
		}
		
		for (TestSignature ts:clazz.getTests()){
			if(sigs == null || sigs.contains(ts)){
				ts.createTest(this);
			}
		}
		
		//crea main
		//this nelle fixture e test diventa argomento?
	}
}