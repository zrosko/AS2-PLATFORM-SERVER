package hr.as2.inf.server.transaction.compensation.facade;

import hr.as2.inf.common.data.AS2Record;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.transaction.compensation.dto.AS2CompensationRs;
import hr.as2.inf.common.transaction.compensation.dto.AS2CompensationStepVo;
import hr.as2.inf.common.transaction.compensation.dto.AS2CompensationVo;
import hr.as2.inf.common.transaction.compensation.dto.AS2TransactionRs;
import hr.as2.inf.common.transaction.compensation.dto.AS2TransactionStepVo;
import hr.as2.inf.common.transaction.compensation.dto.AS2TransactionVo;
import hr.as2.inf.common.transaction.compensation.facade.AS2TransactionCompensationFacade;
import hr.as2.inf.server.transaction.compensation.da.jdbc.AS2CompensationJdbc;
import hr.as2.inf.server.transaction.compensation.da.jdbc.AS2TransactionJdbc;
import hr.as2.inf.server.transaction.compensation.da.jdbc.AS2TransactionStepJdbc;

public final class AS2TransactionCompensationFacadeServer implements AS2TransactionCompensationFacade {
    private static AS2TransactionCompensationFacadeServer _instance = null;
    public static AS2TransactionCompensationFacadeServer getInstance() {
        if (_instance == null) {
            _instance = new AS2TransactionCompensationFacadeServer();
        }
        return _instance;
    }
    private AS2TransactionCompensationFacadeServer() {
    }
    public AS2TransactionVo beginTxn(AS2TransactionVo value) throws Exception {
        return value;
    }
    public AS2TransactionVo commitTxn(AS2TransactionVo value) throws Exception {
        return value;
    }
    public AS2TransactionVo rollbackTxn(AS2TransactionVo value) throws Exception {
        return value;
    }
    public AS2Record compensateTxn(AS2Record value) throws Exception {
        //used for client side caching
        AS2Record response = new AS2Record();
        try {
            //real business operation is called from client
            //no need to do the work here
            //response = value.execute();
        } catch (Exception e) {
            throw new AS2Exception("100");
        }
        return response;
    }
    public AS2TransactionRs readAllTransactions(AS2TransactionVo value) throws Exception {
        /*
         * RDBMS
         */
        AS2TransactionJdbc _dao = new AS2TransactionJdbc();
        return _dao.daoReadAll(value);
    }
    public AS2TransactionRs readAllTransactionSteps(AS2TransactionVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2TransactionRs searchTransactions(AS2TransactionVo value) throws Exception {
        /*
         * RDBMS
         */
        AS2TransactionJdbc _dao = new AS2TransactionJdbc();
        return _dao.daoFind(value);
    }
    public AS2TransactionVo addTransaction(AS2TransactionVo value) throws Exception {
        /*
         * ADMIN
         */
        return null;
    }
    public AS2TransactionStepVo addTransactionStep(AS2TransactionStepVo value) throws Exception {
        AS2TransactionStepJdbc dao = new AS2TransactionStepJdbc();
        dao.daoCreate(value);
        return value;
    }
    public AS2TransactionVo deleteTransaction(AS2TransactionVo value) throws Exception {
        /*
         * RDBMS
         */
        return value;
    }
    public AS2TransactionStepVo deleteTransactionStep(AS2TransactionStepVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2TransactionVo updateTransaction(AS2TransactionVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2TransactionStepVo updateTransactionStep(AS2TransactionStepVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2CompensationRs readAllCompensations(AS2CompensationVo value) throws Exception {
        /*
         * RDBMS
         */
        AS2CompensationJdbc _dao = new AS2CompensationJdbc();
        return _dao.daoReadAll(value);
    }
    public AS2CompensationRs readAllCompensationSteps(AS2CompensationVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2CompensationRs searchCompensations(AS2CompensationVo value) throws Exception {
        /*
         * RDBMS
         */
        AS2CompensationJdbc _dao = new AS2CompensationJdbc();
        return _dao.daoFind(value);
    }
    public AS2CompensationVo addCompensation(AS2CompensationVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2CompensationVo deleteCompensation(AS2CompensationVo value) throws Exception {
        /*
         * RDBMS TODO voditi racuna o aktivnom txn compenzacije
         */
        return null;
    }
    public AS2CompensationStepVo deleteCompensationStep(AS2CompensationStepVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2CompensationVo updateCompensation(AS2CompensationVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2CompensationStepVo updateCompensationStep(AS2CompensationStepVo value) throws Exception {
        /*
         * RDBMS
         */
        return null;
    }
    public AS2TransactionVo rollbackTxnCTS(AS2TransactionVo value) throws Exception {
        /* 
         **/
        return null;
    }
    public AS2CompensationStepVo addCompensationStep(AS2CompensationStepVo value) throws Exception {
        /* 
         **/
        return null;
    }
}