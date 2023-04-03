Releasing
=========

 1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 2. Update the `CHANGELOG.md` for the impending release
 3. Update the `README.md` with the new version.
 4. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 5. `./gradlew clean publishRelease`
 6. `git push --tags`
 7. `git checkout -b release/bump-version`
 8. Update the `gradle.properties` to the next SNAPSHOT version.
 9. `git commit -am "Prepare next development version"`
 10. `git push && git push --tags`
 11. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.
 12. Merge `release/bump-version` Pull-Request
