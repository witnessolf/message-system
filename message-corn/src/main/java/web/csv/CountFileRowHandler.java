package web.csv;

import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvRowHandler;
import lombok.Data;

/**
 * @Author 17131
 * @Date 2024/3/17
 * @Description:统计当前文件有多少行
 */
@Data
public class CountFileRowHandler implements CsvRowHandler{

    private long rowSize;

    @Override
    public void handle(CsvRow csvRow) {
        rowSize++;
    }

    public long getRowSize() {
        return rowSize;
    }
}
