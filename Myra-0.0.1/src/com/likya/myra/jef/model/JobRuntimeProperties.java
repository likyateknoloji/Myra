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
package com.likya.myra.jef.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;

public class JobRuntimeProperties implements JobRuntimeInterface, Serializable {
	
	private static final long serialVersionUID = -247742981750784916L;

	/**
	 * null means job is not member of any NetTree, just free
	 */
	private String memberIdOfNetTree = null; 
	
	private ArrayList<Integer> previousStatusList = new ArrayList<Integer>();
	
	private Calendar completionDate = null;
	
	private String workDuration = "-";
	public long workDurationNumeric = 0;
	
	public String recentWorkDuration = "-";
	public long recentWorkDurationNumeric = 0;
	
	private StringBuffer messageBuffer = new StringBuffer();
	
	private String logAnalyzeString;
	
	public JobRuntimeProperties() {
		super();
		// jobSimpleProperties = SimpleProperties.Factory.newInstance();
	}
	
	public String getPreviousStatusListString() {
		
		String statusListStr = "("; //$NON-NLS-1$
		if (previousStatusList != null) {
			int i = 0;
			while (i < previousStatusList.size()) {
				// int jobStatus = previousStatusList.get(i).intValue();

				BigInteger checkValue = BigInteger.valueOf(i + 1).mod(BigInteger.valueOf(3));
				if ((checkValue.intValue() == 0) && (i > 0 && i + 1 < previousStatusList.size())) {
					// statusListStr += ObjectUtils.getStatusAsString(jobStatus) + ",<br> "; //$NON-NLS-1$
				} else {
					// statusListStr += ObjectUtils.getStatusAsString(jobStatus) + ","; //$NON-NLS-1$
				}

				i++;
			}
		}

		char lastChar = statusListStr.charAt(statusListStr.length() - 1);
		if (lastChar == ',') {
			statusListStr = statusListStr.substring(0, statusListStr.length() - 1);
		}

		statusListStr += ")"; //$NON-NLS-1$

		return statusListStr;
	}

	public ArrayList<Integer> getPreviousStatusList() {
		return previousStatusList;
	}

	public Calendar getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Calendar completionDate) {
		this.completionDate = completionDate;
	}

	public String getWorkDuration() {
		return workDuration;
	}

	public void setWorkDuration(String workDuration) {
		this.workDuration = workDuration;
	}

	public String getRecentWorkDuration() {
		return recentWorkDuration;
	}

	public void setRecentWorkDuration(String recentWorkDuration) {
		this.recentWorkDuration = recentWorkDuration;
	}

	public long getRecentWorkDurationNumeric() {
		return recentWorkDurationNumeric;
	}

	public void setRecentWorkDurationNumeric(long recentWorkDurationNumeric) {
		this.recentWorkDurationNumeric = recentWorkDurationNumeric;
	}

	public long getWorkDurationNumeric() {
		return workDurationNumeric;
	}

	public void setWorkDurationNumeric(long workDurationNumeric) {
		this.workDurationNumeric = workDurationNumeric;
	}

	public StringBuffer getMessageBuffer() {
		return messageBuffer;
	}

	public void setMessageBuffer(StringBuffer messageBuffer) {
		this.messageBuffer = messageBuffer;
	}

	public String getLogAnalyzeString() {
		return logAnalyzeString;
	}

	public void setLogAnalyzeString(String logAnalyzeString) {
		this.logAnalyzeString = logAnalyzeString;
	}

	public String getMemberIdOfNetTree() {
		return memberIdOfNetTree;
	}

	public void setMemberIdOfNetTree(String memberIdOfNetTree) {
		this.memberIdOfNetTree = memberIdOfNetTree;
	}

}
