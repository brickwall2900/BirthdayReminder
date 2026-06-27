package com.github.brickwall2900.birthdays;

import java.util.*;

// is this approach better than the old system we had?

/// The `BundleMultiplexer` allows you to combine two {@link ResourceBundle} and treat it as one!
public class BundleMultiplexer extends ResourceBundle {
    private final ResourceBundle bundleA;
    private final ResourceBundle bundleB;

    public BundleMultiplexer(ResourceBundle bundleA, ResourceBundle bundleB) {
        this.bundleA = bundleA;
        this.bundleB = bundleB;
    }

    @Override
    protected Object handleGetObject(String key) {
        if (bundleA.containsKey(key)) {
            return bundleA.getObject(key);
        }
        return bundleB.containsKey(key)
                ? bundleB.getObject(key)
                : null;
    }

    private Set<String> keys = null;

    @Override
    public Enumeration<String> getKeys() {
        // haha ancient java apis go brrrr
        if (keys == null) {
            keys = new HashSet<>();
            bundleA.getKeys().asIterator().forEachRemaining(keys::add);
            bundleB.getKeys().asIterator().forEachRemaining(keys::add);
        }
        return Collections.enumeration(keys);
    }
}
