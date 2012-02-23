/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.server.impl.rtp;

import java.util.Random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.spi.memory.Frame;

/**
 *
 * kulikov
 */
public class JitterBufferTest {
    
    private WallTestClock wallClock = new WallTestClock();
    private RtpClock rtpClock = new RtpClock(wallClock);

    private int period = 20;
    private int jitter = 40;

    private JitterBuffer jitterBuffer = new JitterBuffer(rtpClock, jitter);

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        jitterBuffer.setFormats(AVProfile.audio);
        rtpClock.setClockRate(8000);
        jitterBuffer.reset();
    }

    @After
    public void tearDown() {
    }


    @Test
    public void testNormalReadWrite() throws Exception {
        RtpPacket[] stream = createStream(100);

        Frame[] media = new Frame[stream.length];
        for (int i = 0; i < stream.length; i++) {
            wallClock.tick(20000000L);
            jitterBuffer.write(stream[i]);
            media[i] = jitterBuffer.read(wallClock.getTime());
        }

        this.checkSequence(media);
        assertEquals(0, 0);
    }

    @Test
    public void testInnerSort() throws Exception {
        RtpPacket p1 = new RtpPacket(172, false);
        p1.wrap(false, 8, 1, 160 * 1, 123, new byte[160], 0, 160);

        RtpPacket p2 = new RtpPacket(172, false);
        p2.wrap(false, 8, 2, 160 * 2, 123, new byte[160], 0, 160);

        RtpPacket p3 = new RtpPacket(172, false);
        p3.wrap(false, 8, 3, 160 * 3, 123, new byte[160], 0, 160);

        RtpPacket p4 = new RtpPacket(172, false);
        p4.wrap(false, 8, 4, 160 * 4, 123, new byte[160], 0, 160);

        RtpPacket p5 = new RtpPacket(172, false);
        p5.wrap(false, 8, 5, 160 * 5, 123, new byte[160], 0, 160);

        jitterBuffer.write(p1);
        jitterBuffer.write(p2);
        jitterBuffer.write(p4);
        jitterBuffer.write(p3);
        jitterBuffer.write(p5);

        Frame buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(1, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(2, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(3, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(4, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(5, buffer.getSequenceNumber());
    }

    @Test
    public void testOutstanding() throws Exception {
        RtpPacket p1 = new RtpPacket(172, false);
        p1.wrap(false, 8, 1, 160 * 1, 123, new byte[160], 0, 160);

        RtpPacket p2 = new RtpPacket(172, false);
        p2.wrap(false, 8, 2, 160 * 2, 123, new byte[160], 0, 160);

        RtpPacket p3 = new RtpPacket(172, false);
        p3.wrap(false, 8, 3, 160 * 3, 123, new byte[160], 0, 160);

        RtpPacket p4 = new RtpPacket(172, false);
        p4.wrap(false, 8, 4, 160 * 4, 123, new byte[160], 0, 160);

        RtpPacket p5 = new RtpPacket(172, false);
        p5.wrap(false, 8, 5, 160 * 5, 123, new byte[160], 0, 160);

        jitterBuffer.write(p1);
        jitterBuffer.write(p3);
        jitterBuffer.write(p5);

        assertEquals(0, jitterBuffer.getDropped());

        //60ms + 40ms
        wallClock.tick(100000000L);

        Frame buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(1, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(3, buffer.getSequenceNumber());

        jitterBuffer.write(p2);
        assertEquals(1, jitterBuffer.getDropped());



//        buffer = jitterBuffer.read(wallClock.getTime());
//        assertEquals(3, buffer.getSequenceNumber());

//        buffer = jitterBuffer.read(wallClock.getTime());
//        assertEquals(null, buffer);

    }

    @Test
    public void testEmpty() throws Exception {
        RtpPacket p1 = new RtpPacket(172, false);
        p1.wrap(false, 8, 1, 160 * 1, 123, new byte[160], 0, 160);

        RtpPacket p2 = new RtpPacket(172, false);
        p2.wrap(false, 8, 2, 160 * 2, 123, new byte[160], 0, 160);

        RtpPacket p3 = new RtpPacket(172, false);
        p3.wrap(false, 8, 3, 160 * 3, 123, new byte[160], 0, 160);

        jitterBuffer.write(p1);
        jitterBuffer.write(p2);
        jitterBuffer.write(p3);

        Frame buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(1, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(2, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(3, buffer.getSequenceNumber());

        buffer = jitterBuffer.read(wallClock.getTime());
        assertEquals(null, buffer);

    }

    @Test
    public void testOverflow() {
        RtpPacket[] stream = createStream(11);
        for (int i = 0; i < stream.length; i++) {
            jitterBuffer.write(stream[i]);
        }

        Frame data = jitterBuffer.read(wallClock.getTime());
        assertEquals(2, data.getSequenceNumber());
    }

    @Test
    public void testJitter() {
        RtpPacket p1 = new RtpPacket(172, false);
        p1.wrap(false, 8, 1, 160 * 1, 123, new byte[160], 0, 160);

        RtpPacket p2 = new RtpPacket(172, false);
        p2.wrap(false, 8, 2, 160 * 2, 123, new byte[160], 0, 160);

        RtpPacket p3 = new RtpPacket(172, false);
        p3.wrap(false, 8, 3, 160 * 3, 123, new byte[160], 0, 160);

        //write first packet, expected jitter = 0
        jitterBuffer.write(p1);
        assertEquals(0, jitterBuffer.getJitter(), 0.1);

        //move time forward on 20ms and write second packet
        //expected jitter = 0;
        wallClock.tick(20000000L);
        jitterBuffer.write(p2);

        assertEquals(0, jitterBuffer.getJitter(), 0.1);

        //move time forward on 30ms and wriate third packet
        //packet was delayed on 10ms and expected jitter equals
        //160/2/16=5ms
        wallClock.tick(30000000L);
        jitterBuffer.write(p3);
        assertEquals(0, jitterBuffer.getJitter(), 0.1);
    }

    private RtpPacket[] createStream(int size) {
        RtpPacket[] stream = new RtpPacket[size];

        int it = 12345;

        for (int i = 0; i < stream.length; i++) {
            stream[i] = new RtpPacket(172, false);
            stream[i].wrap(false, 8, i + 1, 160 * (i+1) + it, 123, new byte[160], 0, 160);
        }
        return stream;
    }

    private void checkSequence(Frame[] media) throws Exception {
        boolean res = true;
        for (int i = 0; i < media.length - 1; i++) {
            if (media[i] ==  null) {
                throw new Exception("Null data at position: " + i);
            }

            if (media[i + 1] ==  null) {
                throw new Exception("Null data at position: " + (i+1));
            }

            res &= (media[i + 1].getSequenceNumber() - media[i].getSequenceNumber() == 1);
        }

        assertTrue("Wrong sequence ", res);
    }

    private void shuffle(RtpPacket[] stream) {
        Random rnd = new Random();
        for (int k = 0; k < 5; k++) {
            int i = rnd.nextInt(stream.length - 1);

            RtpPacket tmp = stream[i];
            stream[i] = stream[i + 1];
            stream[i + 1] = tmp;
        }
    }
}