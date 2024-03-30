package net.lerariemann.infinity.access;

import net.lerariemann.infinity.client.InfinityOptions;

public interface MinecraftClientAccess {
    InfinityOptions getInfinityOptions();
    void setInfinityOptions(InfinityOptions options);
}
