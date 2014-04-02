package org.scrabble.client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Container;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.GameState;
import org.game_api.GameApi.Message;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;


public class EmulatorContainer implements Container {

	private static final List<String> integerMapNames = ImmutableList.<String>of(
			"playerIdToNumberOfTokensInPot", "playerIdToTokenChange", "playerIdToScore");

	public static final String PLAYER_ID = "playerId";
	private int noOfPlayers;
	private String updateUiPlayerId = "0";
	private final List<String> playerIds;
	private Game game;
	private GameState gameState = new GameState();
	private GameState lastGameState = null;
	private final List<Map<String, Object>> playersInfo = Lists.newArrayList();
	private List<Operation> lastMove = null;
	private String lastMovePlayerId = "0";

	public int getNoOfPlayers() {
		return noOfPlayers;
	}

	public void setNoOfPlayers(int noOfPlayers) {
		this.noOfPlayers = noOfPlayers;
	}

	public EmulatorContainer(Game game, int noOfplayers){
		this.game = game;
		this.noOfPlayers = noOfplayers;
		List<String> playerIds = Lists.newArrayList();
		for (int i = 0; i < noOfplayers; i++) {
			Integer playerId = 42 + i;
			playerIds.add(playerId.toString());
			playersInfo.add(ImmutableMap.<String, Object>of(PLAYER_ID, playerId));
		}
		this.playerIds = ImmutableList.copyOf(playerIds);
	}

	@Override
	public void sendGameReady() {
	}

	public void updateUi(String yourPlayerId) {
		updateUiPlayerId = yourPlayerId;	
		game.sendUpdateUI(new UpdateUI(yourPlayerId, playersInfo,
				gameState.getStateForPlayerId(yourPlayerId),
				lastGameState == null ? null : lastGameState.getStateForPlayerId(yourPlayerId),
						lastMove, lastMovePlayerId, gameState.getPlayerIdToNumberOfTokensInPot()));
	}

	@Override
	public void sendMakeMove(List<Operation> operations) {
		lastMovePlayerId = updateUiPlayerId;
		lastMove = ImmutableList.copyOf(operations);
		lastGameState = gameState.copy();
		gameState.makeMove(operations);
		// Verify the move on all players
		for (String playerId : playerIds) {
			game.sendVerifyMove(new VerifyMove(playersInfo,
					gameState.getStateForPlayerId(playerId),
					lastGameState.getStateForPlayerId(playerId), lastMove, lastMovePlayerId,
					gameState.getPlayerIdToNumberOfTokensInPot()));
		}
		updateUi(updateUiPlayerId);
	}

	@Override
	public void sendVerifyMoveDone(VerifyMoveDone verifyMoveDone) {
		if (!verifyMoveDone.getHackerPlayerId().equals("0")) {
			throw new RuntimeException("Found a hacker! verifyMoveDone=" + verifyMoveDone);
		}
	}



	public static String getJsonString(Message messageObject) {
		Map<String, Object> messageMap = messageObject.toMessage();
		return getJsonStringFromMap(messageMap);
	}

	public static String getJsonStringFromMap(Map<String, Object> map) {
		return getJsonObject(map).toString();
	}

	public static JSONObject getJsonObject(Map<String, Object> messageMap) {
		JSONObject jsonObj = new JSONObject(); 
		for (Map.Entry<String, Object> entry: messageMap.entrySet()) {
			JSONValue jsonVal = null;
			if (entry.getValue() == null) {
				jsonVal = JSONNull.getInstance();
			}
			else if (entry.getValue() instanceof Boolean) {
				jsonVal = JSONBoolean.getInstance((Boolean)entry.getValue());
			}
			else if (entry.getValue() instanceof Integer) {
				jsonVal = new JSONNumber((Integer)entry.getValue());
			}
			else if (entry.getValue() instanceof String) {
				jsonVal = new JSONString((String)entry.getValue());
			}
			else if (entry.getValue() instanceof List) {
				jsonVal = getJsonArray((List<Object>)entry.getValue());
			}
			else if (entry.getValue() instanceof Map) {
				if (integerMapNames.contains(entry.getKey())) {
					jsonVal = getJsonObjectFromIntegerMap((Map<Integer, Integer>)entry.getValue());
				}
				else {
					jsonVal = getJsonObject((Map<String, Object>)entry.getValue());
				}
			}
			else {
				throw new IllegalStateException("Invalid object encountered");
			}
			jsonObj.put(entry.getKey(), jsonVal);
		}
		return jsonObj;
	}

