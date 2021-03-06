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
import com.google.gson.JsonObject;

import java.util.ArrayList;

public abstract class ActionChannel extends Action {

	public static final String SCRIPT_LABEL = "channel-";

	private String name = "";

	public ActionChannel() {}

	public ActionChannel(Script script, String name) {
		super(script);
		setName(name);
	}

	@Override
	public boolean execute(ActionTestScript ts, String testName, int testLine) {
		return super.execute(ts, testName, testLine);
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		return super.getActionLogs(scriptName, scriptLine, getActionLogsData());
	}
	
	protected JsonObject getActionLogsData() {
		JsonObject data = new JsonObject();
		data.addProperty("name", name);
		return data;
	}
	
	@Override
	public ArrayList<String> getKeywords() {
		ArrayList<String> keywords = super.getKeywords();
		keywords.add(name);
		return keywords;
	}
	
	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}
}