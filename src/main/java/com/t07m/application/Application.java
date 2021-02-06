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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.t07m.application.command.RestartCommand;
import com.t07m.console.Console;
import com.t07m.console.NativeConsole;
import com.t07m.console.swing.ConsoleWindow;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public abstract class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	private @Getter boolean running = false;
	private ExecutorService es = null;
	private List<Service> services;
	private static @Getter @Setter long minUpdateFrequency = 1000;

	private @Getter Console console;

	private boolean pause;

	public Application(boolean gui) {
		this(gui, "Console");
	}

	public Application(boolean gui, String guiName) {
		internalInit();
		initConsole(gui, guiName);
		logger.debug("Java Version: " + Runtime.version());
		logger.debug("Available Processors: " + Runtime.getRuntime().availableProcessors());
		logger.debug("Max Memory: " + formatSize(Runtime.getRuntime().maxMemory()));
	}

	private static String formatSize(long v) {
		if (v < 1024) return v + " B";
		int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
		return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
	}

	public abstract void init();

	private void initConsole(boolean gui, String guiName) {
		if(gui) {
			ConsoleWindow cw = new ConsoleWindow(guiName) {
				public void close() {
					stop();
				}
			};
			cw.setup();
			cw.setLocationRelativeTo(null);
			cw.setVisible(true);
			this.console = cw;
		}else {
			this.console = new NativeConsole() {
				public void close() {
					stop();
					console.cleanup();
				}
			};
			this.console.setup();
		}
		this.console.registerCommand(new RestartCommand(this));
	}

	private void internalInit() {
		es = Executors.newCachedThreadPool();
		services = new ArrayList<Service>();
	}

	public void registerService(Service service) {
		synchronized(services) {
			if (!this.services.contains(service)) {
				this.services.add(service);
			}
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
		long nextUpdate = (long) (minUpdateFrequency/.75);
		if(!pause) {
			synchronized(services) {
				for (Service service : this.services) {
					if (!service.isRunning() && !service.isQueued() ) {
						long next = service.getUpdateFrequency() - (System.currentTimeMillis() - service.getLastUpdate());
						if(next > 0 && nextUpdate > next) {
							nextUpdate = next;
						}
						if(next <= 0)
							service.setFuture(this.es.submit(service));
					}else if(service.isRunning()) {
						Future future = service.getFuture();
						if(future != null && (future.isDone() || future.isCancelled())) {
							service.setRunning(false);
							service.setFuture(null);
						}
					}
				}
			}
		}
		return Math.max((int) (nextUpdate * .75), 0);
	}

	private void internalCleanup() {
		synchronized(services) {
			for (Service service : this.services) {
				service.cleanup(); 
			}
			cleanup();
		}
	}

	public void start() {
		if (!this.running) {
			this.running = true;
			init();
			for(Service s : services) {
				s.init();
			}
			Runnable runnable = () -> {
				while (this.running) {
					try {
						Thread.sleep(loop());
					} catch (Exception e) {
						e.printStackTrace();
					} 
				} 
			};
			Thread thread1 = new Thread(runnable);
			thread1.start();
		} 
	}

	public void stop() {
		this.stop(true);
	}

	public void stop(boolean exit) {
		synchronized(services) {
			this.running = false;
			this.internalCleanup();
			this.services.clear();
			this.es.shutdown();
			try {
				this.es.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {}
			if(exit) {
				System.exit(0);
			}else {
				System.gc();
			}
		}
	}

	public void restart() {
		this.stop(false);
		this.console.clear();
		this.internalInit();
		this.start();
	}

	public void pauseExecution() {
		this.pause = true;
	}

	public void resumeExecution() {
		this.pause = false;
	}

	public void cleanup() {
		
	}
	
}
