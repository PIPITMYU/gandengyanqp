package com.yzt.logic.mj.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.yzt.netty.client.WSClient;

/**
 * Created by Administrator on 2017/7/8.
 */
/**
 * @author wsw_008
 *
 */
public class RoomResp extends Room {

	private static final long serialVersionUID = -5308844344084689942L;
	// 本房间状态，1等待玩家入坐；2游戏中；3小结算
	private Integer state;

	// 游戏里使用
	// 坐庄方式(逆时针坐庄),庄胡牌继续坐庄,荒庄下庄
	private Integer lastNum;// 房间剩余局数
	private Integer circleNum;// 总局数
	private Integer xiaoJuNum;// 局数
	private Integer roomAction;// 房间状态 2种:出和摸

	// 这来字段断线重连给,为了显示风向和发牌
	private Long lastChuUserId;// 最后一个出牌玩家
	private List<Integer> lastChuPai;// 从出牌玩家的下家开始的有动作的玩家集合
	private Integer lastFaPai;// 房间最后发的那张牌

	private Integer position;// 找到当前的玩家风向
	private Integer nowBeiShu;// 此局的倍数
	private Long xjst;// 小局开始时间
	private Integer circleWind;// 当前庄的位置...
	private Long nextActionUserId;// 当前动作玩家
	private Integer nextAction;// 当前动作玩家的动作
	private Long lastPlayerUserId;// 上一个动作玩家
	private Integer action;// 上一个玩家的动作的上一个动作

	private List<Integer> initPositions;// 创建时根据模式初始化集合,随着加入的人而减少
	private List<Long> nowPlayerIds;// 从出牌玩家的下家开始的有动作的玩家集合
	private Long[] playerIds;// 玩家id集合
	private List<Integer> currentPaiList;// 房间内剩余牌集合；
	private Long zhuangId;// 庄的玩家id

	private List<List<Integer>> xiaoJuInfo = new ArrayList<List<Integer>>();// 只存本局的分,在结算里面存储,若是中途解散,在大结算中存储

	private List<List<Integer>> nextCanChulList;// 下个人能出的牌型集合
	private List<List<Integer>> nowChulList;// 1：本轮出牌集合,有人缴牌时清空,2:上个出牌人是自己的时候清空
	private List<List<Integer>> chulList;// 出的牌的集合
	private Boolean tianHu;
	private Long winPlayerId;// 当局胡牌的玩家
	private Boolean paiXu;//每次开局之后第一次请求出牌要排序

	// 申请解散使用
	private Integer createDisId;
	private Integer applyDisId;
	private Integer outNum;
	private DissolveRoom dissolveRoom;// 申请解散信息

	private Integer wsw_sole_action_id;// 吃碰杠出牌发牌id
	private String openName;
	private Collection<WSClient> group;// 房间 4个channel集合

	public void initRoom() {
		this.nowPlayerIds = new ArrayList<Long>(6);
		this.dissolveRoom = null;
		this.tianHu = false;
		this.xjst = null;
		this.winPlayerId = null;
		this.action = null;
		this.nextActionUserId = null;
		this.nextAction = null;
		this.lastChuUserId = null;
		this.lastChuPai = null;
		this.nextCanChulList = new ArrayList<List<Integer>>();// 初始化出牌集合
		this.chulList = new ArrayList<List<Integer>>();
		// 每轮的出牌集合
		this.nowChulList = new ArrayList<List<Integer>>();
		this.lastFaPai=null;
		this.lastPlayerUserId=null;
		this.paiXu=false;
		
	}

	public Boolean getPaiXu() {
		return paiXu;
	}

	public void setPaiXu(Boolean paiXu) {
		this.paiXu = paiXu;
	}

	public Boolean getTianHu() {
		return tianHu;
	}

	public void setTianHu(Boolean tianHu) {
		this.tianHu = tianHu;
	}

	public List<List<Integer>> getNowChulList() {
		return nowChulList;
	}

	public void setNowChulList(List<List<Integer>> nowChulList) {
		this.nowChulList = nowChulList;
	}

	public List<List<Integer>> getChulList() {
		return chulList;
	}

	public void setChulList(List<List<Integer>> chulList) {
		this.chulList = chulList;
	}

	public List<List<Integer>> getNextCanChulList() {
		return nextCanChulList;
	}

	public void setNextCanChulList(List<List<Integer>> nextCanChulList) {
		this.nextCanChulList = nextCanChulList;
	}

	public Integer getNowBeiShu() {
		return nowBeiShu;
	}

	public void setNowBeiShu(Integer nowBeiShu) {
		this.nowBeiShu = nowBeiShu;
	}

	public Integer getLastFaPai() {
		return lastFaPai;
	}

