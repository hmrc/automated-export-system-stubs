import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "automated-export-system-stubs"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.8"
ThisBuild / scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s", // Silence all warnings in generated routes
  "-Xlint:all",
  "-Werror"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    scalafmtOnCompile := true,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort := 5002,
    CodeCoverageSettings.settings
  )
  .settings(scalacOptions ~= (options => options.distinct))
  .settings(
    addCommandAlias("runTestOnly", "run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes")
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    scalafmtOnCompile := true,
    Test / unmanagedSourceDirectories ++= Seq(baseDirectory.value / "it"),
    DefaultBuildSettings.itSettings()
  )
  .settings(scalacOptions ~= (options => options.distinct))