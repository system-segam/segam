import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TestTools {

    public static String base_path = "/Users/caozicheng/Library/CloudStorage/OneDrive-stu.suda.edu.cn/repository/repository-project/FDS4OLAP/core/src/test/java";


    public static void main(String[] args) {
        for (int i = 1; i <= 2 * 3 * 4; i++) {
            String permutationElement = findPermutationElement(i);
            System.out.println(permutationElement);
        }
    }

    public static String findPermutationElement(int index) {
        Map<String, List<String>> container = new LinkedHashMap<>();

        List<String> list1 = Arrays.asList("A", "B", "C");
        List<String> list2 = Arrays.asList("X", "Y");
        List<String> list3 = Arrays.asList("1", "2", "3", "4");

        container.put("list1", list1);
        container.put("list2", list2);
        container.put("list3", list3);

        List<String> keys = new ArrayList<>(container.keySet());

        List<String> result = new ArrayList<>();
        int k = index - 1; // 将索引从1开始的方式转换为从0开始的方式

        for (int i = 0; i < container.values().size(); i++) {
            List<String> currentList = container.get(i);
            int currentSize = currentList.size();
            int currentIndex = k % currentSize; // 当前数组的索引修正
            result.add(currentList.get(currentIndex));
            k = k / currentSize; // 更新k的值
        }

        return String.join("_", result);
    }


    public static List<Integer> getRandomList(int size, int lowBound, int highBound) {
        int[] array = new Random().ints(size, lowBound, highBound).toArray();
        // System.out.println(Arrays.toString(array));
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    public static List<List<Integer>> getRandomTwoDimensionalList(int rows, int size, int lowBound, int highBound) {
        List<List<Integer>> list = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            int[] array = new Random().ints(size, lowBound, highBound).toArray();
            list.add(Arrays.stream(array).boxed().collect(Collectors.toList()));
        }
        return list;
    }

    public static Integer getListMax(List<Integer> list) {
        // return list.stream().mapToInt(x -> x).max().getAsInt();
        return list.stream().max(Integer::compareTo).get();
    }

    public static void readByBR(String filepath) {
        File file = new File(base_path + "/" + filepath);
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeByBR(String filepath, String content) {

        File file = new File(base_path + "/" + filepath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(content);
            writer.flush();
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
