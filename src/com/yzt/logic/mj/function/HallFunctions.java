package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.dao.UserLogin;
import com.yzt.logic.mj.dao.UserMapper;
import com.yzt.logic.mj.domain.Feedback;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.mj.domain.SystemMessage;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.CommonUtil;
import com.yzt.logic.util.MahjongUtils;
import com.yzt.logic.util.RoomUtil;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

/**
 * Created by Administrator on 2017/7/8. 大厅方法类
 */
public class HallFunctions extends TCPGameFunctions {

	/**
	 * 大厅查询战绩
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100002(WSClient channel, Map<String, Object> readData) {
		logger.info("大厅查询战绩,interfaceId -> 100002");
		Integer interfaceId = Integer.parseInt(String.valueOf(readData.get("interfaceId")));
		String userId = String.valueOf(readData.get("userId"));
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		String cid = channel.getCid();
		String userKey = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(cid).concat(userId);
		Long pageSize = RedisUtil.llen(userKey);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(userKey, start, end);
		JSONObject info = new JSONObject();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			String redisRecordKey = Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey);
			if (RedisUtil.exists(redisRecordKey)) {
				Map<String, String> roomInfos = RedisUtil.hgetAll(redisRecordKey);
				maps.add(roomInfos);
			} else {
				RedisUtil.lrem(userKey, roomKey);
			}

		}
		info.put("infos", maps);
		info.put("pages", pageSize == null ? 0 : pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE : (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大厅查询系统消息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100003(WSClient channel, Map<String, Object> readData) {
		logger.info("大厅查询系统消息,interfaceId -> 100003");
		Integer interfaceId = Integer.parseInt(String.valueOf(readData.get("interfaceId")));
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		List<SystemMessage> info = UserMapper.getSystemMessage(null, (page - 1) * Cnst.PAGE_SIZE, Cnst.PAGE_SIZE);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大厅请求联系我们
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100004(WSClient channel, Map<String, Object> readData) {
		logger.info("大厅请求联系我们,interfaceId -> 100004");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("connectionInfo", UserMapper.getConectUs());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大厅请求帮助信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100005(WSClient channel, Map<String, Object> readData) {
		logger.info("大厅请求帮助信息,interfaceId -> 100005");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("help", "帮助帮助帮助帮助帮助帮助帮助帮助帮助");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 反馈信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100006(WSClient channel, Map<String, Object> readData) {
		logger.info("反馈信息,interfaceId -> 100006");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String content = String.valueOf(readData.get("content"));
		String tel = String.valueOf(readData.get("tel"));
		// 插入反馈信息
		Feedback back = new Feedback();
		back.setContent(content);
		back.setCreateTime(new Date().getTime());
		back.setTel(tel);
		back.setUserId(userId);
		UserMapper.userFeedback(back);
		// 返回反馈信息
		Map<String, String> info = new HashMap<>();
		info.put("content", "感谢您的反馈！");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 创建房间-经典玩法
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100007(WSClient channel, Map<String, Object> readData) {
		logger.info("创建房间,interfaceId -> 100007");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));// 接口Id
		String cid = channel.getCid();
		Long userId = StringUtils.parseLong(readData.get("userId"));// 用户id
		Integer circleNum = StringUtils.parseInt(readData.get("circleNum"));// 局数
																			// 2(默认)/4/8
		Integer roomType = StringUtils.parseInt(readData.get("roomType"));// 房间类型：1房主模式；2带开房间

		Integer lianDui = StringUtils.parseInt(readData.get("lianDui"));// 连对：1不可以；2可以
		// Integer shuangWang =
		// StringUtils.parseInt(readData.get("shuangWang"));// 双王：1不可以；2可以
		Integer siLu = StringUtils.parseInt(readData.get("siLu"));// 四路炸弹：1不加倍；2加倍
		Integer wuLu = StringUtils.parseInt(readData.get("wuLu"));// 五路炸弹：1不加倍；2加倍
		Integer liuLu = StringUtils.parseInt(readData.get("liuLu"));// 六路炸弹：1不加倍；2加倍
		Integer beiShu = StringUtils.parseInt(readData.get("beiShu"));// 倍数：8/16/32/-1(不封顶)
		Integer maxPeople = StringUtils.parseInt(readData.get("maxPeople"));// 人数上限：3/4/5/6
		Integer zhuangRule = StringUtils.parseInt(readData.get("zhuangRule"));// 坐庄规则：1胜者坐庄 , 2轮流坐庄
		Integer sameLv = StringUtils.parseInt(readData.get("sameLv"));// 同级必管 ：默认 1 （不选） 2 选

		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId), cid);

		RoomResp room = new RoomResp();
		// 检测需要的房卡
		Integer needMoney = 0;
		if (circleNum == 30) {// 30张的时候，房卡扣双倍
			needMoney = 2 * maxPeople;
		} else {// 按照选的人数扣除房卡
			needMoney = maxPeople;
		}
		if (p.getMoney() < needMoney) {// 玩家房卡不足
			playerMoneyNotEnough(interfaceId, channel, roomType);
			return;
		}

		if (p.getRoomId() != null) {// 已存在其他房间
			playerExistOtherRoom(interfaceId, channel);
			return;
		}

		if (roomType != null && roomType.equals(Cnst.ROOM_TYPE_2)) {// 代开模式开房，玩家房卡必须大于等于100
			if (p.getMoney() < 100) {
				playerMoneyNotEnough(interfaceId, channel, roomType);
				return;
			}
		}

		if (roomType != null && roomType.equals(Cnst.ROOM_TYPE_2)) {
			// 从自己的代开房间列表中查找代开房间
			Map<String, String> rooms = RedisUtil.hgetAll(Cnst.getREDIS_PREFIX_DAI_ROOM_LIST(cid) + userId);
			int size = 0;
			if (rooms != null) {
				size = rooms.size();
			}
			if (size >= 10) {
				roomEnough(interfaceId, channel);
				return;
			}
		}
		String createTime = String.valueOf(new Date().getTime());
		room.setCreateId(userId);// 创建人
		room.setState(Cnst.ROOM_STATE_CREATED);// 房间状态为等待玩家入坐
		room.setCircleNum(circleNum);// 总圈数
		room.setLastNum(circleNum);// 剩余圈数
		room.setRoomType(roomType);// 房间类型：房主模式或者自由模式
		room.setCreateTime(createTime);// 创建时间，long型数据
		room.setOpenName(p.getUserName());
		// 房间规则
		room.setLianDui(lianDui);
		// 没双王,那么就全改成1---若是以后修去不该再删掉
		room.setShuangWang(1);
		room.setSiLu(siLu);
		room.setWuLu(wuLu);
		room.setLiuLu(liuLu);
		room.setBeiShu(beiShu);
		room.setMaxPeople(maxPeople);
		room.setZhuangRule(zhuangRule);
		room.setSameLv(sameLv);
		// 设置id1
		room.setWsw_sole_action_id(1);
		room.setCid(cid);
		room.initRoom();
		// toEdit 需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
		while (true) {
			room.setRoomId(CommonUtil.getGivenRamdonNum(6));// 设置随机房间密码
			if (RedisUtil.getRoomRespByRoomId(String.valueOf(room.getRoomId()), cid) == null) {
				break;
			}
		}

		Long[] userIds;

		Map<String, Object> info = new HashMap<>();
		Map<String, Object> userInfos = new HashMap<String, Object>();
		// 处理开房模式
		if (roomType == null) {
			illegalRequest(interfaceId, channel);
			return;
		} else if (roomType.equals(Cnst.ROOM_TYPE_1)) {// 房主模式
			// 房主创建的房间,人数座位座位都是6人的
			room.setInitPositions(MahjongUtils.initPosition(6));
			// 设置用户信息
			p.setPosition(getWind(room.getInitPositions()));// 设置庄家位置
			// 现在的庄位置是开局随机的，因为不知道玩家坐那?几个人开局
			userIds = new Long[6];
			p.setRoomId(room.getRoomId());
			p.setMaxNum(0);// 最大的次数
			p.setZhaNum(0);
			p.setLoseNum(0);
			p.setWinNum(0);
			p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);
			userIds[p.getPosition() - 1] = p.getUserId();
			info.put("reqState", Cnst.REQ_STATE_1);
			info.put("playerNum", 1);
			p.setMoney(p.getMoney() - needMoney);
			userInfos.put("position", p.getPosition());
		} else if (roomType.equals(Cnst.ROOM_TYPE_2)) {// 代开模式
			userIds = new Long[maxPeople];
			// 必须满足代开房主要求的人数,不能多不能少
			room.setInitPositions(MahjongUtils.initPosition(maxPeople));
			// 将新创建的房间加入到代开房间列表里面
			RedisUtil.hset(Cnst.getREDIS_PREFIX_DAI_ROOM_LIST(cid) + userId, room.getRoomId().toString(), "1", null);
			p.setMoney(p.getMoney() - needMoney);
			info.put("reqState", Cnst.REQ_STATE_10);
		} else {
			illegalRequest(interfaceId, channel);
			return;
		}
		room.setPlayerIds(userIds);
		room.setIp(Cnst.SERVER_IP);

		info.put("userInfo", userInfos);
		// 直接把传来的readData处理 返回
		readData.put("roomId", room.getRoomId());
		readData.put("state", room.getState());
		readData.remove(interfaceId);
		info.put("roomInfo", readData);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());

		// 更新redis数据 player
		RedisUtil.updateRedisData(null, p, cid);
		// 更新redis数据 room信息,并设置时间---5小时之内自动解散
		RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(room.getRoomId())), room, Cnst.ROOM_LIFE_TIME_CREAT);

		// 解散房间命令 TODO
		RoomUtil.addFreeRoomTask(StringUtils.parseLong(room.getRoomId()), System.currentTimeMillis() + Cnst.ROOM_CREATE_DIS_TIME, cid);
	}

	/**
	 * 加入房间
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100008(WSClient channel, Map<String, Object> readData) {
		logger.info("加入房间,interfaceId -> 100008");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String cid = channel.getCid();
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(channel.getUserId()), cid);

		// 已经在其他房间里
		if (p.getRoomId() != null) {// 玩家已经在非当前请求进入的其他房间里
			playerExistOtherRoom(interfaceId, channel);
			return;
		}
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null || room.getState() == Cnst.ROOM_STATE_YJS) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}

		// 房间人满
		Long[] userIds = room.getPlayerIds(); // 获取所有人
		int jionIndex = 0;
		for (Long uId : userIds) {
			if (uId != null) {
				jionIndex++;
			}
		}
		if (jionIndex == room.getMaxPeople()) {// 加入的人数已经等于房间的最大人数
			roomFully(interfaceId, channel);
			return;
		}

		// 验证ip是否一致
		if (!Cnst.SERVER_IP.equals(room.getIp())) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_14);
			info.put("roomId", roomId);
			info.put("roomIp", room.getIp().concat(":").concat(Cnst.MINA_PORT));
			JSONObject result = getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(channel, result.toJSONString());
			return;
		}

		// 加入房间成功,开始设置用户信息

		p.setPosition(getWind(room.getInitPositions()));
		p.setRoomId(roomId);
		p.setMaxNum(0);// 最大的次数
		p.setZhaNum(0);// 炸的次数
		p.setLoseNum(0);// 输的次数
		p.setWinNum(0);// 赢得次数
		userIds[p.getPosition() - 1] = p.getUserId();
		p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);

		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		//房主创建者
		info.put("userId", room.getCreateId());
		info.put("roomId", roomId);
		info.put("circleNum", room.getCircleNum());
		info.put("roomType", room.getRoomType());
		// 规则
		info.put("lianDui", room.getLianDui());
		info.put("shuangWang", room.getShuangWang());
		info.put("siLu", room.getSiLu());
		info.put("wuLu", room.getWuLu());
		info.put("liuLu", room.getLiuLu());
		info.put("beiShu", room.getBeiShu());
		info.put("maxPeople", room.getMaxPeople());
		info.put("zhuangRule", room.getZhuangRule());
		info.put("sameLv", room.getSameLv());
		JSONArray allUserInfos = new JSONArray();
		for (Long ids : userIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				JSONObject infos = TCPGameFunctions.getUserInfoJSON(p);// 当前用户的信息
				allUserInfos.add(infos);
				continue;
			}
			Player player = RedisUtil.getPlayerByUserId(String.valueOf(ids), cid);
			allUserInfos.add(TCPGameFunctions.getUserInfoJSON(player));
		}

		info.put("userInfo", allUserInfos);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());

		// 更新redis数据
		RedisUtil.updateRedisData(room, p, cid);

		// 通知另外几个人
		JSONObject userInfos = TCPGameFunctions.getUserInfoJSON(p);
		userInfos.remove("state");
		for (Long ids : userIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				continue;
			}
			Player pp = RedisUtil.getPlayerByUserId(String.valueOf(ids), cid);
			WSClient ws = getWSClientManager().getWSClient(pp.getChannelId());
			if (ws != null) {
				JSONObject result1 = getJSONObj(interfaceId, 1, userInfos);
				MessageUtils.sendMessage(ws, result1.toJSONString());
			}
		}

		// 如果加入的代开房间 通知房主
		if (room.getRoomType() == Cnst.ROOM_TYPE_2 && !userId.equals(room.getCreateId())) {
			MessageFunctions.interface_100112(p, room, Cnst.PLAYER_EXTRATYPE_ADDROOM, cid);
		}
	}

	/**
	 * 用户点击同意协议
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100009(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("用户点击同意协议,interfaceId -> 100009");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String cid = channel.getCid();
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(channel.getUserId()), cid);
		if (p == null) {
			illegalRequest(interfaceId, channel);
			return;
		}
		p.setUserAgree(1);
		Map<String, Object> info = new JSONObject();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
		// 更新redis数据
		RedisUtil.updateRedisData(null, p, cid);
		/* 刷新数据库，用户同意协议 */
		UserMapper.updateUserAgree(p.getUserId(), cid);
	}

	/**
	 * 查看代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100010(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("查看代开房间列表,interfaceId -> 100010");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		List<Map<String, Object>> info = new ArrayList<Map<String, Object>>();
		String cid = channel.getCid();
		String daiKaiKey = Cnst.getREDIS_PREFIX_DAI_ROOM_LIST(cid) + String.valueOf(userId);
		Map<String, String> hgetAll = RedisUtil.hgetAll(daiKaiKey);
		if (hgetAll != null && hgetAll.size() > 0) {
			Set<String> keySet = hgetAll.keySet();
			for (String roomId : keySet) {
				// 获取每个房间的信息
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId, cid);
				if (room != null && room.getCreateId().equals(userId) && room.getState() != Cnst.ROOM_STATE_YJS
						&& room.getRoomType() == Cnst.ROOM_TYPE_2) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("roomId", room.getRoomId());
					map.put("createTime", room.getCreateTime());
					map.put("circleNum", room.getCircleNum());
					map.put("lastNum", room.getLastNum());
					map.put("state", room.getState());
					// 规则
					map.put("lianDui", room.getLianDui());
					map.put("shuangWang", room.getShuangWang());
					map.put("siLu", room.getSiLu());
					map.put("wuLu", room.getWuLu());
					map.put("liuLu", room.getLiuLu());
					map.put("beiShu", room.getBeiShu());
					map.put("maxPeople", room.getMaxPeople());
					map.put("zhuangRule", room.getZhuangRule());
					List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();

					List<Player> list = RedisUtil.getPlayerList(room, cid);
					if (list != null && list.size() > 0) {
						for (Player p : list) {
							Map<String, Object> pinfo = new HashMap<String, Object>();
							pinfo.put("userId", p.getUserId());
							pinfo.put("position", p.getPosition());
							pinfo.put("userName", p.getUserName());
							pinfo.put("userImg", p.getUserImg());
							WSClient wsClient = TCPGameFunctions.getWSClientManager().getWSClient(p.getChannelId());
							if (wsClient == null) {
								pinfo.put("state", 2);
							} else {
								pinfo.put("state", 1);
							}
							userInfo.add(pinfo);
						}
					}
					map.put("userInfo", userInfo);
					info.add(map);
				}
				if (room == null) {
					RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
				}
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 查看历史代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100011(WSClient channel, Map<String, Object> readData) throws Exception {
		logger.info("查看历史代开房间列表,interfaceId -> 100011");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String userId = String.valueOf(readData.get("userId"));
		Integer page = StringUtils.parseInt(readData.get("page"));
		String cid = channel.getCid();
		String key = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(cid).concat(userId);

		Long pageSize = RedisUtil.llen(key);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(key, start, end);

		Map<String, Object> info = new HashMap<>();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			String redisRecordKey = Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey);
			if (RedisUtil.exists(redisRecordKey)) {
				Map<String, String> roomInfos = RedisUtil.hgetAll(redisRecordKey);
				maps.add(roomInfos);
			} else {
				RedisUtil.lrem(key, roomKey);
			}

		}
		info.put("infos", maps);
		info.put("pages", pageSize == null ? 0 : pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE : (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 代开模式中踢出玩家
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100012(WSClient channel, Map<String, Object> readData) {
		logger.info("准备,interfaceId -> 100012");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String cid = channel.getCid();
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}

		try {
			// 验证解散人是否是真正的房主
			Long createId = channel.getUserId();
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, channel);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, channel);
			return;
		}
		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, channel);
			return;
		}

		List<Player> list = RedisUtil.getPlayerList(room, cid);
		boolean hasPlayer = false;// 列表中有当前玩家
		for (Player p : list) {
			if (p.getUserId().equals(userId)) {
				Integer position = p.getPosition();
				List<Integer> initPositions = room.getInitPositions();
				initPositions.add(position);
				room.setInitPositions(initPositions);

				// 初始化玩家 TODO
				p.initPlayer(null, Cnst.PLAYER_STATE_IN, 0);

				// 刷新房间用户列表
				Long[] pids = room.getPlayerIds();
				if (pids != null) {
					for (int i = 0; i < pids.length; i++) {
						if (userId.equals(pids[i])) {
							pids[i] = null;
							break;
						}
					}
				}

				// 更新redis数据
				RedisUtil.updateRedisData(room, p, cid);
				hasPlayer = true;
				WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
				MessageFunctions.interface_100107(ws, Cnst.EXIST_TYPE_EXIST, list);
				break;
			}
		}

		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState", String.valueOf(hasPlayer ? Cnst.REQ_STATE_1 : Cnst.REQ_STATE_8));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 代开模式中房主解散房间
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100013(WSClient channel, Map<String, Object> readData) {
		logger.info("代开模式中踢出玩家,interfaceId -> 100013");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		String cid = channel.getCid();
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		// 房间不存在
		if (room == null) {
			roomDoesNotExist(interfaceId, channel);
			return;
		}

		try {
			// 验证解散人是否是真正的房主
			Long createId = channel.getUserId();
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, channel);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, channel);
			return;
		}

		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, channel);
			return;
		}
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				// 初始化玩家 TODO
				p.initPlayer(null, Cnst.PLAYER_STATE_IN, 0);
			}
			RedisUtil.setPlayersList(players, cid);
		}

		MessageFunctions.interface_100107(channel, Cnst.EXIST_TYPE_DISSOLVE, players);
		Integer maxPeople = room.getMaxPeople();
		Integer circleNum = room.getCircleNum();
		Integer needMoney = 0;
		if (circleNum == 30) {// 30张的时候，房卡扣双倍
			needMoney = 2 * maxPeople;
		} else {// 按照选的人数扣除房卡
			needMoney = maxPeople;
		}
		// 归还玩家房卡
		Player cp = RedisUtil.getPlayerByUserId(String.valueOf(channel.getUserId()), cid);
		cp.setMoney(cp.getMoney() + needMoney);

		// 更新房主的redis数据
		RedisUtil.updateRedisData(null, cp, cid);

		RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));
		String daiKaiKey = Cnst.get_ROOM_DAIKAI_KEY(cid).concat(String.valueOf(room.getCreateId()));
		if (RedisUtil.hexists(daiKaiKey, roomId.toString())) {
			RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
		}
		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState", String.valueOf(Cnst.REQ_STATE_1));
		info.put("money", String.valueOf(cp.getMoney()));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 强制解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100015(WSClient channel, Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String cid = channel.getCid();
		Integer roomId = StringUtils.parseInt(readData.get("roomId"));
		System.out.println("*******强制解散房间" + roomId);
		Long userId = channel.getUserId();
		if (userId == null) {
			illegalRequest(interfaceId, channel);
			return;
		}
		if (roomId != null) {
			RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
			if (room != null && room.getCreateId().equals(userId)) {
				if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
					// 中途准备阶段解散房间不计入回放中
					List<Integer> xiaoJSInfo = new ArrayList<Integer>();
					for (int i = 0; i < room.getPlayerIds().length; i++) {
						xiaoJSInfo.add(0);
					}
					room.addXiaoJuInfo(xiaoJSInfo);
					room.setState(Cnst.ROOM_STATE_YJS);
					List<Player> players = RedisUtil.getPlayerList(room, cid);

					RoomUtil.updateDatabasePlayRecord(room, cid);

					RedisUtil.deleteByKey(Cnst.REDIS_PREFIX_ROOMMAP.concat(String.valueOf(roomId)));// 删除房间
					if (players != null && players.size() > 0) {
						for (Player p : players) {
							// 初始化玩家 TODO
							p.initPlayer(null, Cnst.PLAYER_STATE_IN, 0);
							RedisUtil.updateRedisData(null, p, cid);
						}
						for (Player p : players) {
							WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
							if (ws != null) {
								Map<String, Object> data = new HashMap<String, Object>();
								data.put("interfaceId", 100100);
								data.put("openId", p.getOpenId());
								data.put("cId", cid);
								MessageFunctions.interface_100100(ws, data);
							}
						}
					}

					if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
						String daiKaiKey = Cnst.REDIS_PREFIX_DAI_ROOM_LIST.concat(String.valueOf(room.getCreateId()));
						if (RedisUtil.hexists(daiKaiKey, roomId.toString())) {
							RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
						}
					}
				} else {
					System.out.println("*******强制解散房间" + roomId + "，房间不存在");
				}
			}

			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_1);
			JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
			MessageUtils.sendMessage(channel, result.toJSONString());
		}
	}

	/**
	 * 获取玩家坐标
	 * 
	 * @param channel
	 * @param readData
	 */
	public static void interface_100016(WSClient channel, Map<String, Object> readData) {
		logger.info("获取玩家坐标,interfaceId -> 100016");
		String cid = channel.getCid();
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Double x_index = Double.parseDouble(String.valueOf(readData.get("x_index")));
		Double y_index = Double.parseDouble(String.valueOf(readData.get("y_index")));
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId), cid);
		if (p != null && x_index != null && y_index != null) {
			p.setX_index(x_index);
			p.setY_index(y_index);
		}
		RedisUtil.updateRedisData(null, p, cid);
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 产生随机的风
	 * 
	 * @param players
	 * @return
	 */
	private static Integer getWind(List<Integer> positions) {
		Integer position = 0;
		int size = positions.size();
		if (size == 1) {
			position = positions.get(0);
			positions.remove(0);
		} else {
			Random rd = new Random();
			int nextInt = rd.nextInt(size);
			position = positions.get(nextInt);
			positions.remove(nextInt);
		}
		return position;
	}

	/**
	 * 或得到的是一个正数，要拿当前玩家的剩余房卡，减去这个值
	 * 
	 * @param userId
	 * @return
	 */
	private static Integer getFrozenMoney(Long userId, String cid) {
		int frozenMoney = 0;
		Set<String> roomMapKeys = RedisUtil.getSameKeys(Cnst.get_REDIS_PREFIX_ROOMMAP(cid));
		if (roomMapKeys != null && roomMapKeys.size() > 0) {
			for (String roomId : roomMapKeys) {
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId, cid);
				if (room.getCreateId().equals(userId) && room.getState() == Cnst.ROOM_STATE_CREATED) {
					Integer maxPeople = room.getMaxPeople();
					Integer circleNum = room.getCircleNum();
					if (circleNum == 30) {// 30张的时候，房卡扣双倍
						frozenMoney += 2 * maxPeople;
					} else {// 按照选的人数扣除房卡
						frozenMoney += maxPeople;
					}
				}
			}
		}
		return frozenMoney;
	}

	/**
	 * 返回用户
	 * 
	 * @param openId
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static Player getPlayerInfos(String openId, String ip, String cid, WSClient channel) {
		if (cid == null ) {
			return null;
		}
		Player p = null;
		String clientIp = channel.getIp();
		long updateTime = 0;
		try {
			String notice = RedisUtil.getStringByKey(Cnst.get_NOTICE_KEY(cid));
			if (notice == null) {
				notice = UserMapper.getNotice(cid);
				RedisUtil.setObject(Cnst.get_NOTICE_KEY(cid), notice, null);
			}
			Set<String> openIds = RedisUtil.getSameKeys(Cnst.get_REDIS_PREFIX_OPENIDUSERMAP(cid));
			if (openIds != null && openIds.contains(openId)) {// 用户是断线重连
				Long userId = RedisUtil.getUserIdByOpenId(openId, cid);
				p = RedisUtil.getPlayerByUserId(String.valueOf(userId), cid);
				WSClient ws = getWSClientManager().getWSClient(p.getChannelId());
				p.setNotice(notice);
				p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
				updateTime = p.getUpdateTime() == null ? 0l : p.getUpdateTime();
				if (ws != null) {
					Long tempuserId = ws.getUserId();
					if (ws.getId() != channel.getId() && userId.equals(tempuserId)) {
						MessageFunctions.interface_100106(ws);
					}
				}
				if (p.getPlayStatus() != null && p.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)) {// 去数据库重新请求用户，//需要减去玩家开的房卡
					Player loaclMysql = UserMapper.findByOpenId(openId, cid);
					if (loaclMysql == null) {
						p = UserLogin.getUserInfoByOpenId(openId,cid);
						if (p == null) {
							return null;
						} else {
							p.setUserAgree(0);
							p.setGender(p.getGender());
							// p.setTotalGameNum("0");
							p.setMoney(Cnst.MONEY_INIT);
							p.setLoginStatus(1);
							p.setCid(cid);
							String time = String.valueOf(new Date().getTime());
							p.setLastLoginTime(time);
							p.setSignUpTime(time);
							p.setUpdateTime(System.currentTimeMillis());
							p.setIp(ip);
							UserMapper.insert(p);

						}
					} else {
						// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
						// 但是这个库里面金币是正确的 UID也是正确的 只是昵称不太一样
						if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
							Player updatep = UserLogin.getUserInfoByOpenId(openId,cid);
							p.setUserName(updatep.getUserName());
							p.setUserImg(updatep.getUserImg());
							p.setGender(updatep.getGender());
							p.setUpdateTime(System.currentTimeMillis());
						}
					}
					p.setScore(0);
					p.setIp(ip);
					p.setNotice(notice);
					p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
					p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
					p.setMoney(loaclMysql.getMoney() - getFrozenMoney(p.getUserId(), cid));
				}
				// 更新用户ip 最后登陆时间
				// UserMapper.updateIpAndLastTime(openId, clientIp);
				return p;
			}
			p = UserMapper.findByOpenId(openId, cid);
			if (p != null) {// 当前游戏的数据库中存在该用户
				p.setNotice(notice);

				Player redisP = RedisUtil.getPlayerByUserId(String.valueOf(p.getUserId()), cid);
				updateTime = (redisP == null || redisP.getUpdateTime() == null) ? 0l : redisP.getUpdateTime();
				// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
				// 但是这个库里面金币是正确的 UID也是正确的
				if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
					Player updatep = UserLogin.getUserInfoByOpenId(openId,cid);
					p.setUserName(updatep.getUserName());
					p.setUserImg(updatep.getUserImg());
					p.setGender(updatep.getGender());
					p.setUpdateTime(System.currentTimeMillis());
					// TODO 更新mysql用户头像,姓名,性别
					UserMapper.updateNameImgGer(updatep, cid);
				}
			} else {// 如果没有，需要去微信的用户里查询
				p = UserLogin.getUserInfoByOpenId(openId,cid);
				if (p == null) {
					return null;
				} else {
					// p.setTotalGameNum("0");
					p.setMoney(Cnst.MONEY_INIT);
					p.setLoginStatus(1);
					p.setCid(cid);
					String time = String.valueOf(new Date().getTime());
					p.setLastLoginTime(time);
					p.setSignUpTime(time);
					p.setUpdateTime(System.currentTimeMillis());
					p.setUserAgree(0);
					p.setIp(ip);
					UserMapper.insert(p);
				}
			}
			p.setScore(0);
			p.setIp(ip);
			p.setNotice(notice);
			p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
			p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
			p.setMoney(p.getMoney() - getFrozenMoney(p.getUserId(), cid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 更新用户ip 最后登陆时间
		// UserMapper.updateIpAndLastTime(openId, clientIp);
		return p;
	}

}
