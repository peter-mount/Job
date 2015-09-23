// Script to build various tests based on the test .job files
def mkdir = { File f -> if( f.mkdirs() ) println 'Created ' + f; f }

def destDir = new File( project.basedir, "target/generated-test-sources/groovy/uk/trainwatch/job/lang" );
mkdir(destDir);

generated = "@Generated(\""+new Date()+"\")";

new File( "$destDir/CompilerTest.java" ).withWriter { out ->
    out.println "package uk.trainwatch.job.lang;";
    out.println "";
    //out.println "import static org.junit.Assert.*;";
    out.println "import javax.annotation.Generated;";
    out.println "import org.junit.Test;";
    out.println "import uk.trainwatch.job.Job;";
    out.println "";
    out.println "/**";
    out.println " * Runs our test .job files through the Compiler ensuring that";
    out.println " * the compilation actually does work.";
    out.println " */";
    out.println generated;
    out.println "public class CompilerTest";
    out.println "\textends AbstractCompilerTest";
    out.println "{";

    def sourceDir = new File( project.basedir, "src/test/resources" );
    sourceDir.eachDirRecurse {
        it.eachFileMatch( ~/.*\.job/ ) {
            file -> name = file.name;
            name=name.replace(".job","");
            out.println "";
            out.println "\t@Test";
            out.println "\tpublic void " + name + "()";
            out.println "\t\tthrows Exception";
            out.println "\t{";
            out.println "\t\tJob job = compile( \"" + name + "\");";
            out.println "\t\texecute(job);";
            out.println "\t}";
        }
    }

    out.println "";
    out.println "}";
};
