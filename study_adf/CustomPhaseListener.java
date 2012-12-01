import java.util.logging.Logger;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * The Phaselistener which outputs an order of the phase of JSF to a log.
 * Usage: write into faces-config.xml
 */
@SuppressWarnings("serial")
public class CustomPhaseListener implements PhaseListener {

    private static final Logger logger =
        Logger.getLogger(CustomPhaseListener.class.getName());

    public CustomPhaseListener() {
        super();
    }

    public void afterPhase(PhaseEvent phaseEvent) {
        logger.info(String.format("[Thread:%d]called JSF:afterPhase --> Phase:[%s]",
                                  Thread.currentThread().getId(),
                                  phaseEvent.getPhaseId().toString()));
    }

    public void beforePhase(PhaseEvent phaseEvent) {
        logger.info(String.format("[Thread:%d]called JSF:beforePhase --> Phase:[%s]",
                                  Thread.currentThread().getId(),
                                  phaseEvent.getPhaseId().toString()));
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }
}
