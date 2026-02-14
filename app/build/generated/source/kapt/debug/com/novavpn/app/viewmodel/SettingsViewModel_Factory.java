package com.novavpn.app.viewmodel;

import com.novavpn.app.security.SecureStorage;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SecureStorage> secureStorageProvider;

  private final Provider<WireGuardManager> wireGuardManagerProvider;

  public SettingsViewModel_Factory(Provider<SecureStorage> secureStorageProvider,
      Provider<WireGuardManager> wireGuardManagerProvider) {
    this.secureStorageProvider = secureStorageProvider;
    this.wireGuardManagerProvider = wireGuardManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(secureStorageProvider.get(), wireGuardManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<SecureStorage> secureStorageProvider,
      Provider<WireGuardManager> wireGuardManagerProvider) {
    return new SettingsViewModel_Factory(secureStorageProvider, wireGuardManagerProvider);
  }

  public static SettingsViewModel newInstance(SecureStorage secureStorage,
      WireGuardManager wireGuardManager) {
    return new SettingsViewModel(secureStorage, wireGuardManager);
  }
}
