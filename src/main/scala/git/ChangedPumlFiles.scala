package git

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.{ObjectId, Repository}
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import uml.Puml

import scala.jdk.CollectionConverters._
import scala.util.Try

class ChangedPumlFiles(private val values: Iterator[ChangedPumlFile]) {
  def toArray: Array[ChangedPumlFile] = values.toArray
}

object ChangedPumlFiles extends LazyLogging {

  def load(
      repository: Repository,
      from: String,
      to: String
  ): Try[ChangedPumlFiles] = {
    val fromId = repository.resolve(from)
    val toId = repository.resolve(to)
    for {
      files <- Try(new Git(repository)).map { git =>
        val fromParser = prepareTreeParser(repository, fromId)
        val toParser = prepareTreeParser(repository, toId)

        val list = git
          .diff()
          .setNewTree(toParser)
          .setOldTree(fromParser)
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

  private def prepareTreeParser(
      repository: Repository,
      objectId: ObjectId
  ): CanonicalTreeParser = {
    // from the commit we can build the tree which allows us to construct the TreeParser
    //noinspection Duplicates
    try {
      val walk = new RevWalk(repository)
      try {
        val commit = walk.parseCommit(objectId)
        val tree = walk.parseTree(commit.getTree.getId)
        val treeParser = new CanonicalTreeParser
        try {
          val reader = repository.newObjectReader
          try treeParser.reset(reader, tree.getId)
          finally if (reader != null) reader.close()
        }
        walk.dispose()
        treeParser
      } finally if (walk != null) walk.close()
    }
  }
}
