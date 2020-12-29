package notification

import git.ChangedPumlFile
import uml.Puml

sealed trait Notification {
  def id(): String
  def text(): String
}

object Notification {
  def apply(changedPumlFile: ChangedPumlFile): Notification = {
    changedPumlFile match {
      case ChangedPumlFile.Added(o) =>
        new PumlCreated(o)
      case ChangedPumlFile.Modified(from, to) =>
        new PumlModified(from, to)
    }
  }

  def imageUrl(imageName: String, url: String): String = {
    s"![$imageName]($url)"
  }
}

class PumlCreated(puml: Puml) extends Notification {
  override def id(): String = puml.id

  override def text(): String = {
    s"uml created ${puml.id} ${Notification.imageUrl(puml.id, puml.previewUrl())}"
  }
}

class PumlModified(from: Puml, to: Puml) extends Notification {
  override def id(): String = s"from=${from.id}&to=${to.id}"

  override def text(): String = {
    s"uml modified. from ${Notification.imageUrl(from.id, from.previewUrl())} to ${Notification
      .imageUrl(to.id, to.previewUrl())}"
  }
}

trait NotificationPublisher {
  def publish(notifications: Seq[Notification])
}

object ConsolePublisher extends NotificationPublisher {
  override def publish(notifications: Seq[Notification]): Unit = {
    notifications.foreach { notification =>
      System.out.println(notification.text())
    }
  }
}
