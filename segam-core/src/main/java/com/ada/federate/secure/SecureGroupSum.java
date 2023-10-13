package com.ada.federate.secure;

import com.ada.federate.cache.ResultKVSet;
import com.ada.federate.rpc.RPCCommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SecureGroupSum {

    // public static List<Integer> publicX = Arrays.asList(1, 2, 3);

    public static List<RPCCommon.SSMessage> localEncrypt(ResultKVSet resultKVSet, List<Integer> publicX, int n) {
        // resultTable.get()
        int[][] share = new int[n][];
        for (int i = 0; i < n; i++) {
            share[i] = new int[resultKVSet.rowNumber];
        }
        int i = 0;
        for (String key : resultKVSet.keySet()) {
            int val = resultKVSet.get(key);
            List<Integer> fx = SecureSum.localEncrypt(publicX, val, n);
            for (int k = 0; k < fx.size(); k++) {
                share[k][i] = fx.get(k);
            }
            i++;
        }
        // for (int i = 0; i < resultKeyValuePair.rowNumber; i++) {
        //     long val = resultKeyValuePair.getAggVal().get(i);
        //     List<Long> fx = SecureSum.localEncrypt(publicX, val, n);
        //     for (int k = 0; k < fx.size(); k++) {
        //         share[k][i] = fx.get(k);
        //     }
        // }

        List<RPCCommon.SSMessage> ssMessage = new ArrayList<>();
        for (i = 0; i < n; i++) {
            RPCCommon.SSMessage.Builder ssMessageBuilder = RPCCommon.SSMessage.newBuilder();
            ssMessageBuilder.addAllShareVal(Arrays.stream(share[i]).boxed().collect(Collectors.toList()));
            ssMessage.add(ssMessageBuilder.build());
        }
        return ssMessage;
    }


    public static int[] decryptResult(List<RPCCommon.SSMessage> ssMessageList, List<Integer> publicX, int n) {
        int count = ssMessageList.get(0).getShareValCount();
        int[] result = new int[count];
        // for (int i = 0; i < count / filedCount; i++) {
        //     result[i] = new int[filedCount];
        // }
        int[] tempArray = new int[n];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < n; j++)
                tempArray[j] = ssMessageList.get(j).getShareVal(i);
            int sum = SecureSum.lagrangeInterpolation(publicX.stream().mapToInt(Integer::intValue).toArray(), tempArray, 0);
            result[i] = sum;
        }
        return result;
    }


    public static List<Integer> generatePublicKeyList(int size) {
        List<Integer> xList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            xList.add(i + 1);
        }
        return xList;
    }

    /***
     * globalKeySet - localKeySet
     * @param globalKeyList
     * @param localKeyList
     * @return
     */
    public static List<String> getDifferenceSet(List<String> globalKeyList, List<String> localKeyList) {
        List<String> differentSet = new ArrayList<>();
        for (String val : localKeyList) {
            if (!globalKeyList.contains(val)) {
                differentSet.add(val);
            }
        }
        return differentSet;
    }

    public static List<Integer> mergeSSMessage(List<RPCCommon.SSMessage> ssMessageList) {
        int[] secretVal = new int[ssMessageList.get(0).getShareValCount()];

        for (RPCCommon.SSMessage ssMessage : ssMessageList) {
            // LogUtils.debug(ssMessage.getShareValCount());
            for (int i = 0; i < ssMessage.getShareValCount(); i++) {
                secretVal[i] += ssMessage.getShareVal(i);
            }
        }

        return Arrays.stream(secretVal).boxed().collect(Collectors.toList());
    }

}
