package com.dcfs.smartaibank.manager.monitor.web.job.task;

import com.dcfs.smartaibank.manager.monitor.core.util.SpringContextUtil;
import com.dcfs.smartaibank.manager.monitor.web.dao.ReportDao;
import com.dcfs.smartaibank.manager.monitor.web.util.DateUtil;
import com.dcfs.smartaibank.manager.monitor.web.util.ReportUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc 报表抽象类
 * @date 2016年8月24日 下午2:51:02
 * @author wangtingo
 */
public abstract class AbstractCommonReportTask implements IReportTask {

	private ReportDao dao;

	private  static final String FAULT_TIME_INT = "FAULT_TIME_INT";
	private  static final String NORMAL_TIME_INT = "NORMAL_TIME_INT";

	private String startDateStr;
	private String endDateStr;


	/**
	 * 执行报表生成任务
	 * @param  reportType
	 * @date 2016年8月30日 上午9:25:02
	 */
	public abstract void execute(String reportType);

	/**
	 * 向数据库中持久化报表数据
	 * @author ligg
	 * @date 2016年8月24日 上午11:49:45
	 * @param reportInfoList
	 */
	protected abstract void insertReport(
			List<Map<String, Object>> reportInfoList);

	/**
	 *  删除数据
	 * @author zhaofy
	 * @param reportType
	 * @date 2016年11月15日
	 */
	protected abstract void deleteReport(String reportType);

	/**
	 *  添加报表数据项，该方法可以为空
	 * @author ligg
	 * @date 2016年8月24日 上午11:50:13
	 * @param record
	 */
	protected abstract void addItem(Map<String, Object> record);


	/**
	 *  设置开始时间，如果报表开始日期和结束日期相同，该方法可以为空
	 * @param c
	 * @date 2016年8月24日 上午11:50:13
	 */
	protected abstract void setStartDate(Calendar c);


	protected ReportDao getDao() {
		if (dao == null) {
			dao = (ReportDao) SpringContextUtil.getBean("reportDao");
		}
		return dao;
	}

	/**
	 *  获得报表查询条件
	 * @author ligg
	 * @date 2016年8月24日 下午3:13:08
	 * @return
	 */
	protected Map<String, Object> getQueryCondition() {
		// 获得当前月报表的结束时间
		Date endDate = ReportUtil.getReportDate();
		String endDateString = DateUtil.getStrSplitDate(endDate);

		// 获得当前月报表的开始时间
		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		setStartDate(c);
		Date starDate = c.getTime();
		String startDateString = DateUtil.getStrSplitDate(starDate);

		// 将开始日期和结束日期放入容器中
		Map<String, Object> queryMap = new HashMap<>(5);
		queryMap.put("START_DATE", startDateString);
		queryMap.put("END_DATE", endDateString);

		this.startDateStr = startDateString;
		this.endDateStr = endDateString;
		return queryMap;
	}

	protected int getYear() {
		Map<String, Object> queryMap = getQueryCondition();
		String endDate = ReportUtil.getValue(queryMap, "END_DATE");
		String[] dates = endDate.split("-");
		// 获得年份
		int year = Integer.parseInt(dates[0]);
		return year;
	}

	/**
	 *  获得需要持久化的数据信息
	 * @author ligg
	 * @date 2016年8月24日 上午11:50:40
	 * @param reportInfoList
	 * @return
	 */
	private Map<String, Map<String, Object>> getReportInfo(
			List<Map<String, Object>> reportInfoList) {
		// key: 机构ID_故障类型_机具ID 或者 key:机构ID_故障类型_机具ID_外设ID
		Map<String, Map<String, Object>> reportInfoMap = new HashMap<>(100);
		// 临时时间容器
		Map<String, Object> timeMap = new HashMap<>(100);

		// 遍历报表数据
		for (Map<String, Object> record : reportInfoList) {
			String branchNo = ReportUtil.getValue(record, "BRANCH_NO");
			String mid = ReportUtil.getValue(record, "MID");
			String type = ReportUtil.getValue(record, "M_TYPE");
			String devClassKey = ReportUtil.getValue(record, "DEVCLASSKEY");
			String key = ReportUtil.getKey(branchNo, type, mid);
			// 获得原始报表时间
			Date preReportDate = ReportUtil.getValue(record, "CREATE_TIME");
			String dayDateStr = DateUtil.getStrSplitDate(preReportDate);

			int year = getYear();
			record.put("R_YEAR", year);
			// 设置新报表生成时间
			record.put("CREATE_TIME", ReportUtil.getReportDate());

			// 新增数据项
			addItem(record);

			record.remove("SEQ_NO");
			if (ReportUtil.DEV_DEVICE.equals(type)) {
				key = ReportUtil.getKey(key, devClassKey);
				// 计算故障总时间
				calcFaultTime(reportInfoMap, record, key);
			} else {
				record.put("DEVMODELKEY", "");
				record.put("DEVCLASSKEY", "");
				record.put("DEVCLASSNAME", "");
				// 计算故障总时间
				calcFaultTime(reportInfoMap, record, key);
			}
			// 获取当前工作时间
			BigDecimal workTime =  ReportUtil.getValue(record, NORMAL_TIME_INT);
			// key: 机构ID_机具ID_日前
			String timeKey = ReportUtil.getKey(branchNo, mid, dayDateStr);
			// 计算应工作总时间时，同一个机构的同一台机具同一天日期只计算一次，
			BigDecimal preWorkTime = ReportUtil.getValue(timeMap, timeKey);
			if (preWorkTime == null) {
				timeMap.put(timeKey, workTime);
			}
		}

		// 计算每个机具的总工作时间
		Map<String, Object> totalTimeMap = calcTotalTime(timeMap);


		//  将应工作总时间放入容器
		reportInfoMap.put(ReportUtil.WORK_TIME, totalTimeMap);

		return reportInfoMap;
	}

