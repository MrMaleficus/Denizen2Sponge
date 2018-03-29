package com.denizenscript.denizen2sponge.events.entity;

import com.denizenscript.denizen2core.events.ScriptEvent;
import com.denizenscript.denizen2core.tags.AbstractTagObject;
import com.denizenscript.denizen2core.tags.objects.NumberTag;
import com.denizenscript.denizen2sponge.Denizen2Sponge;
import com.denizenscript.denizen2sponge.events.D2SpongeEventHelper;
import com.denizenscript.denizen2sponge.tags.objects.*;
import com.denizenscript.denizen2sponge.utilities.Utilities;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.health.HealthFunction;
import org.spongepowered.api.event.entity.HealEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;

public class EntityHealedScriptEvent extends ScriptEvent {

    // <--[event]
    // @Since 0.4.0
    // @Events
    // entity damaged
    //
    // @Updated 2018/03/28
    //
    // @Group Entity
    //
    // @Cancellable true
    //
    // @Triggers when an entity is healed.
    //
    // @Switch type (EntityTypeTag) checks the entity type.
    // @Switch world (WorldTag) checks the world.
    // @Switch cuboid (CuboidTag) checks the cuboid area.
    // @Switch weather (TextTag) checks the weather.
    //
    // @Context
    // entity (EntityTag) returns the entity that was damaged.
    // heal_amount (NumberTag) returns the (final) amount of health healed.
    //
    // @Determinations
    // heal_amount (NumberTag) to set the (final) amount of health healed.
    // -->

    @Override
    public String getName() {
        return "EntityHealed";
    }

    @Override
    public boolean couldMatch(ScriptEventData data) {
        return data.eventPath.startsWith("entity healed");
    }

    @Override
    public boolean matches(ScriptEventData data) {
        Entity ent = entity.getInternal();
        Location<World> loc = ent.getLocation();
        World world = loc.getExtent();
        return D2SpongeEventHelper.checkEntityType(ent.getType(), data, this::error)
                && D2SpongeEventHelper.checkWorld(world, data, this::error)
                && D2SpongeEventHelper.checkCuboid((new LocationTag(loc)).getInternal(), data, this::error)
                && D2SpongeEventHelper.checkWeather(Utilities.getIdWithoutDefaultPrefix(world.getWeather().getId()), data, this::error);
    }

    public EntityTag entity;

    public NumberTag healAmount;

    public HealEntityEvent internal;

    @Override
    public HashMap<String, AbstractTagObject> getDefinitions(ScriptEventData data) {
        HashMap<String, AbstractTagObject> defs = super.getDefinitions(data);
        defs.put("entity", entity);
        defs.put("heal_amount", healAmount);
        return defs;
    }

    @Override
    public void enable() {
        Sponge.getEventManager().registerListeners(Denizen2Sponge.instance, this);
    }

    @Override
    public void disable() {
        Sponge.getEventManager().unregisterListeners(this);
    }

    @Listener
    public void onEntityHealed(HealEntityEvent evt) {
        EntityHealedScriptEvent event = (EntityHealedScriptEvent) clone();
        event.internal = evt;
        event.entity = new EntityTag(evt.getTargetEntity());
        event.healAmount = new NumberTag(evt.getOriginalHealAmount());
        event.cancelled = evt.isCancelled();
        // TODO: Cause viewing
        event.run();
        evt.setCancelled(event.cancelled);
    }

    @Override
    public void applyDetermination(boolean errors, String determination, AbstractTagObject value) {
        if (determination.equals("heal_amount")) {
            NumberTag nt = NumberTag.getFor(this::error, value);
            healAmount = nt;
            internal.setBaseHealAmount(nt.getInternal());
            for (HealthFunction tuple : internal.getModifiers()) {
                internal.setHealAmount(tuple.getModifier(), (x) -> 0.0);
            }
        }
        else {
            super.applyDetermination(errors, determination, value);
        }
    }
}
