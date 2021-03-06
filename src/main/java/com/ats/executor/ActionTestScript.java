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

package com.ats.executor;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.TestRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.ats.crypto.Passwords;
import com.ats.element.SearchedElement;
import com.ats.executor.channels.Channel;
import com.ats.executor.channels.ChannelManager;
import com.ats.generator.objects.Cartesian;
import com.ats.generator.objects.MouseDirectionData;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.objects.mouse.MouseKey;
import com.ats.generator.objects.mouse.MouseScroll;
import com.ats.generator.objects.mouse.MouseSwipe;
import com.ats.generator.variables.CalculatedProperty;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.ConditionalValue;
import com.ats.generator.variables.RandomStringValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.parameter.Parameter;
import com.ats.generator.variables.parameter.ParameterList;
import com.ats.generator.variables.transform.DateTransformer;
import com.ats.generator.variables.transform.NumericTransformer;
import com.ats.generator.variables.transform.RegexpTransformer;
import com.ats.generator.variables.transform.TimeTransformer;
import com.ats.generator.variables.transform.Transformer;
import com.ats.recorder.IVisualRecorder;
import com.ats.recorder.VisualRecorder;
import com.ats.recorder.VisualRecorderNull;
import com.ats.script.Project;
import com.ats.script.Script;
import com.ats.script.ScriptHeader;
import com.ats.script.actions.Action;
import com.ats.script.actions.ActionCallscript;
import com.ats.script.actions.ActionComment;
import com.ats.script.actions.ActionExecute;
import com.ats.tools.Utils;
import com.ats.tools.logger.ExecutionLogger;
import com.ats.tools.logger.levels.AtsFailError;
import com.ats.tools.report.CampaignReportGenerator;
import com.ats.tools.report.SuitesReport;
import com.ats.tools.report.SuitesReportItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import okhttp3.OkHttpClient;

@Listeners({ExecutionListener.class, TestListener.class})
public class ActionTestScript extends Script implements ITest{

	public static final String MAIN_TEST_FUNCTION = "testMain";
	public static final String TEST_STOPPABLE = "com.ats.test.stoppable";
	public static final String SUITE_PARAMETERS = "parameters.txt";

	protected ActionTestScript topScript;

	private String[] returnValues;

	private String testName;
	private Project projectData;

	protected ScriptHeader getHeader() {return new ScriptHeader();}

	private ScriptStatus status = new ScriptStatus();

	public ActionTestScript() {
		super(null);
		init(new Passwords(getAssetsFile("")));
	}

	public ActionTestScript(File assetsFolder) {
		super(null);
		init(new Passwords(assetsFolder.toPath()));
	}

	public ActionTestScript(ExecutionLogger logger) {
		super(logger);
		init(null);
	}

	public ActionTestScript(ActionTestScript topScript) {
		setTopScript(topScript);
	}

	public void setTopScript(ActionTestScript topScript, String scriptName) {
		setTopScript(topScript);
		this.testName = scriptName;
		topScript.addToScriptCallTree(this, scriptName);
	}

	public void setTopScript(ActionTestScript topScript) {
		init(topScript, topScript.getChannelManager(), topScript.getPasswords());
	}

	private void init(Passwords passwords) {
		init(this, new ChannelManager(this), passwords);
	}

	private void init(ActionTestScript topScript, ChannelManager channelManager, Passwords passwords) {
		this.topScript = topScript;
		this.channelManager = channelManager;
		this.passwords = passwords;

		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger(Actions.class.getName()).setLevel(Level.OFF);
		java.util.logging.Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
	}

	public String[] getReturnValues() {
		return returnValues;
	}

	public Passwords getPasswords() {
		return passwords;
	}

	public void updateTestName(String name) {
		this.testName = name;
	}

	public void scriptFail(String message) {
		fail(message);
	}

	public void addErrorStack(String value) {
		status.addErrorStack(value);
	}

	public ScriptStatus getStatus() {
		return status;
	}

	//----------------------------------------------------------------------------------------------------------
	// TestNG management
	//----------------------------------------------------------------------------------------------------------

