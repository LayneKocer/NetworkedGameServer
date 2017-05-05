package GameServer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GameState {

	public List<PlayerState> playStates;
	
	public GameState(){
		playStates = new ArrayList<>();
	}
	
	// Update the state of Players on the server side
	public synchronized void HandleMessage(String message){
		JSONParser parser = new JSONParser();
		String type,msg;
		try{
			JSONObject obj = (JSONObject) parser.parse(message);
			type = obj.get("type").toString();
			msg = obj.get("msg").toString();
			switch(type){
				case "connect":
					handleConnect(msg);
					break;
				case "close":
					handleClose(msg);
					break;
				case "disconnect":
					handleDisconnect(msg);
					break;
				default:
					System.out.println("Invalid Request");
			}
		}catch(Exception pe){
			System.out.println("Bad String");
			System.out.println(pe);
		}
	}
	
	private void handleConnect(String msg) {
		JSONParser parser = new JSONParser();
		String playerID;
		float posx;
		float posy;
		Instant lastUpdate;

		try{
			JSONObject obj = (JSONObject)parser.parse(msg);
			playerID = obj.get("user").toString();
			posx = Float.parseFloat(obj.get("posx").toString());
			posy = Float.parseFloat(obj.get("posy").toString());
//			System.out.println("POSX: " + posx+ " POSY: " + posy);
			lastUpdate = Instant.parse(obj.get("lastUpdate").toString());
			boolean inStates = false;
			for(PlayerState ps : playStates){
				if(ps.id.equals(playerID)){
//					System.out.println("Update User: " + playerID);
					inStates = true;
					ps.posx = posx;
					ps.posy = posy;
					ps.time = Instant.now();
				}
			}
			if(!inStates){
//				System.out.println("Create User: " + playerID);
				playStates.add(new PlayerState(playerID,posx,posy,Instant.now()));
			}
			
		} catch(Exception pe){
			System.out.println("Failed!");
			System.out.println(pe);
		}
	}

	private void handleClose(String msg) {
		JSONParser parser = new JSONParser();
		String playerID;
		float posx = 0;
		float posy = 0;
		Instant lastUpdate;

		try{
			JSONObject obj = (JSONObject)parser.parse(msg);
			playerID = obj.get("user").toString();
			lastUpdate = Instant.parse(obj.get("lastUpdate").toString());
			boolean inStates = false;
			for(PlayerState ps : playStates){
				if(ps.id.equals(playerID)){
					playStates.remove(ps);
				}
			}
			
		} catch(Exception pe){
			System.out.println("Failed!");
			System.out.println(pe);
		}
	}

	private void handleDisconnect(String msg) {
		JSONParser parser = new JSONParser();
		String playerID;
		float posx;
		float posy;
		Instant lastUpdate;
		try{
			JSONObject obj = (JSONObject)parser.parse(msg);
			playerID = obj.get("user").toString();
			posx = Float.parseFloat(obj.get("posx").toString());
			posy = Float.parseFloat(obj.get("posy").toString());
			lastUpdate = Instant.parse(obj.get("lastUpdate").toString());
			boolean inStates = false;
			for(PlayerState ps : playStates){
				if(ps.id.equals(playerID)){
					inStates = true;
					playStates.remove(ps);
				}
			}
		} catch(Exception pe){
			System.out.println("Failed!");
			System.out.println(pe);
		}
	}
	
	public String GetGameStateMessage(){
		String message = "{\"updateType\":\"refresh\",\"msg\": [";
		for(int i=0; i<playStates.size(); i++){
			message += playStates.get(i).toString();
			if(i != playStates.size()-1){
				message += ",";
			}
		}
		message += "],\"updateTime\":\""+Instant.now()+"\"}";
		return message;
	}

	public class PlayerState {
		public String id;
		public float posx;
		public float posy;
		public Instant time;
		
		public PlayerState(String id, float posx, float posy, Instant time){
			this.id = id;
			this.posx = posx;
			this.posy = posy;
			this.time = time;
		}

		public String toString(){
			return "{\"user\": \""+id+"\",\"posx\":"+(Math.round(posx*100.0)/100.0)+",\"posy\":"+(Math.round(posy*100.0)/100.0)+"}";
		}
	}
}
