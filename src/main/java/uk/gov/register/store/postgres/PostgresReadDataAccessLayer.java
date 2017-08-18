package uk.gov.register.store.postgres;

import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.db.EntryIterator;
import uk.gov.register.db.EntryQueryDAO;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.db.ItemQueryDAO;
import uk.gov.register.store.DataAccessLayer;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public abstract class PostgresReadDataAccessLayer implements DataAccessLayer {
    private final EntryQueryDAO entryQueryDAO;
    private final ItemQueryDAO itemQueryDAO;
    protected final IndexQueryDAO indexQueryDAO;
    protected final String schema;
    
    public PostgresReadDataAccessLayer(
            EntryQueryDAO entryQueryDAO, IndexQueryDAO indexQueryDAO,
            ItemQueryDAO itemQueryDAO, String schema) {
        this.entryQueryDAO = entryQueryDAO;
        this.indexQueryDAO = indexQueryDAO;
        this.itemQueryDAO = itemQueryDAO;
        this.schema = schema;
    }

    // Entry Log

    @Override
    public Optional<Entry> getEntry(int entryNumber) {
        checkpoint();
        return entryQueryDAO.findByEntryNumber(entryNumber, schema);
    }

    @Override
    public Collection<Entry> getEntries(int start, int limit) {
        checkpoint();
        return entryQueryDAO.getEntries(start, limit, schema);
    }

    @Override
    public Collection<Entry> getAllEntries() {
        checkpoint();
        return entryQueryDAO.getAllEntriesNoPagination(schema);
    }

    @Override
    public Iterator<Entry> getEntryIterator() {
        checkpoint();
        return entryQueryDAO.getIterator(schema);
    }

    @Override
    public Iterator<Entry> getEntryIterator(int totalEntries1, int totalEntries2) {
        checkpoint();
        return entryQueryDAO.getIterator(totalEntries1, totalEntries2, schema);
    }

    @Override
    public Iterator<Entry> getIndexEntryIterator(String indexName) {
        return indexQueryDAO.getIterator(indexName, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
    }

    @Override
    public Iterator<Entry> getIndexEntryIterator(String indexName, int totalEntries1, int totalEntries2) {
        return indexQueryDAO.getIterator(indexName, totalEntries1, totalEntries2, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
    }

    @Override
    public <R> R withEntryIterator(Function<EntryIterator, R> callback) {
        checkpoint();
        return EntryIterator.withEntryIterator(entryQueryDAO, entryIterator -> callback.apply(entryIterator), schema);
    }

    @Override
    public int getTotalEntries() {
        return entryQueryDAO.getTotalEntries(schema);
    }

    public int getTotalSystemEntries() {
        checkpoint();
        return entryQueryDAO.getTotalSystemEntries(schema);
    }

    @Override
    public Optional<Instant> getLastUpdatedTime() {
        checkpoint();
        return entryQueryDAO.getLastUpdatedTime(schema);
    }

    // Item Store

    @Override
    public Optional<Item> getItemBySha256(HashValue hash) {
        return itemQueryDAO.getItemBySHA256(hash.getValue(), schema);
    }

    @Override
    public Collection<Item> getAllItems() {
        checkpoint();
        return itemQueryDAO.getAllItemsNoPagination(schema);
    }

    @Override
    public Iterator<Item> getItemIterator() {
        checkpoint();
        return itemQueryDAO.getIterator(schema);
    }

    @Override
    public Iterator<Item> getItemIterator(int start, int end) {
        checkpoint();
        return itemQueryDAO.getIterator(start, end, schema);
    }
    
    @Override
    public Iterator<Item> getSystemItemIterator() {
        checkpoint();
        return itemQueryDAO.getSystemItemIterator(schema);
    }

    // Record Index

    @Override
    public Optional<Record> getRecord(String key) {
        checkpoint();
        return getIndexRecord(key, IndexNames.RECORD);
    }

    @Override
    public List<Record> getRecords(int limit, int offset) {
        checkpoint();
        return indexQueryDAO.findRecords(limit, offset, IndexNames.RECORD, schema, "entry");
    }

    @Override
    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        checkpoint();
        return indexQueryDAO.findMax100RecordsByKeyValue(key, value, schema);
    }

    @Override
    public Collection<Entry> getAllEntriesByKey(String key) {
        checkpoint();
        return entryQueryDAO.getAllEntriesByKey(key, schema);
    }

    // Index
    @Override
    public Optional<Record> getIndexRecord(String key, String indexName) {
        List<Record> records = getIndexRecords(Arrays.asList(key), indexName);
        
        return records.size() == 1 ? Optional.of(records.get(0)) : Optional.empty();
    }
    
    protected List<Record> getIndexRecords(List<String> keys, String indexName) {
        return indexQueryDAO.findRecords(keys, indexName, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
    }

    @Override
    public List<Record> getIndexRecords(int limit, int offset, String indexName) {
        return indexQueryDAO.findRecords(limit, offset, indexName, schema, indexName.equals(IndexNames.METADATA) ? "entry_system" : "entry");
    }

    @Override
    public int getTotalIndexRecords(String indexName) {
        return indexQueryDAO.getTotalRecords(indexName, schema);
    }

    @Override
    public int getCurrentIndexEntryNumber(String indexName) {
        return indexQueryDAO.getCurrentIndexEntryNumber(indexName, schema);
    }

    protected abstract void checkpoint();
}