	public static JSONObject getJsonObjectFromIntegerMap(Map<Integer, Integer> messageMap) {
		JSONObject jsonObj = new JSONObject();
		for (Map.Entry<Integer, Integer> entry: messageMap.entrySet()) {
			jsonObj.put(entry.getKey().toString(), new JSONNumber(entry.getValue()));
		}
		return jsonObj;
	}

	private static JSONArray getJsonArray(List<Object> messageList) {
		JSONArray jsonArr = new JSONArray();
		int index = 0;
		for (Object object: messageList) {
			if (object == null) {
				jsonArr.set(index++, JSONNull.getInstance());
			}
			else if (object instanceof Boolean) {
				jsonArr.set(index++, JSONBoolean.getInstance((Boolean)object));
			}
			else if (object instanceof Integer) {
				jsonArr.set(index++, new JSONNumber((Integer)object));
			}
			else if (object instanceof String) {
				jsonArr.set(index++, new JSONString((String)object));
			}
			else if (object instanceof List) {
				jsonArr.set(index++, getJsonArray((List<Object>)object));
			}
			else if (object instanceof Map) {
				jsonArr.set(index++, getJsonObject((Map<String, Object>)object));
			}
			else {
				throw new IllegalStateException("Invalid object encountered");
			}
		}
		return jsonArr;
	}

	public static Message getMessageObject(String jsonString) {
		return Message.messageToHasEquality(getMapObject(jsonString));
	}

	public static Map<String, Object> getMapObject(String jsonString) {
		JSONValue jsonVal = JSONParser.parseStrict(jsonString);
		JSONObject jsonObj = jsonVal.isObject();
		if (jsonObj == null) {
			throw new IllegalStateException("JSONObject expected");
		}
		return getMapFromJsonObject(jsonObj);
	}

	public static Map<String, Object> getMapFromJsonObject(JSONObject jsonObj) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String key : jsonObj.keySet()) {
			JSONValue jsonVal = jsonObj.get(key); 
			if (jsonVal instanceof JSONNull) {
				map.put(key, null);
			}
			else if (jsonVal instanceof JSONBoolean) {
				map.put(key, (Boolean)((JSONBoolean)jsonVal).booleanValue());
			}
			else if (jsonVal instanceof JSONNumber) {
				map.put(key, new Integer((int)((JSONNumber)jsonVal).doubleValue()));
			}
			else if (jsonVal instanceof JSONString) {
				map.put(key, ((JSONString)jsonVal).stringValue());
			}
			else if (jsonVal instanceof JSONArray) {
				map.put(key, getListFromJsonArray((JSONArray)jsonVal));
			}
			else if (jsonVal instanceof JSONObject) {
				if (integerMapNames.contains(key)) {
					map.put(key, getIntegerMapFromJsonObject((JSONObject)jsonVal));
				}
				else {
					map.put(key, getMapFromJsonObject((JSONObject)jsonVal));
				}
			}
			else {
				throw new IllegalStateException("Invalid JSONValue encountered");
			}
		}
		return map;
	}

	public static Map<Integer, Integer> getIntegerMapFromJsonObject(JSONObject jsonObj) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (String key : jsonObj.keySet()) {
			JSONValue jsonVal = jsonObj.get(key); 
			map.put(Integer.parseInt(key), new Integer((int)((JSONNumber)jsonVal).doubleValue()));
		}
		return map;
	}

	private static Object getListFromJsonArray(JSONArray jsonArr) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < jsonArr.size(); i++) {
			JSONValue jsonVal = jsonArr.get(i);
			if (jsonVal instanceof JSONNull) {
				list.add(null);
			}
			else if (jsonVal instanceof JSONBoolean) {
				list.add((Boolean)((JSONBoolean)jsonVal).booleanValue());
			}
			else if (jsonVal instanceof JSONNumber) {
				list.add(new Integer((int)((JSONNumber)jsonVal).doubleValue()));
			}
			else if (jsonVal instanceof JSONString) {
				list.add(((JSONString)jsonVal).stringValue());
			}
			else if (jsonVal instanceof JSONArray) {
				list.add(getListFromJsonArray((JSONArray)jsonVal));
			}
			else if (jsonVal instanceof JSONObject) {
				list.add(getMapFromJsonObject((JSONObject)jsonVal));
			}
			else {
				throw new IllegalStateException("Invalid JSONValue encountered");
			}
		}
		return list;
	}


}