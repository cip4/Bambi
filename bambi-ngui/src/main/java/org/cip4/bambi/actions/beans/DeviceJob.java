package org.cip4.bambi.actions.beans;

public class DeviceJob
{
	private String jobId;
	private String priority;
	private String status;
	private String submitted;
	private String started;
	private String ended;

	public String getJobId()
	{
		return jobId;
	}

	public void setJobId(String jobId)
	{
		this.jobId = jobId;
	}

	public String getPriority()
	{
		return priority;
	}

	public void setPriority(String priority)
	{
		this.priority = priority;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getSubmitted()
	{
		return submitted;
	}

	public void setSubmitted(String submitted)
	{
		this.submitted = submitted;
	}

	public String getStarted()
	{
		return started;
	}

	public void setStarted(String started)
	{
		this.started = started;
	}

	public String getEnded()
	{
		return ended;
	}

	public void setEnded(String ended)
	{
		this.ended = ended;
	}

}
