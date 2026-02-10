package com.bonc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println( "\n"+ "(♥◠‿◠)ﾉﾞ ——  ——  ——  success  ——  ——  ——  ლ(´ڡ`ლ)" + "\n\n" +
                "  |============= 已 成 功 启 动 =============| \n"+
                "  |     ____     ___    _   _     ____     | \n"+
                "  |    | __ )   / _ \\  | \\ | |  / ___|     | \n"+
                "  |    |  _ \\  | | | | |  \\| | | |         | \n"+
                "  |    | |_) | | |_| | | |\\  | | |___      | \n"+
                "  |    |____/   \\___/  |_| \\_|  \\____|     | \n"+
                "  |========================================| \n");
    }
}
