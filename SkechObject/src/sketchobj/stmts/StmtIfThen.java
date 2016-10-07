package sketchobj.stmts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constraintfactory.ConstData;
import constraintfactory.ConstraintFactory;
import sketchobj.core.Context;
import sketchobj.core.SketchObject;
import sketchobj.core.Type;
import sketchobj.expr.ExprConstant;
import sketchobj.expr.ExprFunCall;
import sketchobj.expr.Expression;

public class StmtIfThen extends Statement {
	private Expression cond;
	private Statement cons, alt;
	private boolean isSingleFunCall = false;
	private boolean isSingleVarAssign = false;
	private int line;

	/**
	 * Create a new conditional statement, with the specified condition,
	 * consequent, and alternative. The two statements may be null if omitted.
	 * @param i 
	 */
	public StmtIfThen(Expression cond, Statement cons, Statement alt, int i) {
		this.cond = cond;
		this.cons = cons;
		this.alt = alt;
		this.line = i;
	}

	// @Override
	// public int size() {
	// int sz_cons = cons == null ? 0 : cons.size();
	// int sz_alt = alt == null ? 0 : alt.size();
	// return sz_cons + sz_alt;
	// }

	/** Returns the condition of this. */
	public Expression getCond() {
		return cond;
	}

	/**
	 * Returns the consequent statement of this, which is executed if the
	 * condition is true.
	 */
	public Statement getCons() {
		return cons;
	}

	/**
	 * Return the alternative statement of this, which is executed if the
	 * condition is false.
	 */
	public Statement getAlt() {
		return alt;
	}

	public boolean isSingleFunCall() {
		return isSingleFunCall;
	}

	public void singleFunCall() {
		isSingleFunCall = true;
	}

	public boolean isSingleVarAssign() {
		return isSingleVarAssign;
	}

	public void singleVarAssign() {
		isSingleVarAssign = true;
	}

	public String toString() {
		String result = "if(" + this.cond + "){\n";
		result += this.cons;
		result += "}";
		if (this.alt != null) {
			result += "else{\n";
			result += this.alt + "}\n";
		} else {
			result += "\n";
		}
		return result;
	}

	@Override
	public ConstData replaceConst(int index) {
		List<SketchObject> toAdd = new ArrayList<SketchObject>();
		toAdd.add(cons);
		if (alt != null)
			toAdd.add(alt);
		if (cond instanceof ExprConstant) {
			int value = ((ExprConstant) cond).getVal();
			Type t = ((ExprConstant) cond).getType();
			cond = new ExprFunCall("Const" + index, new ArrayList<Expression>());
			return new ConstData(t, toAdd, index + 1, value,null);
		}
		return new ConstData(null, toAdd, index, 0,null);
	}

	@Override
	public Context buildContext(Context ctx) {
		ctx.setLinenumber(this.line);;
		this.setCtx(new Context(ctx));
		ctx = new Context(ctx);
		ctx.pushVars(new HashMap<String, Type>());
		ctx = cons.buildContext(ctx);
		ctx.popVars();
		if (alt != null) {
			ctx.pushNewVars();
			ctx = alt.buildContext(ctx);
			ctx.popVars();
		}
		return ctx;
	}

	@Override
	public Map<String, Type> addRecordStmt(StmtBlock parent, int index, Map<String, Type> m) {
		this.cons = new StmtBlock(ConstraintFactory.recordState(this.getCtx().getLinenumber(), this.getCtx().getAllVars()), cons);
		m.putAll(((StmtBlock) cons).stmts.get(1).addRecordStmt((StmtBlock) cons, 1, m));
		if (alt != null) {
			this.alt = new StmtBlock(ConstraintFactory.recordState(this.getCtx().getLinenumber(), this.getCtx().getAllVars()), alt);
			m.putAll(((StmtBlock) alt).stmts.get(1).addRecordStmt((StmtBlock) alt, 1, m));
		}
		return m;
	}

}