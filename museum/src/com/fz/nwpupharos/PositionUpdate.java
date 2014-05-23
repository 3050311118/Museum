package com.fz.nwpupharos;

public class PositionUpdate {
	private long id;
	private long messageId;
	private boolean needMonitor;
	private String title;
	private String content;
	private long positionId;
	private String sourceMD5;
	private String destinationMd5;
	long setupTime;
	long trigerTime;

	public long getSetupTime() {
		return setupTime;
	}

	public void setSetupTime(long setupTime) {
		this.setupTime = setupTime;
	}

	public long getTrigerTime() {
		return trigerTime;
	}

	public void setTrigerTime(long trigerTime) {
		this.trigerTime = trigerTime;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public boolean isNeedMonitor() {
		return needMonitor;
	}

	public void setNeedMonitor(boolean needMonitor) {
		this.needMonitor = needMonitor;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getPositionId() {
		return positionId;
	}

	public void setPositionId(long positionId) {
		this.positionId = positionId;
	}

	public String getSourceMD5() {
		return sourceMD5;
	}

	public void setSourceMD5(String sourceMD5) {
		this.sourceMD5 = sourceMD5;
	}

	public String getDestinationMd5() {
		return destinationMd5;
	}

	public void setDestinationMd5(String destinationMd5) {
		this.destinationMd5 = destinationMd5;
	}

	public PositionUpdate(long messageId, boolean needMonitor, String title, String content, long positionId,
			String sourceMD5, String destinationMD5, long setupTime, long trigerTime) {
		this.messageId = messageId;
		this.needMonitor = needMonitor;
		this.title = title;
		this.content = content;
		this.positionId = positionId;
		this.sourceMD5 = sourceMD5;
		this.destinationMd5 = destinationMD5;
		this.sourceMD5 = sourceMD5;
		this.setupTime = setupTime;
		this.trigerTime = trigerTime;
	}

	public PositionUpdate(String mac) {
		this.messageId = 1l;
		this.needMonitor = false;
		this.title = mac;
		this.content = null;
		this.positionId = 1l;
		this.sourceMD5 = "fffffffffffffffffffffffffffffff";
		this.destinationMd5 = "fffffffffffffffffffffffffffffff";
		this.sourceMD5 = "fffffffffffffffffffffffffffffff";
		this.setupTime = System.currentTimeMillis();
		this.trigerTime = System.currentTimeMillis() + 1000000;
	}
}
