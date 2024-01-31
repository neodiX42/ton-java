package org.ton.java.tlb.types;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ton.java.cell.Cell;
import org.ton.java.cell.CellBuilder;

import java.math.BigInteger;

@Builder
@Getter
@Setter
@ToString
/**
 ed25519_pubkey#8e81278a pubkey:bits256 = SigPubKey;  // 288 bits
 */
public class SigPubKey {
    int magic;
    BigInteger pubkey;

    public Cell toCell() {
        return CellBuilder.beginCell()
                .storeUint(0x8e81278a, 32)
                .storeUint(pubkey, 256)
                .endCell();
    }
}