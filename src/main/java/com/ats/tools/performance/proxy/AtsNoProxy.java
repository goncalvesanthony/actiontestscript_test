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

package com.ats.tools.performance.proxy;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.script.actions.Action;
import com.ats.script.actions.performance.octoperf.ActionOctoperfVirtualUser;
import org.openqa.selenium.Proxy;

import java.util.List;

public class AtsNoProxy implements IAtsProxy {
	
	@Override
	public void startRecord(ActionStatus status, List<String> whiteList, int trafficIddle, int latency, long sendBandWidth, long receiveBandWidth) {
		status.setError(ActionStatus.PERF_NOT_STARTED, "Channel not started with \"performance\" option");
	}
	
	@Override
	public void startAction(Action action, String testLine) {
	}

	@Override
	public Proxy startProxy() {
		return null;
	}
	
	@Override
	public void terminate(String channelName) {
	}

	@Override
	public void endAction() {
	}

	@Override
	public void resumeRecord() {
	}

	@Override
	public void pauseRecord() {
	}

	@Override
	public void sendToOctoperfServer(Channel channel, ActionOctoperfVirtualUser action) {
	}
}