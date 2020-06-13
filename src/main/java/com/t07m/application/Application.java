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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Application {

	private boolean running = false;
	private ExecutorService es = null;
	private List<Service> services;

	public Application() {
		internalInit();
		start();
	}
	
	public abstract void init();

	private void internalInit() {
		es = Executors.newWorkStealingPool();
		services = new ArrayList<Service>();
		init();
		for(Service s : services) {
			s.init();
		}
	}

	public void registerService(Service service) {
		synchronized(services) {
			if (!this.services.contains(service))
				this.services.add(service); 
		}
	}

	public void removeService(Service service) {
		synchronized(services) {
			this.services.remove(service);
		}
	}

	protected void setExecutorService(ExecutorService es) {
		synchronized(this.es) {
			if(this.es != null) {
				this.es.shutdown();
			}
			this.es = es;
		}
	}

	private int loop() {
		synchronized(services) {
			for (Service service : this.services) {
				if (!service.isRunning() && !service.isQueued()) {
					service.queued();
					this.es.execute(service);
				} 
			} 
		}
		return 10;
	}

	private void internalCleanup() {
		synchronized(services) {
			for (Service service : this.services)
				service.cleanup(); 
		}
	}

	private void start() {
		if (!this.running) {
			this.running = true;
			Runnable runnable = () -> {
				while (this.running) {
					try {
						Thread.sleep(loop());
					} catch (Exception e) {
						e.printStackTrace();
					} 
				} 
				internalCleanup();
				System.exit(0);
			};
			Thread thread1 = new Thread(runnable);
			thread1.start();
			try {
				Thread.currentThread().join();
			} catch (InterruptedException interruptedException) {}
		} 
	}

	public void stop() {
		this.running = false;
	}
}
