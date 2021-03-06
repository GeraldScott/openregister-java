package uk.gov.register.serialization;

import com.google.common.collect.Iterators;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.proofs.ProofGenerator;
import uk.gov.register.util.HashValue;

import java.util.*;

public class RSFCreator {

    private final HashValue EMPTY_ROOT_HASH = new HashValue(HashingAlgorithm.SHA256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");

    private Map<Class, RegisterCommandMapper> registeredMappers;

    public RSFCreator() {
        this.registeredMappers = new HashMap<>();
    }

    public RegisterSerialisationFormat create(Register register, ProofGenerator proofGenerator) {
        Iterator<?> iterators = Iterators.concat(
                Iterators.singletonIterator(EMPTY_ROOT_HASH),
                register.getItemIterator(EntryType.system),
                register.getItemIterator(EntryType.user),
                register.getEntryIterator(EntryType.system),
                register.getEntryIterator(EntryType.user),
                Iterators.singletonIterator(proofGenerator.getRootHash()));

        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(commands);
    }

    public RegisterSerialisationFormat create(Register register, ProofGenerator proofGenerator, int totalEntries1, int totalEntries2) {
        Iterator<?> iterators;

        if (totalEntries1 > 0 && totalEntries1 == totalEntries2) {
            iterators = Iterators.singletonIterator(proofGenerator.getRootHash(totalEntries1));
        } else {

            HashValue previousRootHash = totalEntries1 == 0 ? EMPTY_ROOT_HASH : proofGenerator.getRootHash(totalEntries1);
            HashValue nextRootHash = proofGenerator.getRootHash(totalEntries2);
            Iterator<Item> metadataItemIterator = totalEntries1 == 0 ? register.getItemIterator(EntryType.system) : Collections.emptyIterator();
            Iterator<Entry> metadataEntryIterator = totalEntries1 == 0 ? register.getEntryIterator(EntryType.system) : Collections.emptyIterator();

            iterators = Iterators.concat(
                    Iterators.singletonIterator(previousRootHash),
                    register.getItemIterator(totalEntries1, totalEntries2),
                    metadataItemIterator,
                    metadataEntryIterator,
                    register.getEntryIterator(totalEntries1, totalEntries2),
                    Iterators.singletonIterator(nextRootHash));
        }
        Iterator<RegisterCommand> commands = Iterators.transform(iterators, obj -> (RegisterCommand) getMapper(obj.getClass()).apply(obj));
        return new RegisterSerialisationFormat(commands);
    }

    public void register(RegisterCommandMapper commandMapper) {
        registeredMappers.put(commandMapper.getMapClass(), commandMapper);
    }

    private RegisterCommandMapper getMapper(Class objClass) {
        if (registeredMappers.containsKey(objClass)) {
            return registeredMappers.get(objClass);
        } else {
            throw new RuntimeException("Mapper not registered for class: " + objClass.getName());
        }
    }
}
