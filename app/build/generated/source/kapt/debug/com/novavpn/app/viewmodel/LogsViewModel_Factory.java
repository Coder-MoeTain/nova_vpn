package com.novavpn.app.viewmodel;

import com.novavpn.app.data.LogRepository;
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
public final class LogsViewModel_Factory implements Factory<LogsViewModel> {
  private final Provider<LogRepository> logRepositoryProvider;

  public LogsViewModel_Factory(Provider<LogRepository> logRepositoryProvider) {
    this.logRepositoryProvider = logRepositoryProvider;
  }

  @Override
  public LogsViewModel get() {
    return newInstance(logRepositoryProvider.get());
  }

  public static LogsViewModel_Factory create(Provider<LogRepository> logRepositoryProvider) {
    return new LogsViewModel_Factory(logRepositoryProvider);
  }

  public static LogsViewModel newInstance(LogRepository logRepository) {
    return new LogsViewModel(logRepository);
  }
}
