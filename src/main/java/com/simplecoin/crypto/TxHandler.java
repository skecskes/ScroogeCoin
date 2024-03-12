package com.simplecoin.crypto;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool
        if (tx.getInputs().stream().anyMatch(input -> {
            var utxo = new UTXO(input.prevTxHash, input.outputIndex);
            return !utxoPool.contains(utxo);
        })) {
            return false;
        }

        // (2) the signatures on each input of {@code tx} are valid
        if (tx.getInputs().stream().anyMatch(input -> {
            var utxo = new UTXO(input.prevTxHash, input.outputIndex);
            var pubKey = utxoPool.getTxOutput(utxo).address;
            var message = tx.getRawDataToSign(input.outputIndex);
            return !Crypto.verifySignature(pubKey, message, input.signature);
        })) {
            return false;
        }

        // (3) no UTXO is claimed multiple times by {@code tx}
        if (tx.getInputs().stream().anyMatch(input -> {
            var utxo = new UTXO(input.prevTxHash, input.outputIndex);
            return utxoPool.getAllUTXO().stream().filter(utxo::equals).count() > 1;
        })) {
            return false;
        }

        // (4) all of {@code tx}s output values are non-negative
        if (tx.getOutputs().stream().anyMatch(output -> output.value < 0)) {
            return false;
        }

        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        var inputSum = tx.getInputs().stream().mapToDouble(input -> {
            var utxo = new UTXO(input.prevTxHash, input.outputIndex);
            return utxoPool.getTxOutput(utxo).value;
        }).sum();
        var outputSum = tx.getOutputs().stream().mapToDouble(output -> output.value).sum();
        if (inputSum < outputSum) {
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        return null;
    }

}