	public void setLastFaPai(Integer lastFaPai) {
		this.lastFaPai = lastFaPai;
	}

	public List<Integer> getLastChuPai() {
		return lastChuPai;
	}

	public void setLastChuPai(List<Integer> lastChuPai) {
		this.lastChuPai = lastChuPai;
	}

	public Long getNextActionUserId() {
		return nextActionUserId;
	}

	public void setNextActionUserId(Long nextActionUserId) {
		this.nextActionUserId = nextActionUserId;
	}

	public Integer getNextAction() {
		return nextAction;
	}

	public void setNextAction(Integer nextAction) {
		this.nextAction = nextAction;
	}

	public Integer getAction() {
		return action;
	}

	public void setAction(Integer action) {
		this.action = action;
	}

	public List<Integer> getInitPositions() {
		return initPositions;
	}

	public void setInitPositions(List<Integer> initPositions) {
		this.initPositions = initPositions;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Long getLastChuUserId() {
		return lastChuUserId;
	}

	public void setLastChuUserId(Long lastChuUserId) {
		this.lastChuUserId = lastChuUserId;
	}

	public Long getLastPlayerUserId() {
		return lastPlayerUserId;
	}

	public void setLastPlayerUserId(Long lastPlayerUserId) {
		this.lastPlayerUserId = lastPlayerUserId;
	}

	public List<Integer> getCurrentPaiList() {
		return currentPaiList;
	}

	public void setCurrentPaiList(List<Integer> currentPaiList) {
		this.currentPaiList = currentPaiList;
	}

	public Integer getRoomAction() {
		return roomAction;
	}

	public void setRoomAction(Integer roomAction) {
		this.roomAction = roomAction;
	}

	public Integer getXiaoJuNum() {
		return xiaoJuNum;
	}

	public void setXiaoJuNum(Integer xiaoJuNum) {
		this.xiaoJuNum = xiaoJuNum;
	}

	public Long getZhuangId() {
		return zhuangId;
	}

	public void setZhuangId(Long zhuangId) {
		this.zhuangId = zhuangId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getLastNum() {
		return lastNum;
	}

	public void setLastNum(Integer lastNum) {
		this.lastNum = lastNum;
	}

	public Integer getCircleNum() {
		return circleNum;
	}

	public void setCircleNum(Integer circleNum) {
		this.circleNum = circleNum;
	}

	public Long getXjst() {
		return xjst;
	}

	public void setXjst(Long xjst) {
		this.xjst = xjst;
	}

	public Integer getCircleWind() {
		return circleWind;
	}

	public void setCircleWind(Integer circleWind) {
		this.circleWind = circleWind;
	}

	public DissolveRoom getDissolveRoom() {
		return dissolveRoom;
	}

	public void setDissolveRoom(DissolveRoom dissolveRoom) {
		this.dissolveRoom = dissolveRoom;
	}

	public Integer getCreateDisId() {
		return createDisId;
	}

	public void setCreateDisId(Integer createDisId) {
		this.createDisId = createDisId;
	}

	public Integer getApplyDisId() {
		return applyDisId;
	}

	public void setApplyDisId(Integer applyDisId) {
		this.applyDisId = applyDisId;
	}

	public Integer getOutNum() {
		return outNum;
	}

	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}

	public Integer getWsw_sole_action_id() {
		return wsw_sole_action_id;
	}

	public void setWsw_sole_action_id(Integer wsw_sole_action_id) {
		this.wsw_sole_action_id = wsw_sole_action_id;
	}

	public String getOpenName() {
		return openName;
	}

	public void setOpenName(String openName) {
		this.openName = openName;
	}

	public Long[] getPlayerIds() {
		return playerIds;
	}

	public void setPlayerIds(Long[] playerIds) {
		this.playerIds = playerIds;
	}

	public Collection<WSClient> getGroup() {
		return group;
	}

	public void setGroup(Collection<WSClient> group) {
		this.group = group;
	}

	public List<List<Integer>> getXiaoJuInfo() {
		return xiaoJuInfo;
	}

	public void setXiaoJuInfo(List<List<Integer>> xiaoJuInfo) {
		this.xiaoJuInfo = xiaoJuInfo;
	}

	public void addXiaoJuInfo(List<Integer> list) {
		xiaoJuInfo.add(list);
	}


	public Long getWinPlayerId() {
		return winPlayerId;
	}

	public void setWinPlayerId(Long winPlayerId) {
		this.winPlayerId = winPlayerId;
	}

	public List<Long> getNowPlayerIds() {
		return nowPlayerIds;
	}

	public void setNowPlayerIds(List<Long> nowPlayerIds) {
		this.nowPlayerIds = nowPlayerIds;
	}

}
