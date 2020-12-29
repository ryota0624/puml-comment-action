package github

import better.files.File
import git.ChangedPumlFiles
import notification.{ConsolePublisher, Notification, NotificationPublisher}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class Reactor(publisher: NotificationPublisher) {
  def perform(gitDirPath: String, fromHash: String, toHash: String): Unit = {

    val repository = new FileRepositoryBuilder()
      .setGitDir(File(gitDirPath).toJava)
      .build()
    for {
      changed <- ChangedPumlFiles.load(repository, fromHash, toHash)
      comments = changed.toArray.map(Notification(_))
    } yield {
      publisher.publish(comments)
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
