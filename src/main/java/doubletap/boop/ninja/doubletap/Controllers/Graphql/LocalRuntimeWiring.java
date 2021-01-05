package doubletap.boop.ninja.doubletap.Controllers.Graphql;

import doubletap.boop.ninja.doubletap.Authorizors.Base.Policy;
import doubletap.boop.ninja.doubletap.Authorizors.Base.Role;
import doubletap.boop.ninja.doubletap.Mutations.WhitelistMutations;
import doubletap.boop.ninja.doubletap.Queries.ServerFetcher;
import doubletap.boop.ninja.doubletap.Queries.WorldFetcher;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.idl.TypeRuntimeWiring;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.bukkit.Bukkit.getPlayer;
import static org.bukkit.Bukkit.getServer;

public class LocalRuntimeWiring {
    public graphql.schema.idl.RuntimeWiring Build(@NotNull Policy[] policies) {
        Role configuredRole = new Role();
        configuredRole.policies = policies;
        CustomFieldVisibility cfv = new CustomFieldVisibility(configuredRole);
        return graphql.schema.idl.RuntimeWiring.newRuntimeWiring()
                .fieldVisibility(cfv)
                .type("QueryType", this::queryType)
                .type("MutationType", this::mutationType)
                .type("Server", this::serverType)
                .type("Player", this::playerType)
                .type("OfflinePlayer", this::offlinePlayer)
                .type("Location", this::locationType)
                .type("WhitelistMutation", this::whitelistMutation)
                .type("WhitelistQuery", this::whitelistQuery)
                .build();
    }

    private TypeRuntimeWiring.Builder queryType(TypeRuntimeWiring.Builder builder) {
        return builder
                .dataFetcher("server", environment -> getServer())
                .dataFetcher("world", WorldFetcher::world)
                .dataFetcher("player", environment -> {
                    String playerName = environment.getArgument("name");
                    assert playerName != null;
                    return getPlayer(playerName);
                })
                .dataFetcher("whitelist",  environment -> environment);
    }

    private TypeRuntimeWiring.Builder mutationType(TypeRuntimeWiring.Builder builder) {
        return builder
            .dataFetcher("whitelist", environment -> environment);
    }


    private TypeRuntimeWiring.Builder locationType(TypeRuntimeWiring.Builder builder) {
        return builder
                .dataFetcher("x", PropertyDataFetcher.fetching("blockX"))
                .dataFetcher("y", PropertyDataFetcher.fetching("blockY"))
                .dataFetcher("z", PropertyDataFetcher.fetching("blockZ"));
    }

    private TypeRuntimeWiring.Builder serverType(TypeRuntimeWiring.Builder builder) {
        return builder
                .dataFetcher("world", WorldFetcher::world)
                .dataFetcher("ip", environment -> getServer().getIp())
                .dataFetcher("whitelistedPlayers", ServerFetcher::whiteListedPlayers);
    }

    private TypeRuntimeWiring.Builder playerBaseType(TypeRuntimeWiring.Builder builder) {
        return builder
                .dataFetcher("isWhitelisted", PropertyDataFetcher.fetching(OfflinePlayer::isWhitelisted))
                .dataFetcher("isOnline", PropertyDataFetcher.fetching(OfflinePlayer::isOnline))
                .dataFetcher("isBanned", PropertyDataFetcher.fetching(OfflinePlayer::isBanned));
    }

    private TypeRuntimeWiring.Builder playerType(TypeRuntimeWiring.Builder builder) {
        return playerBaseType(builder)
                .dataFetcher("name", PropertyDataFetcher.fetching("playerListName"));
    }

    private TypeRuntimeWiring.Builder offlinePlayer(TypeRuntimeWiring.Builder builder) {
        return playerBaseType(builder);
    }

    private TypeRuntimeWiring.Builder whitelistQuery(TypeRuntimeWiring.Builder builder) {
        return builder
                .dataFetcher("players", ServerFetcher::whiteListedPlayers);
    }

    private TypeRuntimeWiring.Builder whitelistMutation(TypeRuntimeWiring.Builder builder) {
        return builder
                .dataFetcher("add", WhitelistMutations::add)
                .dataFetcher("remove", WhitelistMutations::remove);
    }
}