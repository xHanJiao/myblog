package com.xhan.myblog.model.content.repo;

import com.xhan.myblog.utils.BlogUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

@Data
@Document(collection = MongoLog.COLLECTION_NAME)
public class MongoLog {

    public static final String COLLECTION_NAME = "MY_BLOG_LOG";

    @Id
    private String id;
    private String host;
    private String time;
    private String date;
    private String requestURI;
    private String userAgent;
    private String queryString;
    private Set<String> parameterNames;

    public MongoLog(HttpServletRequest request) {
        setHost(request.getRemoteAddr());
        String dateTime = BlogUtils.getCurrentDateTime();
        setTime(dateTime.split(" ")[1]);
        setDate(dateTime.split(" ")[0]);
        setRequestURI(request.getRequestURI());
        setQueryString(request.getQueryString());
        setParameterNames(request.getParameterMap().keySet());
        setUserAgent(request.getHeader("user-agent"));
    }
}
