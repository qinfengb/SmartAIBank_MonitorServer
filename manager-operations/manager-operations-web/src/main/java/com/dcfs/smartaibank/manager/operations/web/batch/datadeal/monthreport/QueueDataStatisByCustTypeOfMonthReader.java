package com.dcfs.smartaibank.manager.operations.web.batch.datadeal.monthreport;

import com.dcfs.smartaibank.manager.operations.web.batch.datadeal.AbstractTasklet;
import com.dcfs.smartaibank.manager.operations.web.constant.Constant;
import com.dcfs.smartaibank.manager.operations.web.dao.OperationDataStatsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 排队情况按照客户类型统计
 *
 * @author
 */
@Component
@Slf4j
@StepScope
public class QueueDataStatisByCustTypeOfMonthReader extends AbstractTasklet {
	@Autowired
	private OperationDataStatsDao operationDataStatsDao;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		String monthStr = this.getMonthStr();
		log.info("排队客户类型数据-->月统计，传入参数：[dateType,monthStr]=[{},{}]",
			Constant.MONTH, monthStr);
		try {
			operationDataStatsDao.queueByCust(Constant.MONTH, monthStr);
		} catch (Exception e) {
			log.error("排队-客户类型数据月统计发生异常", e);
		}
		return RepeatStatus.FINISHED;
	}
}
