package org.ton.java.emulator.tx;

import static java.util.Objects.isNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.sun.jna.Native;
import lombok.Builder;
import lombok.extern.java.Log;
import org.ton.java.emulator.EmulateTransactionResult;
import org.ton.java.utils.Utils;

/**
 * If not specified then tries to find emulator in system folder, more info <a
 * href="https://github.com/ton-blockchain/packages">here</a>
 */
@Log
@Builder
public class TxEmulator {

  private String pathToEmulatorSharedLib;
  private final TxEmulatorI txEmulatorI;
  private final long txEmulator;

  private String configBoc;
  private TxVerbosityLevel verbosityLevel;

  public static class TxEmulatorBuilder {}

  public static TxEmulatorBuilder builder() {
    return new CustomEmulatorBuilder();
  }

  private static class CustomEmulatorBuilder extends TxEmulatorBuilder {
    @Override
    public TxEmulator build() {

      if (isNull(super.pathToEmulatorSharedLib)) {
        if ((Utils.getOS() == Utils.OS.WINDOWS) || (Utils.getOS() == Utils.OS.WINDOWS_ARM)) {
          super.pathToEmulatorSharedLib = Utils.detectAbsolutePath("emulator", true);
        } else {
          super.pathToEmulatorSharedLib = Utils.detectAbsolutePath("libemulator", true);
        }
      }

      super.txEmulatorI = Native.load(super.pathToEmulatorSharedLib, TxEmulatorI.class);
      if (isNull(super.verbosityLevel)) {
        super.verbosityLevel = TxVerbosityLevel.WITH_ALL_STACK_VALUES;
      }
      if (isNull(super.configBoc)) {
        throw new Error("Config is not set");
      }

      super.txEmulator =
          super.txEmulatorI.transaction_emulator_create(
              super.configBoc, super.verbosityLevel.ordinal());

      if (super.txEmulator == 0) {
        throw new Error("Can't create tx emulator instance");
      }

      log.info(
          String.format(
              "Java TON Tx Emulator configuration:\n" + "Location: %s\n" + "Verbosity level: %s",
              super.pathToEmulatorSharedLib, super.verbosityLevel));
      return super.build();
    }
  }

  public void destroy() {
    txEmulatorI.transaction_emulator_destroy(txEmulator);
  }

