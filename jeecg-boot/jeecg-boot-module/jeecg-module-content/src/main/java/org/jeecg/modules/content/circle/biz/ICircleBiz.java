package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.vo.CircleVO;

public interface ICircleBiz {

    CircleVO createCircle(CircleCreateReq req, String userId);

    void updateCircle(CircleUpdateReq req, String userId);
}
