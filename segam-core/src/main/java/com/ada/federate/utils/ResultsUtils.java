package com.ada.federate.utils;

import com.ada.federate.cache.ResultKVSet;
import com.ada.federate.rpc.RPCCommon;
import com.alibaba.fastjson2.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ResultsUtils {

    public static ResultKVSet assembleResultTable(List<RPCCommon.RPCResult> resultKVSetList, RPCCommon.SQLExpression expression) {
        Map<String, Integer> mergeResultKVPS = new LinkedHashMap<>();
        // ResultKVP.Builder builder = ResultKVP.newBuilder();
        if (expression.getAggField().toLowerCase().contains("sum") || expression.getAggField().toLowerCase().contains(
                "count")) {

            for (RPCCommon.RPCResult table : resultKVSetList) {
                for (int i = 0; i < table.getKeyCount(); i++) {
                    String key = table.getKey(i);
                    Integer value = table.getValue(i);
                    Integer tempVal = mergeResultKVPS.getOrDefault(key, 0);
                    mergeResultKVPS.put(key, tempVal + value);
                }
            }

        } else if (expression.getAggField().toLowerCase().contains("max")) {

            for (RPCCommon.RPCResult table : resultKVSetList) {
                for (int i = 0; i < table.getKeyCount(); i++) {
                    String key = table.getKey(i);
                    Integer value = table.getValue(i);
                    Integer tempVal = mergeResultKVPS.getOrDefault(key, Integer.MIN_VALUE);
                    if (value > tempVal)
                        mergeResultKVPS.put(key, value);
                }
            }
        } else if (expression.getAggField().toLowerCase().contains("median")) {
            Map<String, List<Integer>> resultPair = new LinkedHashMap<>();

            for (RPCCommon.RPCResult table : resultKVSetList) {
                for (int i = 0; i < table.getKeyCount(); i++) {
                    String key = table.getKey(i);
                    Integer tempVal = table.getValue(i);
                    if (!resultPair.containsKey(key)) {
                        resultPair.put(key, new ArrayList<>());
                    }
                    resultPair.get(key).add(tempVal);
                }
            }

            for (Map.Entry<String, List<Integer>> entry : resultPair.entrySet()) {
                String key = entry.getKey();
                List<Integer> values = entry.getValue();
                int len = values.size();
                // System.out.println(values);
                // 对 List 进行排序
                Collections.sort(values);

                // 计算中位数
                int median;
                if (len % 2 == 0) {
                    median = (values.get(len / 2 - 1) + values.get(len / 2)) / 2;
                } else {
                    median = values.get(len / 2);
                }

                mergeResultKVPS.put(key, median);
            }

        }
        return new ResultKVSet(mergeResultKVPS);
    }


    public static ResultKVSet array2QueryResult(int[] results, List<String> fullKeyList) throws Exception {
        if (results.length != fullKeyList.size()) {
            throw new Exception(String.format("Error: results size: [%d], full key size: [%d] ", results.length, fullKeyList.size()));
        }
        ResultKVSet resultKVSet = new ResultKVSet();
        for (int i = 0; i < results.length; i++) {
            resultKVSet.addRow(fullKeyList.get(i), results[i]);
        }
        return resultKVSet;
    }

    public static String getCurrentTime() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 定义东八区的时区偏移量
        int zoneOffset = 8;
        // 构造时区对象
        var zoneId = "GMT+" + zoneOffset;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // 格式化当前时间为 "HH:mm:ss" 的字符串
        return now.atZone(java.time.ZoneId.of(zoneId))
                .format(formatter);
    }

    // public static void printResultRows(RPCCommon.QueryResults resultRows, List<String> metricList, List<String> dimensionList) {
    //     // List<List<Objects>> resultRowList = new ArrayList<>();
    //     if (resultRows == null) return;
    //
    //     List<String> expandList = new ArrayList<>();
    //     expandList.addAll(dimensionList);
    //     expandList.addAll(metricList);
    //
    //     for (int i = 0; i < expandList.size(); i++) {
    //         System.out.printf("%-25s", expandList.get(i));
    //     }
    //     System.out.println();
    //
    //     for (int i = 0; i < resultRows.getRecordCount(); i++) {
    //         System.out.printf("%-25s", resultRows.getRecord(i).getKey());
    //         for (int j = 0; j < resultRows.getRecord(i).getElementCount(); j++) {
    //             System.out.printf("%-25s", resultRows.getRecord(i).getElement(j));
    //         }
    //         System.out.println();
    //     }
    // }


    public static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData(); // 获取键名
        int columnCount = md.getColumnCount(); // 获取行的数量
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("%-25s", md.getColumnName(i));
        }
        System.out.println();
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-25s", rs.getObject(i));
            }
            System.out.println();
        }
    }

    public static List<Map<String, Object>> convertList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList();
        ResultSetMetaData md = rs.getMetaData(); // 获取键名
        int columnCount = md.getColumnCount(); // 获取行的数量
        while (rs.next()) {
            Map<String, Object> rowData = new HashMap();// 声明Map
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));// 获取键名及值
            }
            list.add(rowData);
        }
        return list;
    }

    public static <T> String flatList(List<T> list, Class<T> clazz) {
        // String str = "";
        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            if (clazz == RPCCommon.SSMessage.class) {
                RPCCommon.SSMessage ssMessage = (RPCCommon.SSMessage) t;
                // sb.append(ssMessage.getKeyList()).append("\n");
                sb.append("from index: ").append(ssMessage.getFrom()).append(" ");
                sb.append(ssMessage.getShareValList()).append("\n");
            }
        }
        return sb.toString();
    }


    public static RPCCommon.RPCResult.Builder resultSet2RPCResult(ResultSet rs) throws SQLException {
        List<String> keyList = new ArrayList<>();
        List<Integer> valueList = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData(); // 获取键名
        int columnCount = md.getColumnCount(); // 获取列的数量

        if (columnCount == 2) {
            //  dimension >= 1
            while (rs.next()) {
                // List<Object> list;
                String key = rs.getObject(2).toString();
                String str = rs.getObject(1).toString();
                int decimalIndex = str.indexOf(".");
                String integerPart = (decimalIndex != -1) ? str.substring(0, decimalIndex) : str;
                Integer val = Integer.parseInt(integerPart);
                keyList.add(key);
                valueList.add(val);
            }
        } else if (columnCount == 1) {
            // 无 dimension
            while (rs.next()) {
                Integer val = Integer.parseInt(rs.getObject(1).toString());
                keyList.add("");
                valueList.add(val);
            }
        }
        return RPCCommon.RPCResult.newBuilder().addAllKey(keyList).addAllValue(valueList);
    }

    public static String map2JsonString(Map<String, List<String>> fullKeyListMap) {
        // Map<String, String> tempMap = new HashMap<>();
        // for (String key : fullKeyListMap.keySet()) {
        //     List<String> tempList = fullKeyListMap.get(key);
        //     tempMap.put(key,String.join(",", tempList)) ;
        // }
        return JSONObject.toJSONString(fullKeyListMap);
    }

    public static void main(String[] args) {
        Map<String, List<String>> fullKeyListMap = new HashMap<>();
        fullKeyListMap.put("city", Arrays.asList("shagnhai", "beijing", "nanjing"));
        fullKeyListMap.put("year", Arrays.asList("1892", "1998", "2562"));

        System.out.println();
        JSONObject jsonObject = JSONObject.parse(map2JsonString(fullKeyListMap));
        System.out.println(jsonObject);
        for (String key : jsonObject.keySet()) {
            System.out.println(jsonObject.getJSONArray(key));
        }
    }
}
