package com.ada.federate.cache;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class ResultKVsSet {
    private Map<String, List<Integer>> resultPair = new LinkedHashMap<>();
    public Integer rowNumber = 0;

    public ResultKVsSet() {

    }

    // 计算每个 key 对应 List 的中位数
    public Map<String, Integer> getMedianValues() {
        Map<String, Integer> medianValues = new LinkedHashMap<>();

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

            medianValues.put(key, median);
        }

        return medianValues;
    }

    public ResultKVsSet(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        for (String key : jsonObject.keySet()) {
            JSONArray arr = jsonObject.getJSONArray(key);
            for (int i = 0; i < arr.size(); i++) {
                addRow(key, arr.getIntValue(i));
            }
        }
    }


    public ResultKVsSet(ResultSet rs) throws Exception {
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
                addRow(key, val);
            }
        } else if (columnCount == 1) {
            // 无 dimension
            while (rs.next()) {
                Integer val = Integer.parseInt(rs.getObject(1).toString());
                addRow("", val);
            }
        }
    }

    public String dump() {
        return JSONObject.toJSONString(resultPair);
    }

    public boolean contains(String key) {
        return resultPair.containsKey(key);
    }

    public Set<String> keySet() {
        return resultPair.keySet();
    }

    public List<Integer> get(String key) {
        return resultPair.get(key);
    }

    public void addRow(String key, Integer val) {
        if (!contains(key)) {
            resultPair.put(key, new ArrayList<>());
        }
        resultPair.get(key).add(val);
        rowNumber++;
    }

    public void addRow(String key, List<Integer> val) {
        if (!contains(key)) {
            resultPair.put(key, new ArrayList<>());
        }
        resultPair.get(key).addAll(val);
        rowNumber++;
    }


    public void addAll(ResultKVsSet resultKVsSet) {
        for (String key : resultKVsSet.keySet()) {
            addRow(key, resultKVsSet.get(key));
        }
    }

    public static void main(String[] args) {
        ResultKVsSet resultKVSet = new ResultKVsSet();
        resultKVSet.addRow("shanghai", 1);
        resultKVSet.addRow("shanghai", 2);
        resultKVSet.addRow("shanghai", 5);
        resultKVSet.addRow("shanghai", 6);
        resultKVSet.addRow("shanghai", 7);
        resultKVSet.addRow("guangzhou", 3);
        // System.out.println(resultKVSet.dump());
        ResultKVsSet resultKVsSet = new ResultKVsSet(resultKVSet.dump());
        System.out.println(resultKVsSet.getMedianValues());

        // System.out.println(ResultKeyValuePair.parse(resultKeyValuePair.dump()).dump());
    }
}
