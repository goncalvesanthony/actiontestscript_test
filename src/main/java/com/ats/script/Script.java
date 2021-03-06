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

package com.ats.script;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import com.ats.crypto.Passwords;
import com.ats.executor.ActionTestScript;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelManager;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.ScriptValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.parameter.ParameterList;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.generator.variables.transform.Transformer;
import com.ats.script.actions.Action;
import com.ats.tools.logger.ExecutionLogger;
import com.ats.tools.logger.levels.AtsLogger;

public class Script {

	public static final Pattern OBJECT_PATTERN = Pattern.compile("(.*)\\[(.*)\\]", Pattern.CASE_INSENSITIVE);
	public final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	public final static String ATS_EXTENSION = "ats";
	public final static String ATS_FILE_EXTENSION = "." + ATS_EXTENSION;
	public final static String ATS_VISUAL_EXTENSION = "atsv";
	public final static String ATS_VISUAL_FILE_EXTENSION = "." + ATS_VISUAL_EXTENSION;

	public final static String ATS_VISUAL_FOLDER = "visual";

	public final static String SCRIPT_LOG = "SCRIPT";
	public final static String COMMENT_LOG = "COMMENT";

	//-------------------------------------------------------------------------------------
	// instances
	//-------------------------------------------------------------------------------------

	private ParameterList parameterList;
	private List<Variable> variables = new ArrayList<Variable>();
	private ArrayList<CalculatedValue> returns;

	protected List<ActionTestScript> scriptCallTree;

	protected String csvAbsoluteFilePath;
	protected int iteration = 0;

	protected ChannelManager channelManager;

	private Map<String, String> testExecutionVariables;
	protected ExecutionLogger logger = new ExecutionLogger();
	protected Passwords passwords;

	public Script() {}

	public Script(ExecutionLogger logger) {
		setLogger(logger);
	}

	public String getPassword(String name) {
		return passwords.getPassword(name);
	}

	//-------------------------------------------------------------------------------------

	public void sendLog(int code, String message) {
		logger.sendLog(code, message, "");
	}

	public void sendLog(int code, String message, String value) {
		logger.sendLog(code, message, value);
	}

	public void sendLog(int code, String message, Object value) {
		logger.sendLog(code, message, value);
	}

	public void sendInfoLog(String message, String value) {
		logger.sendInfo(message, value);
	}

	public void sendActionLog(Action action, String testName, int line) {
		logger.sendAction(action, testName, line); 
	}

	public void sendWarningLog(String message, String value) {
		logger.sendWarning(message, value); 
	}

	public void sendErrorLog(String message, String value) {
		logger.sendError(message, value); 
	}

	public void sendCommentLog(String calculated) {
		logger.sendExecLog(COMMENT_LOG, calculated);
	}

	public void sendScriptInfo(String value) {
		logger.sendExecLog(SCRIPT_LOG, value); 
	}

	public void sendScriptFail(String value) {
		if(value != null) {
			logger.sendExecLog(AtsLogger.FAILED, value); 
		}
	}

	//---------------------------------------------------------------------------------------------------

