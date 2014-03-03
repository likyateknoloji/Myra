/*******************************************************************************
 * Copyright 2013 Likya Teknoloji
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.likya.myra.jef.jobs;

import java.util.Calendar;

import com.likya.myra.commons.utils.LiveStateInfoUtils;
import com.likya.myra.commons.utils.MyraDateUtils;
import com.likya.myra.jef.core.CoreFactory;
import com.likya.myra.jef.model.JobRuntimeInterface;
import com.likya.myra.jef.utils.Scheduler;
import com.likya.xsd.myra.model.joblist.AbstractJobType;
import com.likya.xsd.myra.model.stateinfo.LiveStateInfoDocument.LiveStateInfo;
import com.likya.xsd.myra.model.stateinfo.StateNameDocument.StateName;
import com.likya.xsd.myra.model.stateinfo.StatusNameDocument.StatusName;
import com.likya.xsd.myra.model.stateinfo.SubstateNameDocument.SubstateName;
import com.likya.xsd.myra.model.wlagen.CascadingConditionsDocument.CascadingConditions;
import com.likya.xsd.myra.model.wlagen.TriggerDocument.Trigger;

public abstract class GenericInnerJob extends JobImpl {

	private static final long serialVersionUID = -680353114457170591L;

	protected Calendar startTime;

	public GenericInnerJob(AbstractJobType abstractJobType, JobRuntimeInterface jobRuntimeProperties) {
		super(abstractJobType, jobRuntimeProperties);
	}
	
	protected void setRunning(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.RUNNING, SubstateName.ON_RESOURCE, StatusName.TIME_IN);
	}

	protected void setOfCodeMessage(AbstractJobType abstractJobType, StatusName.Enum statusName, int resultCode, String message) {
		ChangeLSI.forValue(abstractJobType, StateName.FINISHED, SubstateName.COMPLETED, statusName, resultCode, message);
	}

	protected void setFailedOfLog(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, "Log da bulunan kelime yüzünden !");
	}

	protected void setFailedOfMessage(AbstractJobType abstractJobType, String message) {
		ChangeLSI.forValue(abstractJobType, StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED, message);
	}

	public void setRenewByTime(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYTIME);
	}

	protected void setRenewByUser(AbstractJobType abstractJobType) {
		ChangeLSI.forValue(abstractJobType, StateName.PENDING, SubstateName.IDLED, StatusName.BYUSER);
	}

	public boolean scheduleForNextExecution(AbstractJobType abstractJobType) {
		return Scheduler.scheduleForNextExecution(abstractJobType);
	}

	private boolean isInDepenedencyChain(String jobId) {
		return !CoreFactory.getInstance().getNetTreeManagerInterface().getFreeJobs().containsKey(jobId);
	}

	public void processJobResult() {

		AbstractJobType abstractJobType = getAbstractJobType();

		String jobId = abstractJobType.getId();

		if (isInDepenedencyChain(jobId)) {
			// Do not touch, leave it to its master !
			return;
		}

		LiveStateInfo liveStateInfo = LiveStateInfoUtils.getLastStateInfo(abstractJobType);

		boolean isSuccess = LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.COMPLETED, StatusName.SUCCESS);
		boolean isFailed = LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.COMPLETED, StatusName.FAILED);

		/**
		 * if a job fails; Two parameters; autoRetry and runEvenIfFailed conflicts. In this is case,
		 * the priority of runEvenIfFailed is higher so, the autoRetry is discarded
		 */

		boolean goOnError = (abstractJobType.getManagement().getCascadingConditions() != null && abstractJobType.getManagement().getCascadingConditions().getRunEvenIfFailed());

		if (isSuccess || (goOnError && isFailed)) {

			JobHelper.setWorkDurations(this, startTime);

			int jobType = abstractJobType.getManagement().getTrigger().intValue();

			switch (jobType) {
			case Trigger.INT_EVENT:
				// Not implemented yet
				break;
			case Trigger.INT_TIME:
				if (scheduleForNextExecution(abstractJobType)) {
					String startTime = MyraDateUtils.getDate(abstractJobType.getManagement().getTimeManagement().getJsPlannedTime().getStartTime().getTime());
					CoreFactory.getLogger().info("Job [" + abstractJobType.getId() + "] bir sonraki zamana kuruldu : " + startTime);
					setRenewByTime(abstractJobType);
				}
				break;
			case Trigger.INT_USER:
				setRenewByUser(abstractJobType);
				break;

			default:
				break;
			}

			CoreFactory.getLogger().info(CoreFactory.getMessage("ExternalProgram.9") + jobId + " => " + (liveStateInfo.getStatusName() == null ? "" : liveStateInfo.getStatusName().toString()));

		} else {

			JobHelper.setWorkDurations(this, startTime);

			boolean manuelStop = LiveStateInfoUtils.equalStates(liveStateInfo, StateName.FINISHED, SubstateName.STOPPED, StatusName.BYUSER);

			if (!manuelStop) {

				CascadingConditions cascadingConditions = abstractJobType.getManagement().getCascadingConditions();

				if (cascadingConditions != null && cascadingConditions.getJobAutoRetryInfo().getJobAutoRetry()) {

					boolean retryCondition = true;

					if (cascadingConditions.getJobAutoRetryInfo().getLiveStateInfo() != null) {
						retryCondition = LiveStateInfoUtils.equalStates(liveStateInfo, cascadingConditions.getJobAutoRetryInfo().getLiveStateInfo());
					}

					if (retryCondition) {
						
						if (retryCounter < cascadingConditions.getJobAutoRetryInfo().getMaxCount().intValue()) {

							CoreFactory.getLogger().info(CoreFactory.getMessage("ExternalProgram.11") + jobId);
							retryCounter++;

							long stepTime = MyraDateUtils.getDurationInMilliSecs(abstractJobType.getManagement().getCascadingConditions().getJobAutoRetryInfo().getStep());

							JobHelper.setJsPlannedTimeForStart(abstractJobType, stepTime);
							setRenewByTime(abstractJobType);

						} else {
							// reset counter and leave for normal scheduling
							retryCounter = 0;
						}
					
					}
				}
			}

			CoreFactory.getLogger().info(jobId + CoreFactory.getMessage("ExternalProgram.12"));
			CoreFactory.getLogger().debug(jobId + CoreFactory.getMessage("ExternalProgram.13"));

		}
	}

}
