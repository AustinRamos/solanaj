package org.p2p.solanaj.serum;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

/**
 * Builds a {@link Market} object, which can have polled data including bid/ask {@link OrderBook}s
 */
public class MarketBuilder {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    private PublicKey publicKey;
    private boolean retrieveOrderbooks = false;
    private static final Logger LOGGER = Logger.getLogger(MarketBuilder.class.getName());

    // TODO move all publickey consts to it's own static class
    private static final PublicKey WRAPPED_SOL_MINT = new PublicKey("So11111111111111111111111111111111111111112");

    public MarketBuilder setRetrieveOrderBooks(boolean retrieveOrderbooks) {
        this.retrieveOrderbooks = retrieveOrderbooks;
        return this;
    }

    public boolean isRetrieveOrderbooks() {
        return retrieveOrderbooks;
    }

    public Market build() {
        Market market = new Market();

        // Get account Info
        byte[] base64AccountInfo = retrieveAccountData();

        // Read market
        if (base64AccountInfo == null) {
            throw new RuntimeException("Unable to read account data");
        }

        market = Market.readMarket(base64AccountInfo);

        // Get Order books
        if (retrieveOrderbooks) {
            // TODO - multi-thread these
            // Data from the order books
            byte[] base64BidOrderbook = retrieveAccountData(market.getBids());
            byte[] base64AskOrderbook = retrieveAccountData(market.getAsks());

            OrderBook bidOrderBook = OrderBook.readOrderBook(base64BidOrderbook);
            OrderBook askOrderBook = OrderBook.readOrderBook(base64AskOrderbook);

            market.setBidOrderBook(bidOrderBook);
            market.setAskOrderBook(askOrderBook);

            // Data from the token mints
            // TODO - multi-thread these
            byte baseDecimals = getMintDecimals(market.getBaseMint());
            byte quoteDecimals = getMintDecimals(market.getQuoteMint());

            LOGGER.info(String.format("Base decimals = %d", baseDecimals));
            LOGGER.info(String.format("Quote decimals = %d", quoteDecimals));

            market.setBaseDecimals(baseDecimals);
            market.setQuoteDecimals(quoteDecimals);
        }

        return market;
    }

    /**
     * Retrieves decimals for a given Token Mint's {@link PublicKey} from Solana account data.
     * @param tokenMint
     * @return
     */
    private byte getMintDecimals(PublicKey tokenMint) {
        if (tokenMint.equals(WRAPPED_SOL_MINT)) {
            return 9;
        }

        // RPC call to get mint's account data into decoded bytes (already base64 decoded)
        byte[] accountData = retrieveAccountData(tokenMint);

        // Deserialize accountData into the MINT_LAYOUT enum
        byte decimals = SerumUtils.readDecimalsFromTokenMintData(accountData);

        return decimals;
    }

    private byte[] retrieveAccountData() {
        return retrieveAccountData(publicKey);
    }

    private byte[] retrieveAccountData(PublicKey publicKey) {
        AccountInfo orderBook = null;

        try {
            orderBook = client.getApi().getAccountInfo(publicKey);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        final List<String> accountData = orderBook.getValue().getData();

        return Base64.getDecoder().decode(accountData.get(0));
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public MarketBuilder setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }
}
