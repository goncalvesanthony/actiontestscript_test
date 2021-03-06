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

import com.ats.element.SearchedElement;
import com.ats.executor.ActionStatus;
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.Variable;
import com.ats.script.Script;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class ActionProperty extends ActionReturnVariable {

	public static final String SCRIPT_LABEL = "property";

	private String name;

	public ActionProperty() {}

	public ActionProperty(Script script, int stopPolicy, ArrayList<String> options, String name, Variable variable, ArrayList<String> objectArray) {
		super(script, stopPolicy, options, objectArray, variable);
		setName(name);
	}

	public ActionProperty(Script script, int stopPolicy, int maxTry, int delay, SearchedElement element, String name, Variable variable) {
		super(script, stopPolicy, maxTry, delay, element, variable);
		setName(name);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		StringBuilder codeBuilder = super.getJavaCode();
		codeBuilder.append(", \"")
		.append(name)
		.append("\", ")
		.append(variable.getName())
		.append(")");
		return codeBuilder;
	}

	@Override
	public ArrayList<String> getKeywords() {
		ArrayList<String> keywords = super.getKeywords();
		keywords.add(name);
		return keywords;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);

		String attributeValue = "";
		
		if (status.isPassed()) {

			attributeValue = getTestElement().getAttribute(status, name);

			if (attributeValue == null) {
				attributeValue = "";
				status.setError(ActionStatus.ATTRIBUTE_NOT_SET, "attribute '" + name + "' not found", name);
				ts.getRecorder().update(ActionStatus.ATTRIBUTE_NOT_SET, status.getDuration(), name);
			} else {
				status.setMessage(attributeValue);
				ts.getRecorder().update(status.getCode(), status.getDuration(), name, attributeValue);
			}

		}else {
			attributeValue = getTestElement().getAttribute(status, name);
			if(status.getCode() == ActionStatus.OBJECT_NOT_FOUND){
				status.setMessage("element not found");
				ts.getRecorder().update(status.getCode(), status.getDuration(), "element not found");
			}else {
				status.setMessage(attributeValue);
				ts.getRecorder().update(status.getCode(), status.getDuration(), name, attributeValue);
			}
		}
		
		updateVariableValue(attributeValue);
		status.setValue(attributeValue);
	}

	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("property", name);
		return super.getActionLogs(scriptName, scriptLine, data);
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