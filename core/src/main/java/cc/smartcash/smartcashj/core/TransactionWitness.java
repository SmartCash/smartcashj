/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.smartcash.smartcashj.core;

import cc.smartcash.smartcashj.crypto.TransactionSignature;
import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionWitness {
    public static final TransactionWitness EMPTY = new TransactionWitness(0);

    /**
     * Creates the stack pushes necessary to redeem a P2WPKH output. If given signature is null, an empty push will be
     * used as a placeholder.
     */
    public static TransactionWitness redeemP2WPKH(@Nullable TransactionSignature signature, ECKey pubKey) {
        checkArgument(pubKey.isCompressed(), "only compressed keys allowed");
        TransactionWitness witness = new TransactionWitness(2);
        witness.setPush(0, signature != null ? signature.encodeToBitcoin() : new byte[0]); // signature
        witness.setPush(1, pubKey.getPubKey()); // pubkey
        return witness;
    }

    private final byte[][] pushes;

    public TransactionWitness(int pushCount) {
        pushes = new byte[pushCount][];
    }

    public byte[] getPush(int i) {
        return pushes[i];
    }

    public int getPushCount() {
        return pushes.length;
    }

    public void setPush(int i, byte[] value) {
        pushes[i] = value;
    }

    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(pushes.length).encode());
        for (int i = 0; i < pushes.length; i++) {
            byte[] push = pushes[i];
            stream.write(new VarInt(push.length).encode());
            stream.write(push);
        }
    }

    @Override
    public String toString() {
        List<String> stringPushes = new ArrayList<>();
        for (int j = 0; j < this.getPushCount(); j++) {
            byte[] push = this.getPush(j);
            if (push != null) {
                stringPushes.add(Utils.HEX.encode(push));
            } else {
                stringPushes.add("NULL");
            }
        }
        return Utils.SPACE_JOINER.join(stringPushes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionWitness other = (TransactionWitness) o;
        return Arrays.deepEquals(pushes, other.pushes);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(pushes);
    }
}
