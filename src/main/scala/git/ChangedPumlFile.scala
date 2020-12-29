package git

sealed trait ChangedPumlFile

object ChangedPumlFile {
  case class Added(o: uml.Puml) extends ChangedPumlFile
  case class Modified(from: uml.Puml, to: uml.Puml) extends ChangedPumlFile
}
