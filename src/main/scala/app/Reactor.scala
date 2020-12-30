package app

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import git.ChangedPumlFiles
import github.{CommentPublisher, RepositoryIdentifier}
import notification.{Notification, NotificationPublisher}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.util.Try

class Reactor(publisher: NotificationPublisher) extends LazyLogging {

  def perform(
      gitDirPath: String,
      fromHash: String,
      toHash: String
  ): Try[Unit] = {
    val repository = new FileRepositoryBuilder()
      .setGitDir(File(gitDirPath + "/.git").toJava)
      .build()
    for {
      changed <- ChangedPumlFiles.load(gitDirPath, repository, fromHash, toHash)
      comments = changed.toArray.map(Notification(_))
      _ <- publisher.publish(comments)
    } yield ()
  }
}

object Reactor extends App with LazyLogging {
  import org.kohsuke.github.GitHubBuilder

  val github = GitHubBuilder.fromEnvironment.build

  val instance = new Reactor(
    new CommentPublisher(
      new RepositoryIdentifier.Name(sys.env("REPOSITORY_NAME")),
      sys.env("PR_NUMBER").toInt,
      github
    )
  )
  val gitDir = Try(args(0)).getOrElse(sys.env("GITHUB_WORKSPACE"))
  val from = Try(args(1)).getOrElse(sys.env("FROM"))
  val to = Try(args(2)).getOrElse(sys.env("TO"))

  logger.info(s"gitDir $gitDir from $from to $to")

  instance.perform(gitDir, from, to).recover {
    case t: Throwable =>
      logger.error(s"$t")
      sys.error(t.getMessage)
  }
}
