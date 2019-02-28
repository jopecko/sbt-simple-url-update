//Copyright (c) 2014 ICRL
//
//See the file license.txt for copying permission.

package org.neolin.sbt.urlupdate

import sbt._
import com.typesafe.sbt.web.{Compat, PathMapping, SbtWeb}
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys._
import sbt.Task

trait Keys {

  val simpleUrlUpdate = TaskKey[Pipeline.Stage]("simple-url-update", "Update assets url in static css or js files with in asset pipeline.")

  val algorithms = SettingKey[Seq[String]]("digest-algorithms", "Types of checksum used in the digest pipeline to generate.")
}

object SbtSimpleUrlUpdate extends AutoPlugin {

  override def requires = SbtWeb

  override def trigger = AllRequirements

  object autoImport extends Keys

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    algorithms := Seq("md5"),
    includeFilter in simpleUrlUpdate := "*.css" || "*.js" ,
    excludeFilter in simpleUrlUpdate := HiddenFileFilter,
    simpleUrlUpdate := simpleURLUpdateFiles.value
  )

  private def updatePipeline(mappings: Seq[PathMapping], algorithm: String): String => String = {
    val reversePathMappings = mappings.map{ case (k, v) => (v, k) }.toMap
    
    def checksummedPath(path: String): String = {
	  
      val pathFile = sbt.file(path)
      reversePathMappings.get(path + "." + algorithm) match {
        case Some(file) => (pathFile.getParentFile / (IO.read(file) + "-" + pathFile.getName)).getPath
        case None => path
      }
    }
    val assetVersions = mappings.map{ 
      case (_, path) => path.replaceAll("\\\\","/") -> checksummedPath(path).replaceAll("\\\\","/")
    }.distinct.filterNot{ 
      case (originalPath, newPath) => originalPath == newPath 
    }

    Function.chain(
      assetVersions.map{ 
        case (originalPath, newPath) => (content: String) => content.replaceAll(originalPath, newPath)
      }
    )
  }

  val simpleURLUpdateFiles: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    import sbt.Path._
    val targetDir = webTarget.value / simpleUrlUpdate.key.label
    val include = (includeFilter in simpleUrlUpdate).value
    val exclude = (excludeFilter in simpleUrlUpdate).value
    val cacheStore = Compat.cacheStore(streams.value, simpleUrlUpdate.key.label)

    mappings: Seq[PathMapping] =>

      SbtWeb.syncMappings(cacheStore, mappings, targetDir)

      val updateMappings = targetDir.allPaths.get.toSet.filter(_.isFile).pair(relativeTo(targetDir))

      for {
        algorithm <- algorithms.value
        (file, _) <- updateMappings.filter(f => !f._1.isDirectory && include.accept(f._1) && !exclude.accept(f._1))
      } yield {
        IO.write(file, updatePipeline(updateMappings, algorithm)(IO.read(file)))
      }

      targetDir.allPaths.get.toSet.filter(_.isFile).pair(relativeTo(targetDir))
  }
}
