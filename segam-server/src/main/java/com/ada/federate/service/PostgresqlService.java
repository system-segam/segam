package com.ada.federate.service;

import com.ada.federate.cache.ResultKVSet;
import com.ada.federate.cache.ResultKVsSet;
import com.ada.federate.pojo.DriverConfig;
import com.ada.federate.rpc.RPCCommon;
import com.ada.federate.secure.SecureSum;
import com.ada.federate.rpc.impl.RPCServiceImpl;
import com.ada.federate.sql.SQLExecutor;
import com.ada.federate.cache.ResultColumn;
import com.ada.federate.utils.LogUtils;
import com.ada.federate.utils.ResultsUtils;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// import static com.ada.federate.TestApplication.timeframeList;

public class PostgresqlService extends RPCServiceImpl {

    private SQLExecutor executor;

    public PostgresqlService(DriverConfig config) throws ClassNotFoundException, SQLException, IOException {
        Class.forName(config.getDriver());
        // Properties properties = new Properties();
        // properties.setProperty("user", config.getUser());
        // properties.setProperty("password", config.getPassword());
        // properties.setProperty("client_name", config.getName());
        Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        // Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        executor = new SQLExecutor(conn);
    }

    @Override
    public void privateQuery(RPCCommon.SQLExpression sqlExpression, StreamObserver<RPCCommon.Status> responseObserver) {
        RPCCommon.Status.Builder statusBuilder = RPCCommon.Status.newBuilder();
        try {
            // timeframeList.clear();
            // startTime = System.currentTimeMillis();
            ResultSet rs = executor.executeSql(sqlExpression);
            // endTime = System.currentTimeMillis();
            // timeframeList.put("plaintext", endTime - startTime);
            if (sqlExpression.getAggField().toLowerCase().contains("median")) {
                ResultKVsSet resultKVsSet = new ResultKVsSet(rs);
                // if (LogUtils.DEBUG)
                // LogUtils.info(String.format("result size: %d", resultKVsSet.rowNumber));
                buffer.sharingCache = new SecureSum.SecretSharingCache();
                buffer.localResultKeyValuesTable = resultKVsSet;
            } else {
                ResultKVSet resultKVSet = new ResultKVSet(rs);
                LogUtils.info(String.format("result size: %d", resultKVSet.rowNumber));
                // if (LogUtils.DEBUG) LogUtils.debug(String.format("local query result: %s \n", resultKeyValuePair));
                buffer.sharingCache = new SecureSum.SecretSharingCache();
                buffer.localResultTable = resultKVSet;
            }
            rs.close();
            statusBuilder.setCode(true);
        } catch (Exception e) {
            String temp = LogUtils.buildErrorMessage(e);
            // LogUtils.error(temp);
            statusBuilder.setCode(false).setMessage(temp);
        } finally {
            responseObserver.onNext(statusBuilder.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void pubicQuery(RPCCommon.SQLExpression sqlExpression, StreamObserver<RPCCommon.RPCResult> responseObserver) {
        ResultSet rs;
        RPCCommon.RPCResult.Builder resultBuilder = null;
        try {
            rs = executor.executeSql(sqlExpression);
            resultBuilder = ResultsUtils.resultSet2RPCResult(rs);
            rs.close();
            LogUtils.info(String.format("result size: %d", resultBuilder.getKeyCount()));
        } catch (Exception e) {
            String temp = LogUtils.buildErrorMessage(e);
            LogUtils.debug(temp);
            resultBuilder.setStatus(RPCCommon.Status.newBuilder().setCode(false).setMessage(temp).build());
        } finally {
            responseObserver.onNext(resultBuilder.setStatus(RPCCommon.Status.newBuilder().setCode(true).build()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void batchQuery(RPCCommon.SQLMessage sqlMessage, StreamObserver<RPCCommon.Status> responseObserver) {
        RPCCommon.Status.Builder statusBuilder = RPCCommon.Status.newBuilder();
        try {
            List<ResultColumn> resultColumnList = new ArrayList<>();
            for (String sql : sqlMessage.getSqlList()) {
                ResultSet rs = executor.executeSql(sql);
                resultColumnList.add(ResultColumn.resultSet2ResultColumn(rs));
                rs.close();
            }

            statusBuilder.setCode(true).setMessage(ResultColumn.batchDump(resultColumnList));
        } catch (Exception e) {
            String temp = LogUtils.buildErrorMessage(e);
            statusBuilder.setCode(false).setMessage(temp);
        } finally {
            responseObserver.onNext(statusBuilder.build());
            responseObserver.onCompleted();
        }
    }
}
