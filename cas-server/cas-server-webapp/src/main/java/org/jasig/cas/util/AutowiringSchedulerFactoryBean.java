package org.jasig.cas.util;

import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.quartz.Trigger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Extension of {@link SchedulerFactoryBean} that collects trigger bean
 * definitions from the application context and calls
 * {@link #setTriggers(org.quartz.Trigger[])} to autowire triggers at
 * {@link #afterPropertiesSet()} time.
 *
 * @author Marvin S. Addison
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.4
 **/
public final class AutowiringSchedulerFactoryBean extends SchedulerFactoryBean implements ApplicationContextAware, InitializingBean {

    private final Log log = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;

    public void afterPropertiesSet() throws Exception {
        final Map<String,Trigger> triggers = (Map<String,Trigger>) this.applicationContext.getBeansOfType(Trigger.class);
        super.setTriggers(triggers.values().toArray(new Trigger[triggers.size()]));

        if (log.isDebugEnabled()) {
            log.debug("Autowired the following triggers defined in application context: " + triggers.keySet().toString());
        }

        super.afterPropertiesSet();
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        this.applicationContext = applicationContext;
    }
}
