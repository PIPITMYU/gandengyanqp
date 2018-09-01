package com.yzt.logic.util.GameUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Action;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;
import com.yzt.logic.util.BackFileUtil;
import com.yzt.logic.util.Cnst;
import com.yzt.logic.util.MahjongUtils;
import com.yzt.logic.util.RoomUtil;
import com.yzt.logic.util.redis.RedisUtil;

/**
 * 玩家分的统计
 * 
 * @author wsw_007
 *
 */
public class JieSuan {
	public static void xiaoJieSuan(String roomId, String cid) {
		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId, cid);
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		// 一圈一定会有赢家，谁赢谁是庄
		// 设置赢得玩家
		Player winPlayer = null;
		// 设置玩家牌数
		int playerPaiSize = 0;
		// 获取输的分数
		int loseScore = 0;
		// 获取赢的分数
		int winScore = 0;
		// 获取房间的总倍数
		Integer nowBeiShu = room.getNowBeiShu();
		if (nowBeiShu == null) {
			nowBeiShu = 1;
		}
		if (room.getBeiShu() != -1) {// 说明是限制倍数的
			if (room.getBeiShu() < nowBeiShu) {
				// 倍数按照房间最大倍数算
				nowBeiShu = room.getBeiShu();
			}
		}
		for (Player player : players) {
			if (player.getIsHu()) {// 找到赢得玩家
				player.setWinNum(player.getWinNum() + 1);
				winPlayer = player;
			} else {// 都是输的玩家
					// 查看每个玩家的手牌
				player.setLoseNum(player.getLoseNum() + 1);
				playerPaiSize = player.getCurrentPaiList().size();
				if (playerPaiSize >= 5) {
					loseScore = 10;
				} else {
					loseScore = playerPaiSize;
				}
				loseScore = -loseScore * nowBeiShu;
				// 设置玩家此局的分
				player.setThisScore(loseScore);
				// 设置玩家总分
				player.setScore(player.getScore() + player.getThisScore());
				winScore += -loseScore;
			}
		}

//		}
		winPlayer.setThisScore(winScore);
		if (winPlayer.getMaxNum() < winScore) {
			winPlayer.setMaxNum(winScore);
		}
		winPlayer.setScore(winPlayer.getScore() + winScore);
		// 赢的人坐庄
		if (room.getZhuangRule().equals(1)) {
			room.setZhuangId(winPlayer.getUserId());
		} else {// 轮流做庄]
			Long setZhuangId = MahjongUtils.getNextZhungPlayerId(room.getZhuangId(), players);
			room.setZhuangId(setZhuangId);
		}
		room.setCircleWind(winPlayer.getPosition());
		// 剩余圈数
		room.setLastNum(room.getLastNum() - 1);
		// 更新redis
		RedisUtil.setPlayersList(players, cid);

		// 添加小结算信息
		List<Integer> xiaoJS = new ArrayList<Integer>();
		for (Player p : players) {
			xiaoJS.add(p.getThisScore());
		}
		room.addXiaoJuInfo(xiaoJS);
		// 写入文件
		List<Map<String, Object>> userInfos = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", p.getUserId());
			Integer pScore = p.getThisScore();
			// //如果是第一局
			map.put("score", pScore);
			// 牌
			List<Integer> currentPaiList = p.getCurrentPaiList();
			if (p.getIsHu()) {
				map.put("isWin", 1);
				if (currentPaiList != null && currentPaiList.size() > 0) {
					map.put("pais", p.getCurrentPaiList());
				}
			} else {
				map.put("pais", MahjongUtils.paiXu(currentPaiList));
				map.put("isWin", 0);
			}
			userInfos.add(map);
		}
		JSONObject info = new JSONObject();
		info.put("lastNum", room.getLastNum());
		info.put("beiShu", room.getNowBeiShu());
		info.put("userInfo", userInfos);
		BackFileUtil.save(100102, room, null, info, null, cid);
		// 小结算 存入一次回放
		BackFileUtil.write(room, cid);
		// 初始化房间
		room.initRoom();
		RedisUtil.updateRedisData(room, null, cid);
		// 大结算判定 (玩的圈数等于选择的圈数)
		if (room.getLastNum() == 0) {
			// 最后一局 大结算
			// room = RedisUtil.getRoomRespByRoomId(roomId,cid);
			room.setState(Cnst.ROOM_STATE_YJS);
			RedisUtil.updateRedisData(room, null, cid);
			// 这里更新数据库吧
			RoomUtil.updateDatabasePlayRecord(room, cid);
		}
	}
}
