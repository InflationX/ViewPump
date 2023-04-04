Releasing
=========

 1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 2. Update the `CHANGELOG.md` for the impending release
 3. Update the `README.md` with the new version.
 4. `git checkout -b release/X.Y.0`
 6. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 7. `./gradlew clean publishRelease`
 8. `git push --tags`
 10. Update the `gradle.properties` to the next SNAPSHOT version.
 11. `git commit -am "Prepare next development version"`
 12. `git push && git push --tags`
 13. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.
 14. Merge `release/X.Y.0` Pull-Request
