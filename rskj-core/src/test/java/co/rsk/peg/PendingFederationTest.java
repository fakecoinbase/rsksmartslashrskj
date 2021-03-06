/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.peg;

import co.rsk.bitcoinj.core.BtcECKey;
import co.rsk.bitcoinj.core.NetworkParameters;
import co.rsk.crypto.Keccak256;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

@RunWith(PowerMockRunner.class)
public class PendingFederationTest {
    private PendingFederation pendingFederation;

    @Before
    public void createPendingFederation() {
        pendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(100, 200, 300, 400, 500, 600));
    }

    @Test
    public void membersImmutable() {
        boolean exception = false;
        try {
            pendingFederation.getMembers().add(new FederationMember(new BtcECKey(), new ECKey(), new ECKey()));
        } catch (Exception e) {
            exception = true;
        }
        Assert.assertTrue(exception);

        exception = false;
        try {
            pendingFederation.getMembers().remove(0);
        } catch (Exception e) {
            exception = true;
        }
        Assert.assertTrue(exception);
    }
    
    @Test
    public void isComplete() {
        Assert.assertTrue(pendingFederation.isComplete());
    }

    @Test
    public void isComplete_not() {
        PendingFederation otherPendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(200));
        Assert.assertFalse(otherPendingFederation.isComplete());
    }

    @Test
    public void testEquals_basic() {
        Assert.assertTrue(pendingFederation.equals(pendingFederation));

        Assert.assertFalse(pendingFederation.equals(null));
        Assert.assertFalse(pendingFederation.equals(new Object()));
        Assert.assertFalse(pendingFederation.equals("something else"));
    }

    @Test
    public void testEquals_differentNumberOfMembers() {
        PendingFederation otherPendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(100, 200, 300, 400, 500, 600, 700));
        Assert.assertFalse(pendingFederation.equals(otherPendingFederation));
    }

    @Test
    public void testEquals_differentMembers() {
        List<FederationMember> members = FederationTestUtils.getFederationMembersFromPks(100, 200, 300, 400, 500);

        members.add(new FederationMember(BtcECKey.fromPrivate(BigInteger.valueOf(610)), ECKey.fromPrivate(BigInteger.valueOf(600)), ECKey.fromPrivate(BigInteger.valueOf(620))));
        PendingFederation otherPendingFederation = new PendingFederation(members);

        members.remove(members.size()-1);
        members.add(new FederationMember(BtcECKey.fromPrivate(BigInteger.valueOf(600)), ECKey.fromPrivate(BigInteger.valueOf(610)), ECKey.fromPrivate(BigInteger.valueOf(630))));
        PendingFederation yetOtherPendingFederation = new PendingFederation(members);

        Assert.assertFalse(otherPendingFederation.equals(yetOtherPendingFederation));
        Assert.assertFalse(pendingFederation.equals(otherPendingFederation));
        Assert.assertFalse(pendingFederation.equals(yetOtherPendingFederation));
    }

    @Test
    public void testEquals_same() {
        PendingFederation otherPendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(100, 200, 300, 400, 500, 600));
        Assert.assertTrue(pendingFederation.equals(otherPendingFederation));
    }

    @Test
    public void testToString() {
        Assert.assertEquals("6 signatures pending federation (complete)", pendingFederation.toString());
        PendingFederation otherPendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(100));
        Assert.assertEquals("1 signatures pending federation (incomplete)", otherPendingFederation.toString());
    }

    @Test
    public void buildFederation_ok_a() {
        PendingFederation otherPendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(100, 200, 300, 400, 500, 600));

        Federation expectedFederation = new Federation(
                FederationTestUtils.getFederationMembersFromPks(100, 200, 300, 400, 500, 600),
                Instant.ofEpochMilli(1234L),
                0L, NetworkParameters.fromID(NetworkParameters.ID_REGTEST)
        );

        Assert.assertEquals(
                expectedFederation,
                otherPendingFederation.buildFederation(
                        Instant.ofEpochMilli(1234L),
                        0L,
                        NetworkParameters.fromID(NetworkParameters.ID_REGTEST)
                )
        );
    }

    @Test
    public void buildFederation_ok_b() {
        PendingFederation otherPendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(
                100, 200, 300, 400, 500, 600, 700, 800, 900
        ));

        Federation expectedFederation = new Federation(
                FederationTestUtils.getFederationMembersFromPks(100, 200, 300, 400, 500, 600, 700, 800, 900),
                Instant.ofEpochMilli(1234L), 0L,
                NetworkParameters.fromID(NetworkParameters.ID_REGTEST)
        );

        Assert.assertEquals(
                expectedFederation,
                otherPendingFederation.buildFederation(
                        Instant.ofEpochMilli(1234L),
                        0L,
                        NetworkParameters.fromID(NetworkParameters.ID_REGTEST)
                )
        );
    }

    @Test
    public void buildFederation_incomplete() {
        PendingFederation otherPendingFederation = new PendingFederation(FederationTestUtils.getFederationMembersFromPks(100));

        try {
            otherPendingFederation.buildFederation(Instant.ofEpochMilli(12L), 0L, NetworkParameters.fromID(NetworkParameters.ID_REGTEST));
        } catch (Exception e) {
            Assert.assertEquals("PendingFederation is incomplete", e.getMessage());
            return;
        }
        Assert.fail();
    }

    @PrepareForTest({ BridgeSerializationUtils.class })
    @Test
    public void getHash() {
        PowerMockito.mockStatic(BridgeSerializationUtils.class);
        PowerMockito.when(BridgeSerializationUtils.serializePendingFederationOnlyBtcKeys(pendingFederation)).thenReturn(new byte[] { (byte) 0xaa });

        Keccak256 expectedHash = new Keccak256(HashUtil.keccak256(new byte[] { (byte) 0xaa }));

        Assert.assertEquals(expectedHash, pendingFederation.getHash());
    }
}
