package com.yzt.logic.mj.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.yzt.logic.util.MyBatisUtils;

/**
 * Created by admin on 2017/6/23.
 */
public class RoomMapper {
	private static Log log = LogFactory.getLog(RoomMapper.class);
    
	public static void insert(Map<String,String> entity) {
        SqlSession session =  MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = RoomMapper.class.getName() + ".insert";
                session.insert(sqlName, entity);
                session.commit();
            }
        } catch (Exception e) {
        	log.error("RoomMapper的insert方法报错", e);
        } finally {
            session.close();
        }
    }
	/**
	 * 更新数据库房间的局数
	 * @param roomId
	 * @param xiaoJuNum
	 * @param cid
	 */
    public static void updateRoomState(Integer roomId,Integer xiaoJuNum, String cid) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = RoomMapper.class.getName() + ".updateRoomState";
                Map<Object, Object> map =new HashMap<>();
                map.put("roomId",roomId);
                map.put("xiaoJuNum", xiaoJuNum);
                map.put("cid", cid);
                session.update(sqlName,map);
                session.commit();
            }
        } catch (Exception e) {
        	log.error("RoomMapper的updateRoomState方法报错", e);
        } finally {
            session.close();
        }
    }
}
