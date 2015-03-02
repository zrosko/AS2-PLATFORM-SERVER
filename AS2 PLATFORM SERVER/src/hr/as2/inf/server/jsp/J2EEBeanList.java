package hr.as2.inf.server.jsp;

import hr.as2.inf.common.data.AS2RecordList;

public class J2EEBeanList{
	AS2RecordList _resultSet;
	J2EEBean _currentBean;
	
public J2EEBeanList() {
}

public J2EEBeanList(AS2RecordList rs) {
	_resultSet=rs;
}

public String get(String key) {
	if (_currentBean!=null)
		return _currentBean.get(key);
	else
		return "";
}

public J2EEBean getBeanAt(int i) {
	try {
		if (size()<i)
			return new J2EEBean(); //empty or null ?
		_currentBean = new J2EEBean(_resultSet.getRowAt(i));
		return _currentBean;
	} catch (Exception e) {
		return new J2EEBean(); //empty or null ?
	}
}
public int size() {
	try{
		return _resultSet.getRows().size();
	}catch (Exception e){
		return 0;
	}
}
}
