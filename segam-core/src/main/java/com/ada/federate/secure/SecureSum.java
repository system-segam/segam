package com.ada.federate.secure;

import com.ada.federate.cache.ResultKVSet;
import com.ada.federate.rpc.RPCCommon;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecureSum {

    public static class SecretSharingCache {
        // for secure sum  secret sharing
        public int secretSharingCount = 0;
        public List<RPCCommon.SSMessage> ssMessageList = new ArrayList<>();

        public List<List<RPCCommon.SSMessage>> ssMessageListList = new ArrayList<>();
        // public List<Integer> signList = new ArrayList<>();
        // for secure max
        public ResultKVSet signList = new ResultKVSet();

        // public void setSignList(ResultKeyValuePair pair) {
        //     for (String key : pair.keySet()) {
        //         signList.put(key, pair.get(key));
        //     }
        // }

        public List<RPCCommon.SSMessage> get() {
            return ssMessageListList.get(0);
        }

        public void add(List<RPCCommon.SSMessage> ssMessageList) {
            ssMessageListList.add(ssMessageList);
        }

        public List<RPCCommon.SSMessage> get(int rounds) {
            if (ssMessageListList.size() > rounds)
                return ssMessageListList.get(rounds);
            else return null;
        }


        public void clean() {
            ssMessageList.clear();
            ssMessageListList.clear();
        }

        public List<RPCCommon.SSMessage> getSsMessageList() {
            return ssMessageList;
        }
    }


    /**
     * generate random array
     *
     * @param min min value (inclusive)
     * @param max max value (inclusive)
     * @param n   array size
     */
    public static int[] generateRandomNum(int min, int max, int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = RandomUtils.nextInt(min, max + 1);
        }
        return arr;
    }

    /**
     * local encryption
     * <p>
     * f(x) = d + a_1*x + a_2*x^2 + ... . which a_i is random array
     *
     * @param n        silo size
     * @param localSum vi the local counting result of silo F_i
     * @param publicX  n different public parameters
     * @return [0, f(x_1), f(x_2), ... ]
     */
    public static List<Integer> localEncrypt(List<Integer> publicX, int localSum, int n) {
        int[] a = generateRandomNum(1, n, n - 1);
        // if (LogUtils.DEBUG) LogUtils.debug(String.format("random array: %s \n", Arrays.toString(a)));
        List<Integer> fx = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int u = publicX.get(i);
            int temp = localSum;
            for (int k = 1; k < n; k++) {
                // FIXME: value may be overflow while n >= 10
                temp += a[k - 1] * (int) Math.pow(u, k);
                // temp += (long) Math.pow(u, k);
            }
            fx.add(temp);
        }
        return fx;
    }

    /**
     * f(x) = d + a_1*x + a_2*x^2 + ... . which a_i is random array
     *
     * @param a        随机数组
     * @param x        x值
     * @param n        参与方数量
     * @param localSum 待加密值
     * @return
     */
    public static double calcFx(int[] a, int x, int n, double localSum) {
        double temp = localSum;
        for (int k = 1; k < n; k++) {
            // FIXME: value may be overflow while n >= 10
            temp += a[k - 1] * (int) Math.pow(x, k);
        }
        return temp;
    }

    /**
     * @param encryptSumList a set of equal length arrays.
     * @return sum by column.
     */
    // public static List<Double> computeSecureSum(List<RPCCommon.Polynomial> encryptSumList) {
    //     int n = encryptSumList.size();
    //     List<Double> S = new ArrayList<>(Collections.nCopies(n, 0.0));
    //     for (RPCCommon.Polynomial poly : encryptSumList) {
    //         for (int j = 0; j < n; j++) {
    //             S.set(j, S.get(j) + poly.getCoefficient(j));
    //         }
    //     }
    //     return S;
    // }


    /**
     * Given a set of equal length arrays X and Y, return the value of the equation is obtained by Lagrangian interpolation.
     *
     * @param X arrays X
     * @param Y arrays Y
     * @param x given x
     * @return the value of the equation while given x.
     */
    public static int lagrangeInterpolation(int[] X, int[] Y, int x) {
        int n = Y.length;
        double y0;
        double L = 0;
        for (int i = 0; i < n; i++) {
            double k = 1;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    k = k * (x - X[j]) / (double) (X[i] - X[j]);
                }
            }
            k = k * Y[i];
            L = L + k;
        }
        y0 = L;
        return (int) Math.round(y0);
    }

    public static int[] tempArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i + 1;
        }
        return arr;
    }

    public static void main(String[] args) {
        // three data owners
        int n = 3;
        List<Integer> publicX = Arrays.asList(1, 2, 3);
        // local encrypt
        List<Integer> fxa = localEncrypt(publicX, -6, n);
        List<Integer> fxb = localEncrypt(publicX, 0, n);
        List<Integer> fxc = localEncrypt(publicX, 0, n);
        System.out.println(fxa);
        System.out.println(fxb);
        System.out.println(fxc);
        // secret sharing
        List<Integer> fx1 = Arrays.asList(fxa.get(0), fxb.get(0), fxc.get(0));
        List<Integer> fx2 = Arrays.asList(fxa.get(1), fxb.get(1), fxc.get(1));
        List<Integer> fx3 = Arrays.asList(fxa.get(2), fxb.get(2), fxc.get(2));

        System.out.println(fx1);
        System.out.println(fx2);
        System.out.println(fx3);

        // local calc
        int localSum1 = fx1.stream().mapToInt(Integer::intValue).sum();
        int localSum2 = fx2.stream().mapToInt(Integer::intValue).sum();
        int localSum3 = fx3.stream().mapToInt(Integer::intValue).sum();
        System.out.println(localSum1 + " " + localSum2 + " " + localSum3);
        // 解密结果
        int sum = lagrangeInterpolation(publicX.stream().mapToInt(Integer::intValue).toArray(), new int[]{localSum1, localSum2, localSum3}, 0);
        System.out.println(sum);
    }
}
