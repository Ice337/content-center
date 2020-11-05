package ribbionconfigruation;

import com.itmuch.contentcenter.configration.NacosWeightRule;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置规则类
 */
@Configuration
public class RibbonConfiguration {
    @Bean
    public IRule ribbonRule() {
        return new NacosWeightRule();
    }
}
