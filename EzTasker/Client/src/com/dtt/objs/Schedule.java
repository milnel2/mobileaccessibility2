package com.dtt.objs;

/**
 * Schedule object to represent single schedule. 
 * This object is mainly used for customized list adapter.
 * 
 * @author Moon Hwan Oh, Amenda Shen
 *
 */
public class Schedule {
	private Long mScheduleId;
	private Long mTaskId;
	private String mTaskName;
	private String mTime = "";
	private Integer mFlag;
	private Integer mImportant;
	private String mPictureUri = "";
	
	public Schedule(Long scheduleId, Long taskId, String taskName, String time, Integer flag, 
			Integer important, String pictureUri) {
		mScheduleId = scheduleId;
		mTaskId = taskId;
		mTaskName = taskName;
		mTime = time;
		mFlag = flag;
		mImportant = important;
		mPictureUri = pictureUri;
	}
	
	public Long getScheduleId() {
		return mScheduleId;
	}
	
	public Long getTaskId() {
		return mTaskId;
	}
	
	public String getTaskName() {
		return mTaskName;
	}
	
	public String getTime() {
		return mTime;
	}
	
	public int getHour() {
		return Integer.parseInt(mTime.split(":")[0]);
	}
	
	public int getMinute() {
		return Integer.parseInt(mTime.split(":")[1]);
	}
	
	public String getPictureUri() {
		return mPictureUri;
	}
	
	public int getFlag() {
		return mFlag;
	}
	
	public int getImportant() {
		return mImportant;
	}
	
	public boolean isDone() {
		return ((mFlag == GlobalConstants.DONE) ? true:false);
	}
	
	public boolean isSkipped() {
		return ((mFlag == GlobalConstants.SKIPPED) ? true:false);
	}
	
	public boolean isImportant() {
		return ((mImportant != 0) ? true : false);
	}
	
	public boolean hasPicture() {
		return ((mPictureUri.compareTo("") != 0) ? true : false);
	}
	
}