  /**
   * Emulate transaction
   *
   * @param shardAccountBoc Base64 encoded BoC serialized ShardAccount
   * @param messageBoc Base64 encoded BoC serialized inbound Message (internal or external)
   * @return Json object with error: { "success": false, "error": "Error description",
   *     "external_not_accepted": false, // and optional fields "vm_exit_code", "vm_log",
   *     "elapsed_time" in case external message was not accepted. } Or success: { "success": true,
   *     "transaction": "Base64 encoded Transaction boc", "shard_account": "Base64 encoded new
   *     ShardAccount boc", "vm_log": "execute DUP...", "actions": "Base64 encoded compute phase
   *     actions boc (OutList n)", "elapsed_time": 0.02 }
   */
  public EmulateTransactionResult emulateTransaction(String shardAccountBoc, String messageBoc) {
    String result =
        txEmulatorI.transaction_emulator_emulate_transaction(
            txEmulator, shardAccountBoc, messageBoc);
    Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.BIG_DECIMAL).create();
    return gson.fromJson(result, EmulateTransactionResult.class);
  }

  /**
   * Set global verbosity level of the library
   *
   * @param verbosityLevel New verbosity level (0 - never, 1 - error, 2 - warning, 3 - info, 4 -
   *     debug)
   */
  public void setVerbosityLevel(int verbosityLevel) {
    txEmulatorI.emulator_set_verbosity_level(txEmulator, verbosityLevel);
  }

  /**
   * Enable or disable TVM debug primitives
   *
   * @param debugEnabled Whether debug primitives should be enabled or not
   * @return true in case of success, false in case of error
   */
  public boolean setDebugEnabled(boolean debugEnabled) {
    return txEmulatorI.transaction_emulator_set_debug_enabled(txEmulator, debugEnabled);
  }

  /**
   * Set libs for emulation
   *
   * @param libsBoc Base64 encoded BoC serialized shared libraries dictionary (HashmapE 256 ^Cell).
   * @return true in case of success, false in case of error
   */
  public boolean setLibs(String libsBoc) {
    return txEmulatorI.transaction_emulator_set_libs(txEmulator, libsBoc);
  }

  /**
   * Set tuple of previous blocks (13th element of c7)
   *
   * @param infoBoc Base64 encoded BoC serialized TVM tuple (VmStackValue).
   * @return true in case of success, false in case of error
   */
  public boolean setPrevBlockInfo(String infoBoc) {
    return txEmulatorI.transaction_emulator_set_prev_blocks_info(txEmulator, infoBoc);
  }

  /**
   * Set rand seed for emulation
   *
   * @param randSeedHex Hex string of length 64
   * @return true in case of success, false in case of error
   */
  public boolean setRandSeed(String randSeedHex) {
    return txEmulatorI.transaction_emulator_set_rand_seed(txEmulator, randSeedHex);
  }

  /**
   * Set unixtime for emulation
   *
   * @param utime Unix timestamp
   * @return true in case of success, false in case of error
   */
  public boolean setUnixTime(long utime) {
    return txEmulatorI.transaction_emulator_set_unixtime(txEmulator, utime);
  }

  /**
   * Set config for emulation
   *
   * @param configBoc Base64 encoded BoC serialized Config dictionary (Hashmap 32 ^Cell)
   * @return true in case of success, false in case of error
   */
  public boolean setConfig(String configBoc) {
    return txEmulatorI.transaction_emulator_set_config(txEmulator, configBoc);
  }

  /**
   * Creates Config object from base64 encoded BoC
   *
   * @param configBoc Base64 encoded BoC serialized Config dictionary (Hashmap 32 ^Cell)
   * @return Pointer to Config object or nullptr in case of error
   */
  public long createConfig(String configBoc) {
    return txEmulatorI.emulator_config_create(configBoc);
  }

  /**
   * Destroy Config object
   *
   * @param config Pointer to Config object
   */
  public void destroyConfig(long config) {
    txEmulatorI.emulator_config_destroy(config);
  }

  /**
   * Emulate tick-tock transaction
   *
   * @param shardAccountBoc Base64 encoded BoC serialized ShardAccount of special account
   * @param isTock True for tock transactions, false for tick
   * @return Json object with error: { "success": false, "error": "Error description",
   *     "external_not_accepted": false } Or success: { "success": true, "transaction": "Base64
   *     encoded Transaction boc", "shard_account": "Base64 encoded new ShardAccount boc", "vm_log":
   *     "execute DUP...", "actions": "Base64 encoded compute phase actions boc (OutList n)",
   *     "elapsed_time": 0.02 }
   */
  public EmulateTransactionResult emulateTickTockTransaction(
      String shardAccountBoc, boolean isTock) {
    String result =
        txEmulatorI.transaction_emulator_emulate_tick_tock_transaction(
            txEmulator, shardAccountBoc, isTock);
    Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.BIG_DECIMAL).create();
    return gson.fromJson(result, EmulateTransactionResult.class);
  }

  /**
   * Set lt for emulation
   *
   * @param lt Logical time
   * @return true in case of success, false in case of error
   */
  public boolean setEmulatorLt(long lt) {
    return txEmulatorI.transaction_emulator_set_lt(txEmulator, lt);
  }

  /**
   * Set ignore_chksig flag for emulation
   *
   * @param ignoreChksig Whether emulation should always succeed on CHKSIG operation
   * @return true in case of success, false in case of error
   */
  public boolean setIgnoreCheckSignature(boolean ignoreChksig) {
    return txEmulatorI.transaction_emulator_set_ignore_chksig(txEmulator, ignoreChksig);
  }
}