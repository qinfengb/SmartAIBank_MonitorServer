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
 * 客户动线年统计
 *
 * @author
 */
@Component
@Slf4j
@StepScope
public class CustRoutesOfYearReader extends AbstractTasklet {
	@Autowired
	private OperationDataStatsDao operationDataStatsDao;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		String year = this.getYearStr();
		log.info("客户动线数据-->年统计，传入参数：[dateType,year]=[{},{}]",
			Constants.YEAR, year);
		try {
			operationDataStatsDao.custRouteStats(Constants.YEAR, year, "");
		} catch (Exception e) {
			log.error("客户动线数据年统计发生异常", e);
		}
		return RepeatStatus.FINISHED;
	}
}