	public void setLogger(ExecutionLogger logger) {
		if(logger != null) {
			this.logger = logger;
		}
	}

	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}

	protected void setTestExecutionVariables(Map<String, String> params) {
		this.testExecutionVariables = params;
	}

	protected Map<String, String> getTestExecutionVariables() {
		return testExecutionVariables;
	}

	//-------------------------------------------------------------------------------------------------
	//  getters and setters for serialization
	//-------------------------------------------------------------------------------------------------

	public List<Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<Variable> data) {
		this.variables = data;
	}

	public ParameterList getParameterList() {
		return parameterList;
	}

	public void setParameterList(ParameterList data) {
		this.parameterList = data;
	}

	public CalculatedValue[] getReturns() {
		if(returns != null) {
			return returns.toArray(new CalculatedValue[returns.size()]);
		}
		return null;
	}

	public void setReturns(CalculatedValue[] data) {
		this.returns = new ArrayList<CalculatedValue>(Arrays.asList(data));
	}

	//-------------------------------------------------------------------------------------------------
	// variables
	//-------------------------------------------------------------------------------------------------

	protected void addToScriptCallTree(ActionTestScript sc, String name) {
		if(scriptCallTree != null && !scriptCallTree.stream().filter(a -> a.getTestName().equals(name)).findFirst().isPresent()) {
			scriptCallTree.add(sc);
		}
	}

	public String getGlobalVariableValue(String varPath) {
		if(scriptCallTree != null) {
			final int lastDot = varPath.lastIndexOf(".");
			if(lastDot > -1) {
				final String scriptPath = varPath.substring(0, lastDot);
				final Optional<ActionTestScript> sc = scriptCallTree.stream().filter(a -> a.getTestName().equals(scriptPath)).findFirst();
				if(sc.isPresent()) {
					final Variable scVar = sc.get().getVariable(varPath.substring(lastDot+1));
					if(scVar != null) {
						return scVar.getCalculatedValue();
					}
				}
			}
			sendWarningLog("Unable to find global variable", varPath);
		}
		return "";
	}

	public boolean checkVariableExists(String name){
		for(Variable variable : getVariables()){
			if(variable.getName().equals(name)){
				return true;
			}
		}
		return false;
	}

	public Variable getVariable(String name, boolean noCalculation){

		Variable foundVar = getVariable(name);

		if(foundVar == null) {
			foundVar = createVariable(name, new CalculatedValue(this, ""), null);
		}

		if(noCalculation) {
			foundVar.getValue().setData("");
			foundVar.setCalculation(false);
		}

		return foundVar;
	}

	public Variable getVariable(String name) {
		final Optional<Variable> opt = (variables.stream().filter(p -> p.getName().equals(name))).findFirst();
		if(opt != null && opt.isPresent()){
			return opt.get();
		}
		return null;
	}

	public Variable addVariable(String name, CalculatedValue value, Transformer transformer){
		Variable foundVar = getVariable(name);
		if(foundVar == null) {
			foundVar = createVariable(name, value, transformer);
		}else {
			foundVar.setValue(value);
			foundVar.setTransformation(transformer);
		}
		return foundVar;
	}

	public Variable createVariable(String name, CalculatedValue value, Transformer transformer){
		final Variable newVar = new Variable(name, value, transformer);
		variables.add(newVar);
		return newVar;
	}

	public String getVariableValue(String variableName) {
		//return getVariable(variableName, false).getValue().getCalculated();
		return getVariable(variableName, false).getCalculatedValue();
	}

	//-------------------------------------------------------------------------------------------------
	// parameters
	//-------------------------------------------------------------------------------------------------

	public String[] getParameters() {
		if(parameterList == null) {
			return new String[0];
		}
		return parameterList.getParameters();
	}

	public ScriptValue getParameter(String name) {
		return new ScriptValue(getParameterValue(name, ""));
	}

	public ScriptValue getParameter(int index) {
		return new ScriptValue(getParameterValue(index, ""));
	}

	public String getParameterValue(String name) {
		return getParameterValue(name, "");
	}

	public String getParameterValue(String name, String defaultValue) {

		if(parameterList == null) {
			return defaultValue;
		}

		try {
			int index = Integer.parseInt(name);
			return getParameterValue(index, defaultValue);
		}catch (NumberFormatException e) {}

		return parameterList.getParameterValue(name, defaultValue);
	}

	public String getParameterValue(int index) {
		return getParameterValue(index, "");
	}

	public String getParameterValue(int index, String defaultValue) {
		if(parameterList == null) {
			return defaultValue;
		}
		return parameterList.getParameterValue(index, defaultValue);
	}

	//-------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------

	public String getSystemValue(String name){
		if(channelManager != null) {
			final Channel cnl = channelManager.getCurrentChannel();
			if(cnl != null) {
				return cnl.getSystemValue(name);
			}
		}
		return "";
	}

	//-------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------
	
	public String getEnvironmentValue(String name, String defaultValue) {

		String value = System.getProperty(name);
		if(value != null) {
			return value;
		}

		if(testExecutionVariables != null) {
			value = testExecutionVariables.get(name);
			if(value != null) {
				return value;
			}
		}

		value = System.getenv(name);
		if(value != null) {
			return value;
		}

		return defaultValue;
	}

	public String getUuidValue() {
		return UUID.randomUUID().toString();
	}	

	public String getTodayValue() {
		return DateTransformer.getTodayValue();
	}	

	public String getNowValue() {
		return TimeTransformer.getNowValue();
	}

	public int getIteration() {
		return iteration;
	}

	public String getCsvFilePath() {
		return csvAbsoluteFilePath;
	}

	public File getCsvFile() {
		return new File(csvAbsoluteFilePath);
	}

	public File getAssetsFile(String relativePath) {
		if(!relativePath.startsWith("/")) {
			relativePath = "/" + relativePath;
		}
		relativePath = Project.ASSETS_FOLDER + relativePath;

		final URL url = getClass().getClassLoader().getResource(relativePath);
		if(url != null) {
			try {
				return Paths.get(url.toURI()).toFile();
			} catch (URISyntaxException e) {}
		}
		return null;
	}

	public String getAssetsUrl(String relativePath) {
		final URL url = getClass().getClassLoader().getResource(relativePath);
		if(url != null) {
			return "file://" + url.getPath();
		}
		return "";
	}
}