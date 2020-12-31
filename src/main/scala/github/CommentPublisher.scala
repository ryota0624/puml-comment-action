package github

import notification.{Notification, NotificationPublisher}
import org.kohsuke.github.{GHIssueComment, GHRepository, GitHub}

import scala.util.{Success, Try}

case class Comment(text: String) {
  def addPrefix(prefix: String): Comment = Comment(prefix + s"\n${text}")
}

object Comment {
  def fromNotifications(notifications: Seq[Notification]): Comment = {
    Comment(notifications.map(_.text()).mkString("\n\n--------\n\n"))
  }
}

sealed abstract class RepositoryIdentifier(val value: String) {
  protected[github] def repository(github: GitHub): Try[GHRepository]
}
object RepositoryIdentifier {
  class Id(value: String) extends RepositoryIdentifier(value) {
    protected[github] override def repository(
        github: GitHub
    ): Try[GHRepository] =
      Try(github.getRepositoryById(value))
  }
  class Name(value: String) extends RepositoryIdentifier(value) {
    protected[github] override def repository(
        github: GitHub
    ): Try[GHRepository] =
      Try(github.getRepository(value))
  }
}

class CommentPublisher(
    repositoryIdentifier: RepositoryIdentifier,
    pullRequestNumber: Int,
    github: GitHub
) extends NotificationPublisher {
  override def publish(notifications: Seq[Notification]): Try[Unit] = {
    val comment = Comment.fromNotifications(notifications)
    publish(comment)
    Success()
  }
  import scala.jdk.CollectionConverters._

  def publish(comment: Comment): Try[Unit] =
    for {
      repository <- repositoryIdentifier.repository(github)
      pr = repository.getPullRequest(pullRequestNumber)
      comments: Array[GHIssueComment] =
        pr.getComments.iterator().asScala.toArray
      _ <- Try(findPublishedComment(comments) match {
        case Some(publishedComment) =>
          publishedComment.update(comment.text)
        case None =>
          pr.comment(comment.addPrefix(publishedPrefix).text)
      })
    } yield ()

  def findPublishedComment(
      comments: Array[GHIssueComment]
  ): Option[GHIssueComment] =
    comments.find(comment => comment.getBody.contains(publishedPrefix))

  def publishedComment(text: String): Boolean = text.contains(publishedPrefix)

  private val publishedPrefix: String = "published by GitHub Action" // TODO

}
