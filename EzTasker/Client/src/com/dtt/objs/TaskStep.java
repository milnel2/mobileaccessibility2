package com.dtt.objs;

/**
 * Task Object to represent single task.
 * This object is mainly used for customized list adapter.
 * 
 * @author Moon Hwan Oh, Amenda Shen
 *
 */
public class TaskStep {
	private Long mStepId;
	private Long mTaskId;
	private Long mStepNum;
	private String mInst = "";
	private String mPictureUri = "";
	private String mAudioUri = "";
	private String mVideoUri = "";
	
	public TaskStep(Long stepId, Long taskId, Long stepNum, String inst, 
			String pictureUri, String audioUri, String videoUri) {
		mStepId = stepId;
		mTaskId = taskId;
		mStepNum = stepNum;
		mInst = inst;
		mPictureUri = pictureUri;
		mAudioUri = audioUri;
		mVideoUri = videoUri;
	}
	
	public Long getStepId() {
		return mStepId;
	}
	
	public Long getStepNum() {
		return mStepNum;
	}
	
	public Long getTaskId() {
		return mTaskId;
	}
	
	public String getPictureUri() {
		return mPictureUri;
	}
	
	public boolean hasInst() {
		return (mInst.compareTo("") != 0) ? true : false;
	}
	
	public boolean hasPicture() {
		return (mPictureUri.compareTo("") != 0) ? true : false;
	}
	
	public boolean hasAudio() {
		return (mAudioUri.compareTo("") != 0) ? true : false;
	}
	
	public boolean hasVideo() {
		return (mVideoUri.compareTo("") != 0) ? true : false;
	}
	
}
