package git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import uml.Puml

import scala.jdk.CollectionConverters._
import scala.util.Try

class ChangedPumlFiles(private val values: Iterator[ChangedPumlFile]) {
  def toArray: Array[ChangedPumlFile] = values.toArray
}

object ChangedPumlFiles {
  def load(
      repository: Repository,
      from: String,
      to: String
  ): Try[ChangedPumlFiles] = {
    val fromId = repository.resolve(from)
    val toId = repository.resolve(to)
    for {
      reader <- Try(repository.newObjectReader())
      files <- Try(new Git(repository)).map { git =>
        val fromParser = new CanonicalTreeParser
        fromParser.reset(reader, fromId)

        val toParser = new CanonicalTreeParser
        toParser.reset(reader, toId)
        val list = git
          .diff()
          .setNewTree(toParser)
          .setNewTree(fromParser)
          .call()
          .iterator()
          .asScala
          .collect {
            case diffEntry: DiffEntry
                if Puml.isPumlFileName(diffEntry.getNewPath) =>
              diffEntry.getChangeType match {
                case ChangeType.ADD =>
                  ChangedPumlFile.Added(Puml.load(diffEntry.getNewPath))
                case ChangeType.MODIFY =>
                  ChangedPumlFile.Modified(
                    Puml.load(diffEntry.getOldPath),
                    Puml.load(diffEntry.getNewPath)
                  )
              }
          }

        new ChangedPumlFiles(list)
      }
    } yield files
  }
}
