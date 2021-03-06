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

import org.openqa.selenium.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SendKeyData {

	public static final String EMPTY_DATA = "&empty;";

	private static final String KEY_DOWN_SHIFT = "SHIFT";
	private static final String KEY_DOWN_ALT = "ALT";
	private static final String KEY_DOWN_CONTROL = "CONTROL";
	
	private static final String KEY_PREFIX = "$KEY-";

	private String data;
	private boolean enterKey = false;
	private Keys downKey;
	
	private String specialKeyString;
	private StringBuffer specialKeys;
	
	public SendKeyData(String key, String spare) {

		this.data = spare;
		this.downKey = null;
		StringBuffer sequence = new StringBuffer();

		if(spare != null && spare.length() > 0) {

			if(KEY_DOWN_SHIFT.equals(key)) {
				sequence.append(Keys.SHIFT);
				downKey = Keys.SHIFT;
			}else if(KEY_DOWN_ALT.equals(key)) {
				sequence.append(Keys.ALT);
				downKey = Keys.ALT;
			}else if(KEY_DOWN_CONTROL.equals(key)) {
				sequence.append(Keys.CONTROL);
				this.enterKey = true;
				downKey = Keys.CONTROL;
			}
			sequence.append(spare.toLowerCase());
		}else {
			try {
				sequence.append(Keys.valueOf(key));
				specialKeyString = KEY_PREFIX + key;
			}catch(IllegalArgumentException e) {}
		}
		
		if(sequence.length() > 0) {
			specialKeys = sequence;
		}
	}

	public SendKeyData(String data) {
		final char[] dataArray = data.toCharArray();
		StringBuffer sequence = new StringBuffer();
		this.data = "";
		for (char val : dataArray) 
		{ 
			final Keys k = Keys.getKeyFromUnicode(val);
			
			if(k != null) {
				switch(k.name())
				{
					case "CONTROL":
						this.enterKey = true;
						downKey = Keys.CONTROL;
						break;
					case "SHIFT":
						downKey = Keys.SHIFT;
						break;
					case "ALT":
						downKey = Keys.ALT;
						break;
				}
			} else {
				sequence.append(val);
				this.data += String.valueOf(val);
			}
		}
	}

	private Keys getNumpad(char d) {

		switch (Character.getNumericValue(d)) {
		case 1 :
			return Keys.NUMPAD1;
		case 2 :
			return Keys.NUMPAD2;
		case 3 :
			return Keys.NUMPAD3;
		case 4 :
			return Keys.NUMPAD4;
		case 5 :
			return Keys.NUMPAD5;
		case 6 :
			return Keys.NUMPAD6;
		case 7 :
			return Keys.NUMPAD7;
		case 8 :
			return Keys.NUMPAD8;
		case 9 :
			return Keys.NUMPAD9;
		}
		return Keys.NUMPAD0;
	}

	public String getData() {
		return data;
	}	

	public boolean isEnterKey() {
		return enterKey;
	}
	
	public Keys getDownKey() {
		return downKey;
	}

	//---------------------------------------------------------------------------
	// get sequence by driver type
	//---------------------------------------------------------------------------

	public CharSequence getSequenceWeb(boolean withDigit) {
		
		if(specialKeys != null) {
			return specialKeys;
		}
		
		final StringBuffer sequence = new StringBuffer();

		for (int i = 0, n = data.length(); i < n; i++) {
			final char c = data.charAt(i);
			if(withDigit && Character.isDigit(c)) {
				sequence.append(getNumpad(c));
			}else {
				sequence.append(c);
			}
		}
		return sequence;
	}	

	public String getSequenceDesktop() {
		String sequenceData = "";
		if (specialKeyString != null) {
			sequenceData = specialKeyString;
		}else if(data.length() > 0){
			sequenceData = data;
		}
		return Base64.getEncoder().encodeToString(sequenceData.getBytes(StandardCharsets.UTF_8));
	}

	public String getSequenceMobile() {
		if (specialKeyString != null) {
			return specialKeyString;
		} else if(data.length() > 0) {
			return data;
		} else {
			return EMPTY_DATA;
		}
	}
}