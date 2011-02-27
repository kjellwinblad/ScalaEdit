import sbt._
import java.io.File
import scala.io.Source
import de.element34.sbteclipsify._
import assembly._

class ScalaEditProject(info: ProjectInfo) extends DefaultProject(info) with Eclipsify  with AssemblyBuilder{
  lazy val projectScalaVersion = "2.8.1"
  lazy val mainJar = "target/scala_" + projectScalaVersion + "/" +
    projectName.value + "_" + projectScalaVersion + "-" + projectVersion.value + ".jar"
  lazy val dependencyJars = new File("lib").listFiles.filter(_.getName.endsWith(".jar")).map(_.getAbsolutePath)
  override lazy val mainClass = Some("me.winsh.scalaedit.Main")

  /**
   * Run the program as a stand alone application
   */
  override lazy val run =
    task { args =>
      task {
        val cp = dependencyJars.mkString("", ":", ":") + mainJar

        val p = Runtime.getRuntime().exec((List("java", "-cp", cp, mainClass.get) ::: args.toList).toArray)

        val in = Source.fromInputStream(p.getInputStream)
        val err = Source.fromInputStream(p.getErrorStream)

        for (l <- in.getLines)
          println(l)

        for (l <- err.getLines)
          println(l)

        None
      } dependsOn (compile)
    }

  lazy val uploadToGooglecode =
    task { args =>
      if (args.length == 3) {

        uploadToGooglecodeConstructor(args(0), args(1), args(2))
      } else
        task { Some("Usage: uploadWebStartToGoogleCode googleCodeProjectID googleCodeUsername googleCodePassword") }
    }

  def uploadToGooglecodeConstructor(googleCodeProjectID: String, googleCodeUsername: String, googleCodePassword: String) =
    task {

	  cleanAction
	  incrementVersionAction
	  assembly
	  
      import java.lang.Runtime
      import java.io._
      import scala.io.Source


      val jarFiles: PathFinder = "target" / ("scala_" + projectScalaVersion) ** ("*assembly-" +  projectVersion.value + ".jar")

      val assemplyFile = jarFiles.get.toList.first.asFile
      
      
        val p = Runtime.getRuntime().exec(Array("python", "./src/helperscripts/googlecode_upload.py", "-s", "JNLPUpload", "-p", googleCodeProjectID, "-u", googleCodeUsername, "-w", googleCodePassword, assemplyFile.getAbsolutePath))

        val in = Source.fromInputStream(p.getInputStream)
        val err = Source.fromInputStream(p.getErrorStream)

        for (l <- in.getLines)
          println(l)

        for (l <- err.getLines)
          println(l)

      None
    } dependsOn assembly

  import SignJar._
  /*override*/ def webstartSignConfiguration = Some(new SignConfiguration("scalaedit", storePassword("scalaedit43") :: Nil))
 /* override*/ def webstartPack200 = false
 /* override */def webstartGzip = false

 /* override*/ def jnlpXML(libraries: Seq[WebstartJarResource]) =
    <jnlp spec="1.0+" codebase="http://scala-edit.googlecode.com/files/" href={ mainJar.split("/").last.replace("jar", "jnlp") }>
      <information>
        <title>ScalaEdit</title>
        <vendor>winsh.me</vendor>
        <homepage href="http://scala-edit.googlecode.com/"/>
        <description kind="one-line">ScalaEdit is a text editor. It has syntax highlighting support and a Scala interpreter console etc.</description>
        <description kind="short">Text editor for Scala programmers</description>
        <description kind="tooltip">Text editor for Scala programmers</description>
        <icon href="http://scala-edit.googlecode.com/files/logo.jpeg" kind="default"/>
        <shortcut online="false"><desktop/><menu submenu="ScalaEdit"/></shortcut>
      </information>
      <security>
        <all-permissions/>
      </security>
      <resources>
        <j2se version="1.6+"/>
        <jar main="true" href={ mainJar.split("/").last }/>
        {
          val jarFiles: PathFinder = "lib" ** "*.jar"
          for (j <- jarFiles.get) yield <jar main="false" href={ j.asFile.getName }/>
        }
      </resources>
      <application-desc main-class={ mainClass.get }>
      </application-desc>
    </jnlp>

}
