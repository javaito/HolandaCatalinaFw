package org.hcjf.events;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class TestEventListener2 implements EventListener<TestEvent2> {


    @Override
    public void onEventReceive(TestEvent2 event) {
        System.out.println("Test 2 " + event.getClass());
    }


}
