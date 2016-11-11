package uk.gov.register.serialization;

import uk.gov.register.core.Item;
import uk.gov.register.core.Register;

import java.util.concurrent.atomic.AtomicInteger;

public class AddItemCommand implements RegisterCommand {

    private Item item;

    public AddItemCommand(Item item) {
        this.item = item;
    }

    @Override
    public void execute(Register register, AtomicInteger entryNumber) {
        register.putItem(item);
    }

    @Override
    public String serialise(CommandParser commandParser) {
        return commandParser.serialise(item);
    }
}
