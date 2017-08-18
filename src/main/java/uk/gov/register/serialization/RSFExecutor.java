package uk.gov.register.serialization;

import com.google.common.base.Splitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.core.Register;
import uk.gov.register.util.HashValue;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static uk.gov.register.core.HashingAlgorithm.SHA256;

public class RSFExecutor {
    public static final int RSF_HASH_POSITION = 3;

    private Map<String, RegisterCommandHandler> registeredHandlers;


    public RSFExecutor() {
        registeredHandlers = new HashMap<>();
    }

    public void register(RegisterCommandHandler registerCommandHandler) {
        registeredHandlers.put(registerCommandHandler.getCommandName(), registerCommandHandler);
    }

    public RegisterResult execute(RegisterSerialisationFormat rsf, Register register) {
        // HashValue and RSF file line number
        Map<HashValue, Integer> hashRefLine = new HashMap<>();
        Iterator<RegisterCommand> commands = rsf.getCommands();
        int rsfLine = 1;
        boolean allowSystemEntries = true;

        while (commands.hasNext()) {
            RegisterCommand command = commands.next();
            
            RegisterResult validationResult = validate(command, register, rsfLine, hashRefLine, allowSystemEntries);
            if (!validationResult.isSuccessful()) {
                return validationResult;
            }

            RegisterResult executionResult = execute(command, register);
            if (!executionResult.isSuccessful()) {
                return executionResult;
            }

            rsfLine++;
        }
        
        return validateOrphanAddItems(hashRefLine);
    }

    private RegisterResult execute(RegisterCommand command, Register register) {
        if (registeredHandlers.containsKey(command.getCommandName())) {
            RegisterCommandHandler registerCommandHandler = registeredHandlers.get(command.getCommandName());
            return registerCommandHandler.execute(command, register);
        } else {
            return RegisterResult.createFailResult("Handler not registered for command: " + command.getCommandName());
        }
    }

    private RegisterResult validate(RegisterCommand command, Register register, int rsfLine, Map<HashValue, Integer> hashRefLine, boolean allowSystemEntries) {
        // this ugly method won't be needed when we have symlinks
        // and won't have to rely on hashes

        String commandName = command.getCommandName();
        if (commandName.equals("add-item")) {
            validateAddItem(command, rsfLine, hashRefLine);
        } else if (commandName.equals("append-entry")) {
            // Check that we should still allow system entries
            if (allowSystemEntries) {
                allowSystemEntries = register.getTotalEntries(EntryType.user) == 0;
            }
            
            return validateAppendEntry(command, rsfLine, register, hashRefLine, allowSystemEntries);
        }
        
        return RegisterResult.createSuccessResult();
    }

    private RegisterResult validateAppendEntry(RegisterCommand command, int rsfLine, Register register, Map<HashValue, Integer> hashRefLine, boolean allowSystemEntries) {
        EntryType entryType = EntryType.valueOf(command.getCommandArguments().get(RSFFormatter.RSF_ENTRY_TYPE_POSITION));
        
        if (!allowSystemEntries && entryType == EntryType.system) {
            return RegisterResult.createFailResult("System entries must be added before user entries (line: " + rsfLine + "): " + command.toString());
        }
        
        String delimitedHashes = command.getCommandArguments().get(RSF_HASH_POSITION);
        if (StringUtils.isEmpty(delimitedHashes)) {
            return RegisterResult.createSuccessResult();
        }
        
        List<HashValue> hashes = Splitter.on(";").splitToList(delimitedHashes).stream()
                .map(s -> HashValue.decode(SHA256, s)).collect(toList());

        for (HashValue hashValue : hashes) {
            if (hashRefLine.containsKey(hashValue)) {
                hashRefLine.put(hashValue, 0);
            } else {
                Optional<Item> item = register.getItemBySha256(hashValue);
                if (!item.isPresent()) {
                    return RegisterResult.createFailResult("Orphan append entry (line:" + rsfLine + "): " + command.toString());
                } else {
                    hashRefLine.put(hashValue, 0);
                }
            }
        }
        return RegisterResult.createSuccessResult();
    }

    private void validateAddItem(RegisterCommand command, int rsfLine, Map<HashValue, Integer> hashRefLine) {
        String hash = DigestUtils.sha256Hex(command.getCommandArguments().get(0).getBytes(StandardCharsets.UTF_8));
        hashRefLine.put(new HashValue(SHA256, hash), rsfLine);
    }

    private RegisterResult validateOrphanAddItems(Map<HashValue, Integer> hashRefLine) {
        List<String> orphanItems = new ArrayList<>();
        hashRefLine.forEach((hash, rsfLine) -> {
            if (rsfLine > 0) {
                orphanItems.add("Orphan add item (line:" + rsfLine + "): " + hash.encode());
            }
        });

        if (orphanItems.isEmpty()) {
            return RegisterResult.createSuccessResult();
        } else {
            return RegisterResult.createFailResult(String.join("; ", orphanItems));
        }
    }
}
