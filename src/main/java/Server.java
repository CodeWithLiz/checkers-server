import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    int count = 1;
    CopyOnWriteArrayList<ClientThread> clients = new CopyOnWriteArrayList<>();
    ConcurrentHashMap<String, ClientThread> userMap = new ConcurrentHashMap<>();

    private ClientThread player1 = null;
    private ClientThread player2 = null;
    private ClientThread currentTurn = null;
    private int playerCount = 0;

    private boolean p1Ready = false;
    private boolean p2Ready = false;

    TheServer server;
    private Consumer<Serializable> callback;

    Server(Consumer<Serializable> call) {
        this.callback = call;
        server = new TheServer();
        server.start();
    }

    private void sendMatchNotification(ClientThread p, ClientThread opp, String color) {
        Message m = new Message("match_found");
        m.setText(color);
        m.setOpponentName(opp.username);
        p.sendToSelf(m);
    }

    public class TheServer extends Thread {
        public void run() {
            try (ServerSocket mysocket = new ServerSocket(5555)) {
                callback.accept("Server listening on 5555...");
                while (true) {
                    Socket s = mysocket.accept();
                    synchronized (Server.class) {
                        ClientThread c = new ClientThread(s, count++);
                        if (player1 == null) {
                            player1 = c;
                            playerCount = 1;
                            clients.add(c);
                            c.start();
                            callback.accept("P1 connected. Waiting for P2.");
                        } else if (player2 == null) {
                            player2 = c;
                            playerCount = 2;
                            player1.opponent = player2;
                            player2.opponent = player1;
                            clients.add(c);
                            c.start();
                            callback.accept("P2 connected. Waiting for usernames...");
                        } else {
                            s.close();
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    class ClientThread extends Thread {
        Socket connection;
        int count;
        ObjectInputStream in;
        ObjectOutputStream out;
        String username;
        ClientThread opponent;

        ClientThread(Socket s, int count) { this.connection = s; this.count = count; }

        public synchronized void sendToSelf(Message msg) {
            try { out.writeObject(msg); out.reset(); } catch (Exception e) { }
        }

        public void run() {
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                while (true) {
                    Message msg = (Message) in.readObject();
                    handleMessage(msg);
                }
            } catch (Exception e) {
                synchronized (Server.class) {
                    clients.remove(this);
                    if (this == player1) { player1 = null; p1Ready = false; }
                    if (this == player2) { player2 = null; p2Ready = false; }
                    playerCount = Math.max(0, playerCount - 1);
                }
            }
        }

        private void handleMessage(Message msg) {
            String type = msg.getType();
            if (type.equals("set_username")) {
                this.username = msg.getSender();
                userMap.put(username, this);
                Message res = new Message("username_response");
                res.setSuccess(true);
                sendToSelf(res);

                // Trigger Lobby only when BOTH have names
                synchronized (Server.class) {
                    if (player1 != null && player2 != null &&
                            player1.username != null && player2.username != null) {
                        sendMatchNotification(player1, player2, "red");
                        sendMatchNotification(player2, player1, "black");
                    }
                }
            } else if (type.equals("player_ready")) {
                synchronized (Server.class) {
                    if (this == player1) p1Ready = true;
                    if (this == player2) p2Ready = true;
                    if (p1Ready && p2Ready) {
                        currentTurn = player1;
                        Message b1 = new Message("begin_game_now"); b1.setYourTurn(true);
                        Message b2 = new Message("begin_game_now"); b2.setYourTurn(false);
                        player1.sendToSelf(b1);
                        player2.sendToSelf(b2);
                    }
                }
            } else if (type.equals("move")) {
                synchronized (Server.class) {
                    if (currentTurn == this && opponent != null) {
                        sendToSelf(msg);
                        opponent.sendToSelf(msg);
                        currentTurn = opponent;
                        opponent.sendToSelf(new Message("your_turn"));
                        sendToSelf(new Message("wait_turn"));
                    }
                }
            }
        }
    }
}
//public class Server {
//
//	int count = 1;
//	ArrayList<ClientThread> clients = new ArrayList<>();
//	HashMap<String, ClientThread> userMap = new HashMap<>();
//	HashMap<String, ArrayList<String>> groups = new HashMap<>();
//
//	private ClientThread player1 = null;
//	private ClientThread player2 = null;
//	private int playerCount = 0;
//
//	TheServer server;
//	private Consumer<Serializable> callback;
//
//	Server(Consumer<Serializable> call) {
//		callback = call;
//		server = new TheServer();
//		server.start();
//	}
//
//	public class TheServer extends Thread {
//		public void run() {
//			try (ServerSocket mysocket = new ServerSocket(5555)) {
//				callback.accept("Server is waiting for clients!");
//
//				while (true) {
//					ClientThread c = new ClientThread(mysocket.accept(), count);
//					callback.accept("Client connected: client #" + count);
//					clients.add(c);
//					c.start();
//					count++;
//
//					synchronized (Server.class) {
//						playerCount++;
//						if (playerCount == 1) {
//							player1 = c;
//							callback.accept("Player 1 connected. Waiting for Player 2...");
//						} else if (playerCount == 2) {
//							player2 = c;
//							player1.opponent = player2;
//							player2.opponent = player1;
//
//							Message redMsg = new Message("game_start");
//							redMsg.setText("red");
//							player1.sendToSelf(redMsg);
//
//							Message blackMsg = new Message("game_start");
//							blackMsg.setText("black");
//							player2.sendToSelf(blackMsg);
//
//							callback.accept("Both players connected. Game started!");
//						}
//					}
//				}
//			} catch (Exception e) {
//				callback.accept("Server socket did not launch");
//				e.printStackTrace();
//			}
//		}
//	}
//
//	class ClientThread extends Thread {
//
//		Socket connection;
//		int count;
//		ObjectInputStream in;
//		ObjectOutputStream out;
//		String username;
//		ClientThread opponent;
//
//		public synchronized void sendToSelf(Message msg) {
//			try {
//				out.writeObject(msg);
//				out.reset();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		ClientThread(Socket s, int count) {
//			this.connection = s;
//			this.count = count;
//			this.username = null;
//		}
//
//		public void updateClients(Message message) {
//			for (ClientThread t : clients) {
//				try {
//					t.out.writeObject(message);
//					t.out.reset();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		private void handleUsername(Message msg) {
//			try {
//				String requestedName = msg.getSender();
//
//				Message response = new Message("username_response");
//
//				if (userMap.containsKey(requestedName)) {
//					response.setSuccess(false);
//					response.setText("Username already taken.");
//					out.writeObject(response);
//					out.reset();
//				} else {
//					username = requestedName;
//					userMap.put(username, this);
//
//					response.setSuccess(true);
//					response.setText("Username accepted.");
//					out.writeObject(response);
//					out.reset();
//
//					callback.accept(username + " joined the server.");
//
//
//					sendUserListToAll();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		private void sendUserListToAll() {
//			try {
//				ArrayList<String> usernames = new ArrayList<>(userMap.keySet());
//
//				Message userListMsg = new Message("user_list");
//				userListMsg.setUsers(usernames);
//
//				for (ClientThread client : clients) {
//					client.out.writeObject(userListMsg);
//					client.out.reset();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		private void handleGroupCreate(Message msg) {
//			try {
//				String groupName = msg.getGroup();
//				ArrayList<String> members = msg.getUsers();
//
//				Message response;
//
//				if (groupName == null || groupName.trim().isEmpty()) {
//					response = new Message("error");
//					response.setText("Enter a group name.");
//					out.writeObject(response);
//					out.reset();
//				} else if (groups.containsKey(groupName)) {
//					response = new Message("error");
//					response.setText("Group already exists.");
//					out.writeObject(response);
//					out.reset();
//				} else {
//					groups.put(groupName, members);
//
//					response = new Message("group_created");
//					response.setGroupName(groupName);
//					response.setText("Group created successfully.");
//
//					for (String member : members) {
//						ClientThread t = userMap.get(member);
//						if (t != null) {
//							t.out.writeObject(response);
//							t.out.reset();
//						}
//					}
//					callback.accept("Group created: " + groupName + " -> " + members);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//		}
//
//		public void run() {
//
//			try {
//				out = new ObjectOutputStream(connection.getOutputStream());
//				in = new ObjectInputStream(connection.getInputStream());
//				connection.setTcpNoDelay(true);
//
//			} catch (Exception e) {
//				callback.accept("Streams not open");
//				e.printStackTrace();
//			}
//
//			Message joinMsg = new Message("server_notice");
//			joinMsg.setText("New client on server: client #" + count);
//			updateClients(joinMsg);
//
//			while (true) {
//				try {
//					Message msg = (Message) in.readObject();
//
//					//callback.accept("Received: " + msg.toString());
//					if (msg.getType().equals("chat_all")) {
//						callback.accept("[ALL] " + msg.getSender() + ": " + msg.getText());
//					}
//					else if (msg.getType().equals("chat_private")) {
//						callback.accept("[private] " + msg.getSender() + " -> " + msg.getRecipient() + ": " + msg.getText());
//					}
//					else if (msg.getType().equals("chat_group")) {
//						callback.accept("[group " + msg.getGroup() + "] " + msg.getSender() + ": " + msg.getText());
//					}
//
//					if (msg.getType().equals("set_username")) {
//						handleUsername(msg);
//					}
//					else if (msg.getType().equals("chat_all")) {
//						updateClients(msg);
//					}
//					else if (msg.getType().equals("chat_private")) {
//						String target = msg.getRecipient();
//						ClientThread recipientThread = userMap.get(target);
//
//						if (recipientThread != null) {
//							try {
//								recipientThread.out.writeObject(msg);
//								recipientThread.out.reset();
//
//								out.writeObject(msg);
//								out.reset();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}
//					else if (msg.getType().equals("group_create")) {
//						handleGroupCreate(msg);
//					}else if (msg.getType().equals("chat_group")) {
//						String groupName = msg.getGroup();
//						ArrayList<String> members = groups.get(groupName);
//
//						if (members != null) {
//							for (String member : members) {
//								ClientThread t = userMap.get(member);
//								if (t != null) {
//									try {
//										t.out.writeObject(msg);
//										t.out.reset();
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
//								}
//							}
//						}
//					} else if (msg.getType().equals("move")) {
//						callback.accept("Move from " + (username != null ? username : "client #" + count));
//						if (opponent != null) {
//							opponent.sendToSelf(msg);
//						}
//					}
//
//				} catch (Exception e) {
//					callback.accept("Socket error from client #" + count + ", closing connection.");
//					clients.remove(this);
//
//					if (username != null) {
//						userMap.remove(username);
//						updateUserLists();
//					}
//
//					Message leaveMsg = new Message("server_notice");
//					leaveMsg.setText("Client #" + count + " has left the server!");
//					updateClients(leaveMsg);
//					break;
//				}
//			}
//		}
//	}
//
//	private void updateUserLists() {
//		ArrayList<String> names = new ArrayList<>(userMap.keySet());
//
//		Message msg = new Message("user_list");
//		msg.setUsers(names);
//
//		for (ClientThread client : clients) {
//			try {
//				client.out.writeObject(msg);
//				client.out.reset();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
//}