package com.dcfs.smartbranch.smartmanager;

import com.dcfs.smartaibank.springboot.schedule.EnableAutoSchedule;
import com.dcfs.smartaibank.springboot.security.EnableAutoSecurity;
import com.dcfs.smartaibank.springboot.swagger2.EnableAutoSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 管理平台启动器
 *
 * @author jiazw
 */
@MapperScan(basePackages = {"com.dcfs.smartaibank.manager.**.dao"})
@SpringBootApplication(scanBasePackages = {"com.dcfs.smartaibank"})
@EnableAutoSwagger2
@EnableAutoSecurity
@EnableAutoSchedule
public class SmartManagerApplication extends SpringBootServletInitializer {
	/**
	 * jar启动入口
	 *
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(SmartManagerApplication.class, args);
	}

	/**
	 * war启动配置入口
	 *
	 * @param builder spring应用构造器
	 * @return spring应用构造器
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(SmartManagerApplication.class);
	}
}
