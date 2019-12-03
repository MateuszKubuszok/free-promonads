import sbt._
import Settings._

lazy val root = project.root
  .setName("free-promonads")
  .setDescription("Build of free promonads")
  .configureRoot
  .aggregate(freePromonads)

lazy val freePromonads = project.from("free-promonads")
  .setName("free-promonads")
  .setDescription("Free Promonads")
  .setInitialImport()
  .configureModule
  .configureTests()

addCommandAlias("fullTest", ";test;scalastyle")
addCommandAlias("fullCoverageTest", ";coverage;test;coverageReport;coverageAggregate;scalastyle")
