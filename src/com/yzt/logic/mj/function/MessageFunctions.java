package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Action;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.BackFileUtil;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.MahjongUtils;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

/**
 * Created by Administrator on 2017/7/10. 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

	/**
	 * 发送玩家信息 (断线重连)
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100100(WSClient channel, Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, Object> info = new HashMap<>();
		if (interfaceId.equals(100100)) {// 刚进入游戏主动请求
			String openId = String.valueOf(readData.get("openId"));
			Player currentPlayer = null;
			String cid = null;
			if (openId == null) {
				illegalRequest(interfaceId, channel);
				return;
			} else {
				String ip = String.valueOf(channel.getIp());
				cid = String.valueOf(readData.get("cId"));
				currentPlayer = HallFunctions.getPlayerInfos(openId, ip, cid, channel);// 断线重连后的数据
			}
			if (currentPlayer == null) {
				illegalRequest(interfaceId, channel);
				return;
			}

			// 更新心跳为最新上线时间
			if (cid != null) {
				currentPlayer.setCid(cid);
			}
			currentPlayer.setChannelId(channel.getId());// 更新channelId
			channel.setUserId(currentPlayer.getUserId());
			channel.setCid(cid);
			currentPlayer.setState(Cnst.PLAYER_LINE_STATE_INLINE);
			if (openId != null) {
				RedisUtil.setObject(Cnst.get_REDIS_PREFIX_OPENIDUSERMAP(cid).concat(openId), currentPlayer.getUserId(), null);
			}

			RoomResp room = null;
			List<Player> players = null;

			if (currentPlayer.getRoomId() != null) {// 玩家下有roomId，证明在房间中
				room = RedisUtil.getRoomRespByRoomId(String.valueOf(currentPlayer.getRoomId()), cid);
				if (room != null && room.getState() != Cnst.ROOM_STATE_YJS) {
					if (room.getState() == Cnst.ROOM_STATE_XJS) {
						currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_IN);
//						currentPlayer.initPlayer(room.getRoomId(), Cnst.PLAYER_STATE_IN, currentPlayer.getScore());
					}
					info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
					info.put("roomInfo", getRoomInfo(room, currentPlayer));
					players = RedisUtil.getPlayerList(room, cid);
					for (int m = 0; m < players.size(); m++) {
						Player p = players.get(m);
						if (p.getUserId().equals(currentPlayer.getUserId())) {
							players.remove(m);
							break;
						}
					}

					info.put("anotherUsers", getAnotherUserInfo(players, room));
					MessageFunctions.interface_100109(players, Cnst.PLAYER_LINE_STATE_INLINE, currentPlayer.getUserId());
					if (room.getRoomType() == Cnst.ROOM_TYPE_2 && !currentPlayer.getUserId().equals(room.getCreateId())) {
						MessageFunctions.interface_100112(currentPlayer, room, Cnst.PLAYER_EXTRATYPE_SHANGXIAN, cid);
					}
				} else {
					currentPlayer.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
				}

			} else {
				currentPlayer.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
			}

			RedisUtil.updateRedisData(room, currentPlayer, cid);
			info.put("currentUser", getCurrentUserInfo(currentPlayer, room));

			if (room != null) {

				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
				Map<String, Object> roomInfo = (Map<String, Object>) info.get("roomInfo");
				List<Map<String, Object>> anotherUsers = (List<Map<String, Object>>) info.get("anotherUsers");

				info.remove("roomInfo");
				info.remove("anotherUsers");

				JSONObject result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());

				info.remove("currentUser");
				info.put("roomInfo", roomInfo);
				result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());

				info.remove("roomInfo");
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());

			} else {
				JSONObject result = getJSONObj(interfaceId, 1, info);
				MessageUtils.sendMessage(channel, result.toJSONString());
			}

		} else {
			channel.getChannelHandlerContext().close();
		}

	}

	// 封装currentUser
	public static Map<String, Object> getCurrentUserInfo(Player player, RoomResp room) {
		Map<String, Object> currentUserInfo = new HashMap<String, Object>();
		currentUserInfo.put("userId", player.getUserId());
		currentUserInfo.put("position", player.getPosition());
		currentUserInfo.put("score", player.getScore());
		currentUserInfo.put("playStatus", player.getPlayStatus());
		currentUserInfo.put("userName", player.getUserName());
		currentUserInfo.put("userImg", player.getUserImg());
		currentUserInfo.put("gender", player.getGender());
		currentUserInfo.put("ip", player.getIp());
		currentUserInfo.put("userAgree", player.getUserAgree());
		currentUserInfo.put("money", player.getMoney());
		currentUserInfo.put("notice", player.getNotice());
		// 自己根据roominfo去判断
		WSClient wsClient = TCPGameFunctions.getWSClientManager().getWSClient(player.getChannelId());

		if (wsClient == null) {
			currentUserInfo.put("state", 2);
		} else {
			currentUserInfo.put("state", 1);
		}
		if (room != null) {
			if (room.getState() == Cnst.ROOM_STATE_GAMIING) {// 游戏进行中
				currentUserInfo.put("hasChu", player.getHasChu());
				currentUserInfo.put("pais", player.getCurrentPaiList());
			}
		}
		return currentUserInfo;
	}

	// 封装anotherUsers
	public static List<Map<String, Object>> getAnotherUserInfo(List<Player> players, RoomResp room) {
		List<Map<String, Object>> anotherUserInfos = new ArrayList<Map<String, Object>>();
		for (Player player : players) {
			Map<String, Object> currentUserInfo = new HashMap<String, Object>();
			currentUserInfo.put("userId", player.getUserId());
			currentUserInfo.put("position", player.getPosition());
			currentUserInfo.put("score", player.getScore());
			currentUserInfo.put("playStatus", player.getPlayStatus());
			currentUserInfo.put("userName", player.getUserName());
			currentUserInfo.put("userImg", player.getUserImg());
			currentUserInfo.put("gender", player.getGender());
			currentUserInfo.put("ip", player.getIp());
			WSClient wsClient = TCPGameFunctions.getWSClientManager().getWSClient(player.getChannelId());
			if (wsClient == null) {
				currentUserInfo.put("state", 2);
			} else {
				currentUserInfo.put("state", 1);
			}

			if (room != null && (room.getState() == Cnst.ROOM_STATE_GAMIING)) {
				if (player.getPlayStatus() == Cnst.PLAYER_STATE_JIAOPAI) {
					currentUserInfo.put("pais", player.getCurrentPaiList());
				} else {
					currentUserInfo.put("pais", player.getCurrentPaiList().size());
				}
				//其他人的hasChu也要给
				currentUserInfo.put("hasChu", player.getHasChu());
			}
			anotherUserInfos.add(currentUserInfo);
		}
		return anotherUserInfos;
	}

	// 封装房间信息
	public static Map<String, Object> getRoomInfo(RoomResp room, Player currentPlayer) {
		Map<String, Object> roomInfo = new HashMap<String, Object>();
		roomInfo.put("userId", room.getCreateId());
		roomInfo.put("userName", room.getOpenName());
		roomInfo.put("roomId", room.getRoomId());
		roomInfo.put("state", room.getState());
		roomInfo.put("lastNum", room.getLastNum());
		roomInfo.put("circleNum", room.getCircleNum());// 总局数
		roomInfo.put("xjst", room.getXjst());
		// 房间规则
		roomInfo.put("roomType", room.getRoomType());
		roomInfo.put("lianDui", room.getLianDui());// 加分类型:点炮三家付或者包三家
		roomInfo.put("shuangWang", room.getShuangWang());
		roomInfo.put("siLu", room.getSiLu());
		roomInfo.put("wuLu", room.getWuLu());
		roomInfo.put("liuLu", room.getLiuLu());
		roomInfo.put("beiShu", room.getBeiShu());
		roomInfo.put("maxPeople", room.getMaxPeople());
		roomInfo.put("zhuangRule", room.getZhuangRule());
		roomInfo.put("sameLv", room.getSameLv());

		if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
			List<List<Integer>> nowChulList = room.getNowChulList();
			if (nowChulList != null) {
				roomInfo.put("nowChulList", nowChulList);
			}
			roomInfo.put("currBeiShu", room.getNowBeiShu());
			// 获取动作信息
			Long lastChuUserId = room.getLastChuUserId();
			if (lastChuUserId != null) {
				roomInfo.put("lastChuUserId", lastChuUserId);
			}
			List<Integer> lastChuPai = room.getLastChuPai();
			if (lastChuPai != null && lastChuPai.size() > 0) {
				roomInfo.put("lastChuPai", lastChuPai);
			}
			Long nextActionUserId = room.getNextActionUserId();
			Integer nextAction = room.getNextAction();
			if (nextActionUserId != null) {
				roomInfo.put("currActionUserId", nextActionUserId);
			}
			if (nextAction != null) {
				roomInfo.put("currAction", nextAction);
			}
			roomInfo.put("currPaiNum", room.getCurrentPaiList().size());
			// }
		}
		if (room.getZhuangId() != null) {
			roomInfo.put("zhuangPlayer", room.getZhuangId());
		}
		if (room.getDissolveRoom() != null) {
			Map<String, Object> dissolveRoom = new HashMap<String, Object>();
			dissolveRoom.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
			dissolveRoom.put("userId", room.getDissolveRoom().getUserId());
			dissolveRoom.put("othersAgree", room.getDissolveRoom().getOthersAgree());
			roomInfo.put("dissolveRoom", dissolveRoom);
		} else {
			roomInfo.put("dissolveRoom", null);
		}
		return roomInfo;
	}

	/**
	 * -小结算 不走数据库,只有开局和大结算走数据库
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100102(WSClient channel, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String cid = channel.getCid();
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		List<Map<String, Object>> userInfos = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", p.getUserId());
			// 小局胡分数
			map.put("score", p.getThisScore());
			List<Integer> currentPaiList = p.getCurrentPaiList();
			if (currentPaiList != null && currentPaiList.size() > 0) {
				map.put("pais", MahjongUtils.paiXu(currentPaiList));
			}
			// 小局杠分
			if (p.getIsHu()) {
				map.put("isWin", 1);
			} else {
				map.put("isWin", 0);
			}
			userInfos.add(map);
		}

		JSONObject info = new JSONObject();
		info.put("lastNum", room.getLastNum());
		// 房间都初始化了
		info.put("beiShu", room.getNowBeiShu());
		info.put("userInfo", userInfos);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大结算
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100103(WSClient channel, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String cid = channel.getCid();
		Long userId2 = channel.getUserId();
		if (userId != null && !userId.equals(userId2)) {
			return;
		}

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		String key = roomId + "-" + room.getCreateTime();
		List<Map> userInfos = RedisUtil.getPlayRecord(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key));
		JSONObject info = new JSONObject();
		// info.put("xiaoJuNum", room.getXiaoJuNum());
		if (!RedisUtil.exists(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key))) {
			List<Map<String, Object>> zeroUserInfos = new ArrayList<Map<String, Object>>();
			List<Player> players = RedisUtil.getPlayerList(room, cid);
			for (Player p : players) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("score", p.getScore());
				map.put("zhaNum", p.getZhaNum());
				map.put("maxNum", p.getMaxNum());
				map.put("winNum", p.getWinNum());
				map.put("loseNum", p.getLoseNum());
				zeroUserInfos.add(map);
			}
			info.put("userInfo", zeroUserInfos);
		} else {
			info.put("userInfo", userInfos);
		}

		JSONObject result = getJSONObj(interfaceId, 1, info.get("userInfo"));
		MessageUtils.sendMessage(channel, result.toJSONString());

		// 更新 player
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId), cid);
		p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0);
		Integer outNum = room.getOutNum() == null ? 1 : room.getOutNum() + 1;
		if (outNum == room.getPlayerIds().length) {
			RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));
		} else {
			// 更新outNum
			room.setOutNum(outNum);
			RedisUtil.updateRedisData(room, p, cid);
		}
	}

	/**
	 * 发牌 打牌 动作推送
	 * 
	 * @param room
	 * @param players
	 * @param info
	 * @param action
	 *            动作实体
	 */
	public static void interface_100104(RoomResp room, List<Player> players, Integer interfaceId) {
		Long nextActionUserId = room.getNextActionUserId();
		Integer wsw_sole_action_id = room.getWsw_sole_action_id();
		Integer nextAction = room.getNextAction();
		Long lastPlayerUserId = room.getLastPlayerUserId();
		Integer lastFaPai = room.getLastFaPai();
		List<Integer> lastChuPai = room.getLastChuPai();
		Integer action = room.getAction();
		Player jiaoPlayer = null;
		if (action == 4) {
			for (Player player : players) {
				if (player.getUserId().equals(lastPlayerUserId)) {
					jiaoPlayer = player;
					break;
				}
			}
		}
		// 房间信息
		for (Player p : players) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("wsw_sole_action_id", wsw_sole_action_id);
			info.put("state", room.getState());
			// if (room.getState() == 3) {// 小结算
			// if(action==2){//上个给动作是出牌
			// if(lastChuPai!=null){
			// info.put("extra", lastChuPai);
			// }
			// }
			// } else {// 肯定不是小结算
			// 上一个执行动作的玩家id
			info.put("userId", lastPlayerUserId);
			// 上一个执行动作的玩家的执行动作
			info.put("action", action);
			if (action == 2) {// 上个给动作是出牌
				if (lastChuPai != null) {
					info.put("extra", lastChuPai);
				}
			} else if (action == 1) {
				if (lastFaPai != null) {
					if (p.getUserId().equals(nextActionUserId)) {
						info.put("extra", room.getLastFaPai());
					}
				}
			} else if (action == 4) {
				// 缴牌动作,所有人都发
				info.put("extra", jiaoPlayer.getCurrentPaiList());
			}
			// // 发牌动作
			// if (nextAction == 1) {
			// // 只给要发的人
			// if(lastFaPai!=null){
			// if (p.getUserId().equals(nextActionUserId)) {
			// info.put("extra", room.getLastFaPai());
			// }
			// }
			// } else if (nextAction == 4) {
			// // 缴牌动作,所有人都发
			// info.put("extra", jiaoPlayer.getCurrentPaiList());
			// }
			if (room.getState() != 3) {// 没有下个动作人和下个动作
				info.put("nextAction", nextAction);
				info.put("nextActionUserId", nextActionUserId);
			}
			// }

			JSONObject result = getJSONObj(interfaceId, 1, info);
			WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
			if (ws != null) {
				MessageUtils.sendMessage(ws, result.toJSONString());
			}
		}
	}

	/**
	 * 多地登陆提示
	 * 
	 * @param session
	 */
	public static void interface_100106(WSClient channel) {
		Integer interfaceId = 100106;
		JSONObject result = getJSONObj(interfaceId, 1, Cnst.PLAYER_LINE_STATE_OUT);
		MessageUtils.sendMessage(channel, result.toJSONString());
		channel.getChannelHandlerContext().close();
	}

	/**
	 * 玩家被踢/房间被解散提示
	 * 
	 * @param session
	 */
	public static void interface_100107(WSClient channel, Integer type, List<Player> players) {
		Integer interfaceId = 100107;
		Map<String, Object> info = new HashMap<String, Object>();

		if (players == null || players.size() == 0) {
			return;
		}
		info.put("userId", channel.getUserId());
		info.put("type", type);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		for (Player p : players) {
			WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
			if (ws != null) {
				MessageUtils.sendMessage(ws, result.toJSONString());
			}
		}
	}

	/**
	 * 方法id不符合
	 * 
	 * @param session
	 */
	public static void interface_100108(WSClient channel) {
		Integer interfaceId = 100108;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", Cnst.REQ_STATE_9);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 用户离线/上线提示
	 * 
	 * @param state
	 */
	public static void interface_100109(List<Player> players, Integer state, Long userId) {
		Integer interfaceId = 100109;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("userId", userId);
		info.put("state", state);

		JSONObject result = getJSONObj(interfaceId, 1, info);

		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p != null && !p.getUserId().equals(userId)) {
					WSClient ws = getWSClientManager().getWSClient(p.getChannelId());

					if (ws != null) {
						MessageUtils.sendMessage(ws, result.toJSONString());
					}
				}

			}
		}
	}

	/**
	 * 后端主动解散房间推送
	 * 
	 * @param reqState
	 * @param players
	 */
	public static void interface_100111(int reqState, List<Player> players, Integer roomId) {
		Integer interfaceId = 100111;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", reqState);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p.getRoomId() != null && p.getRoomId().equals(roomId)) {
					WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
					if (ws != null) {
						MessageUtils.sendMessage(ws, result.toJSONString());
					}
				}
			}
		}

	}

	/**
	 * 后端主动加入代开房间推送
	 * 
	 * @param reqState
	 * @param players
	 */
	public static void interface_100112(Player player, RoomResp room, Integer type, String cid) {
		Integer interfaceId = 100112;
		// 先判断房主是否在线
		Player roomCreater = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()), cid);
		WSClient ws = getWSClientManager().getWSClient(roomCreater.getChannelId());
		if (ws != null) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("roomId", room.getRoomId());
			if (player != null) {
				info.put("userId", player.getUserId());
				info.put("userName", player.getUserName());
				info.put("userImg", player.getUserImg());
				info.put("position", player.getPosition());
			}
			info.put("extraType", type);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(ws, result.toJSONString());
		} else {
			return;
		}
	}

	/***
	 * 获取字段解析
	 * 
	 * @param wsClient
	 * @param readData
	 */
	public static void interface_100999(WSClient wsClient, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		JSONObject obj = new JSONObject();
		obj.put("interfaceId", interfaceId);
		obj.put("state", 1);
		obj.put("message", "");
		obj.put("info", Cnst.ROUTE_MAP_SEND);
		obj.put("others", "");
		MessageUtils.sendMessage(wsClient, obj.toJSONString());
	}

	public static void interface_999800(WSClient wsClient, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String cid = wsClient.getCid();
		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId + "", cid);
		if (room == null) {
			return;
		} else {
			List<Player> players = new ArrayList<Player>();
			Long[] playerIds = room.getPlayerIds();
			for (Long long1 : playerIds) {
				if (long1 != null) {
					Player player = RedisUtil.getPlayerByUserId(long1 + "", cid);
					player.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
					players.add(player);
				}
			}
			RedisUtil.setPlayersList(players, cid);
			room.setState(Cnst.ROOM_STATE_YJS);
			room.setDissolveRoom(null);
			RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)), room, Cnst.ROOM_LIFE_TIME_DIS);
		}

	}

}
