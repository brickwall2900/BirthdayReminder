package com.github.brickwall2900.birthdays.systray;

import com.github.brickwall2900.birthdays.systray.dorkbox.DorkboxTrayIconProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class SystemTrayProvider {
    private static TrayIconProvider trayIconProvider;

    public static TrayIconProvider get() {
        if (trayIconProvider == null) {
            create();
        }

        return trayIconProvider;
    }

    private static void create() {
        // i'm not going to use ServiceProviders for this one
        // unless there's a third provider or extensibility is required
        // we'll be using a simple class based lookup system
        String trayIconClass = System.getProperty("presence.trayIconProvider", DorkboxTrayIconProvider.class.getName());
        try {
            Class<?> potentialTrayIconManagerClass = Class.forName(trayIconClass);
            if (!TrayIconProvider.class.isAssignableFrom(potentialTrayIconManagerClass)) {
                throw new ClassCastException(potentialTrayIconManagerClass + " does not implement TrayIconProvider");
            }

            Constructor<?> constructor = potentialTrayIconManagerClass.getConstructor();
            trayIconProvider = (TrayIconProvider) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Invalid class " + trayIconClass, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No-arg constructor cannot be located", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
