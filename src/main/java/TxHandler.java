import java.util.ArrayList;

public class TxHandler {

    UTXOPool _utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        _utxoPool = new UTXOPool(utxoPool);
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
        double sumInput = 0;
        double sumOutput = 0;
        ArrayList<UTXO> usedUTXO = new ArrayList<>();

        for (Transaction.Input input : tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = _utxoPool.getTxOutput(utxo);
            if (output == null || usedUTXO.contains(utxo)) {
                return false;
            }

            byte[] message = tx.getRawDataToSign(tx.getInputs().indexOf(input));
            if (!Crypto.verifySignature(output.address, message, input.signature)) {
                return false;
            }

            usedUTXO.add(utxo);
            sumInput += output.value;
        }

        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0 || sumInput < (sumOutput += output.value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> acceptedTxs = new ArrayList<>();

        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                acceptedTxs.add(tx);
                byte[] txHash = tx.getHash();
                for (int i = 0; i < tx.numOutputs(); i++) {
                    UTXO utxo = new UTXO(txHash, i);
                    _utxoPool.addUTXO(utxo, tx.getOutput(i));
                }

                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    _utxoPool.removeUTXO(utxo);
                }
            }
        }

        return acceptedTxs.toArray(new Transaction[0]);
    }

    public UTXOPool getUTXOPool() {
        return _utxoPool;
    }
}
