package com.xhan.myblog.model.content.repo;

import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Data
@Document(collection = MongoLog.COLLECTION_NAME)
public class MongoLog {

    public static final String COLLECTION_NAME = "MY_BLOG_LOG";

    @Id
    private String id;
    private String host;
    private String time;
    private String requestURI;
    private String userAgent;
    private String queryString;
    private Map<String, String[]> parameterMap;

    public MongoLog(HttpServletRequest request) {
        setHost(request.getRemoteAddr());
        setTime(BlogUtils.getCurrentTime());
        setRequestURI(request.getRequestURI());
        setQueryString(request.getQueryString());
        setParameterMap(request.getParameterMap());
        setUserAgent(request.getHeader("user-agent"));
    }
}
