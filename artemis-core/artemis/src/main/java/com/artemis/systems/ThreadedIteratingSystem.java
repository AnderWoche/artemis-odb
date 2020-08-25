package com.artemis.systems;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.utils.IntBag;

import java.util.concurrent.*;

public abstract class ThreadedIteratingSystem extends BaseEntitySystem {

    public static final int CPU_THREADS;
    private static final ExecutorService executor;

    private final Future<?>[] runningTasks = new Future[CPU_THREADS];

    static {
        int threads = Runtime.getRuntime().availableProcessors();
        if(threads > 2) {
            CPU_THREADS = threads -1;
        } else {
            CPU_THREADS = threads;
        }
        executor = Executors.newFixedThreadPool(CPU_THREADS);
    }

    /**
     * Creates a new IteratingSystem.
     *
     * @param aspect
     *			the aspect to match entities
     */
    public ThreadedIteratingSystem(Aspect.Builder aspect) {
        super(aspect);

    }

    public ThreadedIteratingSystem() {
        super();
    }

    /**
     * Process a entity this system is interested in.
     *
     * @param entityId
     *			the entity to process
     */
    protected abstract void process(int entityId);

    /** @inheritDoc */
    @Override
    protected void processSystem() {
        IntBag actives = subscription.getEntities();
        final int[] ids = actives.getData();

        int size = actives.size();
        int iterationAmout = size / CPU_THREADS;

//		System.out.println("ITERATE THREADED = " + size);

        for (int k = 0; k < CPU_THREADS; k++) {
            final int indexFrom = k * iterationAmout;
            final int indexTo;
            if ((k + 1) >= CPU_THREADS) {
                indexTo = size;
            } else {
                indexTo = (k + 1) * iterationAmout;
            }
            Future<Boolean> process = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    for (int i = indexFrom; i < indexTo; i++) {
                        ThreadedIteratingSystem.this.process(ids[i]);
                    }
                    return true;
                }
            });
            this.runningTasks[k] = process;
        }
        for(int i = 0; i < this.runningTasks.length; i++) {
            try {
                this.runningTasks[i].get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
