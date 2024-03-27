# Bitcoin and Cryptocurrency Technologies

## 1. ScroogeCoin

In ScroogeCoin, the central authority Scrooge receives transactions from users. Implemented is 
the logic used by Scrooge to process transactions and produce the ledger. Scrooge
organizes transactions into time periods or blocks. In each block, Scrooge will receive a list of
transactions, validate the transactions he receives, and publish a list of validated transactions.

Note that a transaction can reference another in the same block. Also, among the transactions
received by Scrooge in a single block, more than one transaction may spend the same output. This
would of course be a double-spend, and hence invalid. This means that transactions can’t be validated
in isolation; it is a tricky problem to choose a subset of transactions that are `together` valid.

The provided `Transaction` class represents a ScroogeCoin transaction and has inner classes 
`Transaction.Output` and `Transaction.Input`.

A transaction output consists of a value and a public key to which it is being paid. For the public keys,
we use the built-in Java `PublicKey` class.

A transaction input consists of the hash of the transaction that contains the corresponding output, the
index of this output in that transaction (indices are simply integers starting from 0), and a digital
signature. For the input to be valid, the signature it contains must be a valid signature over the
current transaction with the public key in the spent output.

More specifically, the raw data that is signed is obtained from the `getRawDataToSign(int
index)` method. To verify a signature, use the `verifySignature()` method included in the
provided file `Crypto.java`:

    public static boolean verifySignature(PublicKey pubKey, byte[] message,
    byte[] signature)


This method takes a public key, a message and a signature, and returns true if and only `signature`
correctly verifies over `message` with the public key `pubKey`.

Note that given code can verify signatures. The computation of signatures is done outside
the Transaction class by an entity that knows the appropriate private keys.

A transaction consists of a list of inputs, a list of outputs and a unique ID (see the `getRawTx()`
method). The class also contains methods to add and remove an input, add an output, compute
digests to sign/hash, add a signature to an input, and compute and store the hash of the transaction
once all inputs/outputs/signatures have been added.

The provided `UTXO` class represents an unspent transaction output. A `UTXO` contains the hash of 
the transaction from which it originates as well as its index within that transaction. There are also 
included `equals`, `hashCode`, and `compareTo` functions in `UTXO` that allow the testing of equality
and comparison between two `UTXO`s based on their indices and the contents of their txHash arrays.

Further, the provided `UTXOPool` class represents the current set of outstanding `UTXO`s and contains
a map from each `UTXO` to its corresponding transaction output. This class contains constructors to 
create a new empty `UTXOPool` or a copy of a given `UTXOPool`, and methods to add and remove `UTXO`s 
from the pool, get the output corresponding to a given `UTXO`, check if a `UTXO` is in the pool, and 
get a list of all `UTXO`s in the pool.

Implementation of `handleTxs()` returns a mutually valid transaction set of maximal size
(one that can’t be enlarged simply by adding more transactions). It need not compute a set of
maximum size (one for which there is no larger mutually valid transaction set).

Based on the transactions it has chosen to accept, `handleTxs` will also update its internal
`UTXOPool` to reflect the current set of unspent transaction outputs, so that future calls to
`handleTxs()` and `isValidTx()` are able to correctly process/validate transactions that claim
outputs from transactions that were accepted in a previous call to `handleTxs()`.

Extra implementation called `MaxFeeTxHandler.java` whose `handleTxs()` method is able to find a set 
of transactions with maximum total transaction fees -- i.e. Maximum sum over all transactions in 
the set of (sum of input values - sum of output values).

The `BlockChain` class is responsible for maintaining a blockchain. Since the entire blockchain could be
huge, you should only keep around the most recent blocks. The exact number to store can be arbitrary, 
as long as all the API functions are implemented.

Since there can be (multiple) forks, blocks form a tree rather than a list. The design takes this
into account. An UTXO pool is maintained and corresponding to every block on top of which a new
block might be created.

### Assumptions and hints:

- A new genesis block won’t be mined. If an incomming block claims to be a genesis block
(parent is a null hash) in the `addBlock(Block b)` function, then it should return `false`.
- In the case of multiple blocks at the same height, the oldest block in
`getMaxHeightBlock()` function should be returned.
- We assume for simplicity that a coinbase transaction of a block can be spent in the next
block mined on top of it. (This is contrary to the actual Bitcoin protocol when there is a
“maturity” period of 100 confirmations before it can be spent).
- Only one global Transaction Pool is maintained for the blockchain, and we keep adding transactions to
it on receiving transactions and removing transactions from it if a new block is received or
created. It’s okay if some transactions get dropped during a blockchain reorganization, i.e.,
when a side branch becomes the new longest branch. Specifically, transactions present in the
original main branch (and thus removed from the transaction pool) but absent in the side
branch might get lost.
- The coinbase value is kept constant at 25 bitcoins, whereas in reality, it halves roughly every 4
years.
- When checking for the validity of a newly received block, just checking if the transactions form a
valid set is enough. The set need not be a maximum possible set of transactions. Also, we don’t
need to do any proof-of-work checks.
