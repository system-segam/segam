import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TestSecureMax {
    private final int s = 1, e = 100;
    // private final int s = -10, e = 10;
    private final Random random = new Random();
    private static final List<Integer> list = Arrays.asList(-10, 6, 3, 5, 2, 2, 2, 6, -2, 1, -9);

    public static void main(String[] args) {
        // int[] arr = new int[]{-10, 6, 3, 5, 2, 2, 2, 6, -2, 1, -9};
        List<Integer> list = Arrays.asList(52, 68, 75);
        // int max = new TestSecureMax().binaryMax(list);
        int max = new TestSecureMax().binaryMax(list);
        System.out.println(max);
        // System.out.println(max);

    }

    @RepeatedTest(10)
    // @Test
    public void testAlgorithmValidity() {
        int size = random.nextInt(100) + 1;

        List<Integer> list = TestTools.getRandomList(size, s, e);
        int correctMax = TestTools.getListMax(list);
        int binaryMax = binaryMax(list);
        if (correctMax != binaryMax) {
            TestTools.writeByBR("1.txt", list.stream().map(Object::toString).collect(Collectors.joining(",")));
            TestTools.writeByBR("1.txt", correctMax + " " + binaryMax);
        }
        assert correctMax == binaryMax;
    }

    @RepeatedTest(10000)
    // @Test
    public void testAlgorithmValidity1() {
        int size = random.nextInt(200) + 100;
        // int size = 5;
        List<List<Integer>> listlist = TestTools.getRandomTwoDimensionalList(size, 10, 0, 100);

        int[] correctMax = new int[size];
        for (int i = 0; i < size; i++) {
            correctMax[i] = TestTools.getListMax(listlist.get(i));
        }

        int[][] array = new int[listlist.size()][];
        for (int i = 0; i < listlist.size(); i++) {
            array[i] = new int[listlist.get(i).size()];
            for (int j = 0; j < listlist.get(i).size(); j++) {
                array[i][j] = listlist.get(i).get(j);
            }
        }

        int[] binaryMax = batchBinaryMax(array);

        if (!equals(correctMax, binaryMax)) {
            TestTools.writeByBR("1.txt", listlist.toString());
            TestTools.writeByBR("1.txt", Arrays.toString(correctMax));
            TestTools.writeByBR("1.txt", Arrays.toString(binaryMax));
        }
        assert equals(correctMax, binaryMax);

    }

    public boolean equals(int[] a1, int[] a2) {
        if (a1.length != a2.length) return false;
        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i])
                return false;
        }
        return true;
    }


    public int binaryMax(List<Integer> arr) {

        int l = s, r = e;
        int threshold = -1;
        while (l < r) {
            // System.out.println(l + " " + r);
            int lastThreshold = threshold;
            threshold = (l + r + 1) >> 1;
            System.out.println(threshold);
            int count = 0;
            for (int val : arr) {
                int sign = val >= threshold ? random.nextInt(10) + 1 : 0;
                count += sign;
            }
            if (count == 0) r = threshold - 1;
                // 不一定是找大了，最大值有可能重复
            else
                l = threshold;

            // else if (count > 1) l = threshold;
            // else if (count == 1)
            //     return getThresholdMax(arr, threshold);
        }
        // 算法最差情况：最大值重复
        return l;
    }

    public int[] batchBinaryMax(int[][] arr) {
        int s = 0, e = 100;
        // int l = s, r = e;
        int[] l = new int[arr.length];
        int[] r = new int[arr.length];
        Arrays.fill(l, s);
        Arrays.fill(r, e);
        while (judge(l, r)) {
            for (int i = 0; i < l.length; i++) {
                if (l[i] >= r[i]) continue;
                int threshold = (l[i] + r[i] + 1) >> 1;
                int count = 0;
                for (int val : arr[i]) {
                    int sign = val >= threshold ? 1 : 0;
                    count += sign;
                }
                if (count == 0) r[i] = threshold - 1;
                    // 不一定是找大了，最大值有可能重复
                else if (count >= 1) l[i] = threshold;
                // else if (count == 1)
                //     return getThresholdMax(arr, threshold);
            }
        }
        return l;
    }

    public boolean judge(int[] l, int[] r) {
        for (int i = 0; i < l.length; i++) {
            if (l[i] < r[i])
                return true;
        }
        return false;
    }


    public int getThresholdMax(List<Integer> arr, int threshold) {
        for (int val : arr) {
            if (val >= threshold)
                return val;
        }
        return threshold - 1;
    }


}
