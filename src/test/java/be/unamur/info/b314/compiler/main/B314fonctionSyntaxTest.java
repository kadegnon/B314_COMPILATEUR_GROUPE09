package be.unamur.info.b314.compiler.main;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class B314fonctionSyntaxTest {

    private static final Logger LOG = LoggerFactory.getLogger(B314fonctionSyntaxTest.class);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(); // Create a temporary folder for outputs deleted after tests

    @Rule
    public TestRule watcher = new TestWatcher() { // Prints message on logger before each test
        @Override
        protected void starting(Description description) {
            LOG.info(String.format("Starting test: %s()...",
                    description.getMethodName()));
        }
    ;
    };

    //
    // Serie fonction OK
    //
    @Test
    public void testfonction_good_ok() throws Exception{
        CompilerTestHelper.launchCompilation("/syntax/fonction/ok/good.b314", testFolder.newFile(), true, "fonction: good");
    }

    //
    // Serie fonction KO
    //
    @Test
    public void testfonction_wrong_ko() throws Exception {
        CompilerTestHelper.launchCompilation("/syntax/fonction/ko/wrong.b314", testFolder.newFile(), false, "fonction: wrong");
    }

}