package com.artemis;

import com.artemis.EntitySubscription.SubscriptionListener;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;

public class MultiEntitySubscription {

    private final Aspect.Builder aspectBuilder;

    private EntitySubscription entitySubscription;

    private final Bag<SubscriptionListener> subscriptionListenerArray = new Bag<>();

    public MultiEntitySubscription(Aspect.Builder aspectBuilder) {
        this.aspectBuilder = aspectBuilder;

    }

    public void changeWorld(World world) {
        if (this.entitySubscription != null) {
            this.removeListener();
        }

        this.entitySubscription = world.getAspectSubscriptionManager().get(this.aspectBuilder);
        this.addListener();

        //Notify insertetListener cause the world has changed.
        this.notifyInsertedEntity();
    }

    private void removeListener() {
        for (SubscriptionListener listener : this.subscriptionListenerArray) {
            this.entitySubscription.removeSubscriptionListener(listener);
        }
    }

    public void addListener() {
        for (SubscriptionListener listener : this.subscriptionListenerArray) {
            this.entitySubscription.addSubscriptionListener(listener);
        }
    }

    private void notifyInsertedEntity() {
        for (SubscriptionListener listener : this.subscriptionListenerArray) {
            listener.inserted(this.entitySubscription.getEntities());
        }
    }

    public IntBag getEntities() {
        return this.entitySubscription.getEntities();
    }

    public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
        this.subscriptionListenerArray.add(subscriptionListener);
    }

    public void removeSubscriptionListener(SubscriptionListener subscriptionListener) {
        this.subscriptionListenerArray.remove(subscriptionListener);
    }

}
