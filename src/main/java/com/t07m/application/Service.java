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

public abstract class Service implements Runnable {
	
	  public final Application app;
	  
	  private final long UPDATE_FREQUENCY;
	  
	  private long lastUpdate = 0L;
	  
	  private boolean queued = false;
	  
	  private boolean running = false;
	  
	  public Service(Application app, long updateFrequency) {
	    this.app = app;
	    this.UPDATE_FREQUENCY = updateFrequency;
	  }
	  
	  public void init() {}
	  
	  public void cleanup() {}
	  
	  public abstract void process();
	  
	  public final boolean isRunning() {
	    return this.running;
	  }
	  
	  public final boolean isQueued() {
	    return this.queued;
	  }
	  
	  public final void run() {
	    this.running = true;
	    if (System.currentTimeMillis() - this.lastUpdate > this.UPDATE_FREQUENCY) {
	      process();
	      this.lastUpdate = System.currentTimeMillis();
	    } 
	    this.queued = false;
	    this.running = false;
	  }
	  
	  public final void queued() {
	    this.queued = true;
	  }
	  
	  public final void forceUpdate() {
	    this.lastUpdate = 0L;
	  }
	}