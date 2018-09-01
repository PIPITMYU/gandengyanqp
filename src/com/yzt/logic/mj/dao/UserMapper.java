package com.yzt.logic.mj.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.yzt.logic.mj.domain.Feedback;
import com.yzt.logic.mj.domain.Player;
import com.yzt.logic.mj.domain.PlayerMoneyRecord;
import com.yzt.logic.mj.domain.SystemMessage;
import com.yzt.logic.util.MyBatisUtils;

/**
 * Created by admin on 2017/6/22.
 */
public class UserMapper {
	private static Log log = LogFactory.getLog(UserMapper.class);

    public static void insert(Player entity) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insert";
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("userId", entity.getUserId());
                map.put("openId", entity.getOpenId());
                map.put("userName", entity.getUserName());
                map.put("userImg", entity.getUserImg());
                map.put("gender", entity.getGender());
                map.put("money", entity.getMoney());
                map.put("userAgree", entity.getUserAgree());
                map.put("loginStatus", entity.getLoginStatus());
                map.put("signUpTime", entity.getSignUpTime());
                map.put("lastLoginTime", entity.getLastLoginTime());
                map.put("cId", entity.getCid());
                map.put("ip",entity.getIp());
                session.insert(sqlName, map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
        	log.error("UserMapper的insert数据库报错",e);
        } finally {
            session.close();
        }
    }

   

  
    public static void updateMoney(Integer money, String userId,String cid) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".updateMoney";
                Map<Object, Object> map =new HashMap<>();
                map.put("money",money);
                map.put("userId",userId);
                map.put("cid", cid);
                session.update(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
        	log.error("UserMapper的updateMoney数据库报错",e);
        } finally {
            session.close();
        }
    }

   

  
    public static Player findByOpenId(String openId,String cid){
    	Player result = null;
		SqlSession session = MyBatisUtils.getSession();
		try {
			if (session != null) {
				String sqlName = UserMapper.class.getName() + ".findByOpenId";
				Map<Object, Object> map = new HashMap<>();
				map.put("openId", openId);
				map.put("cid", cid);
				List<Player> selectList = session.selectList(sqlName, map);
				if(!selectList.isEmpty())
					result = selectList.get(0);
										
				session.close();
			}
		} catch (Exception e) {
			log.error("UserMapper的findByOpenId数据库报错", e);
		} finally {
			session.close();
		}
		return result;
    }

   

   
    public static void userFeedback(Feedback feedback) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".userFeedback";
                log.info("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",feedback.getUserId());
                map.put("tel",feedback.getTel());
                map.put("content",feedback.getContent());
                map.put("createTime",feedback.getCreateTime());
                session.insert(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            log.error("userFeedback数据库操作出错！");
        }finally {
            session.close();
        }
    }

   
    public static Integer isExistUserId(String userId,String cid) {
        Integer result = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".isExistUserId";
                log.info("sql name ==>>" + sqlName);
                Map<String,Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("cid", cid);
                List<Integer> selectList = session.selectList(sqlName, map);
				if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
            log.error("UserMapper的isExistUserId数据库报错",e);
        }finally {
            session.close();
        }
        return result;
    }

  
    public static void updateUserAgree(Long userId,String cid) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".updateUserAgree";
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("cid", cid);
                session.update(sqlName,map);
                session.commit();
            }
        } catch (Exception e) {
            log.error("UserMapper的updateUserAgree数据库报错",e);
        } finally {
            session.close();
        }
    }

  
    public static String getNotice(String cid) {
        String result = "";
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getNotice";
                log.info("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("cid", cid);
                List<String> selectList = session.selectList(sqlName,map);
				if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
            log.error("UserMapper的getNotice数据库报错",e);
        }finally {
            session.close();
        }
        return result;
    }
    
    
    public static String getConectUs() {
        String result = "";
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getConectUs";
                log.info("sql name ==>>" + sqlName);
                List<String> selectList = session.selectList(sqlName);
				if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
            log.error("UserMapper的getConectUs数据库报错",e);
        }finally {
            session.close();
        }
        return result;
    }




    public static List<SystemMessage> getSystemMessage(Long userId, Integer start, Integer limit) {
        List<SystemMessage> result = new ArrayList<>();
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getSystemMessage";
                log.info("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<Object, Object>();
                map.put("userId",userId);
                map.put("start",start);
                map.put("limit",limit);
                result = session.selectList(sqlName,map);
                session.close();
            }
        }catch (Exception e){
            log.error("UserMapper的getSystemMessage数据库报错",e);
        }finally {
            session.close();
        }
        return result;
    }


    
    
   
    public static void insertPlayerMoneyRecord(PlayerMoneyRecord mr) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insertPlayerMoneyRecord";
                session.insert(sqlName, mr);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            log.error("UserMapper的insertPlayerMoneyRecord数据库报错",e);
        } finally {
            session.close();
        }
    }
    
    

    public static Integer getUserMoneyByUserId(Long userId,String cid) {
        Integer result = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName =  UserMapper.class.getName()+".getUserMoneyByUserId";
                log.info("sql name ==>> " + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("cid", cid);
                List<Integer> selectList = session.selectList(sqlName,map);
				if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        } catch (Exception e) {
            log.error("UserMapper的getUserMoneyByUserId数据库报错",e);
        } finally {
            session.close();
        }
        return result;
    }

	
	public static void updateIpAndLastTime(String openId,String ip,String cid) {
		 SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = UserMapper.class.getName() + ".updateIpAndLastTime";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("openId", openId);
	                map.put("lastLoginTime",System.currentTimeMillis());
	                map.put("ip",ip);
	                map.put("cid", cid);
	                session.update(sqlName,map);
	                session.commit();
//	                MyBatisUtils.closeSessionAndCommit();
	            }
	        } catch (Exception e) {
	            log.error("UserMapper的updateIpAndLastTime数据库报错",e);
	        } finally {
	            session.close();
	        }
	}

	
	public static String findIpByUserId(Long userId,String cid) {
			String result = null;
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = UserMapper.class.getName()+".findIpByUserId";
	                log.info("sql name ==>>" + sqlName);
	                Map<Object,Object> map =new HashMap<>();
	                map.put("userId",userId);
	                map.put("cid", cid);
	                List<String> selectList = session.selectList(sqlName,map);
					if(!selectList.isEmpty())
						result = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	            log.error("UserMapper的findIpByUserId数据库报错",e);
	        }finally {
	            session.close();
	        }
	        return result;
	}
	
	   public static void updateNameImgGer(Player p,String cid) {
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = UserMapper.class.getName() + ".updateNameImgGer";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("userImg",p.getUserImg());
	                map.put("userName",p.getUserName());
	                map.put("gender", p.getGender());
	                map.put("userId", p.getUserId());
	                map.put("cid", cid);
	                session.update(sqlName,map);
	                session.commit();
//	                MyBatisUtils.closeSessionAndCommit();
	            }
	        } catch (Exception e) {
	            log.error("UserMapper的updateNameImgGer数据库报错",e);
	        } finally {
	            session.close();
	        }
	    }
}
