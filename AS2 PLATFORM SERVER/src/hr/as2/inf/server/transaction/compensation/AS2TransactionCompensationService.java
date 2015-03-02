package hr.as2.inf.server.transaction.compensation;

import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.data.AS2RecordList;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;
import hr.as2.inf.common.transaction.compensation.AS2ConfigurationCTS;
import hr.as2.inf.common.transaction.compensation.AS2TransactionAction;
import hr.as2.inf.common.transaction.compensation.AS2TransactionStatus;
import hr.as2.inf.common.transaction.compensation.dto.AS2TransactionRs;
import hr.as2.inf.server.transaction.compensation.da.jdbc.AS2CompensationJdbc;
import hr.as2.inf.server.transaction.compensation.da.jdbc.AS2CompensationStepJdbc;
import hr.as2.inf.server.transaction.compensation.da.jdbc.AS2TransactionJdbc;
import hr.as2.inf.server.transaction.compensation.da.jdbc.AS2TransactionStepJdbc;

import java.net.InetAddress;
import java.util.Iterator;

/**
 * Handles multiple user transaction contexts by using a transaction token as a
 * unique transaction identifier (by default generated on the client side,
 * otherwise geretated by here by calling generateTransactionToken().
 */
public final class AS2TransactionCompensationService {
    private static AS2TransactionCompensationService _instance = null;
    private AS2RecordList _transactionsTXN;
    private AS2RecordList _compensationsCTS;
    private AS2TransactionCompensationService() {
        _transactionsTXN = new AS2RecordList();
        _compensationsCTS = new AS2RecordList();
        AS2Context.setSingletonReference(this);
    }
    public static AS2TransactionCompensationService getInstance() {
        if (_instance == null)
            _instance = new AS2TransactionCompensationService();
        return _instance;
    }
    /**
     * Main service rutine, called by external class to process transaction
     * begin, step, commit and rollback.
     *  
     */
    public AS2Record service(AS2Record req) {
        try {
            AS2Record response = new AS2Record();
            //Object _returned = null;
            //use transaciton compensation service or NOT at all.
            if (AS2ConfigurationCTS.useSERVICE() && req.exists(AS2Record._CTS_SERVICE)) {
                //three possible actions
                if (req.get(AS2Record._TRANSACTION_ACTION).equals(AS2TransactionAction._BEGIN)) {
                    if (AS2ConfigurationCTS.isLONGTransaction()) {
                        if (!req.exists(AS2Record._TRANSACTION_TOKEN)) {
                            //client did not generated the transaction token
                            req.setTransactionToken(generateTransactionToken());
                        }
                        begin(req);
                        req.set(AS2TransactionStatus._STATUS, AS2TransactionStatus.Active);
                        response.set(AS2Record._RESPONSE, req); //possible
                                                                              // new
                                                                              // token
                    }
                } else if (req.get(AS2Record._TRANSACTION_ACTION).equals(AS2TransactionAction._STEP)) {
                    if (AS2ConfigurationCTS.isLONGTransaction()) {
                        AS2RecordList _txn = currentTransactionTXN(req);
                        if (_txn.getAsInt(AS2TransactionStatus._STATUS) == AS2TransactionStatus.Active) {
                            step(req);
                            req.set(AS2TransactionStatus._STATUS, AS2TransactionStatus.Active);
                            response.set(AS2Record._RESPONSE, req); //possible
                                                                                  // new
                                                                                  // token
                        } else {
                        }//TODO} Exception
                    }
                } else if (req.get(AS2Record._TRANSACTION_ACTION).equals(AS2TransactionAction._COMMIT)) {
                    if (AS2ConfigurationCTS.isLONGTransaction()) {
                        commit(req, response);
                    }
                } else if (req.get(AS2Record._TRANSACTION_ACTION).equals(AS2TransactionAction._ROLLBACK)) {
                    if (AS2ConfigurationCTS.isLONGTransaction()) {
                        //rollback(req);
                    }
                } else if (req.get(AS2Record._TRANSACTION_ACTION).equals(AS2TransactionAction._ROLLBACK_CTS)) {
                    if (AS2ConfigurationCTS.isLONGTransaction()) {
                        //rollbackCTS(req);
                    }
                }
                return response;
            } else
                return req; //NO SERVICE is used
        } catch (Exception e) {
            AS2Trace.trace(AS2Trace.E, "Problem J2EETransactionCompensationService.service: " + e);
            throw new AS2Exception("999");//TODO
        }
    }
    private void commit(AS2Record req, AS2Record res) throws Exception {
        AS2Record _txn_status = new AS2Record();
        AS2RecordList txn = new AS2RecordList();
        AS2TransactionJdbc _dao_txn = new AS2TransactionJdbc();
        if (AS2ConfigurationCTS.isCACHETransaction()) {
            if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
                txn = currentTransactionTXN(req);
            } else if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
                req.set("transaction_id", req.get(AS2Record._TRANSACTION_TOKEN));
                _txn_status = new AS2Record(_dao_txn.daoLoad(req));//one
                // row
                // only
                //check status
                if (_txn_status.getAsInt(AS2TransactionStatus._STATUS) == AS2TransactionStatus.Active) {
                    AS2TransactionStepJdbc _dao_step_txn = new AS2TransactionStepJdbc();
                    txn = _dao_step_txn.daoFind(req);
                } else {
                    throw new AS2Exception("999");//TODO
                }
            }
            //commit all
            int txn_status = AS2TransactionStatus.Committed;
            Iterator<AS2Record> E = txn.getRows().iterator();
            while (E.hasNext()) {
                AS2Trace.trace(AS2Trace.I, "J2EETransactionCompensation ", "Commit J2EETransactionCompensation");
                AS2Record txn_step = E.next();
//                txn_step.setComponent(txn_step.get("target_component"));
//                txn_step.setService(txn_step.get("target_service"));
                AS2Record response = new AS2Record();
                System.out.println(txn_step);//TODO
                try {
                    response = txn_step.execute();
                    if (AS2ConfigurationCTS.isCACHECompensation()) {
                        response.set(AS2Record._TRANSACTION_TOKEN, req.get(AS2Record._TRANSACTION_TOKEN));
                        response.set("transaction_id", req.get(AS2Record._TRANSACTION_TOKEN));
                        response.set("step_id", req.get(AS2Record._REQUEST_ID));
                        if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
                            txn = currentCompensationCTS(response);
                            txn.addRow(response);
                        } else if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
                            AS2CompensationStepJdbc _dao_step_cts = new AS2CompensationStepJdbc();
                            _dao_step_cts.daoCreate(response);
                        }
                    }
                } catch (Exception e) {
                    res.set(AS2TransactionStatus._STATUS, txn_step.get("step_id"));
                    txn_status = AS2TransactionStatus.Committing;
                    break;//need to return the step id back to client TODO name
                    // for step id, broj poruke ili...
                }
            }
            if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
                txn.set(AS2TransactionStatus._STATUS, txn_status);
                _dao_txn.daoStore(txn);
            } else if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
                //here do not set to commiting (no need to keep it in memory)
                deleteCurrenTransactionTXN(req);
            }
        }
        if (AS2ConfigurationCTS.isCACHECompensation()) {
            deleteCurrenTransactionCTS(req);
        }
    }
    public void rollback(AS2Record req, AS2Record res) throws Exception {
        AS2Record _txn_status = new AS2Record();
        AS2RecordList txn = new AS2RecordList();
        AS2TransactionJdbc _dao_txn = new AS2TransactionJdbc();
        if (AS2ConfigurationCTS.isCACHETransaction()) {
            if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
                txn = currentTransactionTXN(req);
            } else if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
                req.set("transaction_id", req.get(AS2Record._TRANSACTION_TOKEN));
                _txn_status = new AS2Record(_dao_txn.daoLoad(req));//one
                // row
                // only
                //check status
                if (_txn_status.getAsInt(AS2TransactionStatus._STATUS) == AS2TransactionStatus.Active) {
                    AS2TransactionStepJdbc _dao_step_txn = new AS2TransactionStepJdbc();
                    txn = _dao_step_txn.daoFindForRollback(req); //TODO ??
                } else {
                    throw new AS2Exception("999");//TODO
                }
            }
            int txn_status = AS2TransactionStatus.RolledBack;
            if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
                txn.set(AS2TransactionStatus._STATUS, txn_status);
                _dao_txn.daoStore(txn);
            } else if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
                deleteCurrenTransactionTXN(req);
            }
        }
        if (AS2ConfigurationCTS.isCACHECompensation()) {
            deleteCurrenTransactionCTS(req);
        }
    }
    public void rollbackCTS(AS2Record req, AS2Record res) {
    }
    private void step(AS2Record req) throws Exception {
        if (AS2ConfigurationCTS.isCACHETransaction()) {
            //memory
            if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
                AS2RecordList txn = currentTransactionTXN(req);
                txn.addRow(req);
                //database
            } else if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
                AS2TransactionStepJdbc _dao = new AS2TransactionStepJdbc();
                _dao.daoCreate(prepareStep(req));
            }
        }
    }
    private void begin(AS2Record req) throws Exception {
        if (AS2ConfigurationCTS.isCACHETransaction()) {
            beginTXN(req);
        }
        if (AS2ConfigurationCTS.isCACHECompensation()) {
            beginCTS(req);
        }
    }
    private void beginTXN(AS2Record req) throws Exception {
        //cache at server memory
        if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
            //new transaction
            AS2TransactionRs txn = new AS2TransactionRs();
            txn.setProperties(req.getProperties());
            txn.setTransactionToken(req.get(AS2Record._TRANSACTION_TOKEN));
            txn.set(AS2TransactionStatus._STATUS, AS2TransactionStatus.Active);
            _transactionsTXN.addResultSet(req.get(AS2Record._TRANSACTION_TOKEN), txn);
        } else if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
            //save a new txn to the database
            AS2TransactionJdbc _dao_txn = new AS2TransactionJdbc();
            _dao_txn.daoCreate(prepareTransaction(req));
        }
    }
    private void beginCTS(AS2Record req) throws Exception {
        //cache at server memory
        if (AS2ConfigurationCTS.isCACHEAtServerLocation()) {
            //new transaction
            AS2RecordList cts = new AS2RecordList();
            cts.setProperties(req.getProperties());
            cts.setTransactionToken(req.get(AS2Record._TRANSACTION_TOKEN));
            cts.set(AS2TransactionStatus._STATUS, AS2TransactionStatus.Active);
            _compensationsCTS.addResultSet(req.get(AS2Record._TRANSACTION_TOKEN), cts);
        } else if (AS2ConfigurationCTS.isCACHEAtServerLocationRDBMS()) {
            //save a new cts to the database
            AS2CompensationJdbc _dao_cts = new AS2CompensationJdbc();
            _dao_cts.daoCreate(prepareTransaction(req));
        }
    }
    //not used at the moment
    private String generateTransactionToken() {
        try {
            String _threadName = Thread.currentThread().getName();
            InetAddress _ip = InetAddress.getLocalHost();
            String _ipAddress = _ip.getHostAddress();
            return _ipAddress + "-" + _threadName + "-" + System.currentTimeMillis();
        } catch (Exception e) {
            return "@@default-token-" + System.currentTimeMillis();
        }
    }
    /**
     * Prepares both transaction, compensation.
     */
    private AS2Record prepareTransaction(AS2Record value) {
        AS2Record res = new AS2Record();
        res.set("transaction_id", value.get(AS2Record._TRANSACTION_TOKEN));
        res.set("sequence_number", value.get(AS2Record._REQUEST_ID));
        res.set("target_service", value.get(AS2Record._SERVICE_TXN));
        res.set("target_component", value.get(AS2Record._COMPONENT_TXN));
        res.set("xml", value.toString());//TODO temp
        return res;
    }
    /**
     * Prepares both transaction step, compensation step.
     */
    private AS2Record prepareStep(AS2Record value) {
        AS2Record res = new AS2Record();
        res.set("transaction_id", value.get(AS2Record._TRANSACTION_TOKEN));
        res.set("sequence_number", value.get(AS2Record._REQUEST_ID));
        res.set("target_service", value.get(AS2Record._SERVICE_TXN));
        res.set("target_component", value.get(AS2Record._COMPONENT_TXN));
        res.set("xml", value.toString());//TODO temp
        return res;
    }
    private AS2RecordList currentTransactionTXN(AS2Record value) {
        return _transactionsTXN.getResultSet(value.get(AS2Record._TRANSACTION_TOKEN));
    }
    private AS2RecordList currentCompensationCTS(AS2Record value) {
        return _compensationsCTS.getResultSet(value.get(AS2Record._TRANSACTION_TOKEN));
    }
    private void deleteCurrenTransactionTXN(AS2Record value) {
        _transactionsTXN.deleteResultSet(value.get(AS2Record._TRANSACTION_TOKEN));
    }
    private void deleteCurrenTransactionCTS(AS2Record value) {
        _compensationsCTS.deleteResultSet(value.get(AS2Record._TRANSACTION_TOKEN));
    }
}