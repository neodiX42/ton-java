package org.ton.java.tlb.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ton.java.cell.Cell;
import org.ton.java.cell.CellBuilder;
import org.ton.java.cell.CellSlice;
import org.ton.java.cell.TonHashMapE;

import static java.util.Objects.isNull;

@Builder
@Getter
@Setter
@ToString
/**
 * masterchain_state_extra#cc26
 *   shard_hashes:ShardHashes
 *   config:ConfigParams
 *   ^[ flags:(## 16) { flags <= 1 }
 *      validator_info:ValidatorInfo
 *      prev_blocks:OldMcBlocksInfo
 *      after_key_block:Bool
 *      last_key_block:(Maybe ExtBlkRef)
 *      block_create_stats:(flags . 0)?BlockCreateStats ]
 *   global_balance:CurrencyCollection
 * = McStateExtra;
 */
public class McStateExtra {
    long magic;
    TonHashMapE shardHashes;
    ConfigParams configParams;
    McStateExtraInfo info;
    CurrencyCollection globalBalance;

    private String getMagic() {
        return Long.toHexString(magic);
    }

    public Cell toCell() {
        return CellBuilder.beginCell()
                .storeUint(0xcc26, 32)
                .storeDict(shardHashes.serialize(
                        k -> CellBuilder.beginCell().storeUint((Long) k, 32).bits,
                        v -> CellBuilder.beginCell().storeCell((Cell) v))// ConfigParams
                )
                .storeCell(configParams.toCell())
                .storeRef(info.toCell())
                .storeCell(globalBalance.toCell())
                .endCell();
    }

    public static McStateExtra deserialize(CellSlice cs) {
        if (isNull(cs)) {
            return null;
        }

        if (cs.isExotic()) {
            return null;
        }
//        System.out.println("McStateExtra " + Utils.bitStringToHex(cs.toString()));
        long magic = cs.loadUint(16).longValue();
        assert (magic == 0xcc26L) : "McStateExtra: magic not equal to 0xcc26, found 0x" + Long.toHexString(magic);
//        if (magic != 0xcc26L) {
//            System.out.println("McStateExtra: magic not equal to 0xcc26, found 0x" + Long.toHexString(magic));
//            return McStateExtra.builder().magic(-1L).build();
//        }

        return McStateExtra.builder()
                .magic(0xcc26L)
                .shardHashes(cs.loadDictE(32, k -> k.readInt(32), v -> v)) // todo BinTree
                .configParams(ConfigParams.deserialize(cs))
                .info(McStateExtraInfo.deserialize(CellSlice.beginParse(cs.loadRef()))) // todo parse
                .globalBalance(CurrencyCollection.deserialize(cs))
                .build();
    }
}
