package javaBytecodeGenerator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
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

	/*
	 * ASSERT: ritorna la riga e colonna dell'errore come runtime/String se test e fallito
	 * 			altrimenti continua con l'esecuzione del test
	 *  		messo nel generate java bytecode di Assert.java
	 * TEST:ritorna stringa vuota in fondo (dopo che passa tutti gli assert(messo in TestDeclaration)
	 * 			nel translate
	 * 
	 * 
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
		this.createTestMain(clazz);
	}
	
	private void createTestMain(ClassType clazz){
		//definisco tipi per comodita dopo
		Type strType=ClassType.mk("String").toBCEL();
		Type arg_str[]={strType};
		Type arg_C[]={clazz.toBCEL()};
		//definisco variabili che utilizzero
		//NOTA: LONG E DOUBLE OCCUPANO 2 VARIABILI
		final int VAR_PASSED=1, VAR_FAILED=2, VAR_TIME=3
				, VAR_TOPRINT=5, VAR_TOTALTIME=6;
		
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
		//long totalTime=0
		il.append(InstructionFactory.FCONST_0);
		il.append(InstructionFactory.createStore(Type.FLOAT, VAR_TOTALTIME));
		//String testResult="Test exec..."
		this.pushRunTimeString(il, "\nTest execution for class "+clazz.getName()+":\n");
		il.append(InstructionFactory.createStore(strType, VAR_TOPRINT));
		
		
		
		//per ogni test
		for(TestSignature ts:clazz.getTests()){
			//Concateno nome del test
			this.addConcatToVar(il,VAR_TOPRINT,"  - "+ts.getName()+": ");
			//creo nuovo oggetto della classe che sto testando
			//C obj=new C()
			il.append(this.getFactory().createNew((ObjectType)clazz.toBCEL()));
			il.append(InstructionFactory.DUP);
			il.append(this.getFactory().createInvoke(clazz.getName(), "<init>"
					, Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
			
			//per ogni fixture
			for(FixtureSignature fs:clazz.fixturesLookup()){
				//fixture(obj)
				//chiamo la fixture su quell'oggetto
				il.append(InstructionFactory.DUP);
				il.append(this.getFactory().createInvoke(clazz.getName()+"Test", fs.getName()
						, Type.VOID, arg_C, Constants.INVOKESTATIC));
			}
			
			//STACK |obj|
			//startTest=System.nanoTime()
			//per segnare il tempo prima della chiamata del test
			il.append(this.getFactory().createInvoke(System.class.getName(), "nanoTime"
					, Type.LONG, Type.NO_ARGS, Constants.INVOKESTATIC));
			il.append(InstructionFactory.createStore(Type.LONG, VAR_TIME));
			
			//STACK |obj|
			
			//String result=test(obj)
			//chiamo test (non DUPlico obj e lo perdo... non serve piu)
			il.append(this.getFactory().createInvoke(clazz.getName()+"Test", ts.getName()
					, strType, arg_C, Constants.INVOKESTATIC));
			//STACK |result|
			//NOTA il risultato e una stringa che e vuota se il test e passato
			//     altrimenti contiene <riga.colonna> di dove e fallito
			
			//tempotrascorso=tempoattuale-startTest
			il.append(this.getFactory().createInvoke(System.class.getName(), "nanoTime"
					, Type.LONG, Type.NO_ARGS, Constants.INVOKESTATIC));
			il.append(InstructionFactory.createLoad(Type.LONG, VAR_TIME));
			il.append(InstructionFactory.LSUB);
			//converto in millisecondi e prendo solo 2 cifre dopo la virgola
			//divisione intera e poi divisione float
			//(non sempre rimangono 2 cifre per arrotondamenti ma non ho trovato metodo migliore)
			il.append(this.getFactory().createConstant(10000L));
			il.append(InstructionFactory.LDIV);
			il.append(InstructionFactory.L2F);
			il.append(this.getFactory().createConstant(100.0f));
			il.append(InstructionFactory.FDIV);
			//accumulo totaltime
			//QUA SI AGGIUNGE L'ACCUMULO
			il.append(InstructionFactory.DUP);
			il.append(InstructionFactory.createLoad(Type.FLOAT, VAR_TOTALTIME));
			il.append(InstructionFactory.FADD);
			il.append(InstructionFactory.createStore(Type.FLOAT, VAR_TOTALTIME));
			//STACK |result|float|
			//aggiungo alla stringa da stampare
			this.pushRunTimeString(il, "[");
			il.append(InstructionFactory.SWAP);
			//chiama concat()
			//concateno il tempo di esecuzione di questo test
			il.append(this.getFactory().createInvoke("runTime.String", "concat"
							, strType, new Type[]{Type.FLOAT}, Constants.INVOKEVIRTUAL));
			this.pushRunTimeString(il, "ms] ");
			il.append(this.getFactory().createInvoke("runTime.String", "concat"
					, strType, arg_str, Constants.INVOKEVIRTUAL));
			
			//STACK |result|timeStr|
			
			
			//if result.isEmpty() (Se test e passato)
			il.append(InstructionFactory.SWAP);
			il.append(InstructionFactory.DUP);
			this.pushRunTimeString(il, "");
			il.append(this.getFactory().createInvoke("runTime.String", "equals"
					, Type.BOOLEAN, arg_str, Constants.INVOKEVIRTUAL));
			
			//concateno passed o failed in base al risultato di isEmpty
			
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
			
			//concat time
			il.append(InstructionFactory.SWAP);
			//STACK |result|time|
			this.addConcatToVar(il, VAR_TOPRINT);
			//STACK |result|
			
			//controllo di nuovo il risultato del test
			//aggiungo la riga se il test e fallito, altrimenti vado a capo
			il.append(InstructionFactory.DUP);
			this.pushRunTimeString(il, "");
			il.append(this.getFactory().createInvoke("runTime.String", "equals"
					, Type.BOOLEAN, arg_str, Constants.INVOKEVIRTUAL));
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
			il.append(il_adderrorline);
			
		}
		
		//stack vuoto
		//concateno il numero di test passati e il numero di test falliti e infine il tempo totale
		
		this.addConcatToVar(il, VAR_TOPRINT,"\n");
		il.append(InstructionFactory.createLoad(strType, VAR_TOPRINT));
		
		Type[] arg_int={Type.INT};
		il.append(InstructionFactory.createLoad(Type.INT, VAR_PASSED));
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_int, Constants.INVOKEVIRTUAL));
		
		this.pushRunTimeString(il, " tests passed, ");
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_str, Constants.INVOKEVIRTUAL));
		
		il.append(InstructionFactory.createLoad(Type.INT, VAR_FAILED));
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_int, Constants.INVOKEVIRTUAL));
		
		this.pushRunTimeString(il, " failed [");
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_str, Constants.INVOKEVIRTUAL));
		
		il.append(InstructionFactory.createLoad(Type.FLOAT, VAR_TOTALTIME));
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, new Type[]{Type.FLOAT}, Constants.INVOKEVIRTUAL));
		
		this.pushRunTimeString(il, "ms]");
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
				, strType, arg_str, Constants.INVOKEVIRTUAL));
		
		il.append(this.getFactory().createInvoke("runTime.String", "output"
				, Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
		
		
	
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
	
	//funzione che pusha sullo stack la stringa kitten specificata
	private void pushRunTimeString(InstructionList il,String toPush){
		il.append(this.getFactory().createNew("runTime/String"));
		il.append(InstructionFactory.DUP);
		il.append(this.getFactory().createConstant(toPush));
		il.append(this.getFactory().createInvoke("runTime.String", "<init>"
				, Type.VOID, new Type[]{Type.getType(String.class)}
				,Constants.INVOKESPECIAL));
	}
	
	//funzione che genera il bytecode concatena una stringa ad una variabile
	private InstructionHandle addConcatToVar(InstructionList il,int varIndex, String toConcat){
		Type strType=ClassType.mk("String").toBCEL();
		Type arg_str[]={strType};
		//carica variabile
		InstructionHandle toRet= il.append(InstructionFactory.createLoad(strType, varIndex));
		//carica stringa
		this.pushRunTimeString(il, toConcat);
		
		//chiama concat()
		il.append(this.getFactory().createInvoke("runTime.String", "concat"
						, strType, arg_str, Constants.INVOKEVIRTUAL));
		//salva in variabile
		il.append(InstructionFactory.createStore(strType, varIndex));
		return toRet;
	}
	
	//genero bytecode per concatenare alla var specificata la stringa in cima allo stack
	private InstructionHandle addConcatToVar(InstructionList il,int varIndex){
		Type strType=ClassType.mk("String").toBCEL();
		Type arg_str[]={strType};
		//carica variabile
		InstructionHandle toRet= il.append(InstructionFactory.createLoad(strType, varIndex));
		//swap
		il.append(InstructionFactory.SWAP);
		//chiama concat()
		il.append(this.getFactory()
				.createInvoke("runTime.String", "concat"
						, strType, arg_str, Constants.INVOKEVIRTUAL));
		//salva in variabile
		il.append(InstructionFactory
				.createStore(strType, varIndex));
		return toRet;
	}
}