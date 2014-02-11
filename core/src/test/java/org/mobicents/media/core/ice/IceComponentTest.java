package org.mobicents.media.core.ice;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IceComponentTest {

	List<LocalCandidateWrapper> localCandidates = new ArrayList<LocalCandidateWrapper>();

	@Before
	public void before() throws IOException {
		localCandidates.add(buildCandidate("192.168.1.65", 61000, 1));
		localCandidates.add(buildCandidate("192.168.1.65", 61001, 2));
		localCandidates.add(buildCandidate("192.168.1.65", 61003, 3));
		Collections.sort(this.localCandidates);
	}

	@After
	public void after() {
		clearLocalCandidates();
	}

	@Test
	public void testDefaultLocalCandidateSelection() {
		// given
		IceComponent component = new IceComponent(IceComponent.RTP_ID);
		component.addLocalCandidates(this.localCandidates);

		// when
		LocalCandidateWrapper defaultCandidate = component
				.selectDefaultLocalCandidate();

		// then
		assertEquals(this.localCandidates.get(0), defaultCandidate);
	}

	private LocalCandidateWrapper buildCandidate(String address, int port,
			int priority) throws IOException {
		IceComponent rtpComponent = new IceComponent(IceComponent.RTP_ID);
		IceCandidate candidate = new HostCandidate(rtpComponent, address, port);
		DatagramChannel channel = DatagramChannel.open();
		candidate.setPriority(priority);
		return new LocalCandidateWrapper(candidate, channel);
	}

	private void clearLocalCandidates() {
		for (LocalCandidateWrapper candidateWrapper : this.localCandidates) {
			try {
				DatagramChannel udpChannel = candidateWrapper.getUdpChannel();
				if (udpChannel.isConnected()) {
					udpChannel.disconnect();
				}
				if (udpChannel.isOpen()) {
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
		this.localCandidates.clear();
	}
}