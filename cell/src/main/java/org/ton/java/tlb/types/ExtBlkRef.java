package org.ton.java.tlb.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ton.java.cell.Cell;
import org.ton.java.cell.CellBuilder;
import org.ton.java.cell.CellSlice;

import java.math.BigInteger;

/**
 * <pre>
 * ext_blk_ref$_
 * end_lt:uint64
 * seq_no:uint32
 * root_hash:bits256
 * file_hash:bits256 = ExtBlkRef;
 * </pre>
 */
@Builder
@Getter
@Setter
@ToString

public class ExtBlkRef {
    BigInteger endLt;
    int seqno;
    BigInteger rootHash;
    BigInteger fileHash;

    public String getRootHash() {
        return rootHash.toString(16);
    }

    public String getFileHash() {
        return fileHash.toString(16);
    }

    public Cell toCell() {
        return CellBuilder.beginCell()
                .storeUint(endLt, 64)
                .storeUint(seqno, 32)
                .storeUint(rootHash, 256)
                .storeUint(fileHash, 256)
                .endCell();
    }

    public static ExtBlkRef deserialize(CellSlice cs) {
        return ExtBlkRef.builder()
                .endLt(cs.loadUint(64))
                .seqno(cs.loadUint(32).intValue())
                .rootHash(cs.loadUint(256))
                .fileHash(cs.loadUint(256))
                .build();
    }
}