	@BeforeSuite(alwaysRun=true)
	public void beforeSuite(ITestContext ctx) {

		final SuitesReportItem currentSuite = new SuitesReportItem((TestRunner) ctx);

		setLogger(new ExecutionLogger(System.out, currentSuite.logLevel));

		sendScriptInfo("Start suite -> " + ctx.getSuite().getName());

		Path outputPath = null;
		final String outputFolder = System.getProperty("output-folder");
		if(outputFolder != null) {
			outputPath = Paths.get(outputFolder);
		}else {
			outputPath = Paths.get(ctx.getOutputDirectory()).getParent();
		}
		outputPath.toFile().mkdirs();

		final File jsonSuiteFile = outputPath.resolve(CampaignReportGenerator.ATS_JSON_SUITES).toFile();

		final Gson gson = new Gson();
		SuitesReport suiteReport = null;

		try{
			if(jsonSuiteFile.exists()) {
				final JsonReader reader = new JsonReader(new FileReader(jsonSuiteFile));
				suiteReport = gson.fromJson(reader, SuitesReport.class);
				reader.close();

				suiteReport.add(currentSuite);	

			}else {
				suiteReport = new SuitesReport(gav(), currentSuite);
			}

			final FileWriter writer = new FileWriter(jsonSuiteFile);
			gson.toJson(
					suiteReport, 
					writer);

			writer.close();

		}catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	@AfterSuite(alwaysRun=true)
	public void afterSuite(ITestContext ctx) {
		sendScriptInfo("Suite terminated -> " + ctx.getSuite().getName());
	}

	@BeforeClass(alwaysRun=true)
	public void beforeClass(ITestContext ctx) {

		final TestRunner runner = (TestRunner) ctx;
		final String suiteName = ctx.getSuite().getName();

		final SuitesReportItem currentSuite = new SuitesReportItem((TestRunner) ctx);
		setLogger(new ExecutionLogger(System.out, currentSuite.logLevel));	

		final String outputFolder = System.getProperty("output-folder");
		if(outputFolder != null) {
			runner.setOutputDirectory(outputFolder + File.separator + suiteName);
		}

		testName = this.getClass().getName();
		scriptCallTree = new ArrayList<ActionTestScript>(Arrays.asList(this));

		status = new ScriptStatus(testName, suiteName);

		if("true".equalsIgnoreCase(runner.getTest().getParameter("check.mode"))) {
			throw new SkipException("check mode : " + testName);
		}else {

			final Map<String, String> params = runner.getTest().getAllParameters();
			setTestExecutionVariables(params);

			//-----------------------------------------------------------
			// check report output specified
			//-----------------------------------------------------------

			int visualQuality = Utils.string2Int(getEnvironmentValue("visual.report", "0"));
			boolean xml = getEnvironmentValue("xml.report", "").equalsIgnoreCase("true");

			final String atsReport = System.getProperty(CampaignReportGenerator.ATS_REPORT);
			if(atsReport != null && !atsReport.equals("0")) {
				if(visualQuality == 0) {
					visualQuality = 3;
				}
				xml = true;
			}

			if(visualQuality > 0 || xml) {

				final ScriptHeader header = getHeader();

				header.setName(getTestName());

				final File output = new File(runner.getOutputDirectory());
				if(!output.exists()) {
					output.mkdirs();
				}

				setRecorder(new VisualRecorder(this, output, header, xml, visualQuality, logger));
			}

			final JsonObject logs = new JsonObject();
			logs.addProperty("suite", suiteName);
			logs.addProperty("xmlReport", xml);
			logs.addProperty("visualQuality", visualQuality);

			final Properties parametersProperties = new Properties();
			final JsonArray parametersArray = new JsonArray();
			for(Entry<String, String>param : params.entrySet()) {

				final String k = param.getKey();
				final String v = getEnvironmentValue(k, param.getValue());

				parametersProperties.put(k, v);

				final JsonObject elem = new JsonObject();
				elem.addProperty(k, v);
				parametersArray.add(elem);
			}
			logs.add("parameters", parametersArray);

			try {
				parametersProperties.store(new FileOutputStream(Paths.get(runner.getOutputDirectory(), SUITE_PARAMETERS).toFile()), null);
			} catch (IOException e) {}

			sendScriptInfo("Starting script (" + testName + ") -> " + logs.toString());

			//-----------------------------------------------------------
			//-----------------------------------------------------------

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					tearDown();
				}
			});

