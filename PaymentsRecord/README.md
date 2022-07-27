#交易记录器文档说明
##idea启动
配置jdk maven，maven导入jar包依赖后，启动Main类main函数即可运行。

##操作手册
可在控制台根据指引及提示进行操作。
亦可在项目启动后调用接口进行操作。
提供3个接口
http://127.0.0.1:8888/record/getAll         获取全部货币信息
http://127.0.0.1:8888/record/get/USD        获取USD货币信息
http://127.0.0.1:8888/record/update/USD/300 增加USD货币300的金额 
