package com.ada.federate;

import com.ada.federate.cache.ResultKVSet;
import com.ada.federate.pojo.ClientConfig;
import com.ada.federate.rpc.RPCCommon;
import com.ada.federate.utils.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class TestApplication {
    private static PRCClient queryInterface = null;
    public static LinkedHashMap<String, Long> timeframeList = new LinkedHashMap<>();
    private static RPCCommon.SQLExpression sqlExpression = null;
    private static String querySQL, sqlFileName, publicOrPrivate, targetSchema;
    private static Integer dataSiloNumber = 3, testCount;
    public static void main(String[] args) throws Exception {
        try {
            sqlFileName = args[0];
            publicOrPrivate = args[1];
            targetSchema = args[2];
            testCount = Integer.valueOf(args[3]);
            ThreadTools.delay = Long.parseLong(args[4]);
            System.out.println(Arrays.toString(args));
            System.out.println(String.format("args length: %d\n", args.length));
            dataSiloNumber = args.length == 6 ? Integer.parseInt(args[5]) : 3;
            System.out.println(String.format("data silo number %d\n", dataSiloNumber));
            // TODO 初始化工作
            ClientConfig clientConfig = new ClientConfig("config.json");
            queryInterface = new PRCClient(clientConfig);
            querySQL = PathUtils.readSQL("sql", sqlFileName);
            sqlExpression = SQLUtils.parseSQL2SQLExpression(querySQL, targetSchema);
            LogUtils.debug(String.format("查询语句为：%s", querySQL));
            queryInterface.rpcHello(sqlExpression);
            // 启动流量监控
            for (int i = 1; i <= dataSiloNumber; i++) {
                TestUtils.winCMD(String.format("docker exec postgres%d bash -c \"cd root/container && ./start-monitor.sh\"", i));
            }
            Thread.sleep(2500);
            for (int i = 0; i < testCount; i++) {
                if (publicOrPrivate.contains("public")) {
                    long startTime, endTime;
                    // TODO public query
                    startTime = System.currentTimeMillis();
                    ResultKVSet publicQueryResult = queryInterface.publicQuery(sqlExpression);
                    endTime = System.currentTimeMillis();
                    Tools.formatPrintTestInfo(startTime, endTime);
                    // publicQueryResult.printResultTable(sqlExpression, 20);
                    // ResultKeyValuePair.batchPrintResultTable(sqlExpression, publicQueryResultTableList);
                } else {
                    long startTime, endTime;
                    // TODO public query
                    startTime = System.currentTimeMillis();
                    ResultKVSet privateQueryResult = queryInterface.privateQuery(sqlExpression,
                            SQLUtils.parseQueryType(sqlExpression));
                    endTime = System.currentTimeMillis();
                    Tools.formatPrintTestInfo(startTime, endTime);
                    timeframeList.clear();
                    // privateQueryResult.printResultTable(sqlExpression, 20);
                }
                queryInterface.cleanBuffer(sqlExpression.getUuid());
                // Thread.sleep(200);
            }

            if (PRCClient.executorService != null)
                PRCClient.executorService.shutdown();
            System.exit(0);
        } catch (Exception e) {
            LogUtils.error(LogUtils.buildErrorMessage(e));
        }
    }


    static {
        // sqlFileName="test_sum.sql";
        sqlFileName="test_max.sql";
        // sqlFileName = "test_median.sql";
        targetSchema = "schema2_10";
    }

    @Test
    public void testMain() {
        try {
            long startTime, endTime;
            startTime = System.currentTimeMillis();
            // TODO public query
            ResultKVSet publicQueryResultTable = queryInterface.publicQuery(sqlExpression);
            // ResultKeyValuePair.batchPrintResultTable(sqlExpression, publicQueryResultTableList);
            endTime = System.currentTimeMillis();

            Tools.formatPrintTestInfo(startTime, endTime);
            // publicQueryResultTable.printResultTable(sqlExpression);
            // TODO private query
            SQLUtils.AGG_FUNCTION function = SQLUtils.parseQueryType(sqlExpression);
            startTime = System.currentTimeMillis();
            ResultKVSet privateQueryResultKVSet = queryInterface.privateQuery(sqlExpression, function);
            endTime = System.currentTimeMillis();
            // privateQueryResultKVSet.printResultTable(sqlExpression, 100);
            Tools.formatPrintTestInfo(startTime, endTime);
            // privateQueryResultKeyValuePair.printResultTable(sqlExpression);
            ResultKVSet.batchPrintResultTable(sqlExpression, publicQueryResultTable, privateQueryResultKVSet);
            queryInterface.cleanBuffer(sqlExpression.getUuid());

        } catch (Exception e) {
            LogUtils.error(LogUtils.buildErrorMessage(e));
        }
    }

    @Test
    public void testPublic() {
        try {
            long startTime, endTime;
            // TODO public query
            startTime = System.currentTimeMillis();
            ResultKVSet publicQueryResultTable = queryInterface.publicQuery(sqlExpression);
            endTime = System.currentTimeMillis();
            Tools.formatPrintTestInfo(startTime, endTime);
            publicQueryResultTable.printResultTable(sqlExpression, 20);
            queryInterface.cleanBuffer(sqlExpression.getUuid());
        } catch (Exception e) {
            LogUtils.error(LogUtils.buildErrorMessage(e));
        }
    }

    @Test
    public void testPrivate() {
        try {
            long startTime, endTime;
            // TODO private query
            SQLUtils.AGG_FUNCTION function = SQLUtils.parseQueryType(sqlExpression);
            startTime = System.currentTimeMillis();
            ResultKVSet privateQueryResult = queryInterface.privateQuery(sqlExpression, function);
            endTime = System.currentTimeMillis();
            Tools.formatPrintTestInfo(startTime, endTime);
            privateQueryResult.printResultTable(sqlExpression, 20);
            queryInterface.cleanBuffer(sqlExpression.getUuid());
            // System.out.printf("connectedCount: %d, Avg RTT: %.3f ms \n", pingTimes, rtt);
        } catch (Exception e) {
            LogUtils.error(LogUtils.buildErrorMessage(e));
        }
    }

    @BeforeAll
    static void beforeFunction() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                for (int i = 1; i <= dataSiloNumber; i++) {
                    TestUtils.winCMD(String.format("docker start postgres%d", i));
                    TestUtils.winCMD(String.format("docker exec postgres%d bash -c \"tc qdisc add dev eth0 root netem delay 2ms rate 100mbit\"", i));
                    TestUtils.winCMD(String.format("docker exec postgres%d bash -c \"cd root/container && ./start-driver.sh \"", i));
                }
            }
            Thread.sleep(2000);
            // TODO 初始化工作
            ClientConfig clientConfig = new ClientConfig("config.json");
            queryInterface = new PRCClient(clientConfig);
            querySQL = PathUtils.readSQL("sql", sqlFileName);
            sqlExpression = SQLUtils.parseSQL2SQLExpression(querySQL, targetSchema);
            // System.out.println(sqlExpression);
            queryInterface.rpcHello(sqlExpression);

            for (int i = 1; i <= dataSiloNumber; i++) {
                TestUtils.winCMD(String.format("docker exec postgres%d bash -c \"cd root/container && ./start-monitor.sh\"", i));
            }

            Thread.sleep(3000);

            // LogUtils.debug("rpc hello completed.");
            // for (int i = 1; i <= dataSiloNumber; i++) {
            //     // TestUtils.winCMD(String.format("docker exec clickhouse%d bash -c \"sudo sync && echo 3 | sudo tee /proc/sys/vm/drop_caches\"", i));
            //     // TestUtils.winCMD(String.format("docker exec clickhouse%d bash -c \"clickhouse-client -h 127.0.0.1 --port 9000 -u default --password '123465' --query='SYSTEM DROP UNCOMPRESSED CACHE';\"", i));
            //     TestUtils.winCMD(String.format("docker exec clickhouse%d bash -c \"clickhouse-client -h 127.0.0.1 --port 9000 -u default --password '123465' --query='select count(1) from %s.lineorder_flat';\"", i, targetSchema));
            // }
        } catch (Exception e) {
            LogUtils.error(LogUtils.buildErrorMessage(e));
        }
    }

    @AfterAll
    static void afterFunction() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                for (int i = 1; i <= dataSiloNumber; i++) {
                    TestUtils.winCMD(String.format("docker exec postgres%d bash -c \"cd root/container && ./stop-all.sh\"", i));
                }
            }
            if (PRCClient.executorService != null)
                PRCClient.executorService.shutdown();
            System.exit(0);
        } catch (Exception e) {
            LogUtils.error(LogUtils.buildErrorMessage(e));
        }
    }
}
