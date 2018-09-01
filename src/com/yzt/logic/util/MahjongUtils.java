package com.yzt.logic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.RoomResp;

/**
 * 
 * @author wsw_007
 *
 */
public class MahjongUtils {
	public static final Log logger = LogFactory.getLog(MahjongUtils.class);

	public static void main(String[] args) {
		// 设置手牌信息
		ArrayList<Integer> newList = new ArrayList<Integer>();
//		newList.add(104);
		newList.add(204);
		newList.add(303);
		newList.add(403);
		newList.add(105);
//		newList.add(205);
		newList.add(502);
//		newList.add(401);
		newList.add(503);
		List<Integer> paiXu = paiXu(newList);
		System.out.println(paiXu);
		// getNextZhungPlayerId(zhuangId, players);
		// newList.add(101);
		// newList.add(201);
		// 设置出牌信息
		// ArrayList<Integer> chuList = new ArrayList<Integer>();
		// chuList.add(7);
		// chuList.add(8);
		// chuList.add(9);
		// chuList.add(103);
		// chuList.add(501);
		// chuList.add(502);
		// chuList.add(502);

		// 查看手牌中这张牌的数量
		// Integer paiNum = containMany(newList, 101);
		// System.out.println("此牌的数量为："+paiNum);
		// 设置房间信息
		// RoomResp room = new RoomResp();
		// room.setShuangWang(1);
		// room.setSiLu(1);
		// room.setWuLu(2);
		// room.setLiuLu(1);
		// room.setLianDui(2);
		// 检测是不是顺子
		// if (checkShunZi(newList)) {
		// System.out.println("是顺子");
		// }else{
		// System.out.println("不是顺子");
		// }
		// 检测有没有五路炸弹
		// if(checkHasLiuLu(newList)){
		// System.out.println("有六路");
		// }else{
		// System.out.println("没有六路");
		// }
		// 检测有没有六路炸弹
		// if(checkHasWuLu(newList)){
		// System.out.println("有五路");
		// }else{
		// System.out.println("没有五路");
		// }
		// if(4!=2 && 4!=3){
		// System.out.println("成立");
		// }
		// String extra="[11,22,33]";
		// List<Integer> parseArray = JSONObject.parseArray(extra,
		// Integer.class);//把字符串转换成集合
		// System.out.println(parseArray);
		// if(containsPai(newList, chuList)){
		// System.out.println("包含");
		// }else{
		// System.out.println("不包含");
		// }
		// System.out.println(newList);
		// 检测能不能出牌
		// if(checkCanChu(chuList, room)){
		// System.out.println("能出");
		// }else{
		// System.out.println("不能出");
		// }
		// 检测是不是炸弹
		// Integer checkIsBoom = checkIsBoom(chuList, room);
		// if (checkIsBoom == -1) {
		// System.out.println("不是炸弹");
		// } else if (checkIsBoom == 2) {
		// System.out.println("是王炸");
		// } else if (checkIsBoom == 3) {
		// System.out.println("是3炸弹");
		// } else if (checkIsBoom == 4) {
		// System.out.println("是4炸弹");
		// } else if (checkIsBoom == 5) {
		// System.out.println("是5炸弹");
		// } else if (checkIsBoom == 6) {
		// System.out.println("是6炸弹");
		// }
//		 检测是不是连队
		 if (checkLianDui(newList)) {
		 System.out.println("是连对");
		 } else {
		 System.out.println("不是连对");
		 }
//		 检测是不是三连队
		 if (checkSanLianDui(newList)) {
			 System.out.println("是三连对");
		 } else {
			 System.out.println("不是三连对");
		 }
		// 获取能够组合的牌
		// List<List<Integer>> nextCanChulList =new ArrayList<List<Integer>>();
		// getCanChuFromPaiList(chuList, room, nextCanChulList);
		// System.out.println(nextCanChulList);

	}

	/**
	 * 获得所需要的牌型 并打乱牌型
	 * 
	 * @return
	 */
	public static List<Integer> getPais() {
		// 1-9万 ,10-18饼,19-27条,28-31 东南西北,32-34 中发白
		ArrayList<Integer> pais = new ArrayList<Integer>();
		int huaSe;
		int dianShu;
		int pai;
		// 添加普通牌
		for (int j = 1; j <= 4; j++) {
			huaSe = j * 100;
			for (dianShu = 1; dianShu <= 13; dianShu++) {
				pai = huaSe + dianShu;
				pais.add(pai);
			}
		}
		// 添加大小王
		pais.add(501);
		pais.add(502);
		// 添加混牌
		pais.add(503);
		// 2.洗牌
		Collections.shuffle(pais);
		return pais;
	}

