package hr.as2.inf.server.da;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2DataAccessException;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.server.annotations.AS2DataAccessObject;

/**
 * A common abstract class for all Data Access Objects. The standard access
 * methods daoStore, daoLoad, daoCreate, daoFind, daoRemove, daoRemoveAll,
 * daoLoadNext are defined.
 */
@AS2DataAccessObject
public abstract class J2EEDataAccessObject {
	private Long _id;

	protected J2EEDataAccessObject() {
		this._id = System.currentTimeMillis();
	}

	public J2EEDataAccessObject(Long value) {
		this._id = value;
	}

	public Long getId() {
		return this._id;
	}

	public void setId(Long value) {
		this._id = value;
	}

	public AS2Record daoCreate(AS2Record value) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public AS2RecordList daoFind(AS2Record value) throws Exception {
		throw new AS2DataAccessException("167");
	}

	public AS2RecordList daoFindAttributes(AS2Record value)	throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public AS2RecordList daoLoad(AS2Record value) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public AS2RecordList daoLoadNext(AS2Record value) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public void daoRemove(AS2Record value) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public void daoRemoveAll(AS2Record value) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public AS2Record daoStore(AS2Record value) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public int daoCreateMany(AS2RecordList valueList) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public int daoRemoveMany(AS2RecordList valueList) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}

	public int daoStoreMany(AS2RecordList valueList) throws AS2Exception {
		throw new AS2DataAccessException("167");
	}
}
