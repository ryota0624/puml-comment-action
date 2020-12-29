package app

import better.files.File
import com.typesafe.scalalogging.LazyLogging
import git.ChangedPumlFiles
import notification.{ConsolePublisher, Notification, NotificationPublisher}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class Reactor(publisher: NotificationPublisher) extends LazyLogging {

  def perform(gitDirPath: String, fromHash: String, toHash: String): Unit = {
    logger.info("start")

    val repository = new FileRepositoryBuilder()
      .setGitDir(File(gitDirPath).toJava)
      .build()
    val publishT = for {
      changed <- ChangedPumlFiles.load(repository, fromHash, toHash)
      _ = {
        logger.info("loaded")
      }
      comments = changed.toArray.map(Notification(_))
    } yield {
      logger.info("before publish")
      publisher.publish(comments)
    }

    publishT.recover {
      case t: Throwable =>
        sys.error(t.getMessage)
    }
    // 変更のあったファイルを抽出
    // pumlファイルのみ抽出
    // pumlファイルをURLに変換
    // URLをgithubへコメント
    //  同じファイルへのコメントがあれば上書き
  }
}

object Reactor extends Reactor(ConsolePublisher) with App {

  perform(args(0), args(1), args(2))
}
