import org.junit.jupiter.api.RepeatedTest;

import java.util.*;
import java.util.stream.Collectors;


public class TestSecureMedian {
    // private final int s = -1000, e = 1000;
    private final int boundS = 0, boundE = 10000;
    private final Random random = new Random();
    private static final List<Integer> list = Arrays.asList(2574, 5296, 3333, 5854, 3657, 6265, 3843, 8561, 4488, 2866, 5801, 8311, 2194, 1909, 6743, 8658, 5261, 2089, 5370, 2444, 1215, 6750, 5597, 7513, 7918, 4671, 7184, 2096, 3985, 1930, 2566, 5532, 8638, 7537, 8727, 3220, 7688, 7158, 5615, 5682, 6664, 2855, 7673, 3088, 7723, 4700, 9209, 5704, 4008, 8323, 9066, 4041, 6086, 7477, 5000, 6563, 8527, 9673, 9832, 1329, 5725, 1673, 6002, 9521, 5928, 9394, 5151, 9085, 7356, 6746, 9859, 1686, 8146, 8166, 6208, 8095, 4605, 6082, 6881, 8461, 8570, 2755, 7004, 3505, 5250, 6806, 3340, 3278, 9826, 3546, 4839, 8986, 6713, 1379, 4609, 4550, 8154, 9901, 4186, 8248, 9236, 8974, 7756, 9146, 9913, 5454, 6767, 6313, 3960, 3750, 1203, 1489, 4555, 2003, 3741, 9320, 2701, 4133, 9098, 4293, 5290, 4924, 5676, 4957, 5346, 8757, 7503, 1636, 4578, 4120, 1103, 1471, 2845, 6182, 5139, 2082, 6217, 7960, 1523, 2217, 8688, 8185, 3761, 8090, 5922, 2154, 1474, 4555, 7212, 1844, 8555, 1634, 7779, 2163, 7709, 7975, 9407, 4604, 5960, 5513, 2683, 3405, 2855, 9273, 6457, 4271, 9321, 9919, 5954, 9088, 9166, 6839, 7242, 9669, 5405, 5364, 6629, 7380, 5861, 4606, 2779, 9545, 7549, 3576, 8115, 8690, 7493, 5665, 4954, 2320, 9038, 3975, 9120, 2850, 7093, 8784, 9042, 5594, 9663, 8556, 6058, 1836, 7713, 8409, 6916, 6782, 8264, 3667, 5041, 9354, 4280, 6378, 8717, 8080, 9141, 5214, 1409, 5737, 9338, 1978, 3110, 9511, 7024, 8536, 5764, 1436, 3565, 2382, 3759, 4837, 9392, 3299, 3924, 8737, 6182, 1249, 2322, 2056, 9123, 9798, 1613, 5129, 7339, 1135, 2716, 1872, 1807, 9054, 5781, 3450, 5688, 9266, 9327, 4189, 1872, 1413, 6569, 5288, 7494, 8825, 4192, 8890, 7947, 3126, 5549, 3765);

    public static void main(String[] args) {
        // int median = new TestSecureMedian().getCorrectMedian(list);
        float median = new TestSecureMedian().binaryMedian1(list);
        System.out.println(median);
    }

    @RepeatedTest(10000)
    // @Test
    public void testAlgorithmValidity() {
        int size = random.nextInt(100) + 100;
        // int size = 11;
        List<Integer> list = TestTools.getRandomList(size, boundS, boundE);
        System.out.println(list);
        float correctMedian = getCorrectMedian(list);
        float binaryMedian = binaryMedian1(list);
        // if (correctMedian != binaryMedian) {
        //     TestTools.writeByBR("1.txt", list.stream().map(Object::toString).collect(Collectors.joining(",")));
        //     TestTools.writeByBR("1.txt", correctMedian + " " + binaryMedian);
        // }
        assert correctMedian == binaryMedian;
    }

    public float binaryMedian1(List<Integer> arr) {
        int lbound = boundS, rbound = boundE;
        boolean flag = false;
        while (lbound < rbound) {
            int thres = (lbound + rbound + 1) / 2;
            int l_count = 0, r_count = 0;
            for (int val : arr) {
                if (val >= thres)
                    r_count++;
                else
                    l_count++;
            }
            System.out.printf("[%s, %s], %s, %s\n", lbound, rbound, thres, l_count - r_count);
            if (l_count - r_count >= 0) {
                rbound = thres - 1;
                flag = l_count == r_count;
            } else
                lbound = thres;

        }
        int l = lbound;
        if (flag) {
            // lbound = boundS;
            rbound = boundE;
            while (lbound < rbound) {
                int thres = (lbound + rbound) / 2;
                int l_count = 0, r_count = 0;
                for (int val : arr) {
                    if (val <= thres)
                        l_count++;
                    else
                        r_count++;
                }
                System.out.printf("[%s, %s], %s, %s\n", lbound, rbound, thres, l_count - r_count);
                if (l_count - r_count <= 0) {
                    lbound = thres + 1;
                } else
                    rbound = thres;
            }
        }
        int r = lbound;
        return (r + l) / 2.0f;
    }

    public float binaryMedian(List<Integer> arr) {
        // int thres = arr.size() % 2 == 0 ? arr.size() / 2 - 1 : arr.size() / 2;
        int n = arr.size();
        int lbound = boundS, rbound = boundE;
        while (lbound < rbound) {
            int mid = (lbound + rbound) / 2;
            int count = getThresholdSign(arr, mid);
            if (count > n / 2)
                lbound = mid + 1;
            else
                rbound = mid;
        }
        int l = rbound;

        if (n % 2 == 0) {
            lbound = l;
            rbound = boundE;
            while (lbound < rbound) {
                int mid = (lbound + rbound) / 2;
                int count = getThresholdSign(arr, mid);
                if (count >= n / 2)
                    lbound = mid + 1;
                else
                    rbound = mid;
            }
        }
        int r = rbound;
        return (r + l) / 2.0f;
    }

    /***
     *
     * @return
     */
    public int getThresholdSign(List<Integer> arr, int thres) {
        int cnt = 0;
        for (int val : arr) {
            if (val > thres)
                cnt++;
        }
        return cnt;
    }


    public float getCorrectMedian(List<Integer> arr) {
        arr.sort(Comparator.comparingInt(x -> x));
        int size = arr.size();
        return size % 2 == 0 ? (arr.get((size - 1) / 2) + arr.get(size / 2)) / 2.0f : arr.get(size / 2);
    }
}
