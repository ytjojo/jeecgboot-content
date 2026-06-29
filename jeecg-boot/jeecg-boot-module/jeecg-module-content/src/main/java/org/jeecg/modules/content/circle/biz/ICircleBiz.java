package org.jeecg.modules.content.circle.biz;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.query.CircleSearchReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.vo.CircleSearchResultVO;
import org.jeecg.modules.content.circle.vo.CircleVO;

public interface ICircleBiz {

    CircleVO createCircle(CircleCreateReq req, String userId);

    void updateCircle(CircleUpdateReq req, String userId);

    Page<CircleVO> myList(Integer pageNum, Integer pageSize, String userId);

    Page<CircleVO> publicList(Integer pageNum, Integer pageSize, String userId);

    CircleVO getDetail(String circleId, String userId);

    Page<CircleSearchResultVO> search(CircleSearchReq req, String userId);

    boolean checkNameAvailable(String name);
}
