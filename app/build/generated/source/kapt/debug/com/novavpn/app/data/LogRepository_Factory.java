package com.novavpn.app.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class LogRepository_Factory implements Factory<LogRepository> {
  @Override
  public LogRepository get() {
    return newInstance();
  }

  public static LogRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LogRepository newInstance() {
    return new LogRepository();
  }

  private static final class InstanceHolder {
    private static final LogRepository_Factory INSTANCE = new LogRepository_Factory();
  }
}
