// Script to build various tests based on the test .job files
def mkdir = { File f -> if( f.mkdirs() ) println 'Created ' + f; f }

def destDir = new File( project.basedir, "target/generated-test-sources/test/uk/trainwatch/job/lang" );
mkdir(destDir);

new File( "$destDir/CompilerTest.java" ).withWriter { out ->
    out.println "package uk.trainwatch.job.lang;";
    out.println "";
    out.println "import java.io.IOException;";
    out.println "import static org.junit.Assert.*;";
    out.println "import org.junit.Test;";
    out.println "import uk.trainwatch.job.Job;";
    out.println "";
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
            out.println "\t\tthrows IOException";
            out.println "\t{";
            out.println "\t\tJob job = test( \"" + name + "\");";
            out.println "\t}";
        }
    }

    out.println "";
    out.println "}";
};
