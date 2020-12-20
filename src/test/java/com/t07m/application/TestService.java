/*
 * Copyright (C) 2020 Matthew Rosato
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
package com.t07m.application;

import java.util.concurrent.TimeUnit;

public class TestService extends Service<TestApplication>{

	int count = 0;
	
	public TestService(TestApplication app) {
		super(app, TimeUnit.SECONDS.toMillis(1));
	}

	public void init() {
		System.out.println("Initialized  Service");
	}
	
	public void process() {
		System.out.println("Hello World");
		count++;
		if(count == 10) {
			System.out.println("Count hit 10. Sending StopCommand");
			getApp().stop();
		}
	}

}
