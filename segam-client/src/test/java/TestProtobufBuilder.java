import com.ada.federate.rpc.RPCCommon;
import org.junit.jupiter.api.RepeatedTest;

public class TestProtobufBuilder {

    public static void main(String[] args) {
        RPCCommon.Status status = RPCCommon.Status.newBuilder().setCode(true).setMessage("test message").build();

        System.out.println(System.identityHashCode(status));
        System.out.println(status);
        // status = status.toBuilder().clone().setMessage("modify test").build();
        status = status.toBuilder().setMessage("modify test").build();

        System.out.println(System.identityHashCode(status));
        System.out.println(status);
    }

    @RepeatedTest(10000)
    public void testFunction1() {
        RPCCommon.Status status = RPCCommon.Status.newBuilder().setCode(true).setMessage("test message").build();
        // status = status.toBuilder().clone().setMessage("modify test").build();
        status = status.toBuilder().setMessage("modify test").build();

    }

    @RepeatedTest(10000)
    public void testFunction2() {
        RPCCommon.Status status = RPCCommon.Status.newBuilder().setCode(true).setMessage("test message").build();
        // status = status.toBuilder().clone().setMessage("modify test").build();
        status = status.toBuilder().clone().setMessage("modify test").build();

    }

    @RepeatedTest(10000)
    public void testFunction3() {
        RPCCommon.Status status = RPCCommon.Status.newBuilder().setCode(true).setMessage("test message").build();
        // status = status.toBuilder().clone().setMessage("modify test").build();
        status = RPCCommon.Status.newBuilder().setCode(status.getCode()).setMessage("modify test").build();

    }
}
