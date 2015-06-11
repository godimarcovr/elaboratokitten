package javaBytecodeGenerator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.BCELifier;

import bytecode.BytecodeList;
import bytecode.NOP;
import bytecode.RETURN;
import absyn.MethodDeclaration;
import translation.Block;
import types.BooleanType;
import types.ClassMemberSignature;
import types.ClassType;
import types.CodeSignature;
import types.FixtureSignature;
import types.IntType;
import types.MethodSignature;
import types.TestSignature;
import types.TypeList;
import types.VoidType;

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
		this.createTestMain(clazz);
	}
	
	private void createTestMain(ClassType clazz){
		Type strType=Type.getType(String.class);
		Type arg_str[]={strType};
		final int VAR_PASSED=1, VAR_FAILED=2, VAR_TIME=3, VAR_TOPRINT=4, VAR_SUPPORTOBJECT=5;
		
		InstructionList il=new InstructionList();
		//int passed=0
		il.append(InstructionFactory.ICONST_0);
		il.append(InstructionFactory.createStore(Type.INT, VAR_PASSED));
		//int failed=0
		il.append(InstructionFactory.ICONST_0);
		il.append(InstructionFactory.createStore(Type.INT, VAR_FAILED));
		//long startTest=0
		il.append(InstructionFactory.LCONST_0);
		il.append(InstructionFactory.createStore(Type.LONG, VAR_TIME));
		//long startTest=0
		il.append(InstructionFactory.createNull(Type.OBJECT));
		il.append(InstructionFactory.createStore(Type.OBJECT, VAR_SUPPORTOBJECT));
		//String testResult="Test exec..."
		il.append(new LDC(this.getConstantPool()
				.addString("Test execution for class "+clazz.getName()+": \n")));
		il.append(InstructionFactory.createStore(strType, VAR_TOPRINT));
		
		
		Type arg_C[]={clazz.toBCEL()};
		//per ogni test
		for(TestSignature ts:clazz.getTests()){
			
			this.addConcatToVar(il,VAR_TOPRINT,"  - "+ts.getName()+": ");
			
			//C obj=new C()
			il.append(this.getFactory().createNew((ObjectType)clazz.toBCEL()));
			il.append(InstructionFactory.DUP);
			il.append(this.getFactory().createInvoke(clazz.getName(), "<init>"
					, Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
			/*
			//per ogni fixture
			for(FixtureSignature fs:clazz.fixturesLookup()){
				//fixture(obj)
				il.append(InstructionFactory.DUP);
				il.append(this.getFactory().createInvoke(clazz.getName()+"Test", fs.getName()
						, Type.VOID, arg_C, Constants.INVOKESTATIC));
			}
			
			//startTest=System.nanoTime()
			il.append(this.getFactory().createInvoke(System.class.getName(), "nanoTime"
					, Type.LONG, Type.NO_ARGS, Constants.INVOKESTATIC));
			
			il.append(InstructionFactory.createStore(Type.LONG, VAR_TIME));
			
			//String result=test(obj)
			il.append(this.getFactory().createInvoke(clazz.getName()+"Test", ts.getName()
					, strType, arg_C, Constants.INVOKESTATIC));
			//.....stack |result|
			
			//tempotrascorso=tempoattuale-startTest
			il.append(this.getFactory().createInvoke(System.class.getName(), "nanoTime"
					, Type.LONG, Type.NO_ARGS, Constants.INVOKESTATIC));
			il.append(InstructionFactory.createLoad(Type.LONG, VAR_TIME));
			il.append(InstructionFactory.LSUB);
			//tempotrascorso/=1000000.0f (millisecondi)
			il.append(InstructionFactory.L2F);
			il.append(new LDC(this.getConstantPool().addFloat(1000000.0f)));
			il.append(InstructionFactory.FDIV);
			//......stack |result|float|
			//creo array di Object che contiene un Float
			Type[] arg_Float={Type.FLOAT};
			il.append(this.getFactory()
					.createInvoke(Float.class.getName(), "valueOf"
							, Type.getType(Float.class), arg_Float, Constants.INVOKESTATIC));
			il.append(InstructionFactory.createStore(Type.OBJECT, VAR_SUPPORTOBJECT));
			il.append(InstructionFactory.ICONST_1);
			il.append(this.getFactory().createNewArray(Type.OBJECT, (short) 1));
			il.append(InstructionFactory.DUP);
			//......stack |result|array|array|
			il.append(InstructionFactory.ICONST_0);
			il.append(InstructionFactory.createLoad(Type.OBJECT, VAR_SUPPORTOBJECT));
			//......stack |result|array|array|0|Float|
			il.append(InstructionFactory.createArrayStore(Type.OBJECT));
			
			//String s=String.format(..
			il.append(new LDC(this.getConstantPool().addString("[%.2fms]")));
			il.append(InstructionFactory.SWAP);
			Type format_args[]={strType,Type.getType(Object[].class)};
			il.append(this.getFactory().createInvoke(String.class.getName(), "format"//possibile problema con float e Float
					, strType, format_args, Constants.INVOKESTATIC));
			//....lo stack ha |result|time|
			
			
			//if result.isEmpty()
			il.append(InstructionFactory.SWAP);
			//....lo stack ha |time|result|   sarebbe meglio il long del tempo nella var
			il.append(InstructionFactory.DUP);
			il.append(this.getFactory().createInvoke("java.lang.String", "isEmpty"
					, Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
			
			//blocco nel caso result e vuota*****************
			InstructionList il_emptyresult=new InstructionList();
			InstructionHandle ih_emptyresult=this.addConcatToVar(il_emptyresult, VAR_TOPRINT, "failed ");
			InstructionHandle ih_emptyresultend=il_emptyresult.append(InstructionFactory.NOP);
			//************************************************
			il.append(new IFEQ(ih_emptyresult));
			//blocco nel caso result non e vuota*****************
			this.addConcatToVar(il, VAR_TOPRINT, "passed ");
			il.append(new GOTO(ih_emptyresultend));
			il.append(il_emptyresult);
			//************************************************
			*/
			/*
			//concat time
			il.append(InstructionFactory.SWAP);
			//....lo stack ha |result|time|
			this.addConcatToVar(il, VAR_TOPRINT);
			//....lo stack ha |result|
			*/
			/*il.append(InstructionFactory.DUP);
			il.append(this.getFactory().createInvoke("java.lang.String", "isEmpty"
							, Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
			//****caso isEmpty sia true**************
			//scrivo "at <riga>" e incremento failed
			InstructionList il_adderrorline=new InstructionList();
			InstructionHandle ih_adderrorlinestart=
					this.addConcatToVar(il_adderrorline, VAR_TOPRINT," at ");
			this.addConcatToVar(il_adderrorline, VAR_TOPRINT);
			this.addConcatToVar(il_adderrorline, VAR_TOPRINT,"\n");
			il_adderrorline.append(new IINC(VAR_FAILED, 1));
			InstructionHandle ih_adderrorlineend=il_adderrorline.append(InstructionFactory.NOP);
			//***************************************
			il.append(new IFEQ(ih_adderrorlinestart));
			//*****isEmpty false*********************
			//rimuovo result dallo stack
			il.append(InstructionFactory.POP);
			//mando a capo la stampa e incremento i passed
			this.addConcatToVar(il, VAR_TOPRINT,"\n");
			il.append(new IINC(VAR_PASSED, 1));
			il.append(new GOTO(ih_adderrorlineend));
			//******************************************
			il.append(il_adderrorline);*/
			
		}
		//MANCA IL TEMPO TOTALE!!
		//String s=String.format(..
		//stack vuoto
		il.append(InstructionFactory.POP);
		il.append(this.getFactory().createGetStatic("java.lang.System", "out",Type.getType(PrintStream.class)));
		
		/*il.append(new LDC(this.getConstantPool()
				.addString("%d tests passed, %d failed [ ms]")));
		
		il.append(InstructionFactory.ICONST_2);
		il.append(this.getFactory().createNewArray(Type.OBJECT, (short) 1));
		
		il.append(InstructionFactory.DUP);
		il.append(InstructionFactory.ICONST_0);
		il.append(InstructionFactory.createLoad(Type.INT, VAR_PASSED));
		Type[] arg_Integer={Type.INT};
		il.append(this.getFactory()
				.createInvoke(Integer.class.getName(), "valueOf"
						, Type.getType(Integer.class), arg_Integer, Constants.INVOKESTATIC));
		il.append(InstructionFactory.createArrayStore(Type.OBJECT));
		
		il.append(InstructionFactory.DUP);
		il.append(InstructionFactory.ICONST_1);
		il.append(InstructionFactory.createLoad(Type.INT, VAR_FAILED));
		il.append(this.getFactory()
				.createInvoke(Integer.class.getName(), "valueOf"
						, Type.getType(Integer.class), arg_Integer, Constants.INVOKESTATIC));
		il.append(InstructionFactory.createArrayStore(Type.OBJECT));
		
		
		
		Type format_args[]={strType,Type.getType(Object[].class)};
		il.append(this.getFactory().createInvoke(String.class.getName(), "format"//possibile problema con float e Float
				, strType, format_args, Constants.INVOKESTATIC));
		
		this.addConcatToVar(il, VAR_TOPRINT,"\n");
		this.addConcatToVar(il, VAR_TOPRINT);
		
		*/
		Type print_args[]={strType};
		il.append(InstructionFactory.createLoad(strType, VAR_TOPRINT));
		il.append(this.getFactory().createInvoke("java.io.PrintStream", "println"
				, Type.VOID, print_args, Constants.INVOKEVIRTUAL));
	
		il.append(InstructionFactory.createReturn(Type.VOID));
		
		
		MethodGen methodGen = new MethodGen
				(Constants.ACC_PUBLIC | Constants.ACC_STATIC, // public and static
				org.apache.bcel.generic.Type.VOID, // return type
				new org.apache.bcel.generic.Type[] // parameters
					{ new org.apache.bcel.generic.ArrayType("java.lang.String", 1) },
				null, // parameters names: we do not care
				"main", // method's name
				this.getClassName(), // defining class
				il, // bytecode of the method
				this.getConstantPool()); // constant pool
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// we add a method to the class that we are generating
		this.addMethod(methodGen.getMethod());
	}
	
	private InstructionHandle addConcatToVar(InstructionList il,int varIndex, String toConcat){
		Type strType=Type.getType(String.class);
		Type arg_str[]={strType};
		//carica variabile
		InstructionHandle toRet= il.append(InstructionFactory.createLoad(strType, varIndex));
		//carica stringa
		il.append(new LDC(this.getConstantPool().addString(toConcat)));
		
		//chiama concat()
		il.append(this.getFactory()
				.createInvoke("java.lang.String", "concat"
						, strType, arg_str, Constants.INVOKEVIRTUAL));
		//salva in variabile
		il.append(InstructionFactory
				.createStore(strType, varIndex));
		return toRet;
	}
	
	//su stack c'e stringa da concatenare in fondo
	private InstructionHandle addConcatToVar(InstructionList il,int varIndex){
		Type strType=Type.getType(String.class);
		Type arg_str[]={strType};
		//carica variabile
		InstructionHandle toRet= il.append(InstructionFactory.createLoad(strType, varIndex));
		//swap
		il.append(InstructionFactory.SWAP);
		
		//chiama concat()
		il.append(this.getFactory()
				.createInvoke("java.lang.String", "concat"
						, strType, arg_str, Constants.INVOKEVIRTUAL));
		//salva in variabile
		il.append(InstructionFactory
				.createStore(strType, varIndex));
		return toRet;
	}
}