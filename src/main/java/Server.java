import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class Server {

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<>();
	HashMap<String, ClientThread> userMap = new HashMap<>();
	HashMap<String, ArrayList<String>> groups = new HashMap<>();

	TheServer server;
	private Consumer<Serializable> callback;

	Server(Consumer<Serializable> call) {
		callback = call;
		server = new TheServer();
		server.start();
	}

	public class TheServer extends Thread {
		public void run() {
			try (ServerSocket mysocket = new ServerSocket(5555)) {
				callback.accept("Server is waiting for clients!");

				while (true) {
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("Client connected: client #" + count);
					clients.add(c);
					c.start();
					count++;
				}

			} catch (Exception e) {
				callback.accept("Server socket did not launch");
				e.printStackTrace();
			}
		}
	}

	class ClientThread extends Thread {

		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;
		String username;

		ClientThread(Socket s, int count) {
			this.connection = s;
			this.count = count;
			this.username = null;
		}

		public void updateClients(Message message) {
			for (ClientThread t : clients) {
				try {
					t.out.writeObject(message);
					t.out.reset();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void handleUsername(Message msg) {
			try {
				String requestedName = msg.getSender();

				Message response = new Message("username_response");

				if (userMap.containsKey(requestedName)) {
					response.setSuccess(false);
					response.setText("Username already taken.");
					out.writeObject(response);
					out.reset();
				} else {
					username = requestedName;
					userMap.put(username, this);

					response.setSuccess(true);
					response.setText("Username accepted.");
					out.writeObject(response);
					out.reset();

					callback.accept(username + " joined the server.");


					sendUserListToAll();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private void sendUserListToAll() {
			try {
				ArrayList<String> usernames = new ArrayList<>(userMap.keySet());

				Message userListMsg = new Message("user_list");
				userListMsg.setUsers(usernames);

				for (ClientThread client : clients) {
					client.out.writeObject(userListMsg);
					client.out.reset();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void handleGroupCreate(Message msg) {
			try {
				String groupName = msg.getGroup();
				ArrayList<String> members = msg.getUsers();

				Message response;

				if (groupName == null || groupName.trim().isEmpty()) {
					response = new Message("error");
					response.setText("Enter a group name.");
					out.writeObject(response);
					out.reset();
				} else if (groups.containsKey(groupName)) {
					response = new Message("error");
					response.setText("Group already exists.");
					out.writeObject(response);
					out.reset();
				} else {
					groups.put(groupName, members);

					response = new Message("group_created");
					response.setGroupName(groupName);
					response.setText("Group created successfully.");

					for (String member : members) {
						ClientThread t = userMap.get(member);
						if (t != null) {
							t.out.writeObject(response);
							t.out.reset();
						}
					}
					callback.accept("Group created: " + groupName + " -> " + members);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public void run() {

			try {
				out = new ObjectOutputStream(connection.getOutputStream());
				in = new ObjectInputStream(connection.getInputStream());
				connection.setTcpNoDelay(true);

			} catch (Exception e) {
				callback.accept("Streams not open");
				e.printStackTrace();
			}

			Message joinMsg = new Message("server_notice");
			joinMsg.setText("New client on server: client #" + count);
			updateClients(joinMsg);

			while (true) {
				try {
					Message msg = (Message) in.readObject();

					//callback.accept("Received: " + msg.toString());
					if (msg.getType().equals("chat_all")) {
						callback.accept("[ALL] " + msg.getSender() + ": " + msg.getText());
					}
					else if (msg.getType().equals("chat_private")) {
						callback.accept("[private] " + msg.getSender() + " -> " + msg.getRecipient() + ": " + msg.getText());
					}
					else if (msg.getType().equals("chat_group")) {
						callback.accept("[group " + msg.getGroup() + "] " + msg.getSender() + ": " + msg.getText());
					}

					if (msg.getType().equals("set_username")) {
						handleUsername(msg);
					}
					else if (msg.getType().equals("chat_all")) {
						updateClients(msg);
					}
					else if (msg.getType().equals("chat_private")) {
						String target = msg.getRecipient();
						ClientThread recipientThread = userMap.get(target);

						if (recipientThread != null) {
							try {
								recipientThread.out.writeObject(msg);
								recipientThread.out.reset();

								out.writeObject(msg);
								out.reset();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					else if (msg.getType().equals("group_create")) {
						handleGroupCreate(msg);
					}else if (msg.getType().equals("chat_group")) {
						String groupName = msg.getGroup();
						ArrayList<String> members = groups.get(groupName);

						if (members != null) {
							for (String member : members) {
								ClientThread t = userMap.get(member);
								if (t != null) {
									try {
										t.out.writeObject(msg);
										t.out.reset();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}

				} catch (Exception e) {
					callback.accept("Socket error from client #" + count + ", closing connection.");
					clients.remove(this);

					if (username != null) {
						userMap.remove(username);
						updateUserLists();
					}

					Message leaveMsg = new Message("server_notice");
					leaveMsg.setText("Client #" + count + " has left the server!");
					updateClients(leaveMsg);
					break;
				}
			}
		}
	}

	private void updateUserLists() {
		ArrayList<String> names = new ArrayList<>(userMap.keySet());

		Message msg = new Message("user_list");
		msg.setUsers(names);

		for (ClientThread client : clients) {
			try {
				client.out.writeObject(msg);
				client.out.reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}