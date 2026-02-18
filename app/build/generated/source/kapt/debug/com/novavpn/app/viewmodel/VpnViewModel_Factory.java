package com.novavpn.app.viewmodel;

import android.content.Context;
import com.novavpn.app.api.ProvisioningApi;
import com.novavpn.app.security.SecureStorage;
import com.novavpn.app.vpn.NovaTunnel;
import com.wireguard.android.backend.Backend;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
  private final Provider<Context> contextProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  private final Provider<ProvisioningApi> provisioningApiProvider;

  private final Provider<Backend> backendProvider;

  private final Provider<NovaTunnel> tunnelProvider;

  public VpnViewModel_Factory(Provider<Context> contextProvider,
      Provider<SecureStorage> secureStorageProvider,
      Provider<ProvisioningApi> provisioningApiProvider, Provider<Backend> backendProvider,
      Provider<NovaTunnel> tunnelProvider) {
    this.contextProvider = contextProvider;
    this.secureStorageProvider = secureStorageProvider;
    this.provisioningApiProvider = provisioningApiProvider;
    this.backendProvider = backendProvider;
    this.tunnelProvider = tunnelProvider;
  }

  @Override
  public VpnViewModel get() {
    return newInstance(contextProvider.get(), secureStorageProvider.get(), provisioningApiProvider.get(), backendProvider.get(), tunnelProvider.get());
  }

  public static VpnViewModel_Factory create(Provider<Context> contextProvider,
      Provider<SecureStorage> secureStorageProvider,
      Provider<ProvisioningApi> provisioningApiProvider, Provider<Backend> backendProvider,
      Provider<NovaTunnel> tunnelProvider) {
    return new VpnViewModel_Factory(contextProvider, secureStorageProvider, provisioningApiProvider, backendProvider, tunnelProvider);
  }

  public static VpnViewModel newInstance(Context context, SecureStorage secureStorage,
      ProvisioningApi provisioningApi, Backend backend, NovaTunnel tunnel) {
    return new VpnViewModel(context, secureStorage, provisioningApi, backend, tunnel);
  }
}
