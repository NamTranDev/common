package com.example.commonframe.core.connection.volley;

import com.example.commonframe.core.base.BaseResponse;
import com.example.commonframe.util.Constant;

import java.util.Map;

public class ParallelResponse extends BaseResponse {

    private final Constant.RequestTarget target;
    private final String tag;

    public ParallelResponse(byte[] content, Map<String, String> headers,
                            Constant.RequestTarget target, String tag) {
        super(content, headers);
        this.target = target;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    /**
     * @return the target
     */
    public Constant.RequestTarget getRequestTarget() {
        return target;
    }
}