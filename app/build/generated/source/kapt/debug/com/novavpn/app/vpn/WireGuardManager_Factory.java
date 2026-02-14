package com.novavpn.app.vpn;

import android.content.Context;
import com.novavpn.app.security.SecureStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WireGuardManager_Factory implements Factory<WireGuardManager> {
  private final Provider<Context> contextProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public WireGuardManager_Factory(Provider<Context> contextProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.contextProvider = contextProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public WireGuardManager get() {
    return newInstance(contextProvider.get(), secureStorageProvider.get());
  }

  public static WireGuardManager_Factory create(Provider<Context> contextProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new WireGuardManager_Factory(contextProvider, secureStorageProvider);
  }

  public static WireGuardManager newInstance(Context context, SecureStorage secureStorage) {
    return new WireGuardManager(context, secureStorage);
  }
}
