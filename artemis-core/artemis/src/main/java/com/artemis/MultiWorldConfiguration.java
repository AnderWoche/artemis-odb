package com.artemis;

import com.artemis.utils.Bag;

public class MultiWorldConfiguration {
	
	final Bag<BaseSystem> systems = new Bag<BaseSystem>(BaseSystem.class);

	public MultiWorldConfiguration with(BaseSystem baseSystem) {
		this.systems.add(baseSystem);
		return this;
	}

	public Bag<BaseSystem> getSystems() {
		return systems;
	}
}
