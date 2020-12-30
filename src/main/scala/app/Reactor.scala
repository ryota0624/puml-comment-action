package app

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import git.ChangedPumlFiles
import notification.{ConsolePublisher, Notification, NotificationPublisher}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.util.Try

class Reactor(publisher: NotificationPublisher) extends LazyLogging {

  def perform(
      gitDirPath: String,
      fromHash: String,
      toHash: String
  ): Try[Unit] = {
    val repository = new FileRepositoryBuilder()
      .setGitDir(File(gitDirPath).toJava)
      .build()
    for {
      changed <- ChangedPumlFiles.load(repository, fromHash, toHash)
      comments = changed.toArray.map(Notification(_))
      _ <- publisher.publish(comments)
    } yield ()
  }
}

object Reactor extends Reactor(ConsolePublisher) with App {

//  import org.kohsuke.github.GitHubBuilder

//  val github = GitHubBuilder.fromEnvironment.build
  perform(args(0), args(1), args(2)).recover {
    case t: Throwable =>
      logger.error(s"$t")
      sys.error(t.getMessage)
  }
}