	/**
	 * 101 103 201----->101 201 103 给手牌排序
	 * 
	 * @param pais
	 * @return
	 */
	public static List<Integer> paiXu(List<Integer> pais) {
		List<Integer> newPais = new ArrayList<Integer>();
		int size = pais.size();
		Collections.sort(pais);
		// 全是花
		if (pais.get(0) >= 501) {
			return pais;
		}
		if (size == 1) {// 就一张牌
			return pais;
		} else {// 好几张牌,切第一张不是花
			newPais.add(pais.get(0));
			for (int i = 1; i < size; i++) {
				Integer pai = pais.get(i);
				if (pai >= 501) {
					newPais.add(pai);
				} else {
					int x = pai % 100;
					if (x <= 3) {// 转变A和2
						x += 13;
					}
					int z = 0;
					int size2 = newPais.size();
					for (int j = 0; j < size2; j++) {
						z++;
						Integer paiNum = newPais.get(j) % 100;
						if (paiNum <= 3) {// 转变A和2
							paiNum += 13;
						}
						if (paiNum > x) {
							newPais.add(j, pai);
							break;
						}
						if (z == size2) {
							newPais.add(pai);
						}

					}

				}
			}
		}
		return newPais;
	}

	/**
	 * 
	 * @param mahjongs
	 *            房间内剩余麻将的组合
	 * @param num
	 *            发的张数
	 * @return
	 */
	public static List<Integer> faPai(List<Integer> mahjongs, Integer num) {
		// 房间必须有牌，且牌的数量必须大于要发的牌数
		if (mahjongs == null || num == null || mahjongs.size() < num) {
			return null;
		}
		// ArrayList
		// rrayList内部是使用可増长数组实现的，所以是用get和set方法是花费常数时间的，但是如果插入元素和删除元素，除非插入和删除的位置都在表末尾，否则代码开销会很大，因为里面需要数组的移动。
		List<Integer> result = new ArrayList<>();
		for (int i = mahjongs.size() - 1; i >= 0; i--) {
			result.add(mahjongs.get(i));
			mahjongs.remove(i);
			num--;
			if (num == 0) {
				break;
			}
		}
		return result;
	}

	/**
	 * 初始化玩家位置集合
	 * 
	 * @param maxPeople
	 * @return
	 */
	public static List<Integer> initPosition(int maxPeople) {
		List<Integer> positions = new ArrayList<Integer>();
		for (int i = 1; i <= maxPeople; i++) {
			positions.add(i);
		}
		return positions;
	}

	/**
	 * 从庄或者出派人开始根据玩家的位置返回玩家集合
	 * 
	 * @param players
	 * @param chuPosition
	 *            //出牌人的位置
	 * @param containChu
	 *            //true 包含出牌人;false,不包含出牌人
	 */
	public static List<Long> getPlayersFromChuPositions(List<Player> players, Integer chuPosition, Boolean containChu) {
		List<Long> nowPlayerIds = new ArrayList<Long>(6);
		int size2 = players.size();
		int max = 6;
		List<Integer> nowPositions = new ArrayList<Integer>(size2);
		for (Player player : players) {
			if (!containChu) {
				if (player.getPosition().equals(chuPosition)) {
					continue;
				}
			}
			// 缴牌的玩家不管
			if (player.getPlayStatus() != Cnst.PLAYER_STATE_JIAOPAI) {
				if (player.getPosition() < chuPosition) {
					nowPositions.add(player.getPosition() + max);
				} else {
					nowPositions.add(player.getPosition());
				}
			}
		}
		Collections.sort(nowPositions);
		List<Integer> nowPositions3 = new ArrayList<Integer>(size2);

		int size = nowPositions.size();
		// 获取按照出牌人顺序的位置数组
		for (Integer integer : nowPositions) {
			if (integer > max) {
				integer -= max;
			}
			nowPositions3.add(integer);
		}
		for (Integer i : nowPositions3) {
			for (Player p : players) {
				if (i == p.getPosition()) {

					nowPlayerIds.add(p.getUserId());
					break;
				}
			}
		}
		return nowPlayerIds;
	}

