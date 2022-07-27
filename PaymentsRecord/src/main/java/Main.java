import com.yanghaihun.entity.Record;
import com.yanghaihun.service.RecordServiceImpl;
import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;

/**
 * Author: 杨海珲
 * Data:   2022/7/27 9:18 下午
 */
public class Main {

    public static void main(String[] args) {
        RecordServiceImpl recordService = new RecordServiceImpl();
        // 接口
        MuServer server = MuServerBuilder.httpServer()
                .withHttpPort(8888)
                .addHandler(Method.GET, "/record/get/{recordName}", (request, response, pathParams) -> {
                    String recordName = pathParams.get("recordName");
                    response.write(recordService.printRecords(recordName));
                })
                .addHandler(Method.GET, "/record/getAll", (request, response, pathParams) -> {
                    response.write(recordService.printRecords());
                })
                .addHandler(Method.GET, "/record/update/{recordName}/{recordMoney}", (request, response, pathParams) -> {
                    String paymentRecord = pathParams.get("recordName") + " " + pathParams.get("recordMoney");
                    Record record = recordService.parseOneLineData(paymentRecord);
                    if (null != record) {
                        String msg = recordService.store(record);
                        response.write(msg);
                    }
                    response.write("格式错误,更新失败");
                })
                .start();

        // 开始业务
        recordService.startToLoadData();
    }
}
