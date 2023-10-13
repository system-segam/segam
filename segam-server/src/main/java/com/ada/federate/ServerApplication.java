package com.ada.federate;

import com.ada.federate.pojo.DriverConfig;
import com.ada.federate.utils.LogUtils;

import static com.ada.federate.utils.LogUtils.buildErrorMessage;

public class ServerApplication {
    private static final String configFile = "config.json";

    public static void main(String[] args) {
        try {
            DriverConfig config = new DriverConfig(configFile);
            int port = config.getServerPort();
            // MemoryMonitor monitor = new MemoryMonitor();
            // 创建新线程
            // Thread monitorThread = new Thread(monitor);
            // 启动线程
            // monitorThread.start();
            RPCServer server = new RPCServer(port, config);
            LogUtils.info(config.getName() + " Server started...");
            server.start();
            server.blockUntilShutdown();
            // 停止监控线程
            // monitor.stop();
        } catch (Exception e) {
            LogUtils.error(buildErrorMessage(e));
        }
    }
}
