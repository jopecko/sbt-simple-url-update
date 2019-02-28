package org.neolin.sbt

import sbt._

package object urlupdate {

  /**
   * .*** renamed to allPaths in sbt 1.0
   */
  implicit class FileSyntax(val file: File) extends AnyVal {
    def allPaths: PathFinder = file.***
  }
}
