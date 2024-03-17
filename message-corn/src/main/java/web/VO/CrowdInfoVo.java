package web.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author 17131
 * @Date 2024/3/17
 * @Description:每一行csv的记录
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrowdInfoVo  implements Serializable {
    /**
     * 消息模板Id
     */
    private Long messageTemplateId;

    /**
     * 接收者id
     */
    private String receiver;

    /**
     * 参数信息
     */
    private Map<String, String> params;
}
