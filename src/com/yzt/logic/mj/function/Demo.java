package com.yzt.logic.mj.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;

import com.alibaba.fastjson.JSONObject;
import com.yzt.logic.mj.domain.Player;

public class Demo extends TCPGameFunctions {

	public static void main(String[] args) {
		// JSONObject obj = new JSONObject();
		// obj.put("ssss", "ssss");
		// obj.put("ss", "ss");
		// obj.put("sss", "sss");
		// obj.remove("ss");
		// System.out.println(obj);
		// Long[] ids=new Long[6];
		// ids[1]=1231l;
		// ids[3]=1233l;
		// ids[4]=1234l;
		// ids[5]=1235l;
		// int x=ids.length;
		// StringBuffer sb=new StringBuffer();
		// for (int i = 0; i < x; i++) {
		// if(i==0){
		// sb.append("[");
		// }
		// if(ids[i]==null){
		// sb.append("0");
		// }else{
		// sb.append(ids[i]);
		// }
		// if (i==x-1){
		// sb.append("]");
		// }else{
		// sb.append(",");
		// }
		//
		// }
		// Map<String, Object> info = new HashMap<>();
		// info.put("currentUser", sb.toString());
		// JSONObject result = getJSONObj(111, 1, info);
		// String jsonString = result.toJSONString();
		// System.out.println(jsonString);
		// System.out.println(sb);
		// String string = ids.toString();
		// System.out.println(string);
		// 定义庄的id
		List<Player> players = new ArrayList<Player>();
		Player p1 = new Player();
		p1.setUserId(1111l);
		Player p2 = new Player();
		p2.setUserId(22222l);
		Player p3 = new Player();
		p3.setUserId(333333l);
		players.add(p1);
		players.add(p2);
		players.add(p3);
		Long zhuangPlayerUserId = null;
		// 随机选出庄玩家
		Random rd = new Random();
		int nextInt = rd.nextInt(players.size());
		zhuangPlayerUserId = players.get(nextInt).getUserId();
		System.out.println(zhuangPlayerUserId);
	}
}
