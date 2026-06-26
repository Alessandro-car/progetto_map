package server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;

import java.io.*;
import java.net.*;
import java.util.List;

@DisplayName("Test di MultiServer")
class MultiServerTest {
	private int port;

	@BeforeEach
	void startServer() throws Exception {
		try (ServerSocket probe = new ServerSocket(0)) {
    	port = probe.getLocalPort();
    }
		Thread serverThread = new Thread(() -> new MultiServer(port));
		serverThread.setDaemon(true);
		serverThread.start();
	}

	private Socket connectWithRetry() throws Exception {
		long deadline = System.currentTimeMillis() + 5000;
		while (true) {
			try {
				return new Socket("localhost", port);
			} catch (IOException e) {
				if (System.currentTimeMillis() > deadline) throw e;
				Thread.sleep(50);
			}
		}
	}

	private Object action3(Socket sock) throws Exception {
		sock.setSoTimeout(5000);
		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
		out.flush();
		ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
		out.writeObject(3);
		out.flush();
		return in.readObject();
  }

	@Test
	@DisplayName("Open a new connection and serves it")
	void serveOneClient() throws Exception {
		try (Socket s = connectWithRetry()) {
			assertEquals("No tree available.", action3(s));
		}
	}

	@Test
	@DisplayName("Serves two simultaneous clients independently")
	void serveTwoClient() throws Exception {
		try (Socket first = connectWithRetry();
				 Socket second = connectWithRetry();
		) {
			assertEquals("No tree available.", action3(first));
			assertEquals("No tree available.", action3(second));
		}
	}
}
