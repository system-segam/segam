import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestSecretSharing {
    public static void main(String[] args) {
        List<Integer> tempList = splitInteger(200, 3);
        System.out.println(tempList);
        System.out.println(tempList.stream().mapToInt(x -> x).sum());
    }

    private final static Random random = new Random();

    public static List<Integer> splitInteger(Integer i, Integer k) {
        List<Integer> list = new ArrayList<>();
        while (k-- != 1) {
            int temp = random.nextInt(i / 2);
            if ((temp & 3) != 0) temp = ~temp + 1;
            i -= temp;
            list.add(temp);
        }
        list.add(i);
        return list;
    }

    // public void test1() {
    //
    // }
}
