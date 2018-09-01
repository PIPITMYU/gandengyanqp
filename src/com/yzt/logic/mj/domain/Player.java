package com.yzt.logic.mj.domain;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by admin on 2017/6/26.
 */
/**
 * @author wsw_008
 *
 */
public class Player extends User {

	private Integer roomId;// 房间密码，也是roomSn

	// out离开状态（断线）;inline正常在线；
	private Integer state;
	private Integer position;// 位置信息；详见Cnst
	private String notice;// 跑马灯信息
	private Integer playStatus;// 用户当前状态， dating用户在大厅中; in刚进入房间，等待状态;prepared准备状态;game游戏中; xjs小结算
	private String channelId;// 通道id
	private Long updateTime;// 更新用户数据时间
	
	
	private List<Integer> currentPaiList;// 用户手中当前的牌
	
	private Boolean isHu;//小结算用
	private Integer score;// 玩家这全游戏的总分
	private Integer thisScore;// 记录当玩家当前局
	private Boolean hasChu;//有没有出过牌
	//大结算的一些统计
	private Integer zhaNum;// 炸的次数
	private Integer maxNum;// 单据最高分
	private Integer winNum;// 赢得次数
	private Integer loseNum;// 输的次数
	
	private Double x_index;
	private Double y_index;

	public void initPlayer(Integer roomId,Integer playStatus,Integer score) {
		if(roomId == null){
			this.roomId = null;
		}
		this.playStatus = playStatus;
		this.isHu = false;
		this.score = score;
		this.thisScore = 0;
		this.currentPaiList = new ArrayList<Integer>();
		this.hasChu = false;
	}

	
	public Boolean getHasChu() {
		return hasChu;
	}


	public void setHasChu(Boolean hasChu) {
		this.hasChu = hasChu;
	}


	public Integer getZhaNum() {
		return zhaNum;
	}


	public void setZhaNum(Integer zhaNum) {
		this.zhaNum = zhaNum;
	}


	public Integer getMaxNum() {
		return maxNum;
	}


	public void setMaxNum(Integer maxNum) {
		this.maxNum = maxNum;
	}


	public Integer getWinNum() {
		return winNum;
	}


	public void setWinNum(Integer winNum) {
		this.winNum = winNum;
	}


	public Integer getLoseNum() {
		return loseNum;
	}


	public void setLoseNum(Integer loseNum) {
		this.loseNum = loseNum;
	}





	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}


	
	public List<Integer> getCurrentPaiList() {
		return currentPaiList;
	}




	public void setCurrentPaiList(List<Integer> currentPaiList) {
		this.currentPaiList = currentPaiList;
	}


	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}


	public Boolean getIsHu() {
		return isHu;
	}

	public void setIsHu(Boolean isHu) {
		this.isHu = isHu;
	}


	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getThisScore() {
		return thisScore;
	}

	public void setThisScore(Integer thisScore) {
		this.thisScore = thisScore;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getPlayStatus() {
		return playStatus;
	}

	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}





	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}


	public Double getX_index() {
		return x_index;
	}




	public void setX_index(Double x_index) {
		this.x_index = x_index;
	}




	public Double getY_index() {
		return y_index;
	}




	public void setY_index(Double y_index) {
		this.y_index = y_index;
	}







	
}
