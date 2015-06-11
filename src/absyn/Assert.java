package absyn;

import java.io.FileWriter;

import bytecode.CONST;
import bytecode.LOAD;
import bytecode.NEWSTRING;
import bytecode.NOP;
import bytecode.RETURN;
import bytecode.VIRTUALCALL;
import semantical.TypeChecker;
import translation.Block;
import types.BooleanType;
import types.ClassType;
import types.CodeSignature;
import types.TypeList;
import types.VoidType;

public class Assert extends Command {

	private final Expression condition;
	private String erroreAssert;
	private String lineAndChar;
	
	
	public Assert(int pos, Expression condition) {
		super(pos);
		this.condition=condition;
		this.erroreAssert="";
	}

	@Override
	protected TypeChecker typeCheckAux(TypeChecker checker) {
		this.condition.mustBeBoolean(checker);
		if(!checker.isAssertAllowed()){
			error("Assert only allowed in Test");
		}
		
		this.erroreAssert="@"+checker.getErrorMsg().getFileName()
				+".kit:"+checker.getErrorMsg().getLineAndChar(this.getPos());
		this.lineAndChar=checker.getErrorMsg().getLineAndChar(this.getPos());
		return checker;
	}

	@Override
	public boolean checkForDeadcode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Block translate(CodeSignature where, Block continuation) {
		continuation.doNotMerge();
		//ritorno la posizione dell'assert che fallisce per indicare dove e fallito il test
		Block no=new NEWSTRING(this.lineAndChar)	
		.followedBy(new Block(new RETURN(ClassType.mk("String"))));
		return this.condition.translateAsTest(where, continuation, no);
	}
	
	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("condition", condition.toDot(where), where);
	}

}
