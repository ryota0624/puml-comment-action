package uml

import better.files.File
import net.sourceforge.plantuml.code.TranscoderUtil

import java.nio.charset.Charset

class Puml(val id: String, val text: String) {
  def previewUrl(): String = {
    s"http://www.plantuml.com/plantuml/png/${Puml.transcoder.encode(text)}"
  }
}

object Puml {
  private val transcoder = TranscoderUtil.getDefaultTranscoder

  def decode(id: String, text: String): Puml = {
    new Puml(id, transcoder.decode(text))
  }

  def isPumlFileName(name: String): Boolean = name.contains(".puml")

  def load(
      path: String
  )(implicit charset: Charset = Charset.defaultCharset()): Puml = {
    new Puml(path, File(path).contentAsString)
  }
}
