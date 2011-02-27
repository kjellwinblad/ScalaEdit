class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  //lazy val retronymSnapshotRepo = "retronym's repo" at "http://retronym.github.com/repo/releases"
  //lazy val onejarSBT = "com.github.retronym" % "sbt-onejar" % "0.2"
  lazy val eclipse = "de.element34" % "sbt-eclipsify" % "0.7.0"
  val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
  val assemblySBT = "com.codahale" % "assembly-sbt" % "0.1.1"
}