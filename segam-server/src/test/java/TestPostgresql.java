import com.ada.federate.pojo.DriverConfig;
import com.ada.federate.cache.ResultColumn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class TestPostgresql {

    public static void main(String[] args) throws Exception {
        DriverConfig config = new DriverConfig("config.json");
        Class.forName(config.getDriver());
        Properties properties = new Properties();
        properties.setProperty("user", config.getUser());
        properties.setProperty("password", config.getPassword());
        properties.setProperty("client_name", config.getName());
        Connection conn = DriverManager.getConnection(config.getUrl(),
                config.getUser(), config.getPassword());

        Statement stmt = conn.createStatement();
        String sql = "select distinct(SUBSTRING(LO_ORDERDATE, 0, 5)) as year from schema10_4.lineorder_flat;";
        ResultSet rs = stmt.executeQuery(sql);
        ResultColumn resultColumnList = ResultColumn.resultSet2ResultColumn(rs);
        // String jsonStr = ResultColumn.batchDump(resultColumnList);
        // List<ResultColumn> resultColumnList1 = ResultColumn.parse(jsonStr);
        //
        // System.out.println(ResultColumn.batchDump(resultColumnList1));
    }


}

