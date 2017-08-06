dependsOn(
  ProjectRef(file("RAII.scala"), "AsynchronousSemaphoreJVM"),
  ProjectRef(file("Compute.scala"), "OpenCLCodeGenerator"),
  ProjectRef(file("Compute.scala"), "OpenCL")
)

libraryDependencies += "com.thoughtworks.raii" %% "asynchronous" % "2.0.0"

libraryDependencies += "eu.timepit" %% "refined" % "0.8.2"

libraryDependencies += "com.thoughtworks.each" %% "each" % "3.3.1"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)

libraryDependencies += "com.dongxiguo" %% "fastring" % "0.3.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % Test

libraryDependencies += "org.lwjgl" % "lwjgl" % "3.1.2"

libraryDependencies += "org.lwjgl" % "lwjgl" % "3.1.2" classifier {
  if (util.Properties.isMac) {
    "natives-macos"
  } else if (util.Properties.osName.startsWith("Linux")) {
    "natives-linux"
  } else if (util.Properties.isWin) {
    "natives-windows"
  } else {
    throw new MessageOnlyException(s"lwjgl does not support ${util.Properties.osName}")
  }
}
