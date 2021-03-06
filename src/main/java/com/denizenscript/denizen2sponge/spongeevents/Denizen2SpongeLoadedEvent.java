package com.denizenscript.denizen2sponge.spongeevents;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class Denizen2SpongeLoadedEvent extends AbstractEvent {

    private Cause cause;

    public Denizen2SpongeLoadedEvent(Cause cause) {
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
