package com.yzt.logic.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.dao.ClubMapper;
import com.yzt.logic.mj.dao.RoomMapper;
import com.yzt.logic.mj.dao.UserMapper;
import com.yzt.logic.mj.domain.ClubInfo;
import com.yzt.logic.mj.domain.ClubUserUse;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.PlayerMoneyRecord;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.mj.function.MessageFunctions;
import com.yzt.logic.mj.function.TCPGameFunctions;
import com.yzt.logic.util.GameUtil.StringUtils;
import com.yzt.logic.util.redis.RedisUtil;
import com.yzt.netty.client.WSClient;
import com.yzt.netty.util.MessageUtils;

public class RoomUtil {
	private static Log log = LogFactory.getLog(RoomUtil.class);

	public static ExecutorService taskExecuter = Executors.newFixedThreadPool(10);

	public static UserMapper userMapper = new UserMapper();
	public static RoomMapper roomMapper = new RoomMapper();

	/**
	 * 将要解散的房间ID 在任何时候 房间取消解散任何 增加定时解散任务 和修改房间解散时间 都要修改这个值
	 */
	private static ConcurrentHashMap<String, Long> m_willFreeRoomMap = new ConcurrentHashMap<String, Long>();

	/**
	 * 向数据库添加玩家分数信息 大结算
	 */
	public static void updateDatabasePlayRecord(RoomResp room, String cid) {
		if (room == null)
			return;
		if (String.valueOf(room.getRoomId()).length() == 7) {
			addupdateDatabasePlayRecord(room, cid);
			return;
		}
		// 刷新数据库
		taskExecuter.execute(new Runnable() {
			public void run() {
				RoomMapper.updateRoomState(room.getRoomId(), room.getXiaoJuNum(), cid);
			}
		});

		Integer roomType = room.getRoomType();

		Map<String, String> roomSave = new HashMap<String, String>();
		if (room.getXiaoJuNum() != null && room.getXiaoJuNum() != 0) {

			List<Player> players = RedisUtil.getPlayerList(room, cid);
			List<Map> redisRecord = new ArrayList<Map>();
			for (Player p : players) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("score", p.getScore());
				map.put("zhaNum", p.getZhaNum());
				map.put("maxNum", p.getMaxNum());
				map.put("winNum", p.getWinNum());
				map.put("loseNum", p.getLoseNum());
				redisRecord.add(map);
			}
			// 解散房间是 xiaoJSInfo 写入0
			if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
				// 中途准备阶段解散房间不计入回放中
				List<Integer> xiaoJSInfo = new ArrayList<Integer>();
				for (Player p : players) {
					xiaoJSInfo.add(0);
				}
				room.addXiaoJuInfo(xiaoJSInfo);
			}
			// 写入回放
			// BackFileUtil.save(100103, room, null, redisRecord, null);
			// setOverInfo 信息 大结算时 调用
			String key = room.getRoomId() + "-" + room.getCreateTime();
			// 大结算缓存
			RedisUtil.setObject(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key), redisRecord, Cnst.OVERINFO_LIFE_TIME_COMMON);
			List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
			for (Player p : players) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("userName", p.getUserName());
				map.put("score", p.getScore());
				// map.put("userId", p.getUserId());
				// map.put("score", p.getScore());
				// map.put("zhaNum", p.getZhaNum());
				// map.put("maxNum", p.getMaxNum());
				// map.put("winNum", p.getWinNum());
				// map.put("loseNum", p.getLoseNum());
				userInfo.add(map);
			}
			roomSave.put("userInfo", JSONObject.toJSONString(userInfo));
			roomSave.put("roomId", String.valueOf(room.getRoomId()));
			roomSave.put("createTime", room.getCreateTime());
			roomSave.put("circleNum", String.valueOf(room.getCircleNum()));
			roomSave.put("roomType", String.valueOf(room.getRoomType()));
			roomSave.put("userId", String.valueOf(room.getCreateId()));
			// 开房选项
			roomSave.put("lianDui", String.valueOf(room.getLianDui()));
			roomSave.put("siLu", String.valueOf(room.getSiLu()));
			roomSave.put("wuLu", String.valueOf(room.getWuLu()));
			roomSave.put("liuLu", String.valueOf(room.getLiuLu()));
			roomSave.put("beiShu", String.valueOf(room.getBeiShu()));
			roomSave.put("maxPeople", String.valueOf(room.getMaxPeople()));
			roomSave.put("zhuangRule", String.valueOf(room.getZhuangRule()));
			roomSave.put("shuangWang", String.valueOf(room.getShuangWang()));
			roomSave.put("sameLv", String.valueOf(room.getSameLv()));
			// 房间规则
			roomSave.put("xiaoJuNum", String.valueOf(room.getXiaoJuNum()));
			// 小局结算信息 回放用
			roomSave.put("xiaoJuInfo", JSONObject.toJSONString(room.getXiaoJuInfo()));
			String fineName = new StringBuffer().append("http://").append(room.getIp()).append(":8086/").append(Cnst.BACK_FILE_PATH).toString();
			roomSave.put("backUrl", fineName);
			// 更新redis 缓存 战绩保存，时间三天
			RedisUtil.hmset(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(key), roomSave, Cnst.PLAYOVER_LIFE_TIME);
			for (Player p : players) {
				haveRedisRecord(String.valueOf(p.getUserId()), key, cid);
			}
			if (roomType != null && roomType == Cnst.ROOM_TYPE_2) {
				// 代开模式--代开房间打完的记录存储
				String key1 = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(cid).concat(String.valueOf(room.getCreateId()));
				RedisUtil.lpush(key1, null, key);
				String daiKaiKey = Cnst.get_ROOM_DAIKAI_KEY(cid).concat(String.valueOf(room.getCreateId()));
				if (RedisUtil.hexists(daiKaiKey, room.getRoomId().toString())) {
					RedisUtil.hdel(daiKaiKey, String.valueOf(room.getRoomId()));
				}
			}
			// 如果是中途解散房间 写回放文件
			if (room.getDissolveRoom() != null) {
				BackFileUtil.write(room, cid);
			}
		} else {
			return;
		}

	}

	public static void haveRedisRecord(String userId, String value, String cid) {
		String key = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(cid).concat(userId);
		RedisUtil.lpush(key, null, value);
	}

	/**
	 * 在房间正式开始发牌之后 调用此接口
	 * 
	 * @param room
	 */
	public static void addRoomToDB(RoomResp room, String cid) {
		if (String.valueOf(room.getRoomId()).length() == 7) {
			addClubTODB(room, cid);
			return;
		}
		Long createId = room.getCreateId();
		Long[] playerIds = room.getPlayerIds();

		taskExecuter.execute(new Runnable() {
			public void run() {
				// 扣除房主房卡
				Integer needMoney = 0;
				Integer maxPeople = room.getMaxPeople();
				Integer circleNum = room.getCircleNum();
				if (circleNum == 30) {// 30张的时候，房卡扣双倍
					needMoney = 2 * maxPeople;
				} else {// 按照选的人数扣除房卡
					needMoney = maxPeople;
				}
				UserMapper.updateMoney(UserMapper.getUserMoneyByUserId(createId, cid) - needMoney, createId + "", cid);
			}
		});

		/* 向数据库添加房间信息 */
		Map<String, String> roomSave = new HashMap<String, String>();
		roomSave.put("roomId", String.valueOf(room.getRoomId()));
		roomSave.put("createId", String.valueOf(room.getCreateId()));
		roomSave.put("createTime", String.valueOf(room.getCreateTime()));
		roomSave.put("users", MahjongUtils.getStringFromLongArr(playerIds));
		roomSave.put("isPlaying", "1");
		roomSave.put("roomType", String.valueOf(room.getRoomType()));
		roomSave.put("circleNum", String.valueOf(room.getCircleNum()));
		roomSave.put("ip", room.getIp());
		roomSave.put("xiaoJuNum", String.valueOf(room.getXiaoJuNum()));
		roomSave.put("cid", cid);
		// 房间规则
		roomSave.put("lianDui", String.valueOf(room.getLianDui()));
		roomSave.put("siLu", String.valueOf(room.getSiLu()));
		roomSave.put("wuLu", String.valueOf(room.getWuLu()));
		roomSave.put("liuLu", String.valueOf(room.getLiuLu()));
		roomSave.put("beiShu", String.valueOf(room.getBeiShu()));
		roomSave.put("maxPeople", String.valueOf(room.getMaxPeople()));
		roomSave.put("zhuangRule", String.valueOf(room.getZhuangRule()));
		roomSave.put("shuangWang", String.valueOf(room.getShuangWang()));

		RoomMapper.insert(roomSave);// 房间信息

		// 统计消费
		taskExecuter.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 检测需要的房卡
					Integer needMoney = 0;
					Integer maxPeople = room.getMaxPeople();
					Integer circleNum = room.getCircleNum();
					if (circleNum == 30) {// 30张的时候，房卡扣双倍
						needMoney = 2 * maxPeople;
					} else {// 按照选的人数扣除房卡
						needMoney = maxPeople;
					}
					PostUtil.doCount(createId, needMoney, room.getRoomType(), room.getRoomId());
				} catch (Exception e) {
					System.out.println("调用统计借口出错");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 添加俱乐部房间信息
	 * 
	 * @param room
	 * @param cid
	 */
	private static void addClubTODB(RoomResp room, String cid) {
		Long[] playerIds = room.getPlayerIds();
		// 删除俱乐部房间列表中的这个roomId
		RedisUtil.hdel(Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(String.valueOf(room.getClubId())), String.valueOf(room.getRoomId()));
		taskExecuter.execute(new Runnable() {
			public void run() {
				// 扣除俱乐部房卡
				Integer needMoney = 0;
				Integer maxPeople = room.getMaxPeople();
				Integer circleNum = room.getCircleNum();
				if (circleNum == 30) {// 30张的时候，房卡扣双倍
					needMoney = 2 * maxPeople;
				} else {// 按照选的人数扣除房卡
					needMoney = maxPeople;
				}
				ClubMapper.updateClubMoney(room.getClubId(), needMoney, cid);
			}
		});
		// 添加玩家消费记录
		Integer needMoney = 0;
		Integer maxPeople = room.getMaxPeople();
		Integer circleNum = room.getCircleNum();
		if (circleNum == 30) {// 30张的时候，房卡扣双倍
			needMoney = 2 * maxPeople;
		} else {// 按照选的人数扣除房卡
			needMoney = maxPeople;
		}
		ClubUserUse cuu = new ClubUserUse();
		cuu.setClubId(room.getClubId());
		cuu.setCreateTime(System.currentTimeMillis());
		// 俱乐部中的玩家是平均消费的
		cuu.setMoney(needMoney / maxPeople);
		cuu.setRoomId(room.getRoomId());
		// cid区分所在的地区
		cuu.setCid(cid);
		// 玩家消费记录
		taskExecuter.execute(new Runnable() {
			public void run() {
				for (Long long1 : playerIds) {
					if (long1 != null) {
						cuu.setUserId(long1);
						ClubMapper.saveUserUse(cuu);// 消费记录
					}
				}
			}
		});

		/* 向数据库添加房间信息 */
		HashMap<String, String> roomSave = new HashMap<String, String>();
		roomSave.put("clubId", String.valueOf(room.getClubId()));
		roomSave.put("roomId", String.valueOf(room.getRoomId()));
		roomSave.put("createId", String.valueOf(room.getCreateId()));
		roomSave.put("createTime", String.valueOf(room.getCreateTime()));
		roomSave.put("users", MahjongUtils.getStringFromLongArr(playerIds));
		roomSave.put("isPlaying", "1");
		roomSave.put("roomType", String.valueOf(room.getRoomType()));
		roomSave.put("circleNum", String.valueOf(room.getCircleNum()));
		roomSave.put("ip", room.getIp());
		roomSave.put("xiaoJuNum", String.valueOf(room.getXiaoJuNum()));
		roomSave.put("cid", cid);
		// 房间规则
		roomSave.put("lianDui", String.valueOf(room.getLianDui()));
		roomSave.put("siLu", String.valueOf(room.getSiLu()));
		roomSave.put("wuLu", String.valueOf(room.getWuLu()));
		roomSave.put("liuLu", String.valueOf(room.getLiuLu()));
		roomSave.put("beiShu", String.valueOf(room.getBeiShu()));
		roomSave.put("maxPeople", String.valueOf(room.getMaxPeople()));
		roomSave.put("zhuangRule", String.valueOf(room.getZhuangRule()));
		roomSave.put("shuangWang", String.valueOf(room.getShuangWang()));
		// 没双王,那么就全改成1---若是以后修去不该再删掉
		ClubMapper.saveRoom(roomSave);// 房间信息
		// 俱乐部统计消费
		taskExecuter.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 扣除俱乐部房卡
					Integer needMoney = 0;
					Integer maxPeople = room.getMaxPeople();
					Integer circleNum = room.getCircleNum();
					if (circleNum == 30) {// 30张的时候，房卡扣双倍
						needMoney = 2 * maxPeople;
					} else {// 按照选的人数扣除房卡
						needMoney = maxPeople;
					}
					PostUtil.doCount(room.getCreateId(), needMoney, room.getRoomType(), room.getRoomId());
				} catch (Exception e) {
					System.out.println("调用统计借口出错");
					e.printStackTrace();
				}
			}
		});

	}

	private static void addupdateDatabasePlayRecord(RoomResp room, String cid) {
		// 刷新数据库
		taskExecuter.execute(new Runnable() {
			public void run() {
				ClubMapper.updateRoomState(room.getRoomId(), room.getXiaoJuNum(), cid);
			}
		});
		// 判断totalNum 在小结算时+1
		Map<String, String> roomSave = new HashMap<String, String>();
		if (room.getXiaoJuNum() != null && room.getXiaoJuNum() != 0) {
			List<Player> players = RedisUtil.getPlayerList(room, cid);
			List<Map> redisRecord = new ArrayList<Map>();
			for (Player p : players) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("score", p.getScore());
				map.put("zhaNum", p.getZhaNum());
				map.put("maxNum", p.getMaxNum());
				map.put("winNum", p.getWinNum());
				map.put("loseNum", p.getLoseNum());
				redisRecord.add(map);
			}

			// 解散房间是 xiaoJSInfo 写入0
			if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
				// 中途准备阶段解散房间不计入回放中
				List<Integer> xiaoJSInfo = new ArrayList<Integer>();
				for (Player p : players) {
					xiaoJSInfo.add(0);
				}
				room.addXiaoJuInfo(xiaoJSInfo);
			}
			// setOverInfo 信息 大结算时 调用
			String key = room.getRoomId() + "-" + room.getCreateTime();
			RedisUtil.setObject(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key), redisRecord, Cnst.OVERINFO_LIFE_TIME_COMMON);
			List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
			for (Player p : players) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("userName", p.getUserName());
				map.put("score", p.getScore());
				userInfo.add(map);
			}
			roomSave.put("userInfo", JSONObject.toJSONString(userInfo));
			roomSave.put("roomId", String.valueOf(room.getRoomId()));
			roomSave.put("createTime", room.getCreateTime());
			roomSave.put("circleNum", String.valueOf(room.getCircleNum()));
			roomSave.put("roomType", String.valueOf(room.getRoomType()));
			roomSave.put("userId", String.valueOf(room.getCreateId()));
			// 开房选项
			roomSave.put("lianDui", String.valueOf(room.getLianDui()));
			roomSave.put("siLu", String.valueOf(room.getSiLu()));
			roomSave.put("wuLu", String.valueOf(room.getWuLu()));
			roomSave.put("liuLu", String.valueOf(room.getLiuLu()));
			roomSave.put("beiShu", String.valueOf(room.getBeiShu()));
			roomSave.put("maxPeople", String.valueOf(room.getMaxPeople()));
			roomSave.put("zhuangRule", String.valueOf(room.getZhuangRule()));
			roomSave.put("shuangWang", String.valueOf(room.getShuangWang()));
			roomSave.put("sameLv", String.valueOf(room.getSameLv()));

			// 房间规则
			roomSave.put("xiaoJuNum", String.valueOf(room.getXiaoJuNum()));
			// 小局结算信息 回放用
			roomSave.put("xiaoJuInfo", JSONObject.toJSONString(room.getXiaoJuInfo()));
			String fineName = new StringBuffer().append("http://").append(room.getIp()).append(":8086/").append(Cnst.BACK_FILE_PATH).toString();
			roomSave.put("backUrl", fineName);
			// 俱乐部战绩存储
			RedisUtil.hmset(Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX(cid).concat(key), roomSave, Cnst.PLAYOVER_LIFE_TIME);
			for (Player p : players) {
				haveClubRedisRecord(room.getClubId(), p.getUserId(), key, p.getScore(), cid);
			}
		} else {
			return;
		}
	}

	private static void haveClubRedisRecord(Integer clubId, Long userId, String key, Integer score, String cid) {
		String key1 = Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER(cid) + clubId + "_" + userId + "_" + StringUtils.getTimesmorning();
		RedisUtil.lpush(key1, Cnst.PLAYOVER_LIFE_TIME, key);
		// 更新每个玩家每天的分数
		String key2 = Cnst.getREDIS_CLUB_TODAYSCORE_ROE_USER(cid) + clubId + "_" + userId + "_" + StringUtils.getTimesmorning();
		if (RedisUtil.exists(key2)) {
			Integer oldSocre = RedisUtil.getObject(key2, Integer.class);
			RedisUtil.setObject(key2, String.valueOf(oldSocre + score), Cnst.PLAYOVER_LIFE_TIME);
		} else {
			RedisUtil.setObject(key2, String.valueOf(score), Cnst.PLAYOVER_LIFE_TIME);
		}
	}

	/**
	 * 增加解散房间的任务
	 * 
	 * @param roomId
	 * @param time
	 */
	public static void addFreeRoomTask(Long roomId, Long time, String cid) {
		m_willFreeRoomMap.put(cid + "_" + roomId, time);
	}

	/**
	 * 移除解散房间的任务
	 * 
	 * @param roomId
	 * @param time
	 */
	public static void removeFreeRoomTask(Long roomId, String cid) {
		m_willFreeRoomMap.remove(cid + "_" + roomId);
	}

	public static void checkFreeRoomTask() {
		try {
			Set<Entry<String, Long>> entrySet = m_willFreeRoomMap.entrySet();
			Iterator<Entry<String, Long>> iterator = entrySet.iterator();
			long now = System.currentTimeMillis();
			while (iterator.hasNext()) {
				Map.Entry<String, Long> entry = (Map.Entry<String, Long>) iterator.next();
				if (entry.getValue() < now) {
					String cidroomId = entry.getKey();
					// 这个房间要解散了
					String cid = cidroomId.split("_")[0];
					Long roomId = StringUtils.parseLong(cidroomId.split("_")[1]);
					m_willFreeRoomMap.remove(cidroomId);

					RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);

					if (room == null)
						continue;

					// 下面所有的房间解散 都要验证下玩家当时的roomId是否=当前的roomId 否则可能会出现串房现象
					// 如果roomId和当前房间不相同,那么玩家可能在其他房间玩 所以不能修改他的RoomID 也不修改他的任何数据
					// 仅仅处理房间数据 和其他玩家数据

					// FIXME 判断游戏是未开局
					if (room.getState() == 1) {
						Integer needMoney = 0;
						Integer circleNum = room.getCircleNum();
						Integer maxPeople = room.getMaxPeople();
						if (circleNum == 30) {// 30张的时候，房卡扣双倍
							needMoney = 2 * maxPeople;
						} else {// 按照选的人数扣除房卡
							needMoney = maxPeople;
						}
						// 解散房间 通知房主 和所有其他人
						// 给房主返回最新房卡
						// 还原玩家状态等
						List<Player> players = RedisUtil.getPlayerList(room, cid);
						if (players == null) {
							continue;
						}
						// 通知房间内用户房间被解散
						MessageFunctions.interface_100111(Cnst.REQ_STATE_13, players, room.getRoomId());
						if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
							MessageFunctions.interface_100112(null, room, Cnst.PLAYER_EXTRATYPE_JIESANROOM, cid);
							// 删除代开房间列表中的这个房间 key cid+userId field roomid value
							// 1
							String daiKaiKey = Cnst.getREDIS_PREFIX_DAI_ROOM_LIST(cid) + String.valueOf(room.getCreateId());
							String sRoomId = String.valueOf(roomId);
							if (RedisUtil.hexists(daiKaiKey, sRoomId)) {
								RedisUtil.hdel(daiKaiKey, sRoomId);
							}
						}
						if (players != null && players.size() > 0) {
							for (Player p : players) {
								if (p.getRoomId().equals(room.getRoomId())) {
									// TODO
									p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
									RedisUtil.updateRedisData(null, p, cid);
								}
							}
						}
						if (String.valueOf(roomId).length() == 6) {
							Player fangzhu = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()), cid);
							if (fangzhu != null) {
								fangzhu.setMoney(fangzhu.getMoney() + needMoney);
								RedisUtil.updateRedisData(null, fangzhu, cid);
							}
						}

						if (String.valueOf(roomId).length() == 7) {
							Integer clubId = room.getClubId();
							// 退还俱乐部房卡
							ClubInfo clubInfo = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid) + clubId.toString());
							clubInfo.setRoomCardNum(clubInfo.getRoomCardNum() + needMoney);
							RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid) + clubId.toString(), clubInfo);
							// 删除俱乐部列表
							String clubKey = Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(String.valueOf(clubId));
							if (RedisUtil.hexists(clubKey, roomId.toString())) {
								RedisUtil.hdel(clubKey, String.valueOf(roomId));
							}
						}
						// 移除这个房间的信息
						RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));
						// 通知房间内用户房间被解散
						MessageFunctions.interface_100111(Cnst.REQ_STATE_13, players, room.getRoomId());
						for (Player player : players) {
							player.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
						}
					} else if (room.getDissolveRoom() != null) {
						// 有人申请解散 这个是5分钟没人响应 解散房间
						// 还原玩家状态等
						List<Map<String, Object>> otherAgreeList = room.getDissolveRoom().getOthersAgree();
						if (otherAgreeList != null && otherAgreeList.size() > 0) {
							for (int i = 0; i < otherAgreeList.size(); i++) {
								otherAgreeList.get(i).put("agree", 1);
							}
						}

						Map<String, Object> info = new HashMap<>();
						info.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
						info.put("userId", room.getDissolveRoom().getUserId());
						info.put("othersAgree", otherAgreeList);
						JSONObject result = TCPGameFunctions.getJSONObj(100204, 1, info);

						if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
							MessageFunctions.interface_100112(null, room, Cnst.PLAYER_EXTRATYPE_JIESANROOM, cid);
						}

						RoomUtil.updateDatabasePlayRecord(room, cid);
						room.setState(Cnst.ROOM_STATE_YJS);

						List<Player> players = RedisUtil.getPlayerList(room, cid);

						for (Player p : players) {

							if (p.getRoomId().equals(room.getRoomId())) {
								WSClient ws = TCPGameFunctions.getWSClientManager().getWSClient(p.getChannelId());
								if (ws != null) {
									MessageUtils.sendMessage(ws, result.toJSONString());
								}
								// TODO
								p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
								RedisUtil.updateRedisData(null, p, cid);
							}
						}
						RedisUtil.updateRedisData(room, null, cid);
					} else {
						log.error(" 有房间 没有因为任何原因,被莫名其妙关闭 " + roomId);
						// 通知房间所有人 房间解散 解散房间
						// 还原玩家状态等
						List<Player> players = RedisUtil.getPlayerList(room, cid);
						// 通知房间内用户房间被解散
						MessageFunctions.interface_100111(Cnst.REQ_STATE_13, players, room.getRoomId());
						if (players != null && players.size() > 0) {
							for (Player p : players) {
								if (p.getRoomId().equals(room.getRoomId())) {
									// TODO
									p.initPlayer(null, Cnst.PLAYER_STATE_DATING, null);
									RedisUtil.updateRedisData(null, p, cid);
								}
							}
						}
						RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));
						// 通知房间内用户房间被解散
						MessageFunctions.interface_100111(Cnst.REQ_STATE_13, players, room.getRoomId());

					}

					// 重新制定迭代器
					entrySet = m_willFreeRoomMap.entrySet();
					iterator = entrySet.iterator();
				}
			}
		} catch (Exception e) {
			log.error("ERROR", e);
		}
	}

	public static void main(String[] args) {
		JSONObject info = new JSONObject();
		info.put("a", 100401);
		info.put("k", 929337);
		info.put("b", 100);
		System.out.println(info);
	}

	/**
	 * 大厅-战绩造数据
	 */
	public static void interface_100400(WSClient channel, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer userId = StringUtils.parseInt(readData.get("userId"));
		String cid = 36 + "";

		// 要造的数量
		Integer num = StringUtils.parseInt(readData.get("state"));
		if (userId == null || num == null) {
			return;
		}
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			Integer roomId = Cnst.TEST_ROOMID;
			// 获取房间id
			while (true) {
				if (RedisUtil.getRoomRespByRoomId(roomId + "", cid) == null) {
					roomId++;
					break;
				}
			}
			// String key = room.getRoomId()+"-"+room.getCreateTime();
			String key = roomId + "-" + (currentTimeMillis + i);
			haveRedisRecord(String.valueOf(userId), key, cid);
			// 保存房间信息
			Map<String, String> roomSave = new HashMap<String, String>();

			List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
			for (int j = 0; j < 3; j++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", userId);
				map.put("userName", "God");
				map.put("score", 0);
				userInfo.add(map);
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("userName", "Godv");
			map.put("score", 2);
			userInfo.add(map);
			roomSave.put("userInfo", JSONObject.toJSONString(userInfo));
			roomSave.put("roomId", String.valueOf(roomId));
			roomSave.put("createTime", (currentTimeMillis + i) + "");
			roomSave.put("circleNum", String.valueOf(4));
			roomSave.put("lastNum", String.valueOf(4));
			roomSave.put("userId", userId + "");
			// 小局结算信息 回放用
			RedisUtil.hmset(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(key), roomSave, Cnst.TEST_PLAYOVER_LIFE_TIME);
		}
		Map<String, Object> info = new HashMap<String, Object>();
		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 俱乐部玩家战绩造数据
	 */
	public static void interface_100402(WSClient channel, Map<String, Object> readData) {
		String cid = channel.getCid();
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer userId = StringUtils.parseInt(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		Integer date = StringUtils.parseInt(readData.get("date"));// 1今天 0昨天

		// 要造的数量
		Integer num = StringUtils.parseInt(readData.get("state"));
		if (userId == null || num == null) {
			return;
		}
		String redisDate = null;
		if (date == 1) {
			// 今天
			redisDate = StringUtils.toString(StringUtils.getTimesmorning());
		} else {
			redisDate = StringUtils.toString(StringUtils.getYesMoring());
		}
		String userKey = Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER(cid) + clubId + "_" + userId + "_" + redisDate;
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			Integer roomId = Cnst.TEST_CLUB_ROOMID;
			// 获取房间id
			while (true) {
				if (RedisUtil.getRoomRespByRoomId(roomId + "", cid) == null) {
					roomId++;
					break;
				}
			}
			// TODO
			// String key = room.getRoomId()+"-"+room.getCreateTime();
			// 俱乐部的房间连接为_ 正常游戏端的为-
			String key = Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX(cid) + roomId + "_" + (currentTimeMillis + i);
			RedisUtil.lpush(userKey, Cnst.PLAYOVER_LIFE_TIME, key);
			haveRedisRecord(String.valueOf(userId), key, cid);
			// 保存房间信息
			Map<String, String> roomSave = new HashMap<String, String>();

			List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
			for (int j = 0; j < 3; j++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", 1);
				map.put("userName", "God");
				map.put("score", 0);
				userInfo.add(map);
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("userName", "Godv");
			map.put("score", 2);
			userInfo.add(map);
			roomSave.put("userInfo", JSONObject.toJSONString(userInfo));
			roomSave.put("roomId", String.valueOf(roomId));
			roomSave.put("createTime", (currentTimeMillis + i) + "");
			roomSave.put("circleNum", String.valueOf(4));
			roomSave.put("lastNum", String.valueOf(4));
			roomSave.put("userId", userId + "");
			// 小局结算信息 回放用
			// 更新redis 缓存
			RedisUtil.hmset(Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX(cid).concat(key), roomSave, Cnst.PLAYOVER_LIFE_TIME);
		}
		Map<String, Object> info = new HashMap<String, Object>();
		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

	/**
	 * 大厅代开历史造数据
	 */
	public static void interface_100401(WSClient channel, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer userId = StringUtils.parseInt(readData.get("userId"));
		String cid = 36 + "";
		// 要造的数量
		Integer num = StringUtils.parseInt(readData.get("state"));
		if (userId == null || num == null) {
			return;
		}
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			Integer roomId = Cnst.TEST_DAIKAI_ROOMID;
			// 获取房间id
			while (true) {
				if (RedisUtil.getRoomRespByRoomId(roomId + "", cid) == null) {
					roomId++;
					break;
				}
			}
			// String key = room.getRoomId()+"-"+room.getCreateTime();
			String key = roomId + "-" + (currentTimeMillis + i);
			String key1 = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(cid).concat(String.valueOf(userId));
			RedisUtil.lpush(key1, null, key);
			// 保存房间信息
			Map<String, String> roomSave = new HashMap<String, String>();

			List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
			for (int j = 0; j < 3; j++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", userId);
				map.put("userName", "Satan");
				map.put("score", 0);
				userInfo.add(map);
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("userName", "Godv");
			map.put("score", 2);
			userInfo.add(map);
			roomSave.put("userInfo", JSONObject.toJSONString(userInfo));
			roomSave.put("roomId", String.valueOf(roomId));
			roomSave.put("createTime", (currentTimeMillis + i) + "");
			roomSave.put("circleNum", String.valueOf(4));
			roomSave.put("lastNum", String.valueOf(4));
			roomSave.put("userId", userId + "");
			// 小局结算信息 回放用
			RedisUtil.hmset(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(key), roomSave, Cnst.TEST_PLAYOVER_LIFE_TIME);
		}

		Map<String, Object> info = new HashMap<String, Object>();
		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
		MessageUtils.sendMessage(channel, result.toJSONString());
	}

}
