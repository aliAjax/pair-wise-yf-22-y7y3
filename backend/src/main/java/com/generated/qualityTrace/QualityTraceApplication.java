package com.generated.qualityTrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * 质量追溯服务启动类。
 *
 * <p>当前演示版本使用内存仓储（repositories 包下的 In-Memory 实现），
 * 不依赖外部数据库，因此排除数据源/JPA/MyBatis 自动配置以保证无库环境下可启动。
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
})
public class QualityTraceApplication {
  public static void main(String[] args) {
    SpringApplication.run(QualityTraceApplication.class, args);
  }
}
