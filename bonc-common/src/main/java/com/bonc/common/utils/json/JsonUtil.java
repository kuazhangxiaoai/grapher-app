package com.bonc.common.utils.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class JsonUtil {
    
    // 将字符串存储为 JSON 到指定路径
    public static void saveJsonToFile(HashMap jsonObj, String filePath, String jsonName) throws IOException {

        File directory = new File(filePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.out.println("目录创建失败！");
                return;
            }
        }

        File file = new File(filePath + File.separator + jsonName);

        // 3. 用ObjectMapper写入文件
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, jsonObj);
            System.out.println("JSON数据已保存到：" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
