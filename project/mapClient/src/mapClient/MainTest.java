package mapClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import utility.Keyboard;


/**
 * Client da riga di comando dell'applicazione.
 * <p>
 * Si connette al server tramite socket e, seguendo un menu testuale, permette
 * all'utente di costruire un nuovo albero di regressione a partire da una tabella
 * del database oppure di caricarne uno salvato in precedenza. Una volta pronto
 * l'albero, avvia la fase di predizione interattiva scambiando messaggi con il server.
 */
public class MainTest {

	/**
	 * Avvia il client: apre la connessione con il server, gestisce il menu di scelta
	 * (apprendimento o caricamento dell'albero) e infine la fase di predizione.
	 *
	 * @param args parametri da riga di comando: {@code args[0]} è l'indirizzo del
	 *        server e {@code args[1]} è la porta su cui è in ascolto
	 */
	public static void main(String[] args){

		InetAddress addr;
		try {
			addr = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
			System.out.println(e.toString());
			return;
		}
		Socket socket=null;
		ObjectOutputStream out=null;
		ObjectInputStream in=null;
		try {
			socket = new Socket(args[0], new Integer(args[1]).intValue());
			System.out.println(socket);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());	;

		}  catch (IOException e) {
			System.out.println(e.toString());
			return;
		}




		String answer="";

		int decision=0;
		do{

			System.out.println("Learn Regression Tree from data [1]");
			System.out.println("Load Regression Tree from archive [2]");
			decision=Keyboard.readInt();
		}while(!(decision==1) && !(decision ==2));

		String tableName="";
		try{

		if(decision==1)
		{
			out.writeObject(0);
			do {
				System.out.println("Table name: ");
				tableName = Keyboard.readString();
				out.writeObject(tableName);
				answer = in.readObject().toString();

				System.out.println(answer);
				if (!answer.equals("Table found!")) {
					System.out.println("Wrong table. Try again. \n");
				}
			} while (!answer.equals("Table found!"));

			answer = in.readObject().toString();
			if (!answer.equals("OK")) {
				System.out.println(answer);
				return;
			}

			System.out.println("Starting data acquisition phase!");
			System.out.println("Starting learning phase!");
			out.writeObject(1);


		}
		else
		{
			out.writeObject(2);
			do {
				System.out.println("File name:");
				tableName = Keyboard.readString();
				out.writeObject(tableName);
				answer = in.readObject().toString();
				if (!answer.equals("Table found!")) {
					System.out.println("Wrong table. Try again. \n Table or file name: ");
				}
			} while(!answer.equals("Table found!"));

		}

		answer=in.readObject().toString();
		if(!answer.equals("OK")){
			System.out.println(answer);
			return;
		}




		char risp='y';

		do{
			out.writeObject(3);

			System.out.println("Starting prediction phase!");
			answer=in.readObject().toString();


			while(answer.equals("QUERY")){
				answer=in.readObject().toString();
				if (answer.equals("OK"))
					break;
				System.out.println(answer);
				int path=Keyboard.readInt();
				out.writeObject(path);
				answer=in.readObject().toString();
			}

			if(answer.equals("OK"))
			{
				answer=in.readObject().toString();
				System.out.println("Predicted class:"+answer);

			}
			else
				System.out.println(answer);


			System.out.println("Would you repeat ? (y/n)");
			risp=Keyboard.readChar();
			while(Character.toUpperCase(risp)!='Y' && Character.toUpperCase(risp)!='N'){
				System.out.println("Please type y or n");
				risp=Keyboard.readChar();
			}

		}while (Character.toUpperCase(risp)=='Y');

		}
		catch(IOException | ClassNotFoundException e){
			System.out.println(e.toString());

		}

		try {
			in.close();
			out.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
