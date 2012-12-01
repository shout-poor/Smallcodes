import java.util.logging.Logger;

import static oracle.adf.controller.v2.lifecycle.Lifecycle.getPhaseName;
import oracle.adf.controller.v2.lifecycle.PagePhaseEvent;
import oracle.adf.controller.v2.lifecycle.PagePhaseListener;

/**
 * ADF��PagePhaseEvent�̔��Ώ����m�F���邽�߂�PagePhaseListener
 * META-INF/adf-settings.xml�ŋL�q���Ďg�p����B
 */
public class CustomPagePhaseListener implements PagePhaseListener {

    private static final Logger logger =
        Logger.getLogger(CustomPagePhaseListener.class.getName());

    public CustomPagePhaseListener() {
    }


    public void afterPhase(PagePhaseEvent pagePhaseEvent) {
        logger.info(String.format("[Thread:%d]called adf:afterPhase --> Phase:[%d:%s]",
                                  Thread.currentThread().getId(),
                                  pagePhaseEvent.getPhaseId(),
                                  getPhaseName(pagePhaseEvent.getPhaseId())));
    }

    public void beforePhase(PagePhaseEvent pagePhaseEvent) {
        logger.info(String.format("[Thread:%d]called adf:beforePhase --> Phase:[%d:%s]",
                                  Thread.currentThread().getId(),
                                  pagePhaseEvent.getPhaseId(),
                                  getPhaseName(pagePhaseEvent.getPhaseId())));
    }


}
