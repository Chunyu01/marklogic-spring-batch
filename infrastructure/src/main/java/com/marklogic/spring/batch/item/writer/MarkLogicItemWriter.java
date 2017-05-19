package com.marklogic.spring.batch.item.writer;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.batch.BatchWriter;
import com.marklogic.client.batch.RestBatchWriter;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.WriteBatcher;
import com.marklogic.client.document.*;
import com.marklogic.client.helper.LoggingObject;
import com.marklogic.client.impl.DocumentWriteOperationImpl;
import org.springframework.batch.item.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The MarkLogicItemWriter is an ItemWriter used to write any type of document to MarkLogic. It expects a list of
 * <a href="http://docs.marklogic.com/javadoc/client/com/marklogic/client/document/DocumentWriteOperation.html">DocumentWriteOperation</a>
 * instances, each of which encapsulates a write operation to MarkLogic.
 *
 * It depends on an instance of BatchWriter from the ml-javaclient-util. This allows for either the REST API - a
 * DatabaseClient instance or set of instances - or XCC to be used for writing documents.
 *
 * A UriTransformer can be optionally set to transform the URI of each incoming DocumentWriteOperation.
 */
public class MarkLogicItemWriter extends LoggingObject implements ItemWriter<DocumentWriteOperation>, ItemStream {

    private UriTransformer uriTransformer;
    protected long writeCount = 0;
    protected long writeCalled = 0;
    protected DataMovementManager dataMovementManager;
    private int BATCH_SIZE = 100;
    private int THREAD_COUNT = 4;

    public MarkLogicItemWriter(DatabaseClient client) {
        dataMovementManager = client.newDataMovementManager();
    }

    @Override
    public void write(List<? extends DocumentWriteOperation> items) throws Exception {
        if (items == null) {
            throw new NullPointerException("items are null");
        }

        WriteBatcher batcher = dataMovementManager.newWriteBatcher();
        batcher
            .withBatchSize(getBatchSize())
            .withThreadCount(getThreadCount());


        for (DocumentWriteOperation item : items) {
            if (uriTransformer != null) {
                String newUri = uriTransformer.transform(item.getUri());
                batcher.add(newUri, item.getMetadata(), item.getContent());
            } else {
                batcher.add(item.getUri(), item.getMetadata(), item.getContent());
            }
        }

        batcher.flushAndWait();

    }

    public int getBatchSize() {
        return BATCH_SIZE;
    }

    public int getThreadCount() {
        return THREAD_COUNT;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
        dataMovementManager.release();
    }

    public void setUriTransformer(UriTransformer uriTransformer) {
        this.uriTransformer = uriTransformer;
    }
}

