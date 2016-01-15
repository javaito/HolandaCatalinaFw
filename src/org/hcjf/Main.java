package org.hcjf;

import org.hcjf.log.Log;

/**
 * Created by javaito on 15/1/2016.
 */
public class Main {

    public static void main(String[] args) {
        Log.d("Hola mundo!!");

        Log.e("Hola mundo!!", new IllegalAccessError("Bla"));
    }

}
