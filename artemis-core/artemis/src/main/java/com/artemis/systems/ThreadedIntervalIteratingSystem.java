package com.artemis.systems;

import com.artemis.Aspect;

public class ThreadedIntervalIteratingSystem extends ThreadedIteratingSystem {

    /** Accumulated delta to keep track of interval. */
    protected float acc;
    /** How long to wait between updates. */
    private final float interval;

    private float intervalDelta;

    public ThreadedIntervalIteratingSystem(Aspect.Builder aspect, float interval) {
        super(aspect);
        this.interval = interval;
    }

    public ThreadedIntervalIteratingSystem(float interval) {
        super(null);
        this.interval = interval;
    }

    @Override
    protected boolean checkProcessing() {
        acc += getTimeDelta();
        if(acc >= interval) {
            acc -= interval;
            intervalDelta = (acc - intervalDelta);

            return true;
        }
        return false;
    }

    @Override
    protected void process(int entityId) {

    }

    protected float getTimeDelta() {
        return world.getDelta();
    }
}