	/**
	 * 
	 * @param p
	 * @param room
	 * @return 6 是六路炸弹 5 5路炸弹 1 顺子 2 不胡
	 */
	public static Integer checkTianHu(Player p, RoomResp room) {
		List<Integer> currentPaiList = p.getCurrentPaiList();
		List<Integer> newList = getNewList(currentPaiList);
		Collections.sort(newList);

		// 检测是不是炸弹
		if (currentPaiList.size() == 6) {// 说明是庄
			if (room.getLiuLu().equals(2)) {// 规则可以六路炸弹
				// 检测庄有没有六路炸弹
				if (checkHasLiuLu(newList)) {
					return 6;
				}
			}
		} else {
			if (room.getWuLu().equals(2)) {// 规则可以六路炸弹
				// 检测庄有没有六路炸弹
				if (checkHasWuLu(newList)) {
					return 5;
				}
			}
		}
		// 说明不是炸弹,检测是不是顺子
		if (checkShunZi(newList)) {
			return 1;
		}
		return 2;
	}

	/**
	 * 检测是不是顺子 牌是不分花色的
	 * 
	 * @param newList
	 */
	private static boolean checkShunZi(List<Integer> newList) {
		// 3张以上才检测顺子
		if (newList.size() < 3) {
			return false;
		}
		int hunNum = 0;
		// Integer huaSe = 0;
		Integer paiNum = 0;
		int[] arr = new int[13];
		for (Integer pai : newList) {
			if (pai >= 501) {
				hunNum++;
			} else {
				// 给第一张牌赋值花色
				// if (huaSe == 0) {
				// huaSe = pai / 100;
				// } else {// 检测牌的花色是否相同
				// if (huaSe != (pai / 100)) {
				// return false;
				// }
				// }
				// 检测值
				paiNum = pai % 100;
				if (paiNum == 2) {// 二不能组成顺子
					return false;
				} else {
					if (paiNum == 1) {
						int i = arr[12];
						arr[12] = i + 1;
					} else {
						int i = arr[paiNum - 2];
						arr[paiNum - 2] = i + 1;
					}
				}
			}
		}
		// 检测这些牌
		int needHunNum = 0;
		int firstPai = 0;
		for (int i = 0; i < arr.length; i++) {
			// 牌里面不能有两张
			if (arr[i] == 2) {
				return false;
			} else if (arr[i] == 1) {
				if (firstPai == 0) {
					firstPai = i;
				} else {
					needHunNum += i - firstPai - 1;
					firstPai = i;
				}
			}
		}
		if (needHunNum > hunNum) {
			return false;
		}
		return true;
	}

	/**
	 * 检测是不是五路胡牌
	 * 
	 * @param currentPaiList
	 * @return
	 */
	private static boolean checkHasWuLu(List<Integer> newList) {
		if (newList.size() != 5) {
			return false;
		}
		// 只有两种情况：3张一样的和3张混;或者4张一样的两张混
		Integer containMany = containMany(newList, newList.get(0));
		if (containMany == 1) {
			return false;
		}
		// 说明是三张或者四张,那么剩下的全是混

		if (containMany == 2) {
			// 从第4张开始必须是花
			if (newList.get(2) >= 501) {
				return true;
			}
		} else if (containMany == 3) {
			// 从第5张开始必须是花
			if (newList.get(3) >= 501) {
				return true;
			}
		} else if (containMany == 4) {
			if (newList.get(4) >= 501) {
				return true;
			}
		}
		return false;

	}

