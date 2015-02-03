package com.jk.common.redis.httpsession;

import com.jk.common.redis.RedisTemplate;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisSessionManager {

    private static final Logger LOG = Logger.getLogger(RedisSessionManager.class);
    private static final String SESSION_ID_PREFIX = "RJSID_";
    private static final String SESSION_ID_COOKIE = "RSESSIONID";

    @Autowired
    private RedisTemplate redisTemplate;
    // Session最大更新间隔时间
    private int expirationUpdateInterval = 60;
    // Session过期时间
    private int sessionTimeOut = 1800;

    public RedisSessionManager() {

    }

    /**
     * 每次请求取得最新Session
     * 
     * @param request
     * @param response
     * @param requestEventSubject
     * @param create
     * @return
     */
    public RedisHttpSession createSession(
            SessionHttpServletRequestWrapper request,
            HttpServletResponse response,
            RequestEventSubject requestEventSubject, boolean create) {
        String sessionId = getRequestedSessionId(request);

        RedisHttpSession session = null;
        // 首次登录没有SeeionID，并且不创建新Session则不处理
        if ((StringUtils.isEmpty(sessionId)) && (!(create))) {
            return null;
        }
        // 如果SessionID不为空则从Redis加载Session
        if (StringUtils.isNotEmpty(sessionId)) {
            session = loadSession(sessionId);
        }
        // 如果是首次登录则Session为空,生成空Session
        if ((session == null) && (create)) {
            session = createEmptySession(request, response);
        }
        // 如果Session不为空则，附加各种回调事件
        if (session != null) {
            attachEvent(session, request, response, requestEventSubject);
        }

        return session;
    }

    public void setExpirationUpdateInterval(int expirationUpdateInterval) {
        this.expirationUpdateInterval = expirationUpdateInterval;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * 从Request的Cookies中取出SessionId
     * 
     * @param request
     * @return
     */
    private String getRequestedSessionId(HttpServletRequestWrapper request) {
        Cookie[] cookies = request.getCookies();

        if (ArrayUtils.isNotEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (SESSION_ID_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 保存Session
     * 
     * @param session
     */
    private void saveSession(RedisHttpSession session) {
        String sessionid = generatorSessionKey(session.id);
        try {
            // 如果Session过期
            if (session.expired) {
                redisTemplate.del(sessionid);
            } else {
                redisTemplate.set(sessionid, SerializationUtils.serialize(session), this.sessionTimeOut);
            }
        } catch (Exception e) {
            throw new SessionException(e);
        }
    }

    /**
     * 增加Session过期和Request请求结束后的回调事件
     * 
     * @param session
     * @param request
     * @param response
     * @param requestEventSubject
     */
    private void attachEvent(final RedisHttpSession session, final HttpServletRequestWrapper request, final HttpServletResponse response,
            RequestEventSubject requestEventSubject) {
        session.setListener(new SessionListener() {
            public void onInvalidated(RedisHttpSession session) {
                // 设置客户端Cookies过期
                saveCookie(session, request, response);
                // 保存Redis中的Session信息
                saveSession(session);
            }
        });
        requestEventSubject.attach(new RequestEventObserver() {
            public void completed(HttpServletRequest servletRequest, HttpServletResponse response) {
                int updateInterval = (int) ((System.currentTimeMillis() - session.lastAccessedTime) / 1000);
                // 如果session非新建的，本地session与redis session一致, 并且在最小间隔同步时间内
                // 则不与Redis同步
                if (!session.isNew && !session.isDirty && updateInterval < expirationUpdateInterval) {
                    return;
                }
                // 如果 Session过期不与Redis同步
                if (session.expired) {
                    return;
                }
                session.lastAccessedTime = System.currentTimeMillis();
                saveSession(session);
            }
        });
    }

    /**
     * 初始化空Session
     * 
     * @param request
     * @param response
     * @return
     */
    private RedisHttpSession createEmptySession(
            SessionHttpServletRequestWrapper request,
            HttpServletResponse response) {
        RedisHttpSession session = new RedisHttpSession();
        session.id = createSessionId();
        session.creationTime = System.currentTimeMillis();
        session.maxInactiveInterval = this.sessionTimeOut;
        session.isNew = true;
        saveCookie(session, request, response);
        return session;
    }

    private String createSessionId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private void saveCookie(RedisHttpSession session, HttpServletRequestWrapper request, HttpServletResponse response) {
        if (!session.isNew && !session.expired) {
            return;
        }

        Cookie cookie = new Cookie(SESSION_ID_COOKIE, null);
        cookie.setPath(request.getContextPath());
        // 如果Session过期则Cookies也过期
        if (session.expired) {
            cookie.setMaxAge(0);
            // 如果Session是新生成的，则需要在客户端设置SessionID
        } else if (session.isNew) {
            cookie.setValue(session.getId());
        }
        response.addCookie(cookie);
    }

    /**
     * 从Redis中重新加载Session
     * 
     * @param sessionId
     * @return
     */
    private RedisHttpSession loadSession(String sessionId) {
        RedisHttpSession session;
        try {
            // 从远程缓存中加载
            session = (RedisHttpSession) SerializationUtils.deserialize(redisTemplate.getByte(generatorSessionKey(sessionId)));
            // 重新加载到本地缓存的Session需要重新设置同步标志与新建标志
            if (session != null) {
                session.isNew = false;
                session.isDirty = false;
            }
            return session;
        } catch (Exception e) {
            LOG.error("failed to retrieve session from redis", e);
        }
        return null;
    }

    private static String generatorSessionKey(String sessionId) {
        return SESSION_ID_PREFIX.concat(sessionId);
    }
}