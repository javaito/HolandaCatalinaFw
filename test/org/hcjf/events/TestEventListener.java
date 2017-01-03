package org.hcjf.events;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class TestEventListener implements EventListener<TestEvent> {

    @Override
    public void onEventReceive(TestEvent event) {
        System.out.println("Test 1 " + event.getClass());
    }
}
