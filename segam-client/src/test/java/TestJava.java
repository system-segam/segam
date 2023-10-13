import java.util.LinkedHashMap;
import java.util.Map;

public class TestJava {

    public static void main(String[] args) throws Exception {
        // List<Integer> ret = IntStream.rangeClosed(0, 100).boxed().collect(Collectors.toList());
        // // System.out.println(ret);
        // String testSQL = "select text_column1, median(numeric_column) as num from example_table\n" +
        //         "where text_column1::int < 1048576\n" +
        //         "group by text_column1\n" +
        //         "order by text_column1;";
        // RPCCommon.SQLExpression sqlExpression = SQLUtils.parseSQL2SQLExpression(testSQL, "public");
        // System.out.println(sqlExpression);
        Map<String, Integer> mergeResultKVPS = new LinkedHashMap<>();
        mergeResultKVPS.put("A", 1);
        mergeResultKVPS.put("B", 2);
        mergeResultKVPS.put("C", 3);
        mergeResultKVPS.put("D", 4);
        mergeResultKVPS.put("E", 5);
        mergeResultKVPS.put("G", 7);
        mergeResultKVPS.put("F", 6);

        System.out.println(mergeResultKVPS.keySet());
        System.out.println(mergeResultKVPS.values());

        // System.out.println(TestJava.class.getResource("/"));
        // String path = PathUtils.getRealPath("postgresql", "clickhouse/Q1.1.sql");
        // System.out.println(path);

    }

}
