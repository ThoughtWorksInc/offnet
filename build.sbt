dependsOn(
  ProjectRef(file("RAII.scala"), "AsynchronousSemaphoreJVM"),
  ProjectRef(file("OpenCL.scala"), "OpenCLCodeGenerator"),
  ProjectRef(file("OpenCL.scala"), "OpenCL")
)

libraryDependencies += "com.thoughtworks.raii" %% "asynchronous" % "2.0.0"

libraryDependencies += "eu.timepit" %% "refined" % "0.8.2"

libraryDependencies += "com.thoughtworks.each" %% "each" % "3.3.1"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
