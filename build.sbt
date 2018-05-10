dependsOn(ProjectRef(file("DeepLearning.scala"), "plugins-Builtins"))

libraryDependencies += ("org.lwjgl" % "lwjgl" % "3.1.6" % Optional).jar().classifier {
  import scala.util.Properties._
  if (isMac) {
    "natives-macos"
  } else if (isLinux) {
    "natives-linux"
  } else if (isWin) {
    "natives-windows"
  } else {
    throw new MessageOnlyException(s"lwjgl does not support $osName")
  }
}

addCompilerPlugin("com.thoughtworks.dsl" %% "compilerplugins-bangnotation" % "1.0.0-RC9")

addCompilerPlugin("com.thoughtworks.dsl" %% "compilerplugins-reseteverywhere" % "1.0.0-RC9")

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-monadic" % "1.0.0-RC9"

libraryDependencies += "com.thoughtworks.dsl" %% "domains-scalaz" % "1.0.0-RC9"

libraryDependencies += "org.rauschig" % "jarchivelib" % "0.5.0"