	/**
	 *  计算工作总时间
	 * @author ligg
	 * @date 2017年1月1日 下午4:32:07
	 * @param timeMap
	 * @return
	 */
	private Map<String, Object> calcTotalTime(Map<String, Object> timeMap) {
		Map<String, Object> totalTimeMap =  new HashMap<>(100);
		for (String timeKey :timeMap.keySet()) {
			String[] keys = timeKey.split(ReportUtil.STR_SPLIT);
			// key: 机构ID_机具ID
			String tempKey = ReportUtil.getKey(keys[0], keys[1]);
			BigDecimal currentTime = ReportUtil.getValue(timeMap, timeKey);
			BigDecimal total = ReportUtil.getValue(totalTimeMap, tempKey);
			if (total == null) {
				total = currentTime;
			} else {
				total = total.add(currentTime);
			}
			totalTimeMap.put(tempKey, total);
		}
		return totalTimeMap;
	}



	/**
	 *  保存报表数据
	 * @author ligg
	 * @param reportType
	 * @date 2016年8月24日 上午11:45:27
	 * @param reportType
	 * @throws Exception
	 */
	protected void saveReport(List<Map<String, Object>> reportInfoList, String reportType) throws Exception {

		// 获得需要保存的报表信息  key: 机构ID_故障类型_机具ID 或者 key:机构ID_故障类型_机具ID_外设ID
		Map<String, Map<String, Object>> reportInfoMap = getReportInfo(reportInfoList);

		deleteReport(reportType);

		List<Map<String, Object>> saveInfoList = new ArrayList<Map<String, Object>>();
		// 获取应工作总时间
		Map<String, Object> timeMap = reportInfoMap.get(ReportUtil.WORK_TIME);
		for (String key : reportInfoMap.keySet()) {
			if (ReportUtil.WORK_TIME.equals(key)) {
				continue;
			}
			Map<String, Object> innerMap = reportInfoMap.get(key);

			String[] keys = key.split(ReportUtil.STR_SPLIT);
			String timeKey = ReportUtil.getKey(keys[0], keys[2]);
			BigDecimal total = ReportUtil.getValue(timeMap, timeKey);
			long totalTime = total.longValue();

			innerMap.put(NORMAL_TIME_INT, totalTime);
			saveInfoList.add(innerMap);
			// 批量保存报表数据
			if (saveInfoList.size() == ReportUtil.BATCH_SAVE_SIZE) {
				insertReport(saveInfoList);
				saveInfoList.clear();
			}
		}
		// 批量保存报表数据
		if (!saveInfoList.isEmpty()) {
			insertReport(saveInfoList);
		}

	}

	/**
	 *  计算故障总时间
	 * @author ligg
	 * @date 2016年8月23日 下午6:44:53
	 * @param reportInfoMap
	 * @param record
	 * @param key
	 */
	private void calcFaultTime(Map<String, Map<String, Object>> reportInfoMap,
			Map<String, Object> record, String key) {
		Map<String, Object> innerMap = reportInfoMap.get(key);
		if (innerMap == null) {
			innerMap = new HashMap<>(100);
			innerMap.putAll(record);
			reportInfoMap.put(key, innerMap);
		} else {
			BigDecimal preErrorTime = ReportUtil.getValue(innerMap,
					FAULT_TIME_INT);
			BigDecimal errorTime = ReportUtil
					.getValue(record, FAULT_TIME_INT);
			// 对故障时间进行累加
			errorTime = errorTime.add(preErrorTime);
			// 将故障时间重新设置到容器中
			innerMap.put(FAULT_TIME_INT, errorTime);
		}
	}

	protected String getEndDateStr() {
		return endDateStr;
	}

}
