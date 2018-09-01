/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.yzt.logic.mj.domain;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * 
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public class Room implements java.io.Serializable {

	private Long id;
	private String cid;
	private Integer roomId;
	private Long createId;
	private String createTime;
	private Integer isPlaying;
	private List<Long> userIds; // 玩家的id集合
	private Integer circleNum; // 圈数
	private Integer clubId;// 俱乐部id
	private String ip;// 当前房间所在服务器的ip
	// 游戏规则
	private Integer roomType;// 房间模式，房主模式1；自由模式2

	private Integer lianDui;// 连对：1不可以；2可以
	private Integer shuangWang;// 双王：1不可以；2可以
	private Integer siLu;// 四路炸弹：1不可以；2可以
	private Integer wuLu;// 五路炸弹：1不可以；2可以
	private Integer liuLu;// 六路炸弹：1不可以；2可以
	private Integer beiShu;// 倍数：8/16/32/-1(不封顶)
	private Integer maxPeople;// 人数上限：3/4/5/6
	private Integer zhuangRule;// 坐庄规则：1胜者坐庄 2轮流坐庄
	private Integer sameLv;// 同级必管 ：默认 1 （不选） 2 选

	
	
	public Integer getSameLv() {
		return sameLv;
	}

	public void setSameLv(Integer sameLv) {
		this.sameLv = sameLv;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Long getCreateId() {
		return createId;
	}

	public void setCreateId(Long createId) {
		this.createId = createId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Integer getIsPlaying() {
		return isPlaying;
	}

	public void setIsPlaying(Integer isPlaying) {
		this.isPlaying = isPlaying;
	}

	public List<Long> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}

	public Integer getCircleNum() {
		return circleNum;
	}

	public void setCircleNum(Integer circleNum) {
		this.circleNum = circleNum;
	}

	public Integer getClubId() {
		return clubId;
	}

	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getRoomType() {
		return roomType;
	}

	public void setRoomType(Integer roomType) {
		this.roomType = roomType;
	}

	public Integer getLianDui() {
		return lianDui;
	}

	public void setLianDui(Integer lianDui) {
		this.lianDui = lianDui;
	}

	public Integer getShuangWang() {
		return shuangWang;
	}

	public void setShuangWang(Integer shuangWang) {
		this.shuangWang = shuangWang;
	}

	public Integer getSiLu() {
		return siLu;
	}

	public void setSiLu(Integer siLu) {
		this.siLu = siLu;
	}

	public Integer getWuLu() {
		return wuLu;
	}

	public void setWuLu(Integer wuLu) {
		this.wuLu = wuLu;
	}

	public Integer getLiuLu() {
		return liuLu;
	}

	public void setLiuLu(Integer liuLu) {
		this.liuLu = liuLu;
	}

	public Integer getBeiShu() {
		return beiShu;
	}

	public void setBeiShu(Integer beiShu) {
		this.beiShu = beiShu;
	}

	public Integer getMaxPeople() {
		return maxPeople;
	}

	public void setMaxPeople(Integer maxPeople) {
		this.maxPeople = maxPeople;
	}

	public Integer getZhuangRule() {
		return zhuangRule;
	}

	public void setZhuangRule(Integer zhuangRule) {
		this.zhuangRule = zhuangRule;
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getId()).toHashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof Room == false)
			return false;
		if (this == obj)
			return true;
		Room other = (Room) obj;
		return new EqualsBuilder().append(getId(), other.getId()).isEquals();
	}

}
