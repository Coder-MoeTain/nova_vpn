package com.novavpn.app.viewmodel;

import com.novavpn.app.vpn.WireGuardManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class VpnViewModel_Factory implements Factory<VpnViewModel> {
  private final Provider<WireGuardManager> wireGuardManagerProvider;

  public VpnViewModel_Factory(Provider<WireGuardManager> wireGuardManagerProvider) {
    this.wireGuardManagerProvider = wireGuardManagerProvider;
  }

  @Override
  public VpnViewModel get() {
    return newInstance(wireGuardManagerProvider.get());
  }

  public static VpnViewModel_Factory create(Provider<WireGuardManager> wireGuardManagerProvider) {
    return new VpnViewModel_Factory(wireGuardManagerProvider);
  }

  public static VpnViewModel newInstance(WireGuardManager wireGuardManager) {
    return new VpnViewModel(wireGuardManager);
  }
}
