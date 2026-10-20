package com.fresh;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 社区便民生鲜配送平台 启动类
 */
@Slf4j
@SpringBootApplication
@EnableScheduling               // 开启定时任务（超时订单/临期批次/预售关闭/团购结算）
@EnableTransactionManagement    // 开启注解式事务
@MapperScan("com.fresh.modules.**.mapper")
public class FreshApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreshApplication.class, args);
        log.info("========== 社区便民生鲜配送平台启动成功 ==========");
        log.info("接口文档: http://localhost:8080/doc.html");
    }
}
