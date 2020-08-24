package com.artemis;

import com.artemis.utils.Bag;

public class MultiWorldConfiguration {
	
	final Bag<BaseSystem> systems = new Bag<BaseSystem>(BaseSystem.class);

	public void with(BaseSystem baseSystem) {
		this.systems.add(baseSystem);
	}

	public Bag<BaseSystem> getSystems() {
		return systems;
	}
}
