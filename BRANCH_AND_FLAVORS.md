# Branch and App Variants

## Git branch

- **`main`** – Your original branch. All current data and the single-app setup remain here. Switch back anytime: `git checkout main`
- **`feature/new-app`** – This branch adds a second app variant (product flavor) so you can build two apps from the same codebase.

## Product flavors (on branch `feature/new-app`)

| Flavor    | Application ID        | App name     | Use case                    |
|----------|------------------------|-------------|-----------------------------|
| **nova** | `com.novavpn.app`      | NovaVPN     | Original app (unchanged)    |
| **novaNew** | `com.novavpn.app.new` | NovaVPN New | New app (install alongside) |

Both can be installed on the same device at the same time. Each has its own data (DataStore, VPN config, etc.) because they use different application IDs.

## Build commands

```bash
# Original app (current behavior)
./gradlew assembleNovaDebug
./gradlew assembleNovaRelease

# New app variant
./gradlew assembleNovaNewDebug
./gradlew assembleNovaNewRelease
```

In Android Studio: use **Build → Select Build Variant** and choose `novaDebug` / `novaNewDebug` (or release).

## Switching back to single-app (main)

```bash
git checkout main
```

On `main` there are no product flavors; only the original `com.novavpn.app` app is built.

## Summary

- **Current data/code:** Preserved on `main`. Nothing is removed.
- **New branch:** `feature/new-app` adds a second build variant.
- **Two apps:** Build `nova` for the original app, `novaNew` for the new one; install both side by side without losing data.
