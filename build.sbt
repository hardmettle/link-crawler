scalaVersion := "2.13.18"

val CatsEffect = "3.6.3"
val Fs2        = "3.12.2"
val Http4s     = "0.23.32"

lazy val root  = (project in file(".")).settings(
  name := "link-crawler",
  libraryDependencies ++= Seq(
    "org.typelevel"         %% "cats-effect"         % CatsEffect,
    "co.fs2"                %% "fs2-core"            % Fs2,
    "org.http4s"            %% "http4s-ember-client" % Http4s,
    "org.http4s"            %% "http4s-dsl"          % Http4s,
    "org.jsoup"              % "jsoup"               % "1.21.1",
    "com.github.pureconfig" %% "pureconfig"          % "0.17.9",
    "org.typelevel"         %% "log4cats-slf4j"      % "2.7.1",
    "ch.qos.logback"         % "logback-classic"     % "1.5.18",
    "org.typelevel"         %% "log4cats-slf4j"      % "2.7.1",
    "ch.qos.logback"         % "logback-classic"     % "1.5.18",
    "org.scalameta"         %% "munit"               % "1.2.1" % Test,
    "org.typelevel"         %% "munit-cats-effect"   % "2.1.0" % Test
  )
)
addCommandAlias("fmt", "scalafmt")
addCommandAlias("fmtCheck", "scalafmtCheck")
