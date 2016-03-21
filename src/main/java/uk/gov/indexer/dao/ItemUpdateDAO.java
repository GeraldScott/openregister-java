package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import java.util.List;

@UseStringTemplate3StatementLocator("/sql/init_item.sql")
public interface ItemUpdateDAO extends DBConnectionDAO {

    String ITEM_TABLE = "item";

    @SqlUpdate
    void ensureItemTableInPlace();

    @SqlBatch("INSERT INTO " + ITEM_TABLE + "(sha256hex, content) VALUES(:itemHash, :content)")
    void writeBatch(@BindBean Iterable<Item> items);

    @SqlQuery("SELECT sha256hex FROM " + ITEM_TABLE + " WHERE sha256hex IN (<item_hex_values>)")
    List<String> getExistingItemHexValues(@BindIn("item_hex_values") List<String> itemHexValues);
}
