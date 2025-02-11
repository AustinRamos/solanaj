package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.serum.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class OrderTest {

    private static final Logger LOGGER = Logger.getLogger(OrderTest.class.getName());
    private final SerumManager serumManager = new SerumManager();
    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    private static final PublicKey SOL_USDC_MARKET_V3 = new PublicKey("9wFFyRfZBsuAha4YcuxcXLKwMxJR43S7fPfQLusDBzvT");

    @Test
    @Ignore
    public void placeOrderTest() {
        LOGGER.info("Placing order");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get SOL/USDC market
        final Market solUsdcMarket = new MarketBuilder()
                .setPublicKey(SOL_USDC_MARKET_V3)
                .build();

        // Get Open Orders account for market - Hardcode this for the test's account - take from explorer
        final Account openOrders = new Account();

        // Place order
        String transactionId = serumManager.placeOrder(account, openOrders, solUsdcMarket, new Order(1, 1, 1));

        // Verify we got a txId
        assertNotNull(transactionId);
    }

    // TODO - fix this
    @Test
    @Ignore
    public void testOpenOrdersAccount() throws IOException {
        String accountData = "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";
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.getValue().setData(List.of(accountData));


        System.out.println(accountInfo.toString());
    }

    @Test
    @Ignore
    public void consumeEventsTest() {
        LOGGER.info("Consuming events");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get SOL/USDC market
        final Market solUsdcMarket = new MarketBuilder()
                .setPublicKey(SOL_USDC_MARKET_V3)
                .setRetrieveOrderBooks(false)
                .build();
        // Place order
        String transactionId = serumManager.consumeEvents(solUsdcMarket, account);

        // Verify we got a txId
        assertNotNull(transactionId);
    }
}
