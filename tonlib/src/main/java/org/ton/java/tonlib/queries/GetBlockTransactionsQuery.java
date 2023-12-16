package org.ton.java.tonlib.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.ton.java.tonlib.base.TypedAsyncObject;
import org.ton.java.tonlib.types.AccountTransactionId;
import org.ton.java.tonlib.types.BlockIdExt;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetBlockTransactionsQuery extends TypedAsyncObject {
    private BlockIdExt id;
    private int mode;
    private long count;
    private AccountTransactionId after;

    @Override
    public String getTypeName() {
        return "blocks.getTransactions";
    }
}
