name := "puml-comment-action"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" % Test,
  "net.sourceforge.plantuml" % "plantuml" % "8059",
  "org.kohsuke" % "github-api" % "1.117",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.10.0.202012080955-r",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)

organization := "ryota0624"
dockerBaseImage := "openjdk:11-jdk"

enablePlugins(JavaAppPackaging, DockerPlugin)

assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// TODO: AOTコンパイルしたjarにする
//  Github Actionで実際に試す
// assemblyするmainオブジェクト指定

// 環境変数 GITHUB_OAUTH
// cli GITHUB_REPOSITORY github.base_ref github.head_ref

//docker run --workdir /github/workspace --rm -e FROM=origin/main -e TO=origin/test/pr -v "/home/runner/work/puml-comment-action/puml-comment-action":"/github/workspace" cf44e53f3cc4 /github/workspace
