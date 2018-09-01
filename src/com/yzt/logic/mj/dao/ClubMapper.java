package com.yzt.logic.mj.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.yzt.logic.mj.domain.ClubInfo;
import com.yzt.logic.mj.domain.ClubUser;
import com.yzt.logic.mj.domain.ClubUserUse;
import com.yzt.logic.util.MyBatisUtils;
import com.yzt.logic.util.GameUtil.StringUtils;

public class ClubMapper {

	public static final Log logger = LogFactory.getLog(ClubMapper.class);
	public static ClubInfo selectByClubId(Integer clubId,String cid) {

		ClubInfo result = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubMapper.class.getName()+".selectByClubId";
                logger.error("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("clubId",clubId);
                map.put("cid",cid);
                List<ClubInfo> selectList =session.selectList(sqlName, map);
                if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
    		logger.error("ClubMapper的selectByClubId数据库操作出错！",e);
        }finally {
            session.close();
        }
        return result;
	}
	public static String selectCreateName(Integer userId,String cid) {
		String result = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubMapper.class.getName()+".selectCreateName";
                logger.error("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("cid",cid);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
    		logger.error("ClubMapper的selectCreateName数据库操作出错！",e);

        }finally {
            session.close();
        }
        return result;
	}
	
	public static Integer allUsers(Integer clubId,String cid) {
		Integer result = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubMapper.class.getName()+".allUsers";
                logger.error("sql name ==>>" + sqlName);
            	Map<String, Object> map =new HashMap<String, Object>();
				map.put("clubId",clubId);
				map.put("cid",cid);
                List<Integer> selectList =session.selectList(sqlName, map);
                if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
    		logger.error("ClubMapper的allUsers数据库操作出错！",e);
        }finally {
            session.close();
        }
        return result;
	}
	
		public static List<ClubUser> selectClubByUserId(Long userId) {
		
			List<ClubUser> list = null;
			SqlSession session = MyBatisUtils.getSession();
			try {
				if (session != null){
					String sqlName = ClubMapper.class.getName()+".selectClubByUserId";
					logger.error("sql name ==>>" + sqlName);
					Map<String, Object> map =new HashMap<String, Object>();
					map.put("userId",userId);
					list = session.selectList(sqlName, map);
					session.close();
				}
			}catch (Exception e){
	    		logger.error("ClubMapper的selectClubByUserId数据库操作出错！",e);
			}finally {
				session.close();
			}
			return list;
		}
		/**
		 *通过该玩家id查找他所在的俱乐部--不包含申请退出后
		 * @param userId
		 * @return
		 */
		public static List<Integer> selectClubIdsByUserId(Long userId,String cid) {
			
			List<Integer> list = null;
			SqlSession session = MyBatisUtils.getSession();
			try {
				if (session != null){
					String sqlName = ClubMapper.class.getName()+".selectClubIdsByUserId";
					logger.error("sql name ==>>" + sqlName);
					Map<String, Object> map =new HashMap<String, Object>();
					map.put("userId",userId);
					map.put("cid",cid);
					list = session.selectList(sqlName, map);
					session.close();
				}
			}catch (Exception e){
	    		logger.error("ClubMapper的selectClubIdsByUserId数据库操作出错！",e);
			}finally {
				session.close();
			}
			return list;
		}
		
		public static ClubUser selectUserByUserIdAndClubId(Long userId, Integer clubId,String cid) {

			ClubUser result = null;
			SqlSession session = MyBatisUtils.getSession();
			try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".selectUserByUserIdAndClubId";
	                logger.error("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("userId",userId);
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                List<ClubUser> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						result = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的selectUserByUserIdAndClubId数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return result;
		}
		
		public static Integer countByClubId(Integer clubId, Integer status) {
			
			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".countByClubId";
	                logger.error("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("clubId",clubId);
	                map.put("status",status);
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的countByClubId数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		// 查询我在这个地区(cid代表不同地区)加入的俱乐部---不包含创建的
		public static Integer countByUserId(Long userId ,String cid) {

			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".countByUserId";
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("clubId",userId);
	                map.put("cid",cid);
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的countByUserId数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static int insert(ClubUser clubUser) {
			
			int num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".insert";
	                logger.error("sql name ==>>" + sqlName);
	                num = session.insert(sqlName, clubUser);
	                session.commit();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的insert数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static int updateById(ClubUser clubUser) {

			int num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".updateById";
	                logger.error("sql name ==>>" + sqlName);
	                num = session.update(sqlName, clubUser);
	                session.commit();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的updateById数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		//查询玩家当天消费的房卡数
		public static Integer sumMoneyByClubIdAndDate(Integer clubId,String cid) {
			
			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".sumMoneyByClubIdAndDate";
	                logger.error("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("morning",StringUtils.getTimesmorning());
	                map.put("night",StringUtils.getTimesNight());
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的sumMoneyByClubIdAndDate数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static List<Integer> todayPerson(Integer clubId) {
			List<Integer>  num = new ArrayList<Integer>();
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".todayPerson";
	                logger.error("sql name ==>>" + sqlName);
	                HashMap<String,Object> map = new HashMap<String, Object>();
	        		map.put("clubId", clubId);
	        		map.put("morning", StringUtils.getTimesmorning());
	        		map.put("night", StringUtils.getTimesNight());
	                num = session.selectList(sqlName, map);
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的todayPerson数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer todayGames(Integer clubId) {
			Integer num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".todayGames";
	                logger.error("sql name ==>>" + sqlName);
	                HashMap<String,Object> map = new HashMap<String, Object>();
	        		map.put("clubId", clubId);
	        		map.put("morning", StringUtils.getTimesmorning());
	        		map.put("night", StringUtils.getTimesNight());
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的todayGames数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer selectUserState(Integer clubId, Long userId,String cid) {
			Integer num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".selectUserState";
	                logger.error("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("userId",userId);
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                Integer selectOne =session.selectOne(sqlName, map);
					num = selectOne;
	                session.close();	                
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的selectUserState数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		

		public static Integer userTodayGames(Integer clubId,Long userId) {
			Integer num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".userTodayGames";
	                logger.error("sql name ==>>" + sqlName);
	                HashMap<String,Object> map = new HashMap<String, Object>();
	        		map.put("clubId", clubId);
	        		map.put("userId", userId);
	        		map.put("morning", StringUtils.getTimesmorning());
	        		map.put("night", StringUtils.getTimesNight());
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的userTodayGames数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer todayUse(Integer clubId, Integer userId, String cid) {
			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".todayUse";
	                logger.error("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("userId",userId);
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                map.put("morning", StringUtils.getTimesmorning());
	                map.put("night",StringUtils.getTimesNight());
	                num = session.selectOne(sqlName,map);
	                if(num==null)
	                	num=0;
	                session.close();
	            }
	        }catch (Exception e){
	    		logger.error("ClubMapper的todayUse数据库操作出错！",e);
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static void saveRoom(HashMap<String, String> map) {
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".saveRoom";
	                session.insert(sqlName, map);
	                session.commit();
	            }
	        } catch (Exception e) {
	    		logger.error("ClubMapper的saveRoom数据库操作出错！",e);
	        } finally {
	            session.close();
	        }
	    }



		
		public static void updateRoomState(Integer roomId, Integer xiaoJuNum, String cid) {
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".updateRoomState";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("roomId",roomId);
	                map.put("xiaoJuNum", xiaoJuNum);
	                map.put("cid", cid);
	                session.update(sqlName,map);
	                session.commit();
	            }
	        } catch (Exception e) {
	    		logger.error("ClubMapper的updateRoomState数据库操作出错！",e);
	        } finally {
	            session.close();
	        }
		}
		/**
		 * 根据俱乐部id和cid更改俱乐部的房卡
		 * @param clubId
		 * @param money
		 * @param cid
		 */
		public static void updateClubMoney(Integer clubId, Integer money, String cid) {
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".updateClubMoney";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("clubId",clubId);
	                map.put("money", money);
	                map.put("cid", cid);
	                session.update(sqlName,map);
	                session.commit();
	            }
	        } catch (Exception e) {
	    		logger.error("ClubMapper的updateClubMoney数据库操作出错！",e);
	        } finally {
	            session.close();
	        }
		}
		/**
		 * 添加俱乐部玩家的消费记录
		 * @param enetiy
		 */
		public static void saveUserUse(ClubUserUse enetiy) {
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".saveUserUse";
	                session.insert(sqlName, enetiy);
	                session.commit();
	            }
	        } catch (Exception e) {
	    		logger.error("ClubMapper的saveUserUse数据库操作出错！",e);
	        } finally {
	            session.close();
	        }
	    }
}
