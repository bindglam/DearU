package com.bindglam.dearu;

import org.jetbrains.annotations.ApiStatus;

public final class DearUProvider {
    private static DearU instance;

    private DearUProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Gets the instance of the WeirdMailbox API.
     *
     * @return the instance
     */
    public static DearU get() {
        return instance;
    }

    /**
     * Sets the instance of the WeirdMailbox API.
     * This method is intended for internal use only.
     *
     * @param instance the instance to set
     */
    @ApiStatus.Internal
    static void register(DearU instance) {
        DearUProvider.instance = instance;
    }

    /**
     * Unregisters the current instance of the WeirdMailbox API.
     * This method is intended for internal use only.
     */
    @ApiStatus.Internal
    static void unregister() {
        DearUProvider.instance = null;
    }
}
