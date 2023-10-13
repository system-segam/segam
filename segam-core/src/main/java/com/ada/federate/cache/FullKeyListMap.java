package com.ada.federate.cache;

import com.ada.federate.utils.ArrayUtils;

import java.util.*;

public class FullKeyListMap {
    private Map<String, List<String>> container = new LinkedHashMap<>();
    public List<String> columnNameList = new ArrayList<>();
    public List<List<String>> keyListList = new ArrayList<>();

    // public void addList(String key, List<String> list) {
    //     keyList.put(key, list);
    //     for (int i = 0; i < list.size(); i++) {
    //         keyIndexMap.put(list.get(i), i);
    //     }
    // }

    public void addKeyList(String columnName, List<String> keyList) {
        columnNameList.add(columnName);
        keyListList.add(keyList);
        container.put(columnName, keyList);
    }

    public String findPermutationElement(int index) {

        List<String> result = new ArrayList<>();
        int k = index - 1; // 将索引从1开始的方式转换为从0开始的方式

        for (int i = 0; i < keyListList.size(); i++) {
            List<String> currentList = keyListList.get(i);
            int currentSize = currentList.size();
            int currentIndex = k % currentSize; // 当前数组的索引修正
            result.add(currentList.get(currentIndex));
            k = k / currentSize; // 更新k的值
        }

        return String.join("_", result);
    }
}
