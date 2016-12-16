package uk.gov.register.db;

import uk.gov.register.core.Entry;
import uk.gov.register.core.RegisterContext;
import uk.gov.verifiablelog.store.memoization.MemoizationStore;

import javax.inject.Inject;


/**
 * An append-only log of Entries, together with proofs
 */
public class UnmodifiableEntryLog extends AbstractEntryLog {
    @Inject
    public UnmodifiableEntryLog(RegisterContext registerContext) {
        this(registerContext.getMemoizationStore(), registerContext.getDbi().onDemand(EntryQueryDAO.class));
    }

    public UnmodifiableEntryLog(MemoizationStore memoizationStore, EntryQueryDAO entryQueryDAO) {
        super(entryQueryDAO, memoizationStore);
    }

    @Override
    public void appendEntry(Entry entry) {
        throw new UnsupportedOperationException();
    }
}