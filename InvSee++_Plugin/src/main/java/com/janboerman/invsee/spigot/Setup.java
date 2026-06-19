package com.janboerman.invsee.spigot;

import com.janboerman.invsee.spigot.api.OfflinePlayerProvider;
import com.janboerman.invsee.spigot.internal.InvseePlatform;
import com.janboerman.invsee.spigot.internal.version.*;
import com.janboerman.invsee.spigot.internal.NamesAndUUIDs;
import com.janboerman.invsee.spigot.internal.OpenSpectatorsCache;
import com.janboerman.invsee.spigot.api.Scheduler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public interface Setup {

    public InvseePlatform platform();

    public default OfflinePlayerProvider offlinePlayerProvider() {
        return OfflinePlayerProvider.Dummy.INSTANCE;
    }

    public static Setup setup(Plugin plugin, Scheduler scheduler, NamesAndUUIDs lookup, OpenSpectatorsCache cache) {
        Server server = plugin.getServer();
        ServerSoftware serverSoftware = ServerSoftware.detect(server);
        plugin.getLogger().info("Detected server software: " + serverSoftware);

        if (serverSoftware == null)
            throw new RuntimeException(SupportedServerSoftware.getUnsupportedPlatformMessage(server));

        SetupProvider provider = SetupImpl.SUPPORTED.getImplementationProvider(serverSoftware);

        if (provider == null) {
            String supportedVersionsMessage = SetupImpl.SUPPORTED.getUnsupportedVersionMessage(serverSoftware.getPlatform(), server);
            String legacyVersionsMessage = LegacyVersions.getLegacyVersionMessage(serverSoftware.getVersion());

            if (legacyVersionsMessage != null) {
                plugin.getLogger().severe(legacyVersionsMessage);
            }

            throw new RuntimeException(supportedVersionsMessage);
        }

        return provider.provide(plugin, lookup, scheduler, cache);
    }

}

//we use separate classes per implementation, to prevent classloading of an incorrect version.
//previously, the Setup#setup(Plugin) method tried to load all implementation classes, even before any of them was needed.

class Impl_1_21_11 extends SetupImpl {
    Impl_1_21_11(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_1_21_11_R7.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_1_21_11_R7.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_Paper_1_21_11 extends SetupImpl {
    Impl_Paper_1_21_11(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.paper.impl_1_21_11.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.paper.impl_1_21_11.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_26_1_1 extends SetupImpl {
    Impl_26_1_1(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_26_1_1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_26_1_1.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_Paper_26_1_1 extends SetupImpl {
    Impl_Paper_26_1_1(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.paper.impl_26_1_1.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.paper.impl_26_1_1.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_26_2 extends SetupImpl {
    Impl_26_2(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.spigot.impl_26_2.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.spigot.impl_26_2.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_Paper_26_2 extends SetupImpl {
    Impl_Paper_26_2(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.paper.impl_26_2.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.paper.impl_26_2.KnownPlayersProvider(plugin, scheduler));
    }
}

class Impl_Glowstone extends SetupImpl {
    Impl_Glowstone(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache) {
        super(new com.janboerman.invsee.glowstone.InvseeImpl(plugin, lookup, scheduler, cache), new com.janboerman.invsee.glowstone.KnownPlayersProvider(plugin, scheduler));
    }
}

//

class SetupImpl implements Setup {

    static SupportedServerSoftware<SetupProvider> SUPPORTED = new SupportedServerSoftware<>();
    static {
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_1_21_11(p, l, s, c), ServerSoftware.CRAFTBUKKIT_1_21_11);
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_Paper_1_21_11(p, l, s, c), ServerSoftware.PAPER_1_21_11);
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_26_1_1(p, l, s, c), ServerSoftware.CRAFTBUKKIT_26_1_1, ServerSoftware.CRAFTBUKKIT_26_1_2, ServerSoftware.CRAFTBUKKIT_26_1);
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_Paper_26_1_1(p, l, s, c), ServerSoftware.PAPER_26_1_1, ServerSoftware.PAPER_26_1_2);
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_Paper_26_2(p, l, s, c), new ServerSoftware(MinecraftPlatform.PAPER, "26.2 Release Candidate 2"), ServerSoftware.PAPER_26_2);
        SUPPORTED.registerSupportedVersion((p, l, s, c) -> new Impl_26_2(p, l, s, c), ServerSoftware.CRAFTBUKKIT_26_2);

        final SetupProvider glowstoneProver = (p, l, s, c) -> new Impl_Glowstone(p, l, s, c);
        final MinecraftVersion[] minecraftVersions = MinecraftVersion.values();
        for (int idx = MinecraftVersion._1_21_11.ordinal(); idx < MinecraftVersion._26_1.ordinal(); idx ++) {
            SUPPORTED.registerSupportedVersion(new ServerSoftware(MinecraftPlatform.GLOWSTONE, minecraftVersions[idx]), glowstoneProver);
        }
    }

    private final InvseePlatform platform;
    private final OfflinePlayerProvider offlinePlayerProvider;

    SetupImpl(InvseePlatform platform, OfflinePlayerProvider offlinePlayerProvider) {
        this.platform = platform;
        this.offlinePlayerProvider = offlinePlayerProvider;
    }

    @Override
    public InvseePlatform platform() {
        return platform;
    }

    @Override
    public OfflinePlayerProvider offlinePlayerProvider() {
        return offlinePlayerProvider;
    }
}

interface SetupProvider {
    public Setup provide(Plugin plugin, NamesAndUUIDs lookup, Scheduler scheduler, OpenSpectatorsCache cache);
}
