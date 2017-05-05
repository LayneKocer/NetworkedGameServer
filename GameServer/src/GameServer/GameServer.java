package GameServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/GameServer")
public class GameServer {

	private static final List<GameServer> connections = new ArrayList<>();
	private Session session;

	public static GameState game;
	public static Timer timer;
	
	public GameServer() {
		System.out.println("The Constructor was called");
	}
	
	private void SendUpdate() {
		// TODO Auto-generated method stub
		if(this.game == null){
			System.out.println("Game Not Initialized");
			return;
		}
		String message = this.game.GetGameStateMessage();
		System.out.println("Sending Massage: " + message);
		for(GameServer client : connections){
			try {
				client.session.getBasicRemote().sendText(message);
			} catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	@OnOpen
	public void onOpen(Session session){
		if(this.game == null){
			this.game = new GameState();
			this.timer = new Timer();
			this.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if(game.playStates.size() != 0){
						SendUpdate();
					}
				}
			}, 0, 100);
			this.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					for(GameServer gs : connections){
						if(!gs.session.isOpen()){
							connections.remove(gs);
						}
					}
					if(connections.size() == 0){
						game.playStates.clear();
					}
				}
			}, 0, 1000);
		}
		this.session = session;
		this.connections.add(this);
		System.out.println(session.getId() + " has opened a session");
	}

	@OnMessage
	public void onMessage(String message, Session session){
		System.out.println(session.getId() + " Sent Message " + message);
		this.game.HandleMessage(message);
	}

	@OnClose
	public void onClose(Session session){
		System.out.println(session.getId() + " Closed Session");
		for(GameServer gs : connections){
			if(gs.session.getId().equals(session.getId())){
				System.out.println(session.getId() + " Was Removed");
				connections.remove(gs);
			}
		}
		if(connections.size() == 0){
			this.game.playStates.clear();
		}
	}

//	@OnError
//	public void onError(Session session, Throwable t){}

}
