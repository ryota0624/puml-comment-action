package github

import notification.{Notification, NotificationPublisher}

case class Comment(text: String)

object Comment {
  def fromNotifications(notifications: Seq[Notification]): Comment = {
    Comment(???)
  }
}

class CommentPublisher extends NotificationPublisher {
  override def publish(notifications: Seq[Notification]): Unit = {
    val comment = Comment.fromNotifications(notifications)
    publish(comment)
  }

  def publish(comment: Comment): Unit = ???
}
