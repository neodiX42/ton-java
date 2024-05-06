package org.ton.java.smartcontract.wallet.v1;

import org.ton.java.address.Address;
import org.ton.java.cell.Cell;
import org.ton.java.cell.CellBuilder;
import org.ton.java.smartcontract.types.WalletCodes;
import org.ton.java.smartcontract.types.WalletV1R1Config;
import org.ton.java.smartcontract.wallet.Contract;
import org.ton.java.smartcontract.wallet.Options;
import org.ton.java.tlb.types.*;
import org.ton.java.tonlib.Tonlib;
import org.ton.java.tonlib.types.ExtMessageInfo;
import org.ton.java.utils.Utils;

public class WalletV1ContractR1 implements Contract<WalletV1R1Config> {

    Options options;

    /**
     * @param options Options
     */
    public WalletV1ContractR1(Options options) {
        this.options = options;
        options.code = CellBuilder.beginCell().fromBoc(WalletCodes.V1R1.getValue()).endCell();
    }

    @Override
    public String getName() {
        return "V1R1";
    }

    @Override
    public Cell createDataCell() {
        CellBuilder cell = CellBuilder.beginCell();
//        cell.storeUint(BigInteger.ZERO, 32); // seqno
        cell.storeBytes(getOptions().publicKey);
        return cell.endCell();
    }

    public Cell createTransferBody(WalletV1R1Config config) {
        Address ownAddress = getAddress();
        CommonMsgInfo internalMsgInfo = InternalMessageInfo.builder()
                .srcAddr(MsgAddressIntStd.builder()
                        .workchainId(ownAddress.wc)
                        .address(ownAddress.toBigInteger())
                        .build())
                .dstAddr(MsgAddressIntStd.builder()
                        .workchainId(config.getDestination().wc)
                        .address(config.getDestination().toBigInteger())
                        .build())
                .value(CurrencyCollection.builder().coins(config.getAmount()).build())
                .createdAt(config.getCreatedAt())
                .build();

        Cell innerMsg = internalMsgInfo.toCell();

        Cell order = Message.builder()
                .info(internalMsgInfo)
                .body(CellBuilder.beginCell()
                        .storeBytes(Utils.signData(getOptions().publicKey, options.getSecretKey(), innerMsg.hash()))
                        .storeRef(innerMsg)
                        .endCell())
                .build().toCell();

        return CellBuilder.beginCell()
//                .storeUint(BigInteger.valueOf(config.getSeqno()), 32)
                .storeUint(config.getMode() & 0xff, 8)
                .storeRef(order)
                .endCell();
    }

    @Override
    public Options getOptions() {
        return options;
    }

    /**
     * Sends amount of nano toncoins to destination address using specified seqno
     *
     * @param tonlib Tonlib
     * @param config WalletV1R1Config
     */
    public ExtMessageInfo sendTonCoins(Tonlib tonlib, WalletV1R1Config config) {
        Cell body = createTransferBody(config);
        Address ownAddress = getAddress();
        Message externalMessage = Message.builder()
                .info(ExternalMessageInfo.builder()
                        .srcAddr(MsgAddressExtNone.builder().build())
                        .dstAddr(MsgAddressIntStd.builder()
                                .workchainId(ownAddress.wc)
                                .address(ownAddress.toBigInteger())
                                .build())
                        .build())
                .init(null)
                .body(CellBuilder.beginCell()
                        .storeBytes(Utils.signData(getOptions().getPublicKey(), options.getSecretKey(), body.hash()))
                        .storeRef(body)
                        .endCell())
                .build();

        return tonlib.sendRawMessage(externalMessage.toCell().toBase64());
    }

    @Override
    public ExtMessageInfo deploy(Tonlib tonlib, WalletV1R1Config config) {
        Address ownAddress = getAddress();

        Cell body = createTransferBody(config);

        Message externalMessage = Message.builder()
                .info(ExternalMessageInfo.builder()
                        .srcAddr(MsgAddressExtNone.builder().build())
                        .dstAddr(MsgAddressIntStd.builder()
                                .workchainId(ownAddress.wc)
                                .address(ownAddress.toBigInteger())
                                .build())
                        .build())
                .init(createStateInit())
                .body(CellBuilder.beginCell()
                        .storeBytes(Utils.signData(getOptions().getPublicKey(), options.getSecretKey(), body.hash()))
                        .storeRef(body) // todo review
                        .endCell())
                .build();

        return tonlib.sendRawMessage(externalMessage.toCell().toBase64());
    }
}
