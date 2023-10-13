import org.junit.jupiter.api.Test;

public class TestSSMessage {

    @Test
    public void testMergeResultRows() {
        int n = 2;
        try {
            // List<RPCCommon.Record> recordList1 =
            //         Arrays.asList(numericList2Record("shanghai", Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0)), numericList2Record("beijing", Arrays.asList(-1.0, 4.0, 3.0, 4.0, 5.0)));
            //
            // List<RPCCommon.Record> recordList2 =
            //         Arrays.asList(numericList2Record("shanghai", Arrays.asList(3.0, 2.0, 3.0, 2.0, 8.0)), numericList2Record("beijing", Arrays.asList(1.0, 4.0, -2.0, 4.0, 5.0)));

            // List<RPCCommon.SSMessage> ssMessages1 = SecureGroupSumNew.localEncrypt(recordList1, 2);
            // List<RPCCommon.SSMessage> ssMessages2 = SecureGroupSumNew.localEncrypt(recordList2, 2);

            // RPCCommon.SSMessage secretSum1 = SecureGroupSumNew.mergeSSMessage(Arrays.asList(ssMessages1.get(0), ssMessages2.get(0)));
            // RPCCommon.SSMessage secretSum2 = SecureGroupSumNew.mergeSSMessage(Arrays.asList(ssMessages1.get(1), ssMessages2.get(1)));

            // double[][] result = SecureGroupSumNew.decryptResult(Arrays.asList(secretSum1, secretSum2), n, 5);
            //
            // System.out.println(Arrays.deepToString(result));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
