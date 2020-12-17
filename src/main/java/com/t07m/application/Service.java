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

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Service<T extends Application> implements Runnable {

	public final T app;

	private final long UPDATE_FREQUENCY;

	private long lastUpdate = 0L;
	private Future<Service<T>> future;

	private AtomicBoolean running = new AtomicBoolean(false);

	public Service(T app, long updateFrequency) {
		this.app = app;
		this.UPDATE_FREQUENCY = updateFrequency;
	}

	public void init() {}

	public void cleanup() {}

	public abstract void process();

	public final boolean isRunning() {
		return this.running.get();
	}

	public final boolean isQueued() {
		return future != null;
	}

	public final void run() {
		this.running.set(true);
		this.lastUpdate = System.currentTimeMillis();
		try {
			process();
		} catch(RuntimeException e) {
			e.printStackTrace();
		}
		this.future = null;
		this.running.set(false);
	}

	public final void forceUpdate() {
		this.lastUpdate = 0L;
	}

	public final long getUpdateFrequency() {
		return this.UPDATE_FREQUENCY;
	}

	public final long getLastUpdate() {
		return this.lastUpdate;
	}

	Future<Service<T>> getFuture() {
		return future;
	}

	void setFuture(Future<Service<T>> future) {
		this.future = future;
	}

}