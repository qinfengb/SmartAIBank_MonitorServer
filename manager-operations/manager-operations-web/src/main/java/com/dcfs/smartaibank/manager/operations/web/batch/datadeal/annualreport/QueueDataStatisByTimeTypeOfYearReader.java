package com.dcfs.smartaibank.manager.operations.web.batch.datadeal.annualreport;

import com.dcfs.smartaibank.manager.operations.web.batch.common.Constants;
import com.dcfs.smartaibank.manager.operations.web.batch.datadeal.AbstractTasklet;
import com.dcfs.smartaibank.manager.operations.web.dao.OperationDataStatsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 排队情况按照时间类型统计
 *
 * @author
 */
@Component
@Slf4j
@StepScope
public class QueueDataStatisByTimeTypeOfYearReader extends AbstractTasklet {
	@Autowired
	private OperationDataStatsDao operationDataStatsDao;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		String year = this.getYearStr();
		log.info("排队时间类型数据-->年统计，传入参数：[dateType,yearStr]=[{},{}]",
			Constants.YEAR, year);
		try {
			operationDataStatsDao.queueByTimeStep(Constants.YEAR, year);
			operationDataStatsDao.queueByTimeGroup(Constants.YEAR, year);
		} catch (Exception e) {
			log.error("排队-时间类型数据年统计发生异常", e);
		}
		return RepeatStatus.FINISHED;
	}
}
