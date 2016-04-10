package sdt;

import java.util.ArrayList;
import java.util.List;

import lex.Token;

public class AParams {
	public Expression expression;
	public AParamsTails aParamsTails;
	public Token downId;
	public List<Expression> getExprList() {
		List<Expression> exprList = new ArrayList<Expression>();
		Expression expression1 = expression;
		AParamsTails aParamsTails1 = aParamsTails;
		while(expression1 != null){
			exprList.add(expression1);
			if(aParamsTails1 != null
					&& aParamsTails1.aParamsTail != null
					&& aParamsTails1.aParamsTail.expression != null){
				expression1 = aParamsTails1.aParamsTail.expression;
				aParamsTails1 = aParamsTails1.aParamsTails;
			} else {
				expression1 = null;
			}
		}
		return exprList;
	}  

}
