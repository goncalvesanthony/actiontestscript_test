/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package com.ats.script.actions;

import com.ats.executor.ActionTestScript;
import com.ats.script.Script;

public class ActionChannelClose extends ActionChannelExist {

	public static final String SCRIPT_CLOSE_LABEL = SCRIPT_LABEL + "close";

	public static final String NO_STOP_LABEL = "nostop";

	private boolean keepRunning = false;

	public ActionChannelClose() {}

	public ActionChannelClose(Script script, String name, boolean keepRunning) {
		super(script, name);
		this.keepRunning = keepRunning;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		super.execute(ts, testName, testLine);
		if(channelEmpty) {
			ts.getTopScript().sendWarningLog("ActionChannelClose (" + testName + ":" + testLine + ")", "Cannot close Channel '" + getName() + "', this channel is not running !");
		}else {
			ts.getChannelManager().closeChannel(status, getName(), keepRunning);
		}
		return true;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public boolean isKeepRunning() {
		return keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode().append("\"").append(getName()).append("\", ").append(keepRunning).append(")");
	}
}