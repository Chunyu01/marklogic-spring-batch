package com.marklogic.spring.batch.job;

import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.junit.ClientTestHelper;
import com.marklogic.junit.Fragment;
import org.junit.Test;

public class LoadDocumentsFromDirectoryJobTest extends AbstractJobTest {

    @Test
    public void loadXmlDocumentsTest() {
        runJobWithMarkLogicJobRepository(
                LoadXmlDocumentsFromDirectoryConfig.class,
                "--input_file_path", "data/*.xml",
                "--input_file_pattern", "(elmo|grover).xml",
                "--uri_id", "/monster/name");
        thenTwoXmlFilesExistInDatabase();
    }

    public void thenTwoXmlFilesExistInDatabase() {
        ClientTestHelper client = new ClientTestHelper();
        client.setDatabaseClientProvider(getClientProvider());
        Fragment frag = client.parseUri("/Grover");
        frag.assertElementExists("/monster/name[text() = 'Grover']");
        frag = client.parseUri("/Elmo");
        frag.assertElementExists("/monster/name[text() = 'Elmo']");
        try {
            client.parseUri("/BigBird");
        } catch (ResourceNotFoundException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void loadJsonDocumentsTest() {
        runJobWithMarkLogicJobRepository(
                LoadJsonDocumentsFromDirectoryConfig.class,
                "--input_file_path", "data/*.json",
                "--input_file_pattern", "(elmo|grover).json");
    }

}