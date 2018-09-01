package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.jdt.internal.compiler.problem.ShouldNotImplement;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Action;
import com.yzt.logic.mj.domain.ClubInfo;
import com.yzt.logic.mj.domain.DissolveRoom;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.BackFileUtil;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.MahjongUtils;
import com.yzt.logic.util.RoomUtil;
import com.yzt.logic.util.GameUtil.JieSuan;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

/**
 * Created by Administrator on 2017/7/13. 游戏中
 */

public class GameFunctions extends TCPGameFunctions {
	final static Object object = new Object();

	/**
	 * 用户点击准备，用在小结算那里，
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100200(WSClient channel, Map<String, Object> readData) {
		logger.info("准备,interfaceId -> 100200");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String cid = channel.getCid();
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		Player currentPlayer = null;
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}

		if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
			return;
		}
		// 将玩家准备
		currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);
//		currentPlayer.initPlayer(currentPlayer.getRoomId(), Cnst.PLAYER_STATE_PREPARED, currentPlayer.getScore());
		// 校验,代开第一把不让1号先准备
		if (room.getXiaoJuNum() == null) {// 第一次
			if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
				// 进来的是1号玩家
				if (currentPlayer.getPosition().equals(1)) {
					// 玩家人数不满,1号不能准备
					if (players.size() != room.getMaxPeople()) {// 带开房间的人数必须满足这些人,必须是1号玩家点开局
//						currentPlayer.initPlayer(currentPlayer.getRoomId(), Cnst.PLAYER_STATE_IN, currentPlayer.getScore());
						currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_IN);
					}
				}
			}else{
				if (currentPlayer.getUserId().equals(room.getCreateId())) {
					// 玩家人数不满,1号不能准备
					if (players.size() != room.getMaxPeople()) {// 带开房间的人数必须满足这些人,必须是1号玩家点开局
						currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_IN);
					}
				}
			}
			
		}
		boolean allPrepared = true;
		for (Player p : players) {
			if (!p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
				allPrepared = false;
			}
		}

		if (allPrepared && players != null) {
			if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
				if (players.size() == room.getMaxPeople()) {// 带开房间的人数必须满足这些人,必须是1号玩家点开局
					if (room.getXiaoJuNum() == null) {// 第一次
						if (currentPlayer.getPosition().equals(1)) {// 最后一个人是房主
							MessageFunctions.interface_100112(null, room, Cnst.PLAYER_EXTRATYPE_KAIJU, cid);
							startGame(room, players, cid);
							BackFileUtil.save(interfaceId, room, players, null, null, cid);// 写入文件内容
						}
					} else {// 不是第一把
						MessageFunctions.interface_100112(null, room, Cnst.PLAYER_EXTRATYPE_KAIJU, cid);
						startGame(room, players, cid);
						BackFileUtil.save(interfaceId, room, players, null, null, cid);// 写入文件内容
					}
				}
			} else {// 房主创建房间
				if (players.size() == room.getMaxPeople()) {// 如果只有1个人不行
					if (room.getXiaoJuNum() == null) {// 第一次
						if (currentPlayer.getUserId().equals(room.getCreateId())) {
							MessageFunctions.interface_100112(null, room, Cnst.PLAYER_EXTRATYPE_KAIJU, cid);
							startGame(room, players, cid);
							BackFileUtil.save(interfaceId, room, players, null, null, cid);// 写入文件内容
						}
					} else {// 不是第一把
						MessageFunctions.interface_100112(null, room, Cnst.PLAYER_EXTRATYPE_KAIJU, cid);
						startGame(room, players, cid);
						BackFileUtil.save(interfaceId, room, players, null, null, cid);// 写入文件内容
					}
				}
			}
		}
		Map<String, Object> info = new HashMap<String, Object>();
		List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> i = new HashMap<String, Object>();
			i.put("userId", p.getUserId());
			i.put("playStatus", p.getPlayStatus());
			userInfo.add(i);
		}
		info.put("userInfo", userInfo);
		Map<String, Object> roominfo = new HashMap<String, Object>();
		roominfo.put("state", room.getState());
		info.put("roomInfo", roominfo);
		for (Player p : players) {
			WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
			if (ws == null)
				continue;
			// 为了显示庄
			// 玩家手牌
			if (room.getState() == Cnst.ROOM_STATE_GAMIING || room.getState() == Cnst.ROOM_STATE_XJS) {
				info.put("nextActionUserId", room.getNextActionUserId());
				info.put("pais", p.getCurrentPaiList());
				// 加入庄的玩家id
				info.put("nextAction", room.getNextAction());
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(ws, result.toJSONString());
		}
		RedisUtil.setPlayersList(players, cid);
		RedisUtil.updateRedisData(room, null, cid);
	}

	/**
	 * 开局发牌
	 * 
	 * @param roomId
	 */
	public static void startGame(RoomResp room, List<Player> players, String cid) {
		// 局数统计
		room.setXiaoJuNum(room.getXiaoJuNum() == null ? 1 : room.getXiaoJuNum() + 1);
		room.setXjst(System.currentTimeMillis());
		// 房间状态游戏中
		room.setState(Cnst.ROOM_STATE_GAMIING);
		room.setNowBeiShu(1);
		//开始游戏再初始化
		for (Player p : players) {
			p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_PREPARED, p.getScore());
		}
		// 获取牌
		List<Integer> roomMjList = MahjongUtils.getPais();
		room.setCurrentPaiList(roomMjList);
		// 第一局开局之后随机选出庄玩家
		if (room.getXiaoJuNum() == 1) {
			Random rd = new Random();
			int nextInt = rd.nextInt(players.size());
			room.setZhuangId(players.get(nextInt).getUserId());
		}
		for (Player p : players) {
			p.setPlayStatus(Cnst.PLAYER_STATE_GAME);// 游戏中..
			if (room.getZhuangId().equals(p.getUserId())) {
				room.setPosition(p.getPosition());
				// 不管怎么样动作人就是庄(显示庄的头像)
				room.setNextActionUserId(p.getUserId());
				// 庄家发6张
				p.setCurrentPaiList(MahjongUtils.faPai(roomMjList, 6));
				// p.setCurrentPaiList(MahjongUtils.faPaiShunZi());
				// p.setCurrentPaiList(MahjongUtils.faPaiZhu());
			} else {
				// 闲家发5张
				// p.setCurrentPaiList(MahjongUtils.faPaiShunZiWithHua());
				p.setCurrentPaiList(MahjongUtils.faPai(roomMjList, 5));
				// p.setCurrentPaiList(MahjongUtils.faPaiXian());
			}
		}
		// 获取从庄开始的玩家集合
		// List<Long> nowPlayerIds = MahjongUtils.getPlayersFromChuPositions(
		// players, zhuangPlayer.getPosition(), true);
		// boolean canHu = false;
		// for (Long id : nowPlayerIds) {
		// for (Player p : players) {
		// if (p.getUserId().equals(id)) {
		// Integer checkTianHu = MahjongUtils.checkTianHu(p, room);
		// if (checkTianHu != 2) {
		// if (checkTianHu != 1) {
		// room.setBeiShu(checkTianHu);
		// }
		// p.setIsHu(true);
		// // 房间状态小结算
		// room.setState(Cnst.ROOM_STATE_XJS);
		// canHu = true;
		// break;
		// }
		// }
		// }
		// // 如果有人能胡,跳出
		// if (canHu) {
		// break;
		// }
		// }
		// 如果胡不了,庄家出牌
		// if (!canHu) {
		room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
		// }
		// 更新缓存数据
		RedisUtil.setPlayersList(players, cid);
		RedisUtil.updateRedisData(room, null, cid);
		// 设置最后发牌人
		if (room.getXiaoJuNum() == 1) {
			Long[] playerIds = room.getPlayerIds();

			if (String.valueOf(room.getRoomId()).length() == 7) {// 说明是俱乐部
				// 今日活跃数 key: cid value ：userId的集合
				Long timesmorning = StringUtils.getTimesmorning();
				// Long scard =
				// RedisUtil.scard(Cnst.get_REDIS_CLUB_ACTIVE_NUM(cid).concat(room.getClubId()+"_").concat(timesmorning+""));
				int dieTime = Cnst.REDIS_CLUB_DIE_TIME;
				// if (scard == null || scard == 0l) {// 当天没人,有人最少为5
				// // 创建一个并设置过期时间(其中1l为假数据)--昨日和前日
				// // 假数据主要是为了设置过期时间--此时间只设置一次
				// RedisUtil.sadd(Cnst.get_REDIS_CLUB_ACTIVE_NUM(cid).concat(room.getClubId()+"_").concat(timesmorning+""),1l,dieTime);
				// for (Long userId : room.getPlayerIds()) {
				// if(userId!=null){
				// RedisUtil.sadd(Cnst.get_REDIS_CLUB_ACTIVE_NUM(cid).concat(room.getClubId()+"_").concat(timesmorning+""),userId,dieTime);
				//
				// }
				// }
				// } else {// 有人
				// 添加俱乐部当天活跃总人数
				for (Long userId : playerIds) {
					if (userId != null) {
						RedisUtil.sadd(Cnst.get_REDIS_CLUB_ACTIVE_NUM(cid).concat(room.getClubId() + "_").concat(timesmorning + ""), userId, dieTime);
					}
				}
				// }
				// 今日俱乐部局数(开一次房间算一局) --昨日和前日
				Integer clubId = room.getClubId();
				Integer todayJuNum = RedisUtil.getTodayJuNum(clubId + "_".concat(timesmorning + ""), cid);
				if (todayJuNum == null || todayJuNum == 0) {
					RedisUtil.setTodayJuNum(clubId + "_".concat(timesmorning + ""), 1, dieTime, cid);
				} else {
					RedisUtil.setTodayJuNum(clubId + "_".concat(timesmorning + ""), 1 + todayJuNum, dieTime, cid);
				}
				// 今日玩家局数 --保存两天
				Integer juNum = null;
				for (Long playerId : playerIds) {
					if (playerId != null) {
						juNum = RedisUtil.getObject(
								Cnst.getREDIS_CLUB_TODAYJUNUM_ROE_USER(cid).concat(clubId + "_").concat(playerId + "_").concat(timesmorning + ""),
								Integer.class);
						if (juNum == null || juNum == 0) {
							RedisUtil
									.setObject(
											Cnst.getREDIS_CLUB_TODAYJUNUM_ROE_USER(cid).concat(clubId + "_").concat(playerId + "_")
													.concat(timesmorning + ""), 1, Cnst.REDIS_CLUB_PLAYERJUNUM_TIME);
						} else {
							RedisUtil
									.setObject(
											Cnst.getREDIS_CLUB_TODAYJUNUM_ROE_USER(cid).concat(clubId + "_").concat(playerId + "_")
													.concat(timesmorning + ""), juNum + 1, Cnst.REDIS_CLUB_PLAYERJUNUM_TIME);
						}
					}
				}
				RedisUtil.hdel(Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(String.valueOf(room.getClubId())), String.valueOf(room.getRoomId()));
			}

			RoomUtil.addRoomToDB(room, cid);
			RoomUtil.removeFreeRoomTask(StringUtils.parseLong(room.getRoomId()), cid);
		}
	}

	/**
	 * 出牌 行为编码(游戏内主逻辑)
	 * 
	 * @param wsClient
	 * @param readData
	 */
	public synchronized static void interface_100201(WSClient channel, Map<String, Object> readData) {
		logger.info("游戏内主逻辑,interfaceId -> 100201");
		String cid = channel.getCid();
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer action = StringUtils.parseInt(readData.get("action")); // 行为编号,牌的信息
		Integer roomId = StringUtils.parseInt(readData.get("roomId")); // 房间号
		Long userId = StringUtils.parseLong(readData.get("userId")); // 玩家ID
		List<Integer> extra = (List<Integer>) readData.get("extra"); // 玩家出的牌
		Integer wsw_sole_action_id = StringUtils.parseInt(readData.get("wsw_sole_action_id")); // 每局的验证id
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null) {
			return;
		}
		if (!room.getWsw_sole_action_id().equals(wsw_sole_action_id)) {
			return;
		}
		Integer state = room.getState();
		if (state == null) {
			return;
		} else {
			// 只要不是游戏阶段就直接不用理会,防止解散房间的时候请求动作报错
			if (!room.getState().equals(Cnst.ROOM_STATE_GAMIING)) {
				return;
			}
		}
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		Player currentPlayer = null;
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}
		if (currentPlayer == null) {
			return;
		}
		if(room.getPaiXu()==false){//玩家手牌没有拍过序
			for (Player p : players) {
				p.setCurrentPaiList(MahjongUtils.paiXu(p.getCurrentPaiList()));
			}
			room.setPaiXu(true);
		}
		List<Integer> currentPaiList = currentPlayer.getCurrentPaiList();
		// 屏蔽多次请求
		if (action != null) {
			// 屏蔽是不是当前动作玩家的请求玩家
			if (!room.getNextActionUserId().equals(userId)) {
				return;
			}
			Integer nextAction = room.getNextAction();
			boolean dzRight = true;
			// 检验动作是不是一致
			// 首先 1：我给缴牌动作,那么必须传回来缴牌动作; 2：我给出牌动作，可能给过牌动作
			if (nextAction.equals(Cnst.ACTION_TYPE_CHUPAI)) {
				// 这个动作不是出牌,也不是不出
				if (!action.equals(Cnst.ACTION_TYPE_BUCHU) && !action.equals(Cnst.ACTION_TYPE_CHUPAI)) {
					dzRight = false;
				}
			} else if (nextAction.equals(Cnst.ACTION_TYPE_JIAOPAI)) {
				// 这个动作不是缴牌
				if (!action.equals(nextAction)) {
					dzRight = false;
				}
			}
			// 动作校验不成立
			if (!dzRight) {
				illegalRequest(interfaceId, channel);
				return;
			}
		}

		// 此时还没更新缓存呢
		room.setWsw_sole_action_id(wsw_sole_action_id + 1);
		// 设置房间上一个动作人，和上一个动作
		room.setAction(action);
		room.setLastPlayerUserId(userId);
		// TODO 不用了ac--等待去掉
		Action ac = null ;
		// 判断动作
		if (action == Cnst.ACTION_TYPE_CHUPAI) {// 出牌
			logger.info("出牌!");
			// 设置出的牌
			List<Integer> chuList;
			// 如果是出牌动作
			if (extra == null) {
				illegalRequest(interfaceId, channel);
				return;
			} else {
				chuList = extra;// 把字符串转换成集合
			}
			ac = new Action(Cnst.ACTION_TYPE_CHUPAI, action, userId, null, extra);
			// 检测玩家手牌是否包含出的牌,不包含的话,不予理会
			if (!MahjongUtils.containsPai(currentPaiList, chuList)) {
				illegalRequest(interfaceId, channel);
				return;
			}
			Collections.sort(chuList);
			// 出的牌不能都是空
			if (chuList.size() == 0) {
				illegalRequest(interfaceId, channel);
				return;
			} else {
				// 出了不能出的牌
				if (!MahjongUtils.checkCanChu(chuList, room)) {
					illegalRequest(interfaceId, channel);
					return;
				}
			}
			boolean needCheck = true;
			if (currentPlayer.getHasChu() == false) {// 这个人没出过牌
				Integer checkTianHu = MahjongUtils.checkTianHu(currentPlayer, room);
				if (checkTianHu != 2) {// 说明能胡
					// 说明全出了
					if (currentPaiList.size() == chuList.size()) {
						needCheck = false;
						room.setTianHu(true);
					}
				}
			}
			Integer isBoom = MahjongUtils.checkIsBoom(chuList, room);

			// 检验能否大于上次的牌
			// 上次出牌人是自己--或者上个出牌人缴牌了也不用检测
			if (needCheck) {
				Long lastChuUserId = room.getLastChuUserId();
				if (lastChuUserId == null) {
					// 说明是本轮第一次出牌
					List<List<Integer>> nextCanChulList = room.getNextCanChulList();
					if (nextCanChulList == null) {
						nextCanChulList = new ArrayList<List<Integer>>();
						room.setNextCanChulList(nextCanChulList);
					}
					if (chuList.size() > 2) {
						if (isBoom == -1) {// 说明不是炸弹或者连队,找到管到它的牌
							MahjongUtils.getCanChuFromPaiList(chuList, room, nextCanChulList);
						}
					}

				} else {// 上个出牌人!=null
					if (lastChuUserId.equals(currentPlayer.getUserId())) {// 上个出牌人是自己
					} else {// 上个出牌人不是自己
							// 找到上个出牌人
						Player lastChuPlayer = null;
						for (Player p : players) {
							if (p.getUserId().equals(lastChuUserId)) {
								lastChuPlayer = p;
								break;
							}
						}
						if (lastChuPlayer.getPlayStatus().equals(Cnst.PLAYER_STATE_JIAOPAI)) {
						} else {// 上个出牌人状态不是缴牌就比较
							if (!MahjongUtils.compairPaiNum(room.getLastChuPai(), chuList, room)) {
								illegalRequest(interfaceId, channel);
								return;
							}
						}
					}
				}
			}
			currentPlayer.setHasChu(true);
			// 当前玩家移除这些出的手牌
			MahjongUtils.removeList(currentPaiList, chuList);
			room.setLastChuPai(chuList);
			// 说明出的是炸弹
			if (isBoom != -1) {
				Integer zhaNum = currentPlayer.getZhaNum();
				if (zhaNum == null) {
					zhaNum = 1;
				}
				currentPlayer.setZhaNum(zhaNum + 1);
				Integer beiShu = room.getBeiShu();
				Integer nowBeiShu = room.getNowBeiShu();
				Integer x = MahjongUtils.getBeiShuFromZhaDanNum(isBoom);
				nowBeiShu *= x;
				// 不能超过最高倍数
				if (beiShu == -1) {
					room.setNowBeiShu(nowBeiShu);
				} else {
					if (nowBeiShu <= beiShu) {
						room.setNowBeiShu(nowBeiShu);
					} else {
						room.setNowBeiShu(beiShu);
					}
				}
			}
			if (currentPaiList.size() == 0) {// 说明出完牌了
				// 设置赢得玩家
				currentPlayer.setIsHu(true);
				// 设置房间状态
				room.setState(Cnst.ROOM_STATE_XJS);
			} else {
				List<List<Integer>> nowChulList = room.getNowChulList();
				if (nowChulList == null) {
					nowChulList = new ArrayList<List<Integer>>();
				}
				nowChulList.add(chuList);

				// 找到动作玩家集合
				List<Long> nowPlayers = MahjongUtils.getPlayersFromChuPositions(players, currentPlayer.getPosition(), false);

				// 找到第一个动作玩家
				Player actionPlayer = null;

				for (Player p : players) {
					if (p.getUserId().equals(nowPlayers.get(0))) {
						actionPlayer = p;
						break;
					}
				}
				// 设置动作人动作
				room.setNextActionUserId(actionPlayer.getUserId());
				// 设置最后房间出的牌
				room.setLastChuUserId(currentPlayer.getUserId());
				// 设置房间剩下的动作人集合
				room.setNowPlayerIds(nowPlayers);
				if (MahjongUtils.checkShouPaiCanChu(actionPlayer.getCurrentPaiList(), room)) {
					room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
				} else {
					room.setNextAction(Cnst.ACTION_TYPE_JIAOPAI);
				}
			}
		} else if (action == Cnst.ACTION_TYPE_BUCHU) {// 不出
			logger.info("不出!!!!!!");
			//如果选不出的人牌必管上家，那么必须出
			List<Integer> lastChuPai = room.getLastChuPai();
			if(room.getSameLv().equals(2)){//选了同级别必管
				boolean noHua=true;
				for (Integer integer : lastChuPai) {
					if(integer>=501){
						noHua=false;
						break;
					}
				}
				//不能有花，不能有炸弹
				if(noHua && MahjongUtils.checkIsBoom(lastChuPai, room).equals(-1)){
					//如果可以必管,必须不能过
					if(MahjongUtils.biGuan(currentPaiList,lastChuPai)){
						illegalRequest(interfaceId, channel);
						return;
					}
				}
			}
			
			ac = new Action(Cnst.ACTION_TYPE_BUCHU, action, userId, null, null);
			List<Long> nowPlayerIds = room.getNowPlayerIds();
			// 移除这个将要有动作的人
			nowPlayerIds.remove(0);
			if (nowPlayerIds.size() == 0) {// 说明所有人都选择过，那么给最后出牌的人摸牌动作
				Long lastChuUserId = room.getLastChuUserId();
				room.setNextActionUserId(lastChuUserId);
				// 继续给动作集合的第一个玩家动作
				// 1.1 判断房间还有没有牌
				List<Integer> roomList = room.getCurrentPaiList();
				if (roomList.size() == 0) {// 房间没牌了
					// 设置玩家所有的手牌
					List<Integer> chuList = null;
					// 1.2没有的话,判断其牌能不能出(都是花不能出)
					for (Player p : players) {
						if (p.getUserId().equals(lastChuUserId)) {
							chuList = p.getCurrentPaiList();
							break;
						}
					}
					if (MahjongUtils.checkShouPaiCanChu(chuList, room)) {
						// 1.2.2能出,给与其动作
						room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
						// 如果能出牌
					} else {
						// 找到动作玩家集合
						room.setLastChuUserId(null);
						// 1.2.1不能出,给予其缴牌动作
						room.setNextAction(Cnst.ACTION_TYPE_JIAOPAI);
						
					}
				} else {
					// 有牌给最后出派人发牌动作
					room.setNextAction(Cnst.ACTION_TYPE_FAPAI);
				}

			} else {
				// 1.2没有的话,判断其牌能不能出(都是花不能出)
				Long long1 = nowPlayerIds.get(0);
				List<Integer> paiList=null;
				for (Player p : players) {
					if (p.getUserId().equals(long1)) {
						paiList = p.getCurrentPaiList();
						break;
					}
				}
				if (MahjongUtils.checkShouPaiCanChu(paiList, room)) {
					// 1.2.2能出,给与其动作
					room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
					// 如果能出牌
				} else {
					// 1.2.1不能出,给予其缴牌动作
					room.setNextAction(Cnst.ACTION_TYPE_JIAOPAI);
				}
				room.setNextActionUserId(long1);
				
			}
		} else if (action == Cnst.ACTION_TYPE_JIAOPAI) {// 缴牌
			logger.info("缴牌!!!!!!");
			currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_JIAOPAI);
			ac = new Action(Cnst.ACTION_TYPE_JIAOPAI, action, userId
					, null, extra);
			List<Long> nowPlayerIds = room.getNowPlayerIds();
			List<List<Integer>> nowChulList = room.getNowChulList();
			if(room.getLastChuUserId()!=null){//说明现在是别人的出牌阶段
				//给下个人出牌动作，或者缴牌动作
				nowPlayerIds.remove(0);
				if (nowPlayerIds.size() == 0) {// 说明所有人都选择过，那么给最后出牌的人摸牌动作
					Long lastChuUserId = room.getLastChuUserId();
					room.setNextActionUserId(lastChuUserId);
					// 继续给动作集合的第一个玩家动作
					// 1.1 判断房间还有没有牌
					List<Integer> roomList = room.getCurrentPaiList();
					if (roomList.size() == 0) {// 房间没牌了
						// 设置玩家所有的手牌
						List<Integer> chuList = null;
						// 1.2没有的话,判断其牌能不能出(都是花不能出)
						for (Player p : players) {
							if (p.getUserId().equals(lastChuUserId)) {
								chuList = p.getCurrentPaiList();
								break;
							}
						}
						if (MahjongUtils.checkShouPaiCanChu(chuList, room)) {
							// 1.2.2能出,给与其动作
							room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
							// 如果能出牌
						} else {
							
							// 1.2.1不能出,给予其缴牌动作
							room.setNextAction(Cnst.ACTION_TYPE_JIAOPAI);
						}
					} else {
						// 有牌给最后出派人发牌动作
						room.setNextAction(Cnst.ACTION_TYPE_FAPAI);
					}

				} else {
					// 1.2没有的话,判断其牌能不能出(都是花不能出)
					Long long1 = nowPlayerIds.get(0);
					List<Integer> paiList=null;
					for (Player p : players) {
						if (p.getUserId().equals(long1)) {
							paiList = p.getCurrentPaiList();
							break;
						}
					}
					if (MahjongUtils.checkShouPaiCanChu(paiList, room)) {
						// 1.2.2能出,给与其动作
						room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
						// 如果能出牌
					} else {
						// 1.2.1不能出,给予其缴牌动作
						room.setNextAction(Cnst.ACTION_TYPE_JIAOPAI);
					}
					room.setNextActionUserId(long1);
				}
			}else{
				nowChulList.clear();
				room.getNextCanChulList().clear();
				List<Long> activePlayerIds = MahjongUtils.getPlayersFromChuPositions(players, currentPlayer.getPosition(), false);
				if (activePlayerIds.size() == 1) {// 说明还有1个玩家
					for (Player p : players) {
						if (activePlayerIds.get(0).equals(p.getUserId())) {
							p.setIsHu(true);
							room.setState(Cnst.ROOM_STATE_XJS);
						}
					}
				} else {
					Player activePlayer = null;
					// 检测下个玩家能不能出牌
					for (Player p : players) {
						if (activePlayerIds.get(0).equals(p.getUserId())) {
							activePlayer = p;
						}
					}
					// 缴牌之后玩家直接出牌,不再摸牌,所以检测玩家的手牌能不能出
					List<Integer> activePaiList = activePlayer.getCurrentPaiList();
					if (MahjongUtils.checkShouPaiCanChu(activePaiList, room)) {
						// 如果能出,让他出牌
						room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
					} else {
						// 不能出,下个动作人也是缴牌
						room.setNextAction(Cnst.ACTION_TYPE_JIAOPAI);
					}
				}
				room.setNextActionUserId(activePlayerIds.get(0));
			}
		} else if (action == Cnst.ACTION_TYPE_FAPAI) { // 系统发牌
			logger.info("发牌!!!!!!");
			List<List<Integer>> nowChulList = room.getNowChulList();
			if (nowChulList == null) {
				nowChulList = new ArrayList<List<Integer>>();
				room.setNowChulList(nowChulList);
			}
			nowChulList.clear();
			// 清空房间的能出的牌集合
			List<List<Integer>> nextCanChulList = room.getNextCanChulList();
			if (nextCanChulList == null) {
				nextCanChulList = new ArrayList<List<Integer>>();
				room.setNextCanChulList(nextCanChulList);
			}
			nextCanChulList.clear();
			// 因为过检测过，只有有牌才会给发牌动作，所以此时房间剩余牌库一定有牌
			List<Integer> pais = MahjongUtils.faPai(room.getCurrentPaiList(), 1);
			Integer faPai = pais.get(0);
			ac = new Action(Cnst.ACTION_TYPE_FAPAI, action, userId, null, pais);
			room.setLastFaPai(faPai);
			// 发完之后出牌
			currentPaiList.add(faPai);
			currentPlayer.setCurrentPaiList(MahjongUtils.paiXu(currentPaiList));
			room.setNextActionUserId(userId);
			if (MahjongUtils.checkShouPaiCanChu(currentPaiList, room)) {
				// 如果能出牌
				room.setNextAction(Cnst.ACTION_TYPE_CHUPAI);
			} else {
				// 不能出牌给与其缴牌操作
				room.setNextAction(Cnst.ACTION_TYPE_JIAOPAI);
			}
			room.setLastChuUserId(null);
		}
		RedisUtil.setPlayersList(players, cid);
		
		RedisUtil.updateRedisData(room, null, cid);
		// 写入回放
		BackFileUtil.save(interfaceId, room, players, null, ac, cid);
		// 统一发消息即可 跟据action 来判断
		MessageFunctions.interface_100104(room, players, 100104);
		// 小结算
		if (room.getState() == Cnst.ROOM_STATE_XJS) {
			JieSuan.xiaoJieSuan(String.valueOf(roomId), cid);
		}
	}

	/**
	 * 玩家申请解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public synchronized static void interface_100203(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("玩家请求解散房间,interfaceId -> 100203");
		String cid = channel.getCid();
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Long useId2 = channel.getUserId();
		if (userId != null && !useId2.equals(userId)) {
			return;
		}
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room.getDissolveRoom() != null) {
			return;
		}
		DissolveRoom dis = new DissolveRoom();
		dis.setDissolveTime(new Date().getTime());
		dis.setUserId(userId);
		List<Map<String, Object>> othersAgree = new ArrayList<>();
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				Map<String, Object> map = new HashMap<>();
				map.put("userId", p.getUserId());
				map.put("agree", 0);// 1同意；2解散；0等待
				othersAgree.add(map);
			}
		}
		dis.setOthersAgree(othersAgree);
		room.setDissolveRoom(dis);

		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", dis.getDissolveTime());
		info.put("userId", dis.getUserId());
		info.put("othersAgree", dis.getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		for (Player p : players) {
			WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
			if (ws != null) {
				MessageUtils.sendMessage(ws, result.toJSONString());
			}
		}

		for (Player p : players) {
			RedisUtil.updateRedisData(null, p, cid);
		}
		RedisUtil.updateRedisData(room, null, cid);

		// 解散房间超时任务开启 TODO
		RoomUtil.addFreeRoomTask(StringUtils.parseLong(room.getRoomId()), System.currentTimeMillis() + Cnst.ROOM_DIS_TIME, cid);
	}

	/**
	 * 同意或者拒绝解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */

	public synchronized static void interface_100204(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("同意或者拒绝解散房间,interfaceId -> interface_100204");
		String cid = channel.getCid();
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer userAgree = StringUtils.parseInt(readData.get("userAgree"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null) {// 房间已经自动解散
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_4);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}
		if (room.getDissolveRoom() == null) {
			Map<String, Object> info = new HashMap<>();
			// info.put("reqState", Cnst.REQ_STATE_7);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}
		List<Map<String, Object>> othersAgree = room.getDissolveRoom().getOthersAgree();
		for (Map<String, Object> m : othersAgree) {
			if (String.valueOf(m.get("userId")).equals(String.valueOf(userId))) {
				m.put("agree", userAgree);
				break;
			}
		}
		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
		info.put("userId", room.getDissolveRoom().getUserId());
		info.put("othersAgree", room.getDissolveRoom().getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);

		int agreeNum = 0;
		int rejectNum = 0;
		for (Map<String, Object> m : othersAgree) {
			if (m.get("agree").equals(1)) {
				agreeNum++;
			} else if (m.get("agree").equals(2)) {
				rejectNum++;
			}
		}
		// 获取房间玩家的总人数
		// ---除了点解散的哪个玩家 3对应1 4对应2 ,5 6对应3
		List<Integer> pNlist = new ArrayList<Integer>(2);
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		int num = players.size();
		MahjongUtils.getNowNeedAndRejectNum(pNlist, num);
		Integer needAgreeNum = pNlist.get(0);
		Integer needRejectNum = pNlist.get(1);

		if (rejectNum >= needRejectNum) {
			// 有玩家拒绝解散房间//关闭解散房间计时任务 TODO
			RoomUtil.removeFreeRoomTask(StringUtils.parseLong(room.getRoomId()), cid);
			room.setDissolveRoom(null);
			RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)), room, Cnst.ROOM_LIFE_TIME_CREAT);
		}
		RedisUtil.updateRedisData(room, null, cid);

		if (agreeNum >= needAgreeNum) {

			if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
				MessageFunctions.interface_100112(null, room, Cnst.PLAYER_EXTRATYPE_JIESANROOM, cid);
			}

			// RedisUtil.setPlayersList(players);
			RoomUtil.updateDatabasePlayRecord(room, cid);
			room.setState(Cnst.ROOM_STATE_YJS);
			for (Player p : players) {
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
			}
			room.setDissolveRoom(null);
			RedisUtil.setObject(Cnst.REDIS_PREFIX_ROOMMAP.concat(cid + "_" + String.valueOf(roomId)), room, Cnst.ROOM_LIFE_TIME_DIS);
			RedisUtil.setPlayersList(players, cid);
			// 关闭解散房间计时任务 TODO
			RoomUtil.removeFreeRoomTask(StringUtils.parseLong(room.getRoomId()), cid);
		}

		for (Player p : players) {
			WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
			if (ws != null) {
				MessageUtils.sendMessage(ws, result.toJSONString());
			}
		}
	}

	/**
	 * 退出房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public synchronized static void interface_100205(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("退出房间,interfaceId -> 100205");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));

		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String cid = channel.getCid();
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}
		if (room.getState() == Cnst.ROOM_STATE_CREATED) {
			List<Player> players = RedisUtil.getPlayerList(room, cid);
			Map<String, Object> info = new HashMap<>();
			info.put("userId", userId);
			if (room.getCreateId().equals(userId)) {// 房主退出，
				if (room.getRoomType().equals(Cnst.ROOM_TYPE_1)) {// 房主模式
					int circle = room.getCircleNum();
					Integer maxPeople = room.getMaxPeople();
					Integer needMoney;
					if (circle == 30) {// 30张的时候，房卡扣双倍
						needMoney = 2 * maxPeople;
					} else {// 按照选的人数扣除房卡
						needMoney = maxPeople;
					}
					info.put("type", Cnst.EXIST_TYPE_DISSOLVE);
					if (String.valueOf(roomId).length() == 6) {
						for (Player p : players) {
							if (p.getUserId().equals(userId)) {
								p.setMoney(p.getMoney() + needMoney);
								break;
							}
						}
					} else if (String.valueOf(roomId).length() == 7) {
						// 退还俱乐部房卡
						Integer clubId = room.getClubId();

						ClubInfo clubInfo = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid) + clubId.toString());

						clubInfo.setRoomCardNum(clubInfo.getRoomCardNum() + needMoney);
						RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid) + clubId, clubInfo);
						// 移除俱乐部创建房间缓存
						RedisUtil.hdel(Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(String.valueOf(room.getClubId())), String.valueOf(room.getRoomId()));
					}
					// 根据房间id移除roomMap的信息
					RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid) + String.valueOf(roomId));

					for (Player p : players) {
						p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
					}
					// 关闭解散房间计时任务 TODO
					RoomUtil.removeFreeRoomTask(StringUtils.parseLong(room.getRoomId()), cid);
				} else {// 自由模式，走正常退出
					info.put("type", Cnst.EXIST_TYPE_EXIST);
					// 代开房主退出
					for (Player player : players) {
						if (player.getUserId().equals(userId)) {// 找到退出的玩家
							Integer position = player.getPosition();
							List<Integer> initPositions = room.getInitPositions();
							initPositions.add(position);
							room.setInitPositions(initPositions);
						}
					}
					existRoom(room, players, userId);
					RedisUtil.updateRedisData(room, null, cid);
				}
			} else {// 正常退出
				for (Player player : players) {
					if (player.getUserId().equals(userId)) {// 找到退出的玩家
						Integer position = player.getPosition();
						List<Integer> initPositions = room.getInitPositions();
						initPositions.add(position);
						room.setInitPositions(initPositions);
						// 如果加入的代开房间 通知房主
						if (room.getRoomType() == Cnst.ROOM_TYPE_2 && !userId.equals(room.getCreateId())) {
							MessageFunctions.interface_100112(player, room, Cnst.PLAYER_EXTRATYPE_EXITROOM, cid);
						}
					}
				}
				info.put("type", Cnst.EXIST_TYPE_EXIST);
				existRoom(room, players, userId);
				RedisUtil.updateRedisData(room, null, cid);
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			for (Player p : players) {
				RedisUtil.updateRedisData(null, p, cid);
			}

			for (Player p : players) {
				WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
				if (ws != null) {
					MessageUtils.sendMessage(ws, result.toJSONString());
				}
			}

		} else {
			roomIsGaming(interfaceId, channel);
		}
	}

	private static void existRoom(RoomResp room, List<Player> players, Long userId) {
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
				break;
			}
		}
		Long[] pids = room.getPlayerIds();
		if (pids != null) {
			for (int i = 0; i < pids.length; i++) {
				if (userId.equals(pids[i])) {
					pids[i] = null;
					break;
				}
			}
		}
	}

	/**
	 * 语音表情
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100206(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("语音表情,interfaceId -> 100206");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String userId = String.valueOf(readData.get("userId"));
		String type = String.valueOf(readData.get("type"));
		String idx = String.valueOf(readData.get("idx"));
		String cid = channel.getCid();
		Map<String, Object> info = new HashMap<>();
		info.put("roomId", roomId);
		info.put("userId", userId);
		info.put("type", type);
		info.put("idx", idx);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		List<Player> players = RedisUtil.getPlayerList(roomId, cid);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
				if (ws != null) {
					MessageUtils.sendMessage(ws, result.toJSONString());
				}
			}
		}
	}

	/**
	 * 定位
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100207(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("定位,interfaceId -> 100207");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String cid = channel.getCid();
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null) {
			return;
		}
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		List<Map<String, Object>> info = new ArrayList<Map<String, Object>>();
		List<Player> agreePlayers = new ArrayList<Player>();
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getX_index() != null && players.get(i).getY_index() != null) {
				agreePlayers.add(players.get(i));
			}
		}
		if (agreePlayers.size() > 1) {
			for (int i = 0; i < agreePlayers.size(); i++) {
				for (int m = i + 1; m < agreePlayers.size(); m++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("userId", agreePlayers.get(i).getUserId());
					map.put("toUserId", agreePlayers.get(m).getUserId());
					Double x1 = agreePlayers.get(i).getX_index() - agreePlayers.get(m).getX_index();
					Double y1 = agreePlayers.get(i).getY_index() - agreePlayers.get(m).getY_index();
					map.put("distance", (int) Math.floor(Math.sqrt(x1 * x1 + y1 * y1)));
					info.add(map);
				}
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

}
