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

package com.ats.driver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ats.executor.drivers.DriverManager;

public class ApplicationProperties {
	
	public static int BROWSER_TYPE = 0;
	public static int DESKTOP_TYPE = 1;
	public static int MOBILE_TYPE = 2;
	public static int API_TYPE = 3;
	
	private int type = BROWSER_TYPE;
	
	private String name = DriverManager.CHROME_BROWSER;
	private String driver;
	private String uri;
	private int wait = -1;
	private int debugPort = -1;
	private int check = -1;
	private String lang;
	private String userDataDir;
	private String title;
	private String[] options;

	public ApplicationProperties(String name) {
		this.name = name;
	}
	
	public ApplicationProperties(int type, String name, String driver, String uri, int wait, int check, String lang, String userDataDir, String title, String[] options, int debugPort) {
		this.type = type;
		this.name = name;
		this.driver = driver;
		this.uri = uri;
		this.wait = wait;
		this.check = check;
		this.lang = lang;
		this.userDataDir = userDataDir;
		this.title = title;
		this.options = options;
		this.debugPort = debugPort;
	}
	
	public static String getUserDataPath(String userDataDir, String browserName) {
		if(userDataDir != null) {
			final Path p = Paths.get(userDataDir);
			if(p.isAbsolute()) {
				if((Files.exists(p) && Files.isDirectory(p)) || !Files.exists(p)) {
					return p.toFile().getAbsolutePath();
				}
			}else{
				final Path userDir = Paths.get(System.getProperty("user.home")).
						resolve("ats").
						resolve("profiles").
						resolve(browserName).
						resolve(userDataDir);
				return userDir.toFile().getAbsolutePath();
			}
		}
		return null;
	}
	
	public String getUserDataDirPath(String browserName) {
		return getUserDataPath(userDataDir, browserName);
	}
	
	public boolean isWeb() {
		return type == BROWSER_TYPE;
	}
	
	public boolean isDesktop() {
		return type == DESKTOP_TYPE;
	}
	
	public boolean isMobile() {
		return type == MOBILE_TYPE;
	}
	
	public boolean isApi() {
		return type == API_TYPE;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDriver() {
		return driver;
	}

	public String getUri() {
		return uri;
	}
	
	public int getWait() {
		return wait;
	}
	
	public int getDebugPort() {
		return debugPort;
	}
	
	public int getCheck() {
		return check;
	}
	
	public String getLang() {
		return lang;
	}
		
	public String getUserDataDir() {
		return userDataDir;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String[] getOptions() {
		return options;
	}
}