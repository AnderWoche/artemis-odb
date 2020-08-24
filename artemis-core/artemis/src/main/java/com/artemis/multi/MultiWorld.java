package com.artemis.multi;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.utils.Bag;

import java.util.HashMap;

public class MultiWorld {

    private World currentWorld;
    private boolean isCurrentWorldNull = false;

    private final HashMap<Class<? extends BaseSystem>, BaseSystem> systemsMap = new HashMap<>();
    private final Bag<BaseSystem> systems = new Bag<>();

//    private final Bag<MultiComponentMapper<?>> multiComponentMappers = new Bag<>();
//
//    private final Bag<MultiEntitySubscription> multiEntitySubscriptions = new Bag<>();
//
//    private final Bag<Resizable> resizableSystemsArray = new Bag<>();


    public MultiWorld() {

    }

}
