import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestKeyDifferentSet {
    // private final static StopWatch stopWatch = new StopWatch();
    List<String> globalKeyList;
    List<String> localKeyList;
    Set<String> localKeySet;

    @BeforeEach
    public void beforeEach() {
        // stopWatch.start();
        globalKeyList = getRandomStringList(10);
        // localKeyList = getRandomStringList(100);
        localKeyList = new ArrayList<>(globalKeyList);
        localKeySet = new HashSet<>(localKeyList);
    }


    // @AfterAll
    // public void after() {
    //     stopWatch.stop();
    //     // 统计执行时间（秒）
    //     System.out.println("执行时长：" + stopWatch.getTime(TimeUnit.SECONDS) + " 秒.");
    //     // 统计执行时间（毫秒）
    //     System.out.println("执行时长：" + stopWatch.getTime(TimeUnit.MILLISECONDS) + " 毫秒.");
    //     // 统计执行时间（纳秒）
    //     System.out.println("执行时长：" + stopWatch.getTime(TimeUnit.NANOSECONDS) + " 纳秒.");
    // }

    public List<String> getRandomStringList(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(RandomStringUtils.random(100));
        }
        return list;
    }

    @RepeatedTest(1000)
    public void testFunction1() {
        // List<String> globalKeySett = getRandomStringList(100);
        // List<String> localKeySett = getRandomStringList(100);
        // List<String> localKeySet = new ArrayList<>(localKeySett);
        // List<String> globalKeySet = new ArrayList<>(globalKeySett);
        // Set<String> keySet = new HashSet<>();
        for (String val : localKeyList) {
            if (!globalKeyList.contains(val)) {
                globalKeyList.add(val);
            }
        }
    }

    @RepeatedTest(1000)
    public void testFunction2() {
        // List<String> globalKeyList = getRandomStringList(100);
        // List<String> localKeyList = getRandomStringList(100);
        List<String> differentSet = new ArrayList<>();
        Set<String> localKeySet = new HashSet<>(localKeyList);
        Set<String> globalKeySet = new HashSet<>(globalKeyList);
        // Set<String> keySet = new HashSet<>();
        for (String val : localKeySet) {
            if (!globalKeySet.contains(val)) {
                differentSet.add(val);
            }
        }
    }


    @RepeatedTest(1000)
    public void testFunction3() {
        for (String val : globalKeyList) {
            localKeySet.remove(val);
        }
        // globalKeyList.forEach(localKeySet::remove);
    }


    // @RepeatedTest(1000)
    // public void testSet2List() {
    //     List<String> list = Arrays.asList("alice", "tom", "alice", "tom", "niudun", "jnkds", "cnjs", "cnsjk");
    //     Set<String> set = new HashSet<>();
    //     set.addAll(list);
    //
    // }
}
