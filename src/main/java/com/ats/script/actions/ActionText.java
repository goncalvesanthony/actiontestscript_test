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
import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.script.Script;
import com.ats.script.ScriptLoader;
import com.ats.tools.Utils;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionText extends ActionExecuteElement {

	public static final String SCRIPT_LABEL = "keyboard";
	public static final Pattern INSERT_PATTERN = Pattern.compile("insert\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
	
	private CalculatedValue text;

	private int insert = -1;

	public ActionText() {}

	public ActionText(ScriptLoader script, int stopPolicy, ArrayList<String> options, String text, ArrayList<String> objectArray) {
		super(script, stopPolicy, options, objectArray);
		setText(new CalculatedValue(script, text));

		Iterator<String> itr = options.iterator();
		while (itr.hasNext())
		{
			final Matcher matcher = INSERT_PATTERN.matcher(itr.next().toLowerCase());
			if(matcher.find()){
				setInsert(Utils.string2Int(matcher.group(1), -1));
				break;
			}
		}
	}

	public ActionText(Script script, int stopPolicy, int maxTry, int delay, SearchedElement element, CalculatedValue text) {
		super(script, stopPolicy, maxTry, delay, element);
		setText(text);
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	// Code Generator
	//---------------------------------------------------------------------------------------------------------------------------------

	@Override
	public StringBuilder getJavaCode() {
		return super.getJavaCode().append(", ").append(text.getJavaCode()).append(")");
	}
	
	@Override
	public ArrayList<String> getKeywords() {
		ArrayList<String> keywords = super.getKeywords();
		keywords.add(text.getKeywords());
		return keywords;
	}

	//---------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------------------------------------------
	
	@Override
	public void terminateExecution(ActionTestScript ts) {
		super.terminateExecution(ts);
		if(status.isPassed()) {
			
			ts.getRecorder().updateScreen(true);

			final String enteredText = getTestElement().enterText(status, text, ts);
			status.endAction();
			
			ts.getRecorder().updateTextScreen(status, enteredText);
		}
	}
	
	@Override
	public StringBuilder getActionLogs(String scriptName, int scriptLine, JsonObject data) {
		data.addProperty("text", text.getCalculated().replaceAll("\"", "\\\""));
		return super.getActionLogs(scriptName, scriptLine, data);
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------	

	public CalculatedValue getText() {
		return text;
	}

	public void setText(CalculatedValue text) {
		this.text = text;
	}

	public int getInsert() {
		return insert;
	}

	public void setInsert(int insert) {
		this.insert = insert;
	}
}