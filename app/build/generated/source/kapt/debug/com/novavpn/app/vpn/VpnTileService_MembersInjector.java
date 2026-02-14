package com.novavpn.app.vpn;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class VpnTileService_MembersInjector implements MembersInjector<VpnTileService> {
  private final Provider<WireGuardManager> wireGuardManagerProvider;

  public VpnTileService_MembersInjector(Provider<WireGuardManager> wireGuardManagerProvider) {
    this.wireGuardManagerProvider = wireGuardManagerProvider;
  }

  public static MembersInjector<VpnTileService> create(
      Provider<WireGuardManager> wireGuardManagerProvider) {
    return new VpnTileService_MembersInjector(wireGuardManagerProvider);
  }

  @Override
  public void injectMembers(VpnTileService instance) {
    injectWireGuardManager(instance, wireGuardManagerProvider.get());
  }

  @InjectedFieldSignature("com.novavpn.app.vpn.VpnTileService.wireGuardManager")
  public static void injectWireGuardManager(VpnTileService instance,
      WireGuardManager wireGuardManager) {
    instance.wireGuardManager = wireGuardManager;
  }
}
