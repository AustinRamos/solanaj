package org.p2p.solanaj.naming;

import org.jetbrains.annotations.Nullable;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class NamingManager {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    private static final Logger LOGGER = Logger.getLogger(NamingManager.class.getName());
    private static final long DATA_LENGTH = 1000L;
    private static final String HASH_PREFIX = "SPL Name Service";
    private static final PublicKey NAME_PROGRAM_ID = new PublicKey("Gh9eN9nDuS3ysmAkKf4QJ6yBzf3YNqsn6MD8Ms3TsXmA");

    /**
     * Creates a .sol domain name with the specified name and payer.
     *
     * @param name domain name
     * @param payer account paying for this transaction
     * @param nameOwner pubkey to associate this domain with
     * @param nameClass "The class of this new name"
     * @param parentName "The parent name of the new name. If specified its owner needs to sign"
     * @return true if domain name creation succeeded.
     */
    public boolean createNameRegistry(final String name,
                                     final Account payer,
                                     final PublicKey nameOwner,
                                     final PublicKey nameClass,
                                     final PublicKey parentName) {

        String fullDomainName = HASH_PREFIX + name;
        byte[] hashedName = getHashedName(fullDomainName);
        LOGGER.info(String.format("Name = %s, Sha256 = %s", fullDomainName, ByteUtils.bytesToHex(hashedName)).toLowerCase());

        PublicKey nameAccountKey = getNameAccountKey(hashedName, nameClass, parentName);

        long minimumBalanceForRentExemption = getMinimumBalanceForRentExemption();
        LOGGER.info(String.format("minimumBalanceForRentExemption = %d", minimumBalanceForRentExemption));

        return true;
    }

    private PublicKey getNameAccountKey(byte[] hashedName, @Nullable PublicKey nameClass, @Nullable PublicKey parentName) {
        // Convert hashedName to a ByteBuffer.
        ByteBuffer seeds = ByteBuffer.wrap(hashedName);
        seeds.order(ByteOrder.LITTLE_ENDIAN);

        if (nameClass != null) {
            byte[] nameClassBytes = toBuffer(nameClass);
            seeds.put(nameClassBytes);
        } else {
            allocateBytes(seeds, 32);
        }

        if (parentName != null) {
            byte[] parentClassBytes = toBuffer(parentName);
            seeds.put(parentClassBytes);
        } else {
           allocateBytes(seeds, 32);
        }

        /*
          let [nameAccountKey, _] = await PublicKey.findProgramAddress(
              seeds,
              NAME_PROGRAM_ID
            );
            return nameAccountKey
         */

        return null;
    }

    private void allocateBytes(ByteBuffer buffer, int amount) {
        buffer.put(new byte[amount]);
    }

    private byte[] toBuffer(PublicKey nameClass) {
        /*
          toBuffer(): Buffer {
              const a = super.toArray().reverse();
              const b = Buffer.from(a);
              if (b.length === 4) {
                return b;
              }
              assert(b.length < 4, "Numberu32 too large");

              const zeroPad = Buffer.alloc(4);
              b.copy(zeroPad);
              return zeroPad;
            }
         */
        ByteBuffer buffer = ByteBuffer.wrap(nameClass.toByteArray());
        reverseByteBuffer(buffer);

        ByteBuffer resultBuffer = buffer.duplicate();

        if (resultBuffer.array().length == 4) {
            return resultBuffer.array();
        }

        if (resultBuffer.array().length < 4) {
            throw new RuntimeException("Resulting buffer is too large");
        }

        ByteBuffer zeroPad = ByteBuffer.allocate(4);
        zeroPad.put(resultBuffer.array());

        return zeroPad.array();
    }

    /**
     * Sha-256 the prefix + name, into byte array
     * @param input domain name
     * @return byte array of sha256 hashed string
     */
    private byte[] getHashedName(String input) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Calculate rent exemption used in domain name creation
     */
    private long getMinimumBalanceForRentExemption() {
        try {
            return client.getApi().getMinimumBalanceForRentExemption(DATA_LENGTH);
        } catch (RpcException e) {
            e.printStackTrace();
        }
        return DATA_LENGTH * 10;
    }

    /**
     * Reverse a {@link ByteBuffer}
     * @param buffer existing ByteBuffer to reverse
     */
    private void reverseByteBuffer(ByteBuffer buffer) {
        for (int i = 0; i < buffer.array().length; i++) {
            byte leftByte = buffer.get(i);
            byte rightByte = buffer.get(buffer.array().length - i);
            buffer.put(i, rightByte);
            buffer.put(buffer.array().length - i, leftByte);
        }
    }
}
