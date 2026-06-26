package server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;

import java.io.*;
import java.net.*;
import java.util.List;

@DisplayName("Test di ServerOneClient")
class ServerOneClientTest {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	@BeforeEach
	void init() throws Exception {
		serverSocket = new ServerSocket(8080);
		Thread serverThread = new Thread(() -> {
			try {
				Socket accepted = serverSocket.accept();
				new ServerOneClient(accepted);
			} catch (IOException ignored) {}
		});

		serverThread.setDaemon(true);
		serverThread.start();

		clientSocket = new Socket("localhost", 8080);
		clientSocket.setSoTimeout(5000);
		out = new ObjectOutputStream(clientSocket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(clientSocket.getInputStream());
	}

	@AfterEach
	void close() {
		try {
			if (in != null) in.close();
		} catch (IOException ignored) {}
    try {
			if (out != null) out.close();
		} catch (IOException ignored) {}
    try {
			if (clientSocket != null) clientSocket.close();
		} catch (IOException ignored) {}
    try {
			if (serverSocket != null) serverSocket.close();
		} catch (IOException ignored) {}
	}

	@Test
	@DisplayName("Test case 0 with wrong table name")
	void case0withWrongTableName() throws Exception {
		out.writeObject(0);
		out.writeObject("doesntexists");
		out.flush();
		Object answer = in.readObject();
		assertEquals("No such table!", answer, "Case 0 with wrong table name gives error message.");
	}

	@Test
	@DisplayName("Test case 0 with right table name")
	void case0withRightTableName() throws Exception {
		out.writeObject(0);
		out.writeObject("servo");
		out.flush();
		Object answer = in.readObject();
		assertEquals("Table found!", answer, "Case 0 with right table name gives table found message");
	}

	@Test
	@DisplayName("Test case 1 without training set")
	void case1withoutTrainingSet() throws Exception {
		out.writeObject(1);
		out.flush();
		Object answer = in.readObject();
		assertEquals("No training data loaded.", answer, "Case 1 without training set gives error message.");
	}

	@Test
	@DisplayName("Test case 1 with training set")
	void case1withTrainingSet() throws Exception {
		String tableName = "servo";
		File dmp = new File(tableName + ".dmp");
		try {
			out.writeObject(0);
			out.writeObject(tableName);
			out.flush();
			assertEquals("Table found!", in.readObject());
			assertEquals("OK", in.readObject());

			out.writeObject(1);
			out.flush();
			assertEquals("OK", in.readObject());
			assertTrue(dmp.exists(), "The .dmp file must have been created!");
		} finally {
			dmp.delete();
		}
	}

	@Test
	@DisplayName("Test case 2 with wrong file name")
	void case2withWrongFileName() throws Exception {
		String tableName = "doesntexists";
		out.writeObject(2);
		out.writeObject(tableName);
		assertEquals("The table " + tableName + " doesn't exist!", in.readObject());
	}

	@Test
	@DisplayName("Test case 2 with right file name")
	void case2withRightFileName() throws Exception {
		String tableName = "servo";
		File dmp = new File(tableName + ".dmp");
		try {
			out.writeObject(0);
			out.writeObject(tableName);
			out.flush();
			assertEquals("Table found!", in.readObject());
			assertEquals("OK", in.readObject());

			out.writeObject(1);
			out.flush();
			assertEquals("OK", in.readObject());
			assertTrue(dmp.exists());

			out.writeObject(2);
			out.writeObject(tableName);
			out.flush();
			assertEquals("Table found!", in.readObject());
			assertEquals("OK", in.readObject());
    } finally {
    	dmp.delete();
    }
	}

	@Test
	@DisplayName("Test case 3 without tree")
	void case3withoutTreeReturnsError() throws Exception {
		out.writeObject(3);
		out.flush();

		Object answer = in.readObject();

		assertEquals("No tree available.", answer, "Case 3 with tree == null return error message.");
	}

	@Test
	@DisplayName("Test case 3 with tree")
	void case3withTree() throws Exception {
		String tableName = "servo";
		File dmp = new File(tableName + ".dmp");
		try {
			out.writeObject(0);
			out.writeObject(tableName);
			out.flush();
			assertEquals("Table found!", in.readObject());
			assertEquals("OK", in.readObject());

			out.writeObject(1);
			out.flush();
			assertEquals("OK", in.readObject());
			assertTrue(dmp.exists());

			out.writeObject(3);
			assertEquals("QUERY", in.readObject());
		} finally {
			dmp.delete();
		}
	}

	@Test
	@DisplayName("Test case 4")
	void case4ListDbTables() throws Exception {
		out.writeObject(4);
		out.flush();
		 Object answer = in.readObject();
		 assertInstanceOf(List.class, answer, "Case 4 must return a list of tables");
	}

	@Test
	@DisplayName("Test case 5")
	void case5ListFileNames() throws Exception {
		String test = "test_case_5_" + System.nanoTime();
		File dmp = new File(test + ".dmp");
		assertTrue(dmp.createNewFile());
		try {
			out.writeObject(5);
			out.flush();

			Object answer = in.readObject();
			assertInstanceOf(List.class, answer);
			List<?> files = (List<?>) answer;
			assertTrue(files.contains(test));
		} finally {
			dmp.delete();
		}
	}
}

