# version-prerelease-milestone-rc

Wave 2, probe #9 of the sbt versioning coverage plan (`docs/SBT_VERSIONING_PLAN.md`). Final probe of Wave 2.

## Feature exercised

Prerelease version tags (`-M*`, `-RC*`, `-beta*`) are reported by Mend verbatim — the full version string including the prerelease qualifier is preserved in the reported artifact version. This is the critical assertion: Mend must NOT normalize, truncate, or drop the qualifier.

## Dependencies

| Declared | Artifact (Mend artifactId) | Version | Prerelease tag | Direct | Notes |
|---|---|---|---|---|---|
| `"com.typesafe.akka" %% "akka-actor" % "2.7.0-M1"` | `akka-actor_2.13` | `2.7.0-M1` | `-M1` milestone | yes | Scala `%%` — `_2.13` suffix; 2.7.0-M1 is a permanent coordinate (final series is 2.8.x) |
| `"org.typelevel" %% "cats-effect" % "3.5.0-RC1"` | `cats-effect_2.13` | `3.5.0-RC1` | `-RC1` release candidate | yes | Scala `%%` — `_2.13` suffix; permanent coordinate (3.5.x final exists) |
| `"org.apache.logging.log4j" % "log4j-api" % "2.0-beta9"` | `log4j-api` | `2.0-beta9` | `-beta9` beta | yes | Java `%` — no suffix; permanent coordinate on Maven Central |

### Direct transitives of akka-actor_2.13:2.7.0-M1

| Artifact (Mend artifactId) | Version | Direct | Notes |
|---|---|---|---|
| `config` | `1.4.2` | no | Java artifact, no Scala suffix |
| `scala-java8-compat_2.13` | `1.0.0` | no | Scala `%%` artifact |

### Direct transitives of cats-effect_2.13:3.5.0-RC1

| Artifact (Mend artifactId) | Version | Direct | Notes |
|---|---|---|---|
| `cats-effect-kernel_2.13` | `3.5.0-RC1` | no | Prerelease qualifier propagates to transitives |
| `cats-effect-std_2.13` | `3.5.0-RC1` | no | Prerelease qualifier propagates to transitives |
| `cats-core_2.13` | `2.9.0` | no | Transitive via cats-effect-kernel |
| `cats-kernel_2.13` | `2.9.0` | no | Transitive via cats-core |

`log4j-api:2.0-beta9` has no compile-scope transitives.

Total: 9 packages. Direct: 3. Transitive: 6.

## Expected dependency tree

```
com.typesafe.akka:akka-actor_2.13:2.7.0-M1         (Compile, direct, registry, main)
  com.typesafe:config:1.4.2                          (Compile, transitive, registry, main)
  org.scala-lang.modules:scala-java8-compat_2.13:1.0.0  (Compile, transitive, registry, main)

org.typelevel:cats-effect_2.13:3.5.0-RC1            (Compile, direct, registry, main)
  org.typelevel:cats-effect-kernel_2.13:3.5.0-RC1   (Compile, transitive, registry, main)
    org.typelevel:cats-core_2.13:2.9.0               (Compile, transitive, registry, main)
      org.typelevel:cats-kernel_2.13:2.9.0           (Compile, transitive, registry, main)
  org.typelevel:cats-effect-std_2.13:3.5.0-RC1      (Compile, transitive, registry, main)

org.apache.logging.log4j:log4j-api:2.0-beta9        (Compile, direct, registry, main)
```

The prerelease qualifier in each version is the critical assertion. `2.7.0-M1` must not appear as `2.7.0`. `3.5.0-RC1` must not appear as `3.5.0`. `2.0-beta9` must not appear as `2.0`.

## Why prereleases matter for vulnerability matching

A CVE assigned against `1.0.0-RC1` is NOT the same vulnerability surface as `1.0.0` final. Maven's version ordering treats prerelease qualifiers as distinct coordinates — `1.0.0-RC1` < `1.0.0` in the ordering, but they share no binary compatibility guarantee. Similarly, a CVE on `2.0-beta9` of log4j may describe a vulnerability that was fixed before `2.0` GA. If Mend normalizes `2.0-beta9` to `2.0`, it will either (a) fail to match the prerelease-specific CVE, or (b) incorrectly apply a `2.0`-final CVE to the beta artifact.

Both failure modes produce wrong vulnerability reports. This probe detects them by asserting that the version string is reported verbatim, including the qualifier.

## Mend config

- `.whitesource` pins `sbt: 1.9.8`, `scala: 2.13.12`, `java: 17` via `scanSettings.versioning`. sbt has no dynamic version detection (mend-knowledge `whitesource-config.md` line 148), so the pin is required to keep the probe deterministic across scans. Without this pin, Mend's install-tool may provision a different sbt/Scala/Java combination, which can change the Coursier-resolved tree.
- No `whitesource.config` (UA) is needed. Coursier-driven detection from `build.sbt` is sufficient; `runPreStep` is not required for this probe. The `.whitesource` file is the only Mend configuration needed.
- The `sbt` and `scala` values in `.whitesource` match `project/build.properties` (`sbt.version=1.9.8`) and `build.sbt` (`scalaVersion := "2.13.12"`) exactly.

## Failure modes exercised

- Mend normalizes `-M1` away and reports `akka-actor_2.13:2.7.0` instead of `2.7.0-M1`.
- Mend drops the `-RC1` suffix and reports `cats-effect_2.13:3.5.0` instead of `3.5.0-RC1`.
- Mend normalizes `-beta9` to `2.0` or `2.0.0` for `log4j-api`.
- Mend treats a prerelease coordinate as a distinct artifact (wrong artifactId or groupId) rather than the same artifact at a prerelease version.
- Prerelease qualifier on transitives (`cats-effect-kernel_2.13:3.5.0-RC1`, `cats-effect-std_2.13:3.5.0-RC1`) is stripped — transitives reported at normalized version even when the direct dep's prerelease was correctly preserved.
- `scala-java8-compat_2.13` transitive of `akka-actor` is missing — Mend did not traverse the milestone artifact's dependency edges.
- `group` classified as `test` instead of `main` (Compile scope misclassified).

## Probe metadata

```json
{
  "probe_name": "sbt-version-prerelease-milestone-rc-probe",
  "pattern": "version-prerelease-milestone-rc",
  "wave": 2,
  "probe_number": 9,
  "pm": "sbt",
  "generated": "2026-05-05",
  "target": "local",
  "sbt_version": "1.9.8",
  "scala_version": "2.13.12",
  "java_version": "17"
}
```