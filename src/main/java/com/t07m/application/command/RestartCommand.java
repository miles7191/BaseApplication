/*
 * Copyright (C) 2021 Matthew Rosato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.t07m.application.command;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.application.Application;
import com.t07m.console.Command;
import com.t07m.console.Console;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class RestartCommand extends Command{

	private static Logger logger = LoggerFactory.getLogger(RestartCommand.class);
	
	private final Application app;
	
	private long lastRun = 0;
	
	public RestartCommand(Application app) {
		super("Restart");
		this.app = app;
		OptionParser op = new OptionParser();
		String[] forceOptions = {"f", "force"};
		op.acceptsAll(Arrays.asList(forceOptions), "Force");
		this.setOptionParser(op);
	}

	public void process(OptionSet optionSet, Console console) {
		if(optionSet != null && optionSet.has("force")) {
			logger.info("Attempting to restart application.");
			app.restart();
		}else {
			if(System.currentTimeMillis() - lastRun < TimeUnit.SECONDS.toMillis(5)) {
				logger.info("Attempting to restart application.");
				lastRun = 0;
				app.restart();
			}else {
				logger.warn("Restarting will make a best effort to cleanup and restart the application. This may result in unpredictable behaviors. Only use this if absolutely necessary.");
				logger.info("Type Restart again to confirm.");
				lastRun = System.currentTimeMillis();
			}
		}
		
	}

}
