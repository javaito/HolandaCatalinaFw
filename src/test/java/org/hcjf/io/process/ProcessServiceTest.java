package org.hcjf.io.process;

import org.hcjf.utils.Strings;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class ProcessServiceTest {

    public static void main(String[] args) {
        ProcessDiscoveryService.getInstance().registerConsumer(new ProcessDiscoveryConsumer() {
            @Override
            public Boolean match(ProcessHandle processHandle) {
                boolean result = false;
//                if(processHandle.info().command().isPresent()) {
//                    if(processHandle.info().command().get().endsWith("java")) {
//                        if(processHandle.info().arguments().isPresent()) {
//                            if(Arrays.stream(processHandle.info().arguments().get()).anyMatch(S -> S.equals("-classpath"))) {
//                                result = true;
//                            }
//                        }
//                    }
//                }
                if(processHandle.info().command().isPresent()) {
                    if(processHandle.info().command().get().contains("tail")) {
                        result = true;
                    }
                }
                return result;
            }

            @Override
            public void onDiscovery(ProcessHandle processHandle) {
                System.out.println("Discovered: " + processHandle.info().commandLine());
            }

            @Override
            public void onKill(ProcessHandle processHandle) {
                System.out.println("Killed: " + processHandle.info().commandLine());
            }
        });
    }

}
