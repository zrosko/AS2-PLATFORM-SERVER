package hr.as2.inf.server.da.jdbc;

public class J2EESqlBuilder{
	//TODO StringBuilder
	private StringBuffer _statement;
	//private StringBuilder _sql;
	private boolean _prefix = false;
	private boolean _whereIndicator = false;

public J2EESqlBuilder()	{
	_statement = new StringBuffer();
}
public J2EESqlBuilder(String sql)	{
	this();
	_statement.append(sql);
}
public final J2EESqlBuilder append(String s){
	_statement.append(s);
	return this;
}
public final J2EESqlBuilder appendWhere(String s){
    if(_whereIndicator)
        _statement.append(" AND ");
    else
        _statement.append( "WHERE ");
	_statement.append(s);
	_whereIndicator = true;
	return this;
}
public final J2EESqlBuilder append(String s, int i, String s1){
	_statement.append(s).append(i).append(s1);
	return this;
}
public final void appendln(String s){
	append(s).append("\n");
}
public final void appendln(String s, int i, String s1){
	append(s, i, s1).append("\n");
}
public final void appendWhere(){
	_whereIndicator = true;
}
public final boolean isWhere(){
	return _whereIndicator;
}
public final boolean appAnd(String s, String s1){
	if(notEmpty(s1)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appEqual(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" = ");
		quote(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appEqualNoQuote(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" = ");
		_statement.append("").append(s2).append("");
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appPrefix(){
		_prefix = true;
		return true;
}
public final boolean appEqualUpper(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append("UPPER ("+s1).append(") = ");
		quote(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appGreather(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" > ");
		quote(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appGreatherOrEqual(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" >= ");
		quote(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appGreatherOrEqualNoQuote(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" >= ");
		_statement.append("").append(s2).append("");
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appIn(String s, String s1, String as[]){
	if(as != null){
		int i = as.length;
		if(i != 0){
			if(!_whereIndicator){
				_whereIndicator = true;
				_statement.append("WHERE ");
			}
			prefixLine(s);
			_statement.append(s1).append(" IN ( ");
			for(int j = 0; j < i - 1; j++){
				quote(as[j]);
				_statement.append(", ");
			}

			quote(as[i - 1]);
			_statement.append(" ) \n");
			return true;
		}
	}
	return false;
}
public final boolean appIn(String s, String s1, String data){
	if(data != null){
		int i = data.length();
		if(i != 0){
			if(!_whereIndicator){
				_whereIndicator = true;
				_statement.append("WHERE ");
			}
			prefixLine(s);
			_statement.append(s1).append(" IN ( ");
			_statement.append(data);
			_statement.append(" ) \n");
			return true;
		}
	}
	return false;
}
public final boolean appInNoQuote(String s, String s1, String data){
	if(s != null && s1 != null && data != null){
		int i = data.length();
		int j = s1.length();
		if(i != 0 && j !=0){
			if(!_whereIndicator){
				_whereIndicator = true;
				_statement.append("WHERE ");
			}
			prefixLine(s);
			_statement.append(s1).append(" IN ( ");
			_statement.append(data);
			_statement.append(" ) \n");
			return true;
		}
	}
	return false;
}
public final boolean appNotIn(String s, String s1, String as[]){
	if(as != null){
		int i = as.length;
		if(i != 0){
			if(!_whereIndicator){
				_whereIndicator = true;
				_statement.append("WHERE ");
			}
			prefixLine(s);
			_statement.append(s1).append(" NOT IN ( ");
			for(int j = 0; j < i - 1; j++){
				quote(as[j]);
				_statement.append(", ");
			}

			quote(as[i - 1]);
			_statement.append(" ) \n");
			return true;
		}
	}
	return false;
}
public final boolean appNotIn(String s, String s1, String data){
	if(data != null){
		int i = data.length();
		if(i != 0){
			if(!_whereIndicator){
				_whereIndicator = true;
				_statement.append("WHERE ");
			}
			prefixLine(s);
			_statement.append(s1).append(" NOT IN ( ");
			_statement.append(data);
			_statement.append(" ) \n");
			return true;
		}
	}
	return false;
}
public final boolean appNotInNoQuote(String s, String s1, String data){
	if(s != null && s1 != null && data != null){
		int i = data.length();
		int j = s1.length();
		if(i != 0 && j !=0){
			if(!_whereIndicator){
				_whereIndicator = true;
				_statement.append("WHERE ");
			}
			prefixLine(s);
			_statement.append(s1).append(" NOT IN ( ");
			_statement.append(data);
			_statement.append(" ) \n");
			return true;
		}
	}
	return false;
}
public final boolean appLess(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" < ");
		quote(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appLessOrEqual(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" <= ");
		quote(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appLessOrEqualNoQuote(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" <= ");
		_statement.append("").append(s2).append("");
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appLessOrGreater(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		_statement.append(s1).append(" <> ");
		quote(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appLike(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		append(s1);
		_statement.append(" LIKE ");
		like(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appLikeRight(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		append(s1);
		_statement.append(" LIKE ");
		likeRight(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appLikeLeft(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		append(s1);
		_statement.append(" LIKE ");
		likeLeft(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public final boolean appNotLike(String s, String s1, String s2){
	if(notEmpty(s2)){
		if(!_whereIndicator){
			_whereIndicator = true;
			_statement.append("WHERE ");
		}
		prefixLine(s);
		append(s1);
		_statement.append(" NOT LIKE ");
		like(s2);
		_statement.append("\n");
		return true;
	}else{
		return false;
	}
}
public static void main(String args[]) {
    //String args1[] = {"i1", "i2", "i3"};
    String f1 = "GGG";
    String f2 = "ROLEX";

    J2EESqlBuilder sqlstatement = new J2EESqlBuilder();
    sqlstatement.appendln("SELECT *");
    sqlstatement.appendln("FROM foo ");
    //sqlstatement.appendWhere();
    sqlstatement.appEqualUpper("OR", "c3", "buz");
    sqlstatement.appEqualUpper("OR", "NAME", f1);
    sqlstatement.appLessOrGreater("AND", "JEDAN", "TRI"); //novi primjer za appLessOrGreater
    sqlstatement.appLike("AND", "SNAME", f2);

    System.out.println(sqlstatement.toString());
}
private boolean notEmpty(String s){
	return s != null && !s.equals("");
}
private void prefixLine(String s) {
	if (s != null && _prefix) {
		_statement.append(s);
		_statement.append(" ");
	}
	_prefix = true;
}
public final J2EESqlBuilder quote(String s){
	_statement.append("'").append(s).append("'");
	return this;
}
public final J2EESqlBuilder like(String s){
	_statement.append("'%").append(s).append("%'");
	return this;
}
public final J2EESqlBuilder likeLeft(String s){
    _statement.append("'%").append(s).append("'");
	return this;
}
public final J2EESqlBuilder likeRight(String s){
    _statement.append("'").append(s).append("%'");
	return this;
}
public String toString(){
	return _statement.toString();
}
}