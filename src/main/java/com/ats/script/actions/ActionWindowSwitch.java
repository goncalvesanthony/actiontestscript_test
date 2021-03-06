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

import com.ats.executor.channels.Channel;
import com.ats.generator.objects.TryAndDelay;
import com.ats.script.Script;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionWindowSwitch extends ActionWindow {

	public static final String SCRIPT_SWITCH_LABEL = SCRIPT_LABEL + "switch";
	
	private int num = 0;
	private int tries = 0;
	private int delay = 0;
	
	public ActionWindowSwitch() {}

	public ActionWindowSwitch(Script script, int num, ArrayList<String> options) {
		super(script);
		
		final int[] data = TryAndDelay.getTryAndDelay(options);
		
		setNum(num);
		setTries(data[0]);
		setDelay(data[1]);
	}
	
	public ActionWindowSwitch(Script script, int tries, int num, int delay) {
		super(script);
		setNum(num);
		setTries(tries);
		setDelay(delay);
	}
	
	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode().append(tries).append(", ").append(num).append(", ").append(delay).append(")");
	}
	
	@Override
	public String exec(Channel channel) {
		channel.switchWindow(status, num, tries, delay);
		return num + "";
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("index", num);
		data.addProperty("tries", tries);
		return super.getActionLogs(scriptName, scriptLine, data);
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getTries() {
		return tries;
	}

	public void setTries(int tries) {
		this.tries = tries;
	}
}