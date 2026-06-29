// Intentionally empty: each module declares its own plugin versions via the
// version catalog (gradle/libs.versions.toml). Keeping the root build script
// free of plugin application means `gradle :domain:test --configure-on-demand`
// can configure only the pure-Kotlin :domain module without ever touching the
// Android Gradle Plugin (and its dependency on Google's Maven repo) — useful in
// sandboxes where that host is blocked.