	/**
	 * 检测是不是六路胡牌
	 * 
	 * @param currentPaiList
	 * @return
	 */
	private static boolean checkHasLiuLu(List<Integer> newList) {

		if (newList.size() != 6) {
			return false;
		}
		// 只有两种情况：3张一样的和3张混;或者4张一样的两张混
		Integer containMany = containMany(newList, newList.get(0));
		if (containMany <= 2) {
			return false;
		}
		// 说明是三张或者四张,那么剩下的全是混

		if (containMany == 3) {
			// 从第4张开始必须是花
			if (newList.get(3) >= 501) {
				return true;
			}
		} else if (containMany == 4) {
			// 从第5张开始必须是花
			if (newList.get(4) >= 501) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 查找currentPaiList中num张pai--这个牌在排除化之后只看拍的大小
	 * 
	 * @param currentPaiList
	 * @param pai
	 * @return
	 */
	public static Integer containMany(List<Integer> currentPaiList, Integer pai) {
		Integer num = 0;
		int paiNum = pai % 100;
		for (Integer integer : currentPaiList) {
			if (integer >= 501) {
				continue;
			}
			if (paiNum == integer % 100) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 获取新的手牌集合(排好序的)
	 * 
	 * @param currentPaiList
	 * @param dongZuoPai
	 * @return
	 */
	private static List<Integer> getNewList(List<Integer> currentPaiList) {
		List<Integer> newList = new ArrayList<Integer>(currentPaiList.size());
		for (Integer pai : currentPaiList) {
			newList.add(pai);
		}

		Collections.sort(newList);
		return newList;
	}

	/**
	 * 检测手牌中是否包含出的牌 此方法不会影响传来的手牌
	 * 
	 * @param currentPaiList
	 * @param chuList
	 * @return
	 */
	public static boolean containsPai(List<Integer> currentPaiList, List<Integer> chuList) {
		List<Integer> newList = getNewList(currentPaiList);
		boolean remove;
		for (Integer chuPai : chuList) {
			remove = false;
			for (int i = 0; i < newList.size(); i++) {
				if (newList.get(i).equals(chuPai)) {
					newList.remove(i);
					remove = true;
					break;
				}
			}
			if (!remove) {
				return false;
			}
		}
		return true;
	}

	/**
	 * currentMjList中移除集合needRemoveList
	 * 
	 * @param currentMjList
	 * @param chi
	 */
	public static void removeList(List<Integer> currentMjList, List<Integer> needRemoveList) {
		for (int j = needRemoveList.size() - 1; j >= 0; j--) {

			for (int i = currentMjList.size() - 1; i >= 0; i--) {
				if (needRemoveList.get(j).equals(currentMjList.get(i))) {
					currentMjList.remove(i);
					break;
				}
			}
		}
	}

	/**
	 * 检测玩家出的牌符不符合出牌规则 牌数1:单张 牌数2:王炸,对子 牌数3:顺子,炸弹 牌数4:顺子,炸弹,连对 牌数5:顺子,炸弹
	 * 牌数6:顺子,炸弹
	 * 
	 * @param chuList
	 * @param room
	 * @return
	 */
	public static boolean checkCanChu(List<Integer> chuList, RoomResp room) {
		int chuSize = chuList.size();
		// 对于全花的判断
		if (chuList.get(0) >= 501) {
			if (room.getShuangWang().equals(1)) {// 双王不能出
				return false;
			} else {
				if (chuList.size() != 2) {// 出的不是两张
					return false;
				} else {
					// 检测这两张是不是大小王
					if (chuList.get(0) == 501 && chuList.get(1) == 502) {
						return true;
					} else {// 出的不是大小王
						return false;
					}
				}
			}
		} else {// 对于带有普通牌
			// 只要有普通牌我就可以出这个单张
			// return true;
			List<Integer> newList = getNewList(chuList);
			// 检测是不是炸弹
			if (checkIsBoom(newList, room) != -1) {
				return true;
			}
			// 检测是不是顺子
			if (checkShunZi(getNewList(newList))) {
				return true;
			}
			// 有连对规则
			if (room.getLianDui().equals(2)) {
				//检测是不是三连
				if (checkSanLianDui(getNewList(newList))) {
					return true;
				}
			}
			if (chuSize == 1) {// 随便出
				return true;
			} else if (chuSize == 2) {// 王炸检测了不用管
				// 对子检测,两张牌大小是不是相同(第二张是花也可以)
				if (newList.get(0) % 100 == newList.get(1) % 100 || newList.get(1) >= 501) {
					return true;
				}
			} else if (chuSize == 4) {// 顺子和炸弹检测了不用管
				// 有连对规则
				if (room.getLianDui().equals(2)) {// 检测是不是连对
					if (checkLianDui(newList)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 检测手里的牌是否能出
	 * 
	 * @param chuList
	 * @param room
	 * @return
	 */
	public static boolean checkShouPaiCanChu(List<Integer> chuList, RoomResp room) {
//		List<Integer> paiXu = paiXu(chuList);
		// 对于全花的判断
		if (chuList.get(0) >= 501) {
			if (room.getShuangWang().equals(1)) {// 双王不能出
				return false;
			} else {
				if (chuList.size() != 2) {// 出的不是两张
					return false;
				} else {
					// 检测这两张是不是大小王
					if (chuList.get(0) == 501 && chuList.get(1) == 502) {
						return true;
					} else {// 出的不是大小王
						return false;
					}
				}
			}
		} else {// 对于带有普通牌
			return true;
		}
	}

	/**
	 * 检测是不是2连对
	 * 
	 * @param newList
	 * @return
	 */
	private static boolean checkLianDui(List<Integer> newList) {
		int size = newList.size();
		if (size != 4  ) {
			return false;
		}
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = size - 1; i >= 0; i--) {
			if (newList.get(i) >= 501) {
				newList.remove(i);
			} else {
				set.add(newList.get(i) % 100);
			}
		}
		int paiNum = set.size();
		if (paiNum > 2) {
			return false;
		} else {
			if (paiNum == 1) {
				for (Integer pai : set) {
					Integer containMany = containMany(newList, pai);
					// 1张或者两张都满足
					if (containMany < 3) {
						return true;
					} else {
						return false;
					}
				}
			} else if (paiNum == 2) {
				Integer paiOneNum = 0;
				Integer paiOne = 0;
				Integer paiTwo = 0;
				Integer paiTwoNum = 0;
				for (Integer pai : set) {
					if (paiOne == 0) {
						paiOne = pai;
						paiOneNum = containMany(newList, pai);
					} else {
						paiTwo = pai;
						paiTwoNum = containMany(newList, pai);
					}
				}
				// 连队中不能有2
				if (paiOne == 2 || paiTwo == 2) {
					return false;
				}
				// 对于A的处理
				if (paiOne == 1) {
					paiOne = 14;
				}
				if (paiTwo == 1) {
					paiTwo = 14;
				}
				// 牌必须相连
				if (paiOne - paiTwo == 1 || paiOne - paiTwo == -1) {
					// 两张牌都不能大于2
					if (paiOneNum <= 2 && paiTwoNum <= 2) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * 检测是不是3连对
	 * 
	 * @param newList
	 * @return
	 */
	private static boolean checkSanLianDui(List<Integer> newList) {
		int size = newList.size();
		if (size != 6  ) {
			return false;
		}
		HashSet<Integer> set = new HashSet<Integer>();
		//重复的
		List<Integer> bigList=new ArrayList<Integer>();
		//不重复的
		List<Integer> paiBigNumList=new ArrayList<Integer>();
		
		for (int i = size - 1; i >= 0; i--) {
			if (newList.get(i) >= 501) {
				newList.remove(i);
			} else {
				int paiBig=newList.get(i) % 100;
				if(paiBig==1){
					paiBig+=13;
				}else if(paiBig==2){//不能有2
					return false;
				}
				bigList.add(paiBig);
				set.add(paiBig);
			}
		}
		int size2 = set.size();
		if(size2!=2 && size2!=3){
			return false;
		}
		for (Integer integer : set) {
			paiBigNumList.add(integer);
		}
		Collections.sort(paiBigNumList);
		Integer firstNum=0;
		Integer secondNum=0;
		Integer thirdNum=0;
		if(size2==2){
			firstNum = paiBigNumList.get(0);
			secondNum = paiBigNumList.get(1);
			if(secondNum-firstNum==1 || secondNum-firstNum==2){
				if(containMany(bigList, firstNum)<=2 &&containMany(bigList, secondNum)<=2){
					return true;
				}
			}
		}else if(size2==3){
			firstNum = paiBigNumList.get(0);
			secondNum = paiBigNumList.get(1);
			thirdNum = paiBigNumList.get(2);
			if(thirdNum-secondNum==1 &&secondNum-firstNum==1){
				if(containMany(bigList, firstNum)<=2 &&containMany(bigList, secondNum)<=2 && containMany(bigList, thirdNum)<=2 ){
					return true;
				}
				
			}
			
		}
		return false;
	}

	/**
	 * 此方法不会检测两次都是顺子或者连队的牌型, 比较上个出牌人出的牌和这个出牌人出的牌的大小 牌数1:单张 牌数2:王炸,对子 牌数3:顺子,炸弹
	 * 牌数4:顺子,炸弹,连对 牌数5:顺子,炸弹 牌数6:顺子,炸弹
	 * 
	 * @param lastChuPai
	 * @param chuList
	 * @param room
	 */
	public static boolean compairPaiNum(List<Integer> lastChuPai, List<Integer> chuList, RoomResp room) {
		List<List<Integer>> nextCanChulList = room.getNextCanChulList();
		int paiSize = lastChuPai.size();
		Integer lastChuPaiFirst = lastChuPai.get(0) % 100;
		Integer chuPaiFirst = chuList.get(0) % 100;
		// 检测炸弹,会带着房间规则检测
		Integer lastBoom = checkIsBoom(lastChuPai, room);
		Integer nowBoom = checkIsBoom(chuList, room);
		// 王炸大于一切
		if (nowBoom == 2) {
			nextCanChulList.clear();
			return true;
		}
		if (lastBoom == 2) {
			return false;
		}
		// 没有王炸,第一张不能是花
		if (lastChuPai.get(0) >= 501 || chuList.get(0) >= 501) {
			return false;
		}
		// 下面说明都是普通牌
		// 将2变成14 ,1变成13
		if (lastChuPaiFirst == 2 || lastChuPaiFirst == 1) {
			lastChuPaiFirst += 13;
		}
		if (chuPaiFirst == 2 || chuPaiFirst == 1) {
			chuPaiFirst += 13;
		}

		// 俩肯定都不是王炸了
		if (lastBoom != -1) {// 上次出的牌是炸弹
			if (nowBoom != -1) {// 这次出的是炸弹
				// 上家炸弹数不能大于管他人的炸弹书
				if (lastBoom > nowBoom) {
					return false;
				} else if (lastBoom == nowBoom) {// 炸弹数相同
					// 比较牌的大小
					if (lastChuPaiFirst < chuPaiFirst) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
		} else {// 上次出的牌不是炸弹
			if (nowBoom != -1) {// 这次出的是炸弹
				nextCanChulList.clear();
				return true;
			} else {// 俩都不是炸弹
					// 牌的数目必须相同
				if (lastChuPai.size() != chuList.size()) {
					return false;
				} else {
					// 牌型检测大小
					if (paiSize < 3) {// 单张 或者对子检测
						if (lastChuPaiFirst < chuPaiFirst) {
							if (chuPaiFirst == 15) {
								return true;
							} else {
								// 单张和对子只能出比前面大1的
								if (chuPaiFirst - lastChuPaiFirst == 1) {
									return true;
								} else {
									return false;
								}
							}
						}
					} else {// 顺子或者连队的比较
						List<List<Integer>> newCanChulList = new ArrayList<List<Integer>>();
						List<Integer> biJiaoChuList = new ArrayList<Integer>(6);

						for (Integer pai : chuList) {
							if (pai < 501) {
								int x=pai % 100;
								if(x==1){
									x+=13;
								}
								biJiaoChuList.add(x);
							}
						}
						boolean contain = false;
						for (List<Integer> list : nextCanChulList) {
							if (containsList(list, biJiaoChuList)) {
								contain = true;
								// 找到能管这套牌的集合--0或者1 0的时候比如Q K A;
								getCanChuFromPaiList(list, room, newCanChulList);
								// newCanChulList.add(canChuFromPaiList.get(0));
								// }
							}
						}
						if (contain) {
							room.setNextCanChulList(newCanChulList);
							return true;
						} else {
							return false;

						}
					}
				}

			}
		}
		return false;
	}

	/**
	 * 检测前面是否有后面所有
	 * 
	 * @param list
	 * @param biJiaoChuList
	 * @return
	 */
	private static boolean containsList(List<Integer> list, List<Integer> biJiaoChuList) {
		List<Integer> newList = getNewList(list);
		for (Integer integer : biJiaoChuList) {
			boolean remove = false;
			int size = newList.size();
			for (int i = size - 1; i >= 0; i--) {
				if (newList.get(i).equals(integer)) {
					newList.remove(i);
					remove = true;
					break;
				}
			}
			if (!remove) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检测牌是不是炸弹
	 * 
	 * @param chuList
	 * @param room
	 * @return -1不是炸弹 2王炸,其他数字代表几炸
	 */
	public static Integer checkIsBoom(List<Integer> chuList, RoomResp room) {
		// 检测是不是王炸
		int chuSize = chuList.size();
		if (chuSize == 2) {
			// 房间带有王炸规则
			if (room.getShuangWang().equals(2)) {
				if (chuList.get(0) == 501 && chuList.get(1) == 502) {
					return 2;
				}
			}
		}
		// 普通炸弹必须大于等于3张
		if (chuList.size() < 3) {// 炸弹最少三张
			return -1;
		}
		// 检测房间选美选这个炸弹的规则
		if (chuSize == 4) {
			if (room.getSiLu().equals(1)) {
				return -1;
			}
		} else if (chuSize == 5) {
			if (room.getWuLu().equals(1)) {
				return -1;
			}
		} else if (chuSize == 6) {
			if (room.getLiuLu().equals(1)) {
				return -1;
			}
		}
		int x = chuList.get(0) % 100;
		for (int i = 1; i < chuSize; i++) {
			if (chuList.get(i) < 501) {
				if ((chuList.get(i) % 100) != x) {
					return -1;
				}
			} else {
				// 后面都是混
				break;
			}
		}
		return chuSize;
	}

	/**
	 * 此方法只传牌的大小也能返回 通过此牌找到能管住的牌型 能进入此方法肯定都是顺子,或者连对,排除2了
	 * 
	 * @param chuList
	 * @param room
	 * @param nextCanChulList
	 * @return 只返回牌的大小集合,跟类型无关
	 */
	public static List<List<Integer>> getCanChuFromPaiList(List<Integer> chuList, RoomResp room, List<List<Integer>> nextCanChulList) {
		// 检测能管住的顺子
		// 创建手牌集合3到14 3---A
		int[] shouPaiArr = new int[15];
		// 设置花的数量
		int huaNum = 0;
		// 设置牌的大小
		int paiBig = 0;
		// 设置牌中最大的牌
		int maxPai = 0;
		// 设置牌中最小的牌
		int smallPai = 0;
		int size = chuList.size();
		for (int i : chuList) {
			if (i >= 501) {
				huaNum++;
			} else {
				paiBig = i % 100;
				if (paiBig == 1) {// A
					paiBig = 14;
				}
				shouPaiArr[paiBig - 3] = 1;

				if (smallPai == 0) {
					smallPai = paiBig;
				} else {
					if (smallPai > paiBig) {
						smallPai = paiBig;
					}
				}
				if (maxPai == 0) {
					maxPai = paiBig;
				} else {
					if (maxPai < paiBig) {
						maxPai = paiBig;
					}
				}
			}
		}
		if (checkShunZi(chuList)) {// 因为有可能仅仅是连对(如7,7,8,花)
			int needHua = 0;
			for (int i = smallPai - 3; i <= (paiBig - 3); i++) {
				if (shouPaiArr[i] == 0) {
					needHua++;
				}
			}
			int shengHua = huaNum - needHua;
			if (smallPai - shengHua <= 3) {// 最小的能到3就不能再小了
				// 从四开始组合
				int x = smallPai - 3;
				for (int i = 0; i <= x; i++) {
					List<Integer> canZuHe = new ArrayList<Integer>();
					for (int j = 0; j < size; j++) {
						canZuHe.add(i + 3 + j + 1);
					}
					nextCanChulList.add(canZuHe);
				}
			} else if (maxPai + shengHua >= 14) {// 最大的能到A就不能再大了
				int x = 14 - maxPai;
				for (int i = 0; i < x; i++) {
					List<Integer> canZuHe = new ArrayList<Integer>();
					for (int j = 0; j < size; j++) {
						canZuHe.add(14 - i - j);
					}
					nextCanChulList.add(paiXu(canZuHe));
				}
			} else {// 最小不到3,最大不到A
					// 设置最小牌能达到的最小的值
				int newSmall = smallPai - shengHua;
				for (int i = (newSmall + 1); i <= (smallPai + 1); i++) {
					List<Integer> canZuHe = new ArrayList<Integer>();
					for (int j = 0; j < size; j++) {
						canZuHe.add(i + j);
					}
					nextCanChulList.add(paiXu(canZuHe));
				}
			}
		}
		// 检测能管住的连队
		if (room.getLianDui().equals(2)) {
			if (size == 4) {
				// 连队必须有两张牌,且相差必须为1
				if (maxPai - smallPai == 1) {
					if (maxPai != 14) {// 最大牌为A没有能管到的
						List<Integer> canZuHe = new ArrayList<Integer>();
						canZuHe.add(smallPai + 1);
						canZuHe.add(smallPai + 1);
						canZuHe.add(maxPai + 1);
						canZuHe.add(maxPai + 1);
						nextCanChulList.add(paiXu(canZuHe));
					}
				}
			}
		}

		return nextCanChulList;
	}

	/**
	 * 通过炸弹的数量获取倍数
	 * 
	 * @param checkIsBoom
	 * @return
	 */
	public static Integer getBeiShuFromZhaDanNum(Integer checkIsBoom) {
		if (checkIsBoom == 2) {
			return 1;
		} else if (checkIsBoom == 3) {
			return 2;
		} else if (checkIsBoom == 4) {
			return 4;
		} else if (checkIsBoom == 5) {
			return 5;
		} else if (checkIsBoom == 6) {
			return 6;
		}
		return 1;
	}

	public static String getStringFromLongArr(Long[] ids) {
		StringBuffer sb = new StringBuffer();
		int x = ids.length;
		for (int i = 0; i < x; i++) {
			if (i == 0) {
				sb.append("[");
			}
			if (ids[i] == null) {
				sb.append("0");
			} else {
				sb.append(ids[i]);
			}
			if (i == x - 1) {
				sb.append("]");
			} else {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * 获取需要点同意或者拒绝的人数
	 * 
	 * @param needAgreeNum
	 * @param needRejectNum
	 * @param num
	 */
	public static void getNowNeedAndRejectNum(List<Integer> pNlist, int num) {
		System.out.println("sss");
		if (num <= 2) {// 俩人
			// 剩下那个人无论点同意或者拒绝都会执行
			pNlist.add(1);
			pNlist.add(1);
		} else if (num == 3) {
			// 有1个玩家点同意就解散
			pNlist.add(1);
			pNlist.add(2);
			// 俩人都点拒绝才不解散
		} else if (num == 4) {
			pNlist.add(2);
			pNlist.add(2);
			// needAgreeNum=2;
			// needRejectNum=2;
		} else if (num == 5) {
			pNlist.add(3);
			pNlist.add(2);
			// needAgreeNum=3;
			// needRejectNum=2;
		} else if (num == 6) {
			pNlist.add(3);
			pNlist.add(3);
			// needAgreeNum=3;
			// needRejectNum=3;
		}

	}

	public static List<Integer> faPaiShunZi() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(103);
		list.add(104);
		list.add(105);
		list.add(106);
		list.add(107);
		list.add(108);
		return list;
	}

	public static List<Integer> faPaiZhu() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(106);
		list.add(206);
		list.add(101);
		list.add(201);
		list.add(501);
		list.add(502);
		return list;
	}

	public static List<Integer> faPaiXian() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(107);
		list.add(207);
		list.add(108);
		// list.add(109);
		list.add(501);
		list.add(502);
		return list;
	}

	public static List<Integer> faPaiShunZiWithHua() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(207);
		list.add(209);
		list.add(308);
		list.add(502);
		list.add(503);
		return list;
	}

	public static Long getNextZhungPlayerId(Long zhuangId, List<Player> players) {
		int size = players.size();
		if (players.get(size - 1).getUserId().equals(zhuangId)) {
			return players.get(0).getUserId();
		} else {
			boolean zhuangIdIsNextId = false;
			for (Player player : players) {
				if (zhuangIdIsNextId) {
					return player.getUserId();
				}
				if (player.getUserId().equals(zhuangId)) {
					zhuangIdIsNextId = true;
				}
			}
		}
		return null;
	}
	/**
	 * 检测手牌是否含有可以必管上家出的牌
	 * 
	 * @param currentPaiList
	 * @param lastChuPai
	 * @return
	 */
	public static boolean biGuan(List<Integer> currentPaiList, List<Integer> lastChuPai) {
		List<Integer> guanList = new ArrayList<Integer>(6);
		int size = lastChuPai.size();
		int paiNum=0;
		for (Integer integer : currentPaiList) {
			if(integer<501){
				paiNum=integer%100;
				if(paiNum<=2){
					paiNum+=13;
				}
				guanList.add(paiNum);
			}
		}
		if(guanList.size()<size){
			return false;
		}
		List<Integer> chuList = new ArrayList<Integer>(size);
		for (Integer integer : lastChuPai) {
			paiNum=integer%100;
			if(paiNum==2){
//				break;
				return false;
			}else if(paiNum==1){
				paiNum=14;
			}
			chuList.add(paiNum);
		}
		int[] chuArr=new int[16];
		for (Integer integer : chuList) {
			int num=chuArr[integer];
			chuArr[integer]=num+1;
		}
		int[] guanArr=new int[16];
		for (Integer integer : guanList) {
			int num=guanArr[integer];
			guanArr[integer]=num+1;
		}
		Integer first = chuList.get(0);
		
		if(size==1){//单张或者对子
			if(guanArr[15]>0|| guanArr[first+1]>0){
				
				return true;
			}else{
				return false;
			}
		}else if(size==2){
			if(guanArr[15]>1|| guanArr[first+1]>1){
				return true;
			}else{
				 return false;
			}
		}else{//顺子
			for (int i = 1; i < chuArr.length; i++) {
				int j = chuArr[i];
				if(j>0){
					if(guanArr[i+1]<j){
						return false;
					}
				}
			}		
		}
		return true;
	}

}