			if("true".equals(System.getProperty(TEST_STOPPABLE))) {
				sendScriptInfo("This script can be stopped if 'q' is sent to 'System.in' stream");
				new StopExecutionThread(this, System.in).start();
			}
		}
	}

	@AfterClass(alwaysRun=true)
	public void afterClass(ITestContext ctx) {
		final TestRunner runner = (TestRunner) ctx;
		status.endLogs(this, runner);
	}

	@AfterTest(alwaysRun=true)
	public void testFinished(ITestContext ctx) {

	}

	@AfterMethod(alwaysRun=true)
	public void cleanup(){
		sendScriptFail(status.getCallscriptStack());
		stopRecorder();
		tearDown();
	}

	@Override
	public String getTestName() {
		return testName;
	}

	//----------------------------------------------------------------------------------------------------------
	// Channels management
	//----------------------------------------------------------------------------------------------------------

	public Channel getCurrentChannel(){
		return getChannelManager().getCurrentChannel();
	}

	public Channel getChannel(String name){
		return getChannelManager().getChannel(name);
	}

	//----------------------------------------------------------------------------------------------------------
	// Call script
	//----------------------------------------------------------------------------------------------------------

	public ActionTestScript getTopScript() {
		return topScript;
	}

	public void initCalledScript(ActionTestScript atsCaller, String testName, int line, ParameterList parameters, List<Variable> variables, int iteration, int iterationMax, String scriptName, String type, String filePath) {

		this.iteration = iteration;
		this.csvAbsoluteFilePath = filePath;
		this.testName = getClass().getName();

		final JsonObject log = atsCaller.getConditionLogs();

		log.addProperty("called", scriptName);
		log.addProperty("iteration", iteration+1 + "/" + iterationMax);
		log.addProperty("type", type);

		if(filePath != null) {
			log.addProperty("url", filePath);
		}

		if(parameters != null) {
			setParameterList(parameters);
			if(parameters.getList().size() > 0) {
				final JsonArray parametersArray = new JsonArray();
				for (Parameter p : parameters.getList()) {
					final JsonObject jso = new JsonObject();
					jso.addProperty(p.getName(), p.getCalculated());
					parametersArray.add(jso);
				}
				log.add("parameters", parametersArray);
			}
		}

		if(variables != null) {
			setVariables(variables);
		}
		setTestExecutionVariables(topScript.getTestExecutionVariables());

		topScript.sendScriptInfo(ActionCallscript.getScriptLog(testName, line, log));
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}

	//----------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------

	public void setProjectData(Project value) {
		projectData = value;
		projectData.synchronize();
		passwords = new Passwords(projectData.getAssetsFolderPath().toFile());
	}

	public void tearDown(){
		sendInfoLog("Drivers", "closing ...");
		getChannelManager().tearDown();
	}

	//----------------------------------------------------------------------------------------------------------
	// Generated methods
	//----------------------------------------------------------------------------------------------------------

	public static final String JAVA_VAR_FUNCTION_NAME = "var";
	public Variable var(String name, CalculatedValue value){
		return createVariable(name, value, null);
	}

	public Variable var(String name){
		return createVariable(name, new CalculatedValue(""), null);
	}

	public Variable var(String name, Transformer transformer){
		return createVariable(name, new CalculatedValue(""), transformer);
	}

	public Variable var(String name, CalculatedValue value, Transformer transformer){
		return createVariable(name, value, transformer);
	}

	public static final String JAVA_GLOBAL_VAR_FUNCTION_NAME = "gvar";
	public String gvar(String varPath){
		return topScript.getGlobalVariableValue(varPath);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_VALUE_FUNCTION_NAME = "clv";
	public CalculatedValue clv(Object ... data) {
		return new CalculatedValue(this, data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PARAM_FUNCTION_NAME = "prm";

	public String prm(String name) {
		return getParameterValue(name);
	}

	public String prm(String name, String defaultValue) {
		return getParameterValue(name, defaultValue);
	}

	public String prm(int index) {
		return getParameterValue(index);
	}

	public String prm(int index, String defaultValue) {
		return getParameterValue(index, defaultValue);
	}

	public CalculatedValue[] prm(CalculatedValue ... values) {
		return values;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_RETURNS_FUNCTION_NAME = "rtn";
	public void rtn(CalculatedValue ... values) {

		int i = 0;
		returnValues = new String[values.length];

		for(CalculatedValue calc : values) {
			returnValues[i] = calc.getCalculated();
			i++;
		}

		updateVariables();
	}

	//---------------------------------------------------------------------------------------------

	public void rtn(String ... values) {

		int i = 0;
		returnValues = new String[values.length];

		for(String value : values) {
			returnValues[i] = value;
			i++;
		}

		updateVariables();
	}

	private void updateVariables() {
		List<Variable> variables = getVariables();

		int index = 0;
		for(String value : returnValues) {
			if(variables.size() < index + 1) {
				break;
			}
			variables.get(index).setData(value);
			index++;
		}
	}

	//---------------------------------------------------------------------------------------------

	public void returnValues(String... values) {
		returnValues = values;
		updateVariables();
	}

	public void returnValues(Object... values) {
		returnValues = Arrays.stream(values).map(Object::toString).toArray(String[]::new);
		updateVariables();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_RNDSTRING_FUNCTION_NAME = "rds";
	public String rds(int len) {
		return new RandomStringValue(len, null).exec();
	}

	public String rds(int len, String type) {
		return new RandomStringValue(len, type).exec();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ENV_FUNCTION_NAME = "env";
	public String env(String name) {
		return getEnvironmentValue(name, "");
	}

	public String env(String name, String defaultValue) {
		return getEnvironmentValue(name, defaultValue);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_SYSTEM_FUNCTION_NAME = "sys";
	public String sys(String name){
		return getSystemValue(name);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_PROPERTY_FUNCTION_NAME = "prp";
	public CalculatedProperty prp(String type, String name, CalculatedValue value){
		return new CalculatedProperty(type, name, value);
	}

	//---------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------

	public static final String JAVA_UUID_FUNCTION_NAME = "uid";
	public String uid() {
		return getUuidValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_TODAY_FUNCTION_NAME = "td";
	public String td() {
		return getTodayValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_NOW_FUNCTION_NAME = "nw";
	public String nw() {
		return getNowValue();
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ITERATION_FUNCTION_NAME = "itr";
	public int itr() {
		return getIteration();
	}

	//---------------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------------

	public static final String JAVA_ELEMENT_FUNCTION_NAME = "el";
	public SearchedElement el(SearchedElement parent, int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(parent, index, tagName, properties);
	}

	public SearchedElement el(int index, String tagName, CalculatedProperty ... properties) {
		return new SearchedElement(null, index, tagName, properties);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_REGEX_FUNCTION_NAME = "rx";
	public RegexpTransformer rx(String patt, int group) {
		return new RegexpTransformer(patt, group);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_DATE_FUNCTION_NAME = "dt";
	public DateTransformer dt(String ... data) {
		return new DateTransformer(data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_TIME_FUNCTION_NAME = "tm";
	public TimeTransformer tm(String ... data) {
		return new TimeTransformer(data);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_NUMERIC_FUNCTION_NAME = "nm";
	public NumericTransformer nm(int dp, boolean comma) {
		return new NumericTransformer(dp, comma);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_POS_FUNCTION_NAME = "ps";
	public MouseDirectionData ps(Cartesian cart, CalculatedValue value) {
		return new MouseDirectionData(cart, value);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_MOUSE_FUNCTION_NAME = "ms";
	public Mouse ms(String type) {
		return new Mouse(type);
	}

	public Mouse ms(String type, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new Mouse(type, hpos, vpos);
	}

	public MouseKey ms(String type, Keys key, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseKey(type, key, hpos, vpos);
	}

	public MouseKey ms(String type, Keys key) {
		return new MouseKey(type, key);
	}

	public MouseScroll ms(int scroll, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseScroll(scroll, hpos, vpos);
	}

	public MouseScroll ms(int scroll) {
		return new MouseScroll(scroll);
	}

	public MouseSwipe ms(MouseDirectionData hdir, MouseDirectionData vdir, MouseDirectionData hpos, MouseDirectionData vpos) {
		return new MouseSwipe(hdir, vdir, hpos, vpos);
	}

	public MouseSwipe ms(MouseDirectionData hdir, MouseDirectionData vdir) {
		return new MouseSwipe(hdir, vdir);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_EMBEDED_FUNCTION_NAME = "emb";
	public String emb(String relativePath) {
		return getAssetsUrl(relativePath);
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_GAV_FUNCTION_NAME = "gav";
	public String gav() {
		return "";
	}

	//----------------------------------------------------------------------------------------------------------
	// Actions
	//----------------------------------------------------------------------------------------------------------

	public static final String JAVA_CONDITION_FUNCTION = "condition";
	public boolean condition(String type, int line, Variable variable, CalculatedValue calculatedValue) {

		condition = new ConditionalValue(type, variable, calculatedValue);

		if(condition.isExec()) {
			return true;
		}else {
			getTopScript().sendScriptInfo(ActionCallscript.getScriptLog(getTestName(), line, condition.getLog()));
			condition = null;
			return false;
		}
	}

	private ConditionalValue condition;
	public JsonObject getConditionLogs() {
		JsonObject result = new JsonObject();
		if(condition != null) {
			result = condition.getLog(result);
			condition = null;
		}
		return result;
	}

	//---------------------------------------------------------------------------------------------

	public static final String JAVA_EXECUTE_FUNCTION_NAME = "exec";

	public void exec(int line, Action action){
		action.execute(this, getTestName(), line);
		getTopScript().actionFinished(getTestName(), line, action);
	}

	public void exec(int line, ActionComment action){
		if(action.execute(this, getTestName(), line)) {
			getTopScript().actionFinished(getTestName(), line, action);
		}
	}

	public void exec(int line, ActionCallscript action){
		action.execute(this, getTestName(), line);
		getTopScript().actionFinished(getTestName(), line, action);
	}

	public void exec(int line, ActionExecute action){
		action.execute(this, getTestName(), line);
		getTopScript().actionFinished(getTestName(), line, action, action.getStopPolicy());
	}

	//---------------------------------------------------------------------------------------------

	private void failedAt(String actionClass, String script, int line, String app, int errorCode, String errorMessage) {
		status.failedAt(actionClass, script, line, app, errorCode, errorMessage);
		sendErrorLog(status.getErrorScript(), status.getErrorInfo());
	}

	public void actionFinished(String testName, int line, Action action) {
		actionFinished(testName, line, action, 0);
	}

	public void actionFinished(String testName, int line, Action action, int stopPolicy) {
		
		status.addAction(action instanceof ActionCallscript);

		final ActionStatus actionStatus = action.getStatus();

		if(actionStatus.isPassed()) {
			sendActionLog(action, testName, line);
		}else {
			if(stopPolicy == ActionExecute.TEST_STOP_FAIL) {
				if(actionFinishedFail(actionStatus, action, line, testName)) {
					throw new AtsFailError(status.getErrorScript(), status.getErrorInfo());
				}
			}else {
				sendActionLog(action, testName, line);
				if(stopPolicy == ActionExecute.TEST_CONTINUE_FAIL) {
					actionFinishedFail(actionStatus, action, line, testName);
				}
			}
		}
	}

	private boolean actionFinishedFail(ActionStatus actionStatus, Action action, int line, String testName) {
		if(status.isSuiteExecution()) {
			final String app = actionStatus.getChannelApplication();
			final String errorMessage = actionStatus.getFailMessage();

			getTopScript().failedAt(action.getClass().getSimpleName(), testName, line, app, actionStatus.getCode(), errorMessage);
			this.getRecorder().updateSummaryFail(testName, line, app, errorMessage);
			return true;
		}

		return false;
	}

	public void la (String message) {
		fail(message);
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Drag drop management
	//-----------------------------------------------------------------------------------------------------------

	private boolean dragWithDesktop = false;

	public void startDrag() {
		topScript.dragWithDesktop = getCurrentChannel().isDesktop();
	}

	public boolean isDesktopDragDrop() {
		return topScript.dragWithDesktop;
	}

	public void endDrag() {
		topScript.dragWithDesktop = false;
	}

	//-----------------------------------------------------------------------------------------------------------
	//  - Animation recorder
	//-----------------------------------------------------------------------------------------------------------

	private IVisualRecorder recorder = new VisualRecorderNull();

	public IVisualRecorder getRecorder() {
		return topScript.recorder;
	}

	public void setRecorder(IVisualRecorder value) {
		if((value instanceof VisualRecorderNull && this.recorder instanceof VisualRecorder) || (value instanceof VisualRecorder && this.recorder instanceof VisualRecorderNull)) {
			this.topScript.recorder.terminate();
			this.topScript.recorder = value;
		}
	}

	public void startRecorder(ScriptHeader info, int quality, boolean xml) {
		topScript.setRecorder(new VisualRecorder(topScript, info, projectData, xml, quality));
	}

	public void stopRecorder() {
		topScript.setRecorder(new VisualRecorderNull());
	}
}