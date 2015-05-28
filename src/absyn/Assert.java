package absyn;

import java.io.FileWriter;

import bytecode.NEWSTRING;
import bytecode.NOP;
import bytecode.VIRTUALCALL;
import semantical.TypeChecker;
import translation.Block;
import types.ClassType;
import types.CodeSignature;
import types.TypeList;

public class Assert extends Command {

	private final Expression condition;
	private String erroreAssert;
	
	
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
		Block no=new NEWSTRING(this.erroreAssert)
		.followedBy(new VIRTUALCALL(ClassType.mk("String"), ClassType.mk("String").methodLookup("output", TypeList.EMPTY))
		.followedBy(continuation));
		return this.condition.translateAsTest(where, continuation, no);
	}
	
	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		linkToNode("condition", condition.toDot(where), where);
	}

}
